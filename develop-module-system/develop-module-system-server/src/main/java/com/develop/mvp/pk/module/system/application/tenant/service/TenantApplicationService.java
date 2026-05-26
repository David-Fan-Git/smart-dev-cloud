package com.develop.mvp.pk.module.system.application.tenant.service;

// Skill: AggregateRoot_Tenant_Validation_Skill — 应用服务 TenantApplicationService
// DDD 角色：应用编排服务，不包含业务规则，仅编排领域对象和基础设施
// 规则 R04/不变式 I04：系统租户不可修改/删除，由本层校验
// 验收标准 AC09：创建租户时的角色+用户创建编排逻辑在 ApplicationService 中

import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantPackageUseCase;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.framework.tenant.config.TenantProperties;
import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.framework.tenant.core.util.TenantUtils;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.tenant.TenantSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantDO;
import com.develop.mvp.pk.module.system.domain.tenant.Tenant;
import com.develop.mvp.pk.module.system.domain.tenant.TenantFactory;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;
import com.develop.mvp.pk.module.system.domain.tenant.repository.TenantPageQuery;
import com.develop.mvp.pk.module.system.domain.tenant.repository.TenantRepository;
import com.develop.mvp.pk.module.system.domain.tenant.service.TenantUniquenessChecker;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.*;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEventPublisher;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantPackageDO;
import com.develop.mvp.pk.module.system.enums.permission.RoleCodeEnum;
import com.develop.mvp.pk.module.system.enums.permission.RoleTypeEnum;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.MenuUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.RoleUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;

/**
 * Tenant Application Service 应用服务。
 */
public class TenantApplicationService implements TenantUseCase {

    private final TenantRepository tenantRepository;
    private final TenantUniquenessChecker uniquenessChecker;
    private final DomainEventPublisher eventPublisher;
    private final TenantPackageUseCase tenantPackageService;
    private final AdminUserUseCase adminUserService;
    private final RoleUseCase roleService;
    private final PermissionUseCase permissionService;
    private final MenuUseCase menuService;

    @Autowired(required = false)
    private TenantProperties tenantProperties;

    /**
     * 设置 set Tenant Properties 对应的数据。
     *
     * @param tenantProperties tenantProperties 参数
     */
    public void setTenantProperties(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    /**
     * 创建 TenantApplicationService 实例。
     *
     * @param tenantRepository tenantRepository 参数
     * @param uniquenessChecker uniquenessChecker 参数
     * @param eventPublisher eventPublisher 参数
     * @param tenantPackageService tenantPackageService 参数
     * @param adminUserService adminUserService 参数
     * @param roleService roleService 参数
     * @param permissionService permissionService 参数
     * @param menuService menuService 参数
     */
    public TenantApplicationService(TenantRepository tenantRepository,
                                     TenantUniquenessChecker uniquenessChecker,
                                     DomainEventPublisher eventPublisher,
                                     TenantPackageUseCase tenantPackageService,
                                     AdminUserUseCase adminUserService,
                                     RoleUseCase roleService,
                                     PermissionUseCase permissionService,
                                     MenuUseCase menuService) {
        this.tenantRepository = tenantRepository;
        this.uniquenessChecker = uniquenessChecker;
        this.eventPublisher = eventPublisher;
        this.tenantPackageService = tenantPackageService;
        this.adminUserService = adminUserService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.menuService = menuService;
    }

    /**
     * 创建租户（含管理员角色+用户创建的编排逻辑）
     * 规则 R02/R03：名称/域名唯一性校验
     * 不变式 I06：套餐必须存在且启用
     */
    @Transactional
    public Long createTenant(Long id, String name, String contactName, String contactMobile,
                              Integer status, List<String> websites, Long packageId,
                              java.time.LocalDateTime expireTime, Integer accountCount,
                              String username, String password) {
        // 校验唯一性（规则 R02/R03）
        assertNameUnique(TenantName.of(name), null);
        if (CollUtil.isNotEmpty(websites)) {
            for (String website : websites) {
                assertWebsiteUnique(website, null);
            }
        }
        // 校验套餐（不变式 I06）
        TenantPackageDO tenantPackage = tenantPackageService.validTenantPackage(packageId);

        TenantStatus tenantStatus = status != null ? TenantStatus.of(status) : TenantStatus.ENABLED;
        Tenant tenant = id == null
                ? tenantRepository.create(name, null, contactName, contactMobile, tenantStatus,
                        websites, packageId, expireTime, accountCount)
                : tenantRepository.save(TenantFactory.create(
                        id, name, null, contactName, contactMobile, tenantStatus,
                        websites, packageId, expireTime, accountCount));

        // 跨聚合编排：创建管理员角色和用户
        TenantUtils.execute(tenant.id().value(), () -> {
            Long roleId = createTenantAdminRole(tenantPackage);
            Long userId = createAdminUser(roleId, username, password, contactName, contactMobile);
            tenant.setContactUser(userId);
            tenantRepository.save(tenant);
        });

        publishEvents(tenant);
        return tenant.id().value();
    }

    /**
     * 更新租户
     * 规则 R09：套餐变更时同步更新
     * 规则 R04/不变式 I04：系统租户不可修改
     */
    @Transactional
    public void updateTenant(Long id, String name, String contactName, String contactMobile,
                              Integer status, List<String> websites, Long packageId,
                              java.time.LocalDateTime expireTime, Integer accountCount) {
        Tenant tenant = findExistingTenant(TenantId.of(id));
        // 不变式 I04：系统租户不可修改
        assertNotSystemTenant(tenant);

        // 校验唯一性
        assertNameUnique(TenantName.of(name), tenant.id());
        if (CollUtil.isNotEmpty(websites)) {
            for (String website : websites) {
                assertWebsiteUnique(website, tenant.id());
            }
        }
        // 校验套餐
        TenantPackageDO tenantPackage = tenantPackageService.validTenantPackage(packageId);

        // 更新租户属性
        tenant.updateProfile(TenantName.of(name), contactName, contactMobile, websites, uniquenessChecker);
        if (status != null) {
            if (TenantStatus.of(status).isEnabled()) tenant.enable();
            else tenant.disable();
        }
        tenant.updateExpiration(TenantExpireTime.of(expireTime), accountCount);

        // 规则 R09：套餐变更时同步角色权限
        if (!tenant.packageRef().packageId().equals(packageId)) {
            tenant.changePackage(TenantPackageRef.of(packageId));
            updateTenantRoleMenu(tenant.id().value(), tenantPackage.getMenuIds());
        }

        tenantRepository.save(tenant);
        publishEvents(tenant);
    }

    /** 删除租户（规则 R04：系统租户不可删除） */
    @Transactional
    public void deleteTenant(Long id) {
        Tenant tenant = findExistingTenant(TenantId.of(id));
        assertNotSystemTenant(tenant);
        tenant.markDeleted();
        tenantRepository.delete(tenant.id());
        publishEvents(tenant);
    }

    /** 批量删除租户 */
    @Transactional
    public void deleteTenantList(List<Long> ids) {
        for (Long id : ids) {
            deleteTenant(id);
        }
    }

    // ── 查询 ──

    /**
     * 创建 create Tenant 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    public Long createTenant(TenantSaveReqVO createReqVO) {
        return createTenant(
                createReqVO.getId(), createReqVO.getName(), createReqVO.getContactName(),
                createReqVO.getContactMobile(), createReqVO.getStatus(), createReqVO.getWebsites(),
                createReqVO.getPackageId(), createReqVO.getExpireTime(), createReqVO.getAccountCount(),
                createReqVO.getUsername(), createReqVO.getPassword());
    }

    /**
     * 更新 update Tenant 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    public void updateTenant(TenantSaveReqVO updateReqVO) {
        updateTenant(
                updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getContactName(),
                updateReqVO.getContactMobile(), updateReqVO.getStatus(), updateReqVO.getWebsites(),
                updateReqVO.getPackageId(), updateReqVO.getExpireTime(), updateReqVO.getAccountCount());
    }

    /**
     * 查询 get Tenant 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public Tenant getTenant(Long id) {
        return tenantRepository.findById(TenantId.of(id));
    }

    /**
     * 查询 get Tenant Do 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public TenantDO getTenantDo(Long id) {
        return toDataObject(getTenant(id));
    }

    /** 规则 R07：校验租户有效性（存在+启用+未过期） */
    public Tenant getAndValidateTenant(Long id) {
        Tenant tenant = getTenant(id);
        if (tenant == null) throw exception(TENANT_NOT_EXISTS);
        String error = tenant.validateActive();
        if (error != null) {
            if (tenant.isDisabled()) throw exception(TENANT_DISABLE, tenant.name().value());
            if (tenant.isExpired()) throw exception(TENANT_EXPIRE, tenant.name().value());
        }
        return tenant;
    }

    /**
     * 查询 get Tenant By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    public Tenant getTenantByName(String name) {
        return tenantRepository.findByName(TenantName.of(name)).orElse(null);
    }

    /**
     * 查询 get Tenant Do By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    public TenantDO getTenantDoByName(String name) {
        return toDataObject(getTenantByName(name));
    }

    /**
     * 查询 get Tenant By Website 对应的数据。
     *
     * @param website website 参数
     * @return 处理结果
     */
    public Tenant getTenantByWebsite(String website) {
        List<Tenant> tenants = tenantRepository.findByWebsite(website);
        return tenants.isEmpty() ? null : tenants.get(0);
    }

    /**
     * 查询 get Tenant Do By Website 对应的数据。
     *
     * @param website website 参数
     * @return 处理结果
     */
    public TenantDO getTenantDoByWebsite(String website) {
        return toDataObject(getTenantByWebsite(website));
    }

    /**
     * 查询 get Tenant Page 对应的数据。
     *
     * @param query query 参数
     * @return 处理结果
     */
    public PageResult<Tenant> getTenantPage(TenantPageQuery query) {
        return tenantRepository.findPage(query);
    }

    /**
     * 查询 get Tenant Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    public PageResult<TenantDO> getTenantPage(TenantPageReqVO pageReqVO) {
        TenantPageQuery query = new TenantPageQuery(
                pageReqVO.getName(), pageReqVO.getContactName(), pageReqVO.getContactMobile(),
                pageReqVO.getStatus(), pageReqVO.getCreateTime(), pageReqVO.getPageNo(), pageReqVO.getPageSize());
        PageResult<Tenant> pageResult = getTenantPage(query);
        return new PageResult<>(pageResult.getList().stream()
                .map(this::toDataObject).collect(Collectors.toList()), pageResult.getTotal());
    }

    /**
     * 查询 get Tenant Domain List By Status 对应的数据。
     *
     * @param statusCode statusCode 参数
     * @return 处理结果
     */
    public List<Tenant> getTenantDomainListByStatus(Integer statusCode) {
        return tenantRepository.findByStatus(TenantStatus.of(statusCode));
    }

    /**
     * 查询 get Tenant List By Status 对应的数据。
     *
     * @param statusCode statusCode 参数
     * @return 处理结果
     */
    public List<TenantDO> getTenantListByStatus(Integer statusCode) {
        return getTenantDomainListByStatus(statusCode).stream()
                .map(this::toDataObject).collect(Collectors.toList());
    }

    /**
     * 查询 get Tenant Count By Package Id 对应的数据。
     *
     * @param packageId packageId 参数
     * @return 处理结果
     */
    public Long getTenantCountByPackageId(Long packageId) {
        return tenantRepository.countByPackageId(TenantPackageRef.of(packageId));
    }

    /**
     * 查询 get Tenant Domain List By Package Id 对应的数据。
     *
     * @param packageId packageId 参数
     * @return 处理结果
     */
    public List<Tenant> getTenantDomainListByPackageId(Long packageId) {
        return tenantRepository.findByPackageId(TenantPackageRef.of(packageId));
    }

    /**
     * 查询 get Tenant List By Package Id 对应的数据。
     *
     * @param packageId packageId 参数
     * @return 处理结果
     */
    public List<TenantDO> getTenantListByPackageId(Long packageId) {
        return getTenantDomainListByPackageId(packageId).stream()
                .map(this::toDataObject).collect(Collectors.toList());
    }

    /**
     * 查询 get Tenant Id List 对应的数据。
     *
     * @return 处理结果
     */
    public List<Long> getTenantIdList() {
        return tenantRepository.findAll().stream()
                .map(t -> t.id().value()).collect(Collectors.toList());
    }

    /**
     * 执行 valid Tenant 对应的业务操作。
     *
     * @param id id 参数
     */
    public void validTenant(Long id) {
        getAndValidateTenant(id);
    }

    /**
     * 处理 handle Tenant Info 对应的业务逻辑。
     *
     * @param handler handler 参数
     */
    public void handleTenantInfo(TenantInfoHandler handler) {
        if (isTenantDisable()) {
            return;
        }
        handler.handle(getTenantDo(TenantContextHolder.getRequiredTenantId()));
    }

    /**
     * 处理 handle Tenant Menu 对应的业务逻辑。
     *
     * @param handler handler 参数
     */
    public void handleTenantMenu(TenantMenuHandler handler) {
        if (isTenantDisable()) {
            return;
        }
        TenantDO tenant = getTenantDo(TenantContextHolder.getRequiredTenantId());
        Set<Long> menuIds;
        if (isSystemTenant(tenant)) {
            menuIds = CollectionUtils.convertSet(menuService.getMenuList(), MenuDO::getId);
        } else {
            menuIds = tenantPackageService.getTenantPackage(tenant.getPackageId()).getMenuIds();
        }
        handler.handle(menuIds);
    }

    // ── 私有方法 ──

    /**
     * 查询 find Existing Tenant 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    private Tenant findExistingTenant(TenantId id) {
        Tenant tenant = tenantRepository.findById(id);
        if (tenant == null) throw exception(TENANT_NOT_EXISTS);
        return tenant;
    }

    /**
     * 判断 is System Tenant 对应的条件是否成立。
     *
     * @param tenant tenant 参数
     * @return 处理结果
     */
    private static boolean isSystemTenant(TenantDO tenant) {
        return Objects.equals(tenant.getPackageId(), TenantDO.PACKAGE_ID_SYSTEM);
    }

    /**
     * 判断 is Tenant Disable 对应的条件是否成立。
     *
     * @return 处理结果
     */
    private boolean isTenantDisable() {
        return tenantProperties == null || Boolean.FALSE.equals(tenantProperties.getEnable());
    }

    /**
     * 执行 to Data Object 对应的业务操作。
     *
     * @param tenant tenant 参数
     * @return 处理结果
     */
    private TenantDO toDataObject(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        TenantDO tenantDO = new TenantDO()
                .setId(tenant.id().value())
                .setName(tenant.name().value())
                .setContactUserId(tenant.contactUserId())
                .setContactName(tenant.contactName())
                .setContactMobile(tenant.contactMobile())
                .setStatus(tenant.status().code())
                .setWebsites(tenant.websites())
                .setPackageId(tenant.packageRef().packageId())
                .setExpireTime(tenant.expireTime().value())
                .setAccountCount(tenant.accountCount());
        tenantDO.setCreateTime(tenant.createTime());
        tenantDO.setUpdateTime(tenant.updateTime());
        tenantDO.setCreator(tenant.creator());
        tenantDO.setUpdater(tenant.updater());
        tenantDO.setDeleted(tenant.deleted());
        return tenantDO;
    }

    /**
     * 执行 assert Not System Tenant 对应的业务操作。
     *
     * @param tenant tenant 参数
     */
    private void assertNotSystemTenant(Tenant tenant) {
        if (tenant.isSystem()) throw exception(TENANT_CAN_NOT_UPDATE_SYSTEM);
    }

    /**
     * 执行 assert Name Unique 对应的业务操作。
     *
     * @param name name 参数
     * @param excludeId excludeId 参数
     */
    private void assertNameUnique(TenantName name, TenantId excludeId) {
        if (!uniquenessChecker.isNameUnique(name, excludeId)) {
            throw exception(TENANT_NAME_DUPLICATE, name.value());
        }
    }

    /**
     * 执行 assert Website Unique 对应的业务操作。
     *
     * @param website website 参数
     * @param excludeId excludeId 参数
     */
    private void assertWebsiteUnique(String website, TenantId excludeId) {
        if (!uniquenessChecker.isWebsiteUnique(website, excludeId)) {
            throw exception(TENANT_WEBSITE_DUPLICATE, website);
        }
    }

    // ── 跨聚合编排：角色+用户创建（后续 Role/User 完成 DDD 重构后可替换为 ApplicationService 调用） ──

    /**
     * 创建 create Tenant Admin Role 对应的数据。
     *
     * @param tenantPackage tenantPackage 参数
     * @return 处理结果
     */
    private Long createTenantAdminRole(TenantPackageDO tenantPackage) {
        RoleSaveReqVO reqVO = new RoleSaveReqVO();
        reqVO.setName(RoleCodeEnum.TENANT_ADMIN.getName())
                .setCode(RoleCodeEnum.TENANT_ADMIN.getCode())
                .setSort(0).setRemark("系统自动生成");
        Long roleId = roleService.createRole(reqVO, RoleTypeEnum.SYSTEM.getType());
        permissionService.assignRoleMenu(roleId, tenantPackage.getMenuIds());
        return roleId;
    }

    /**
     * 创建 create Admin User 对应的数据。
     *
     * @param roleId roleId 参数
     * @param username username 参数
     * @param password password 参数
     * @param contactName contactName 参数
     * @param contactMobile contactMobile 参数
     * @return 处理结果
     */
    private Long createAdminUser(Long roleId, String username, String password,
                                  String contactName, String contactMobile) {
        Long userId = adminUserService.createUser(
                convertToUserSaveReqVO(username, password, contactName, contactMobile));
        permissionService.assignUserRole(userId, singleton(roleId));
        return userId;
    }

    private static com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserSaveReqVO
            convertToUserSaveReqVO(String username, String password, String contactName, String contactMobile) {
        var reqVO = new com.develop.mvp.pk.module.system.controller.admin.user.vo.user.UserSaveReqVO();
        reqVO.setUsername(username);
        reqVO.setPassword(password);
        reqVO.setNickname(contactName);
        reqVO.setMobile(contactMobile);
        return reqVO;
    }

    /**
     * 更新 update Tenant Role Menu 对应的数据。
     *
     * @param tenantId tenantId 参数
     * @param menuIds menuIds 参数
     */
    @Transactional
    public void updateTenantRoleMenu(Long tenantId, Set<Long> menuIds) {
        TenantUtils.execute(tenantId, () -> {
            roleService.getRoleList().forEach(role -> {
                if (Objects.equals(role.getCode(), RoleCodeEnum.TENANT_ADMIN.getCode())) {
                    permissionService.assignRoleMenu(role.getId(), menuIds);
                } else {
                    Set<Long> roleMenuIds = permissionService.getRoleMenuListByRoleId(role.getId());
                    permissionService.assignRoleMenu(role.getId(),
                            CollUtil.intersectionDistinct(roleMenuIds, menuIds));
                }
            });
        });
    }

    /**
     * 发送 publish Events 对应的消息。
     *
     * @param tenant tenant 参数
     */
    private void publishEvents(Tenant tenant) {
        for (DomainEvent event : tenant.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
