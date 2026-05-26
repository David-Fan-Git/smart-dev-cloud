package com.develop.mvp.pk.module.system.domain.tenant.valueobject;

// Skill: AggregateRoot_Tenant_Validation_Skill — 值对象 TenantId
// DDD 角色：租户聚合根标识
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

/**
 * Tenant Id 值对象。
 */
public final class TenantId {
    private final Long value;

    /**
     * 创建 TenantId 实例。
     *
     * @param value value 参数
     */
    private TenantId(Long value) {
        this.value = Objects.requireNonNull(value, "tenantId 不能为空");
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static TenantId of(Long value) { return new TenantId(value); }

    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long value() { return value; }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantId that)) return false;
        return value.equals(that.value);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() { return Objects.hash(value); }

    /**
     * 执行 to String 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public String toString() { return "TenantId{" + value + '}'; }
}
