package com.develop.mvp.pk.module.system.domain.permission.valueobject;

import java.util.Objects;

/**
 * Role Id 值对象。
 */
public final class RoleId {
    private final Long value;
    /**
     * 创建 RoleId 实例。
     *
     * @param value value 参数
     */
    private RoleId(Long value) { this.value = Objects.requireNonNull(value); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static RoleId of(Long value) { return new RoleId(value); }
    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof RoleId r && value.equals(r.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
