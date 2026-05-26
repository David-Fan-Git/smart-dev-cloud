package com.develop.mvp.pk.module.system.application.permission.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.collection.CollectionUtils;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.RoleUseCase;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;
import com.develop.mvp.pk.module.system.dal.mysql.permission.RoleMapper;
import com.develop.mvp.pk.module.system.dal.redis.RedisKeyConstants;
import com.develop.mvp.pk.module.system.domain.permission.Role;
import com.develop.mvp.pk.module.system.domain.permission.RoleFactory;
import com.develop.mvp.pk.module.system.domain.permission.repository.RoleRepository;
import com.develop.mvp.pk.module.system.domain.permission.valueobject.DataScope;
import com.develop.mvp.pk.module.system.domain.permission.valueobject.RoleId;
import com.develop.mvp.pk.module.system.enums.permission.DataScopeEnum;
import com.develop.mvp.pk.module.system.enums.permission.RoleCodeEnum;
import com.develop.mvp.pk.module.system.enums.permission.RoleTypeEnum;
import com.google.common.annotations.VisibleForTesting;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertMap;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static com.develop.mvp.pk.module.system.enums.LogRecordConstants.*;

/**
 * Role Application Service 应用服务。
 */
@Slf4j
public class RoleApplicationService implements RoleUseCase {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionUseCase permissionService;

    /**
     * 创建 RoleApplicationService 实例。
     *
     * @param roleRepository roleRepository 参数
     * @param roleMapper roleMapper 参数
     * @param permissionService permissionService 参数
     */
    public RoleApplicationService(RoleRepository roleRepository,
                                  RoleMapper roleMapper,
                                  @Lazy PermissionUseCase permissionService) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.permissionService = permissionService;
    }

    // ========== RoleUseCase domain-returning methods ==========

    /**
     * 创建 create Role 对应的数据。
     *
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param remark remark 参数
     * @param type type 参数
     * @return 处理结果
     */
    @Override
    public Role createRole(String name, String code, Integer sort, Integer status, String remark, Integer type) {
        return createRoleDomain(name, code, sort, status, remark, type);
    }

    /**
     * 更新 update Role 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param remark remark 参数
     * @return 处理结果
     */
    @Override
    public Role updateRole(Long id, String name, String code, Integer sort, Integer status, String remark) {
        return updateRoleDomain(id, name, code, sort, status, remark);
    }

    /**
     * 更新 update Role Data Scope 对应的数据。
     *
     * @param id id 参数
     * @param dataScope dataScope 参数
     * @param dataScopeDeptIds dataScopeDeptIds 参数
     * @return 处理结果
     */
    @Override
    @CacheEvict(value = RedisKeyConstants.ROLE, key = "#id")
    public Role updateRoleDataScope(Long id, Integer dataScope, Set<Long> dataScopeDeptIds) {
        Role role = validateRoleForUpdate(id);
        role.changeDataScope(DataScope.of(dataScope, dataScopeDeptIds));
        return roleRepository.save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKeyConstants.ROLE, key = "#id")
    @LogRecord(type = SYSTEM_ROLE_TYPE, subType = SYSTEM_ROLE_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = SYSTEM_ROLE_DELETE_SUCCESS)
    /**
     * 删除 delete Role 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public Role deleteRole(Long id) {
        Role role = deleteRoleDomain(id);
        permissionService.processRoleDeleted(id);
        LogRecordContext.putVariable("role", toDataObject(role));
        return role;
    }

    /**
     * 查询 get Role 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public Role getRole(Long id) {
        Role role = roleRepository.findById(RoleId.of(id));
        if (role == null) {
            throw exception(ROLE_NOT_EXISTS);
        }
        return role;
    }

    // ========== Existing public API (VO/DO based) ==========

    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_ROLE_TYPE, subType = SYSTEM_ROLE_CREATE_SUB_TYPE, bizNo = "{{#role.id}}",
            success = SYSTEM_ROLE_CREATE_SUCCESS)
    /**
     * 创建 create Role 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @param type type 参数
     * @return 处理结果
     */
    public Long createRole(RoleSaveReqVO createReqVO, Integer type) {
        Role role = createRoleDomain(createReqVO.getName(), createReqVO.getCode(), createReqVO.getSort(),
                createReqVO.getStatus(), createReqVO.getRemark(), type);
        RoleDO roleDO = toDataObject(role);
        LogRecordContext.putVariable("role", roleDO);
        return role.id().value();
    }

    @CacheEvict(value = RedisKeyConstants.ROLE, key = "#updateReqVO.id")
    @LogRecord(type = SYSTEM_ROLE_TYPE, subType = SYSTEM_ROLE_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = SYSTEM_ROLE_UPDATE_SUCCESS)
    /**
     * 更新 update Role 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    public void updateRole(RoleSaveReqVO updateReqVO) {
        RoleDO oldRole = toDataObject(validateRoleForUpdate(updateReqVO.getId()));
        Role role = updateRoleDomain(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getCode(),
                updateReqVO.getSort(), updateReqVO.getStatus(), updateReqVO.getRemark());
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldRole, RoleSaveReqVO.class));
        LogRecordContext.putVariable("role", toDataObject(role));
    }

    /**
     * 删除 delete Role List 对应的数据。
     *
     * @param ids ids 参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleList(List<Long> ids) {
        ids.forEach(id -> {
            deleteRoleDomain(id);
            permissionService.processRoleDeleted(id);
        });
    }

    /**
     * 查询 get Role DO 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public RoleDO getRoleDO(Long id) {
        return roleMapper.selectById(id);
    }

    /**
     * 查询 get Role From Cache 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Cacheable(value = RedisKeyConstants.ROLE, key = "#id", unless = "#result == null")
    public RoleDO getRoleFromCache(Long id) {
        return roleMapper.selectById(id);
    }

    /**
     * 查询 get Role List By Status 对应的数据。
     *
     * @param statuses statuses 参数
     * @return 处理结果
     */
    public List<RoleDO> getRoleListByStatus(Collection<Integer> statuses) {
        return roleMapper.selectListByStatus(statuses);
    }

    /**
     * 查询 get Role List 对应的数据。
     *
     * @return 处理结果
     */
    public List<RoleDO> getRoleList() {
        return roleMapper.selectList();
    }

    /**
     * 查询 get Role List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    public List<RoleDO> getRoleList(Collection<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return roleMapper.selectByIds(ids);
    }

    /**
     * 查询 get Role List From Cache 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    public List<RoleDO> getRoleListFromCache(Collection<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        RoleApplicationService self = getSelf();
        return CollectionUtils.convertList(ids, self::getRoleFromCache);
    }

    /**
     * 查询 get Role Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    public PageResult<RoleDO> getRolePage(RolePageReqVO reqVO) {
        return roleMapper.selectPage(reqVO);
    }

    /**
     * 判断 has Any Super Admin 对应的条件是否成立。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    public boolean hasAnySuperAdmin(Collection<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return false;
        }
        RoleApplicationService self = getSelf();
        return ids.stream().anyMatch(id -> {
            RoleDO role = self.getRoleFromCache(id);
            return role != null && RoleCodeEnum.isSuperAdmin(role.getCode());
        });
    }

    /**
     * 校验 validate Role List 对应的业务规则。
     *
     * @param ids ids 参数
     */
    public void validateRoleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<RoleDO> roles = roleMapper.selectByIds(ids);
        Map<Long, RoleDO> roleMap = convertMap(roles, RoleDO::getId);
        ids.forEach(id -> {
            RoleDO role = roleMap.get(id);
            if (role == null) {
                throw exception(ROLE_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus())) {
                throw exception(ROLE_IS_DISABLE, role.getName());
            }
        });
    }

    // ========== Domain helpers ==========

    /**
     * 创建 create Role Domain 对应的数据。
     *
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param remark remark 参数
     * @param type type 参数
     * @return 处理结果
     */
    private Role createRoleDomain(String name, String code, Integer sort, Integer status, String remark, Integer type) {
        validateRoleDuplicate(name, code, null);
        Role role = RoleFactory.create(null, name, code, sort,
                status != null ? status : CommonStatusEnum.ENABLE.getStatus(),
                type != null ? type : RoleTypeEnum.CUSTOM.getType(),
                remark, null, DataScopeEnum.ALL.getScope(), null);
        return roleRepository.save(role);
    }

    /**
     * 更新 update Role Domain 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param code code 参数
     * @param sort sort 参数
     * @param status status 参数
     * @param remark remark 参数
     * @return 处理结果
     */
    private Role updateRoleDomain(Long id, String name, String code, Integer sort, Integer status, String remark) {
        Role role = validateRoleForUpdate(id);
        validateRoleDuplicate(name, code, id);
        role.changeBaseInfo(name, code, sort, status, remark);
        return roleRepository.save(role);
    }

    /**
     * 删除 delete Role Domain 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    private Role deleteRoleDomain(Long id) {
        Role role = validateRoleForUpdate(id);
        role.markDeleted();
        roleRepository.delete(RoleId.of(id));
        return role;
    }

    /**
     * 校验 validate Role Duplicate 对应的业务规则。
     *
     * @param name name 参数
     * @param code code 参数
     * @param id id 参数
     */
    @VisibleForTesting
    public void validateRoleDuplicate(String name, String code, Long id) {
        if (RoleCodeEnum.isSuperAdmin(code)) {
            throw exception(ROLE_ADMIN_CODE_ERROR, code);
        }
        roleRepository.findByName(name).ifPresent(role -> {
            if (!role.hasId(id)) {
                throw exception(ROLE_NAME_DUPLICATE, name);
            }
        });
        if (!StringUtils.hasText(code)) {
            return;
        }
        roleRepository.findByCode(code).ifPresent(role -> {
            if (!role.hasId(id)) {
                throw exception(ROLE_CODE_DUPLICATE, code);
            }
        });
    }

    /**
     * 校验 validate Role For Update 对应的业务规则。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @VisibleForTesting
    public Role validateRoleForUpdate(Long id) {
        Role role = roleRepository.findById(RoleId.of(id));
        if (role == null) {
            throw exception(ROLE_NOT_EXISTS);
        }
        if (role.isSystem()) {
            throw exception(ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE);
        }
        return role;
    }

    // ========== Private helpers ==========

    /**
     * 查询 get Self 对应的数据。
     *
     * @return 处理结果
     */
    private RoleApplicationService getSelf() {
        return SpringUtil.getBean(getClass());
    }

    /**
     * 执行 to Data Object 对应的业务操作。
     *
     * @param role role 参数
     * @return 处理结果
     */
    private RoleDO toDataObject(Role role) {
        RoleDO roleDO = new RoleDO();
        roleDO.setId(role.id() != null ? role.id().value() : null);
        roleDO.setName(role.name().value());
        roleDO.setCode(role.code().value());
        roleDO.setSort(role.sort());
        roleDO.setStatus(role.status().code());
        roleDO.setType(role.type().code());
        roleDO.setRemark(role.remark());
        roleDO.setTenantId(role.tenantId());
        roleDO.setDataScope(role.dataScope().scope());
        roleDO.setDataScopeDeptIds(role.dataScope().deptIds());
        return roleDO;
    }
}
