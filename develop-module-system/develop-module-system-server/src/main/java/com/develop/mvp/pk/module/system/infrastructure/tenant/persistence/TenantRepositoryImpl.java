package com.develop.mvp.pk.module.system.infrastructure.tenant.persistence;

// Skill: AggregateRoot_Tenant_Validation_Skill — 仓储实现 TenantRepositoryImpl
// DDD 角色：TenantRepository 的 MyBatis 实现，负责 DO ↔ 领域模型映射
// 验收标准 AC06：在基础设施层，import MyBatis 类

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantDO;
import com.develop.mvp.pk.module.system.dal.mysql.tenant.TenantMapper;
import com.develop.mvp.pk.module.system.domain.tenant.Tenant;
import com.develop.mvp.pk.module.system.domain.tenant.TenantFactory;
import com.develop.mvp.pk.module.system.domain.tenant.repository.TenantPageQuery;
import com.develop.mvp.pk.module.system.domain.tenant.repository.TenantRepository;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tenant Repository Impl 领域仓储实现。
 */
@Repository
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantMapper tenantMapper;

    /**
     * 创建 TenantRepositoryImpl 实例。
     *
     * @param tenantMapper tenantMapper 参数
     */
    public TenantRepositoryImpl(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    /**
     * 创建 create 对应的数据。
     *
     * @param name name 参数
     * @param contactUserId contactUserId 参数
     * @param contactName contactName 参数
     * @param contactMobile contactMobile 参数
     * @param status status 参数
     * @param websites websites 参数
     * @param packageId packageId 参数
     * @param expireTime expireTime 参数
     * @param accountCount accountCount 参数
     * @return 处理结果
     */
    @Override
    @Transactional
    public Tenant create(String name, Long contactUserId, String contactName, String contactMobile,
                         TenantStatus status, List<String> websites, Long packageId,
                         java.time.LocalDateTime expireTime, Integer accountCount) {
        TenantStatus initialStatus = status != null ? status : TenantStatus.ENABLED;
        TenantDO tenantDO = new TenantDO();
        tenantDO.setName(name);
        tenantDO.setContactUserId(contactUserId);
        tenantDO.setContactName(contactName);
        tenantDO.setContactMobile(contactMobile);
        tenantDO.setStatus(initialStatus.code());
        tenantDO.setWebsites(websites);
        tenantDO.setPackageId(packageId);
        tenantDO.setExpireTime(expireTime);
        tenantDO.setAccountCount(accountCount);
        tenantMapper.insert(tenantDO);
        return TenantFactory.create(tenantDO.getId(), name, contactUserId, contactName, contactMobile,
                initialStatus, websites, packageId, expireTime, accountCount);
    }

    /**
     * 创建 save 对应的数据。
     *
     * @param tenant tenant 参数
     * @return 处理结果
     */
    @Override
    @Transactional
    public Tenant save(Tenant tenant) {
        TenantDO tenantDO = toDataObject(tenant);
        if (tenantMapper.selectById(tenant.id().value()) == null) {
            tenantMapper.insert(tenantDO);
        } else {
            tenantMapper.updateById(tenantDO);
        }
        return tenant;
    }

    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    @Override
    @Transactional
    public void delete(TenantId id) {
        tenantMapper.deleteById(id.value());
    }

    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @Override
    public Tenant findById(TenantId id) {
        TenantDO tenantDO = tenantMapper.selectById(id.value());
        return tenantDO != null ? toDomain(tenantDO) : null;
    }

    /**
     * 查询 find By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    @Override
    public Optional<Tenant> findByName(TenantName name) {
        TenantDO tenantDO = tenantMapper.selectByName(name.value());
        return Optional.ofNullable(tenantDO).map(this::toDomain);
    }

    /**
     * 查询 find By Website 对应的数据。
     *
     * @param website website 参数
     * @return 处理结果
     */
    @Override
    public List<Tenant> findByWebsite(String website) {
        return tenantMapper.selectListByWebsite(website).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find By Package Id 对应的数据。
     *
     * @param packageRef packageRef 参数
     * @return 处理结果
     */
    @Override
    public List<Tenant> findByPackageId(TenantPackageRef packageRef) {
        return tenantMapper.selectListByPackageId(packageRef.packageId()).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    @Override
    public List<Tenant> findByStatus(TenantStatus status) {
        return tenantMapper.selectListByStatus(status.code()).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find Page 对应的数据。
     *
     * @param query query 参数
     * @return 处理结果
     */
    @Override
    public PageResult<Tenant> findPage(TenantPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO();
        reqVO.setName(query.name());
        reqVO.setContactName(query.contactName());
        reqVO.setContactMobile(query.contactMobile());
        reqVO.setStatus(query.status());
        reqVO.setCreateTime(query.createTime());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<TenantDO> doPage = tenantMapper.selectPage(reqVO);
        List<Tenant> tenants = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(tenants, doPage.getTotal());
    }

    /**
     * 查询 count By Package Id 对应的数据。
     *
     * @param packageRef packageRef 参数
     * @return 处理结果
     */
    @Override
    public long countByPackageId(TenantPackageRef packageRef) {
        return tenantMapper.selectCountByPackageId(packageRef.packageId());
    }

    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    @Override
    public List<Tenant> findByIds(Collection<TenantId> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        List<Long> rawIds = ids.stream().map(TenantId::value).collect(Collectors.toList());
        return tenantMapper.selectByIds(rawIds).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public List<Tenant> findAll() {
        return tenantMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    /**
     * 执行 exists By Name 对应的业务操作。
     *
     * @param name name 参数
     * @return 处理结果
     */
    @Override
    public boolean existsByName(TenantName name) {
        return tenantMapper.selectByName(name.value()) != null;
    }

    /**
     * 执行 to Data Object 对应的业务操作。
     *
     * @param tenant tenant 参数
     * @return 处理结果
     */
    private TenantDO toDataObject(Tenant tenant) {
        TenantDO tenantDO = new TenantDO();
        tenantDO.setId(tenant.id().value());
        tenantDO.setName(tenant.name().value());
        tenantDO.setContactUserId(tenant.contactUserId());
        tenantDO.setContactName(tenant.contactName());
        tenantDO.setContactMobile(tenant.contactMobile());
        tenantDO.setStatus(tenant.status().code());
        tenantDO.setWebsites(tenant.websites());
        tenantDO.setPackageId(tenant.packageRef().packageId());
        tenantDO.setExpireTime(tenant.expireTime().value());
        tenantDO.setAccountCount(tenant.accountCount());
        return tenantDO;
    }

    /**
     * 执行 to Domain 对应的业务操作。
     *
     * @param tenantDO tenantDO 参数
     * @return 处理结果
     */
    private Tenant toDomain(TenantDO tenantDO) {
        return TenantFactory.reconstitute(
                tenantDO.getId(),
                tenantDO.getName(),
                tenantDO.getContactUserId(),
                tenantDO.getContactName(),
                tenantDO.getContactMobile(),
                tenantDO.getStatus(),
                tenantDO.getWebsites(),
                tenantDO.getPackageId(),
                tenantDO.getExpireTime(),
                tenantDO.getAccountCount(),
                tenantDO.getCreateTime(),
                tenantDO.getUpdateTime(),
                tenantDO.getCreator(),
                tenantDO.getUpdater(),
                tenantDO.getDeleted()
        );
    }
}
