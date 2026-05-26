package com.develop.mvp.pk.module.system.domain.tenant.valueobject;

// Skill: AggregateRoot_Tenant_Validation_Skill — 值对象 TenantExpireTime
// 不变式 I05：过期时间不能为空，过期租户不可被校验通过
// 验收标准 AC04：final 字段，无 setter

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Tenant Expire Time 值对象。
 */
public final class TenantExpireTime {
    private final LocalDateTime value;

    /**
     * 创建 TenantExpireTime 实例。
     *
     * @param value value 参数
     */
    private TenantExpireTime(LocalDateTime value) {
        this.value = Objects.requireNonNull(value, "过期时间不能为空");
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static TenantExpireTime of(LocalDateTime value) { return new TenantExpireTime(value); }

    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public LocalDateTime value() { return value; }

    /** 不变式 I05：租户是否已过期 */
    public boolean isExpired() {
        return value.isBefore(LocalDateTime.now());
    }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantExpireTime that)) return false;
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
    public String toString() { return value.toString(); }
}
