package com.develop.mvp.pk.module.system.domain.permission.valueobject;

import java.util.Objects;

/**
 * Role Name 值对象。
 */
public final class RoleName {
    private final String value;
    /**
     * 创建 RoleName 实例。
     *
     * @param value value 参数
     */
    private RoleName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("角色名称不能为空");
        this.value = value;
    }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static RoleName of(String value) { return new RoleName(value); }
    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof RoleName r && value.equals(r.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
