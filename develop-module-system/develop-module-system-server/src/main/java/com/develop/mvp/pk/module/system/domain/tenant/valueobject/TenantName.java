package com.develop.mvp.pk.module.system.domain.tenant.valueobject;

// Skill: AggregateRoot_Tenant_Validation_Skill — 值对象 TenantName
// 不变式 I01：租户名称在全局不可重复（唯一性由 TenantUniquenessChecker 保证）
// 规则 R02：创建/修改时校验租户名称唯一性
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

/**
 * Tenant Name 值对象。
 */
public final class TenantName {
    private final String value;

    /**
     * 创建 TenantName 实例。
     *
     * @param value value 参数
     */
    private TenantName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("租户名不能为空");
        }
        this.value = value;
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static TenantName of(String value) { return new TenantName(value); }

    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public String value() { return value; }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantName that)) return false;
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
    public String toString() { return value; }
}
