package com.develop.mvp.pk.module.system.domain.tenant.valueobject;

// Skill: AggregateRoot_Tenant_Validation_Skill — 值对象 TenantPackageRef
// DDD 角色：外部聚合（TenantPackage）的 ID 引用
// 不变式 I04：系统租户使用 packageId=0 标识
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

/**
 * Tenant Package Ref 值对象。
 */
public final class TenantPackageRef {
    public static final Long SYSTEM_PACKAGE_ID = 0L;

    private final Long packageId;

    /**
     * 创建 TenantPackageRef 实例。
     *
     * @param packageId packageId 参数
     */
    private TenantPackageRef(Long packageId) {
        this.packageId = Objects.requireNonNull(packageId, "packageId 不能为空");
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param packageId packageId 参数
     * @return 处理结果
     */
    public static TenantPackageRef of(Long packageId) { return new TenantPackageRef(packageId); }

    /**
     * 执行 package Id 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long packageId() { return packageId; }

    /** 规则 R04：判断是否为系统租户 */
    public boolean isSystem() { return SYSTEM_PACKAGE_ID.equals(packageId); }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantPackageRef that)) return false;
        return packageId.equals(that.packageId);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() { return Objects.hash(packageId); }

    /**
     * 执行 to String 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public String toString() { return "TenantPackageRef{" + packageId + '}'; }
}
