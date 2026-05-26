package com.develop.mvp.pk.module.system.domain.dept.valueobject;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

/**
 * Dept Status 值对象。
 */
public final class DeptStatus {
    public static final DeptStatus ENABLED = new DeptStatus(CommonStatusEnum.ENABLE.getStatus());
    public static final DeptStatus DISABLED = new DeptStatus(CommonStatusEnum.DISABLE.getStatus());
    private final Integer code;
    /**
     * 创建 DeptStatus 实例。
     *
     * @param code code 参数
     */
    private DeptStatus(Integer code) { this.code = Objects.requireNonNull(code); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param code code 参数
     * @return 处理结果
     */
    public static DeptStatus of(Integer code) {
        return CommonStatusEnum.ENABLE.getStatus().equals(code) ? ENABLED : DISABLED;
    }
    /**
     * 判断 is Enabled 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isEnabled() { return code.equals(CommonStatusEnum.ENABLE.getStatus()); }
    /**
     * 执行 code 对应的业务操作。
     *
     * @return 处理结果
     */
    public Integer code() { return code; }
    /**
     * 更新 disable 对应的数据。
     *
     * @return 处理结果
     */
    public DeptStatus disable() { return DISABLED; }
    @Override public boolean equals(Object o) { return o instanceof DeptStatus s && code.equals(s.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
