package com.develop.mvp.pk.module.system.domain.permission.valueobject;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

/**
 * Role Status 值对象。
 */
public final class RoleStatus {
    public static final RoleStatus ENABLED = new RoleStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final RoleStatus DISABLED = new RoleStatus(CommonStatusEnum.DISABLE.getStatus());
    private final Integer code;
    /**
     * 创建 RoleStatus 实例。
     *
     * @param code code 参数
     */
    private RoleStatus(Integer code) { this.code = Objects.requireNonNull(code); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param code code 参数
     * @return 处理结果
     */
    public static RoleStatus of(Integer code) {
        if (CommonStatusEnum.ENABLE.getStatus().equals(code)) return ENABLED;
        if (CommonStatusEnum.DISABLE.getStatus().equals(code)) return DISABLED;
        throw new IllegalArgumentException("无效状态: " + code);
    }
    /**
     * 执行 from Persisted 对应的业务操作。
     *
     * @param code code 参数
     * @return 处理结果
     */
    public static RoleStatus fromPersisted(Integer code) {
        return CommonStatusEnum.DISABLE.getStatus().equals(code) ? DISABLED : ENABLED;
    }
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
    @Override public boolean equals(Object o) { return o instanceof RoleStatus r && code.equals(r.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
