package com.develop.mvp.pk.module.system.domain.tenant;

// Skill: AggregateRoot_Tenant_Validation_Skill — 工厂 TenantFactory
// DDD 角色：工厂，负责创建和重建 Tenant 聚合（与 Tenant 同包，可访问包级构造器）
// 规则 R01：创建时默认状态为 ENABLED

import com.develop.mvp.pk.module.system.domain.tenant.valueobject.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tenant Factory 工厂。
 */
public final class TenantFactory {

    /**
     * 创建 TenantFactory 实例。
     */
    private TenantFactory() {}

    /** 创建新租户（规则 R01：默认状态 ENABLED） */
    public static Tenant create(Long id, String name, Long contactUserId, String contactName,
                                 String contactMobile, List<String> websites,
                                 Long packageId, LocalDateTime expireTime, Integer accountCount) {
        return create(id, name, contactUserId, contactName, contactMobile, TenantStatus.ENABLED,
                websites, packageId, expireTime, accountCount);
    }

    /**
     * 创建 create 对应的数据。
     *
     * @param id id 参数
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
    public static Tenant create(Long id, String name, Long contactUserId, String contactName,
                                 String contactMobile, TenantStatus status, List<String> websites,
                                 Long packageId, LocalDateTime expireTime, Integer accountCount) {
        Tenant tenant = new Tenant(
                TenantId.of(id),
                TenantName.of(name),
                contactUserId,
                contactName,
                contactMobile,
                status,
                websites,
                TenantPackageRef.of(packageId),
                TenantExpireTime.of(expireTime),
                accountCount,
                null,
                null,
                null,
                null,
                null
        );
        tenant.recordCreated();
        return tenant;
    }

    /** 从持久化数据重建 Tenant 聚合（供仓储实现调用） */
    public static Tenant reconstitute(Long id, String name, Long contactUserId,
                                       String contactName, String contactMobile,
                                       Integer statusCode, List<String> websites,
                                       Long packageId, LocalDateTime expireTime,
                                       Integer accountCount, LocalDateTime createTime,
                                       LocalDateTime updateTime, String creator,
                                       String updater, Boolean deleted) {
        return new Tenant(
                TenantId.of(id),
                TenantName.of(name),
                contactUserId,
                contactName,
                contactMobile,
                TenantStatus.of(statusCode),
                websites,
                TenantPackageRef.of(packageId),
                TenantExpireTime.of(expireTime),
                accountCount,
                createTime,
                updateTime,
                creator,
                updater,
                deleted
        );
    }
}
