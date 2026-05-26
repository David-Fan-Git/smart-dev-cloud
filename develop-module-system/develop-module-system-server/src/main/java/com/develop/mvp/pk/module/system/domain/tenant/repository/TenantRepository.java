package com.develop.mvp.pk.module.system.domain.tenant.repository;

// Skill: AggregateRoot_Tenant_Validation_Skill — 仓储接口 TenantRepository
// DDD 角色：领域层定义的仓储接口，不依赖任何基础设施
// 验收标准 AC05：不 import MyBatis 类

import com.develop.mvp.pk.module.system.domain.tenant.Tenant;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.TenantId;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.TenantName;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.TenantPackageRef;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.TenantStatus;
import com.develop.mvp.pk.framework.common.pojo.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Tenant Repository 领域仓储接口。
 */
public interface TenantRepository {
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
    Tenant create(String name, Long contactUserId, String contactName, String contactMobile,
                  TenantStatus status, java.util.List<String> websites, Long packageId,
                  java.time.LocalDateTime expireTime, Integer accountCount);
    /**
     * 创建 save 对应的数据。
     *
     * @param tenant tenant 参数
     * @return 处理结果
     */
    Tenant save(Tenant tenant);
    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(TenantId id);
    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Tenant findById(TenantId id);
    /**
     * 查询 find By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    Optional<Tenant> findByName(TenantName name);
    /**
     * 查询 find By Website 对应的数据。
     *
     * @param website website 参数
     * @return 处理结果
     */
    List<Tenant> findByWebsite(String website);
    /**
     * 查询 find By Package Id 对应的数据。
     *
     * @param packageRef packageRef 参数
     * @return 处理结果
     */
    List<Tenant> findByPackageId(TenantPackageRef packageRef);
    /**
     * 查询 find By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    List<Tenant> findByStatus(TenantStatus status);
    /**
     * 查询 find Page 对应的数据。
     *
     * @param query query 参数
     * @return 处理结果
     */
    PageResult<Tenant> findPage(TenantPageQuery query);
    /**
     * 查询 count By Package Id 对应的数据。
     *
     * @param packageRef packageRef 参数
     * @return 处理结果
     */
    long countByPackageId(TenantPackageRef packageRef);
    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<Tenant> findByIds(Collection<TenantId> ids);
    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    List<Tenant> findAll();
    /**
     * 执行 exists By Name 对应的业务操作。
     *
     * @param name name 参数
     * @return 处理结果
     */
    boolean existsByName(TenantName name);
}
