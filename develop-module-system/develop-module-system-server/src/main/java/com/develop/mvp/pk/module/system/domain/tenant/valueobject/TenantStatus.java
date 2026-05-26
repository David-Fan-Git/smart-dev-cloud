package com.develop.mvp.pk.module.system.domain.tenant.valueobject;

// Skill: AggregateRoot_Tenant_Validation_Skill — 值对象 TenantStatus
// 不变式 I03：status 只能是 ENABLE 或 DISABLE
// 规则 R05/R08：默认启用 + 禁用状态变更
// 验收标准 AC04：final 字段，无 setter

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;

import java.util.Objects;

/**
 * Tenant Status 值对象。
 */
public final class TenantStatus {

    public static final TenantStatus ENABLED = new TenantStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final TenantStatus DISABLED = new TenantStatus(CommonStatusEnum.DISABLE.getStatus());

    private final Integer code;

    /**
     * 创建 TenantStatus 实例。
     *
     * @param code code 参数
     */
    private TenantStatus(Integer code) {
        this.code = Objects.requireNonNull(code, "状态不能为空");
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param code code 参数
     * @return 处理结果
     */
    public static TenantStatus of(Integer code) {
        if (CommonStatusEnum.ENABLE.getStatus().equals(code)) return ENABLED;
        if (CommonStatusEnum.DISABLE.getStatus().equals(code)) return DISABLED;
        throw new IllegalArgumentException("无效的租户状态: " + code);
    }

    /** 规则 R08：禁用租户 */
    public TenantStatus disable() { return DISABLED; }
    /**
     * 更新 enable 对应的数据。
     *
     * @return 处理结果
     */
    public TenantStatus enable() { return ENABLED; }

    /**
     * 判断 is Enabled 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isEnabled() { return code.equals(CommonStatusEnum.ENABLE.getStatus()); }
    /**
     * 判断 is Disabled 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isDisabled() { return code.equals(CommonStatusEnum.DISABLE.getStatus()); }

    /**
     * 执行 code 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer code() { return code; }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantStatus that)) return false;
        return code.equals(that.code);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String toString() { return isEnabled() ? "ENABLED" : "DISABLED"; }
}
