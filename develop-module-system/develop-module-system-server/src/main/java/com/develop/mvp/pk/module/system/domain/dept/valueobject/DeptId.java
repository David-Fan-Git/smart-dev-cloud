package com.develop.mvp.pk.module.system.domain.dept.valueobject;

import java.util.Objects;

/**
 * Dept Id 值对象。
 */
public final class DeptId {
    private final Long value;
    /**
     * 创建 DeptId 实例。
     *
     * @param value value 参数
     */
    private DeptId(Long value) { this.value = Objects.requireNonNull(value); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static DeptId of(Long value) { return new DeptId(value); }
    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof DeptId d && value.equals(d.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
