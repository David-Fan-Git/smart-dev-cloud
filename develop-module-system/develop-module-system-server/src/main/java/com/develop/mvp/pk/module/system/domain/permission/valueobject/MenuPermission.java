package com.develop.mvp.pk.module.system.domain.permission.valueobject;

import java.util.Objects;

/**
 * Menu Permission 值对象。
 */
public final class MenuPermission {
    private final String value;
    /**
     * 创建 MenuPermission 实例。
     *
     * @param value value 参数
     */
    private MenuPermission(String value) { this.value = value; }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static MenuPermission of(String value) { return new MenuPermission(value); }
    /**
     * 执行 empty 对应的业务操作。
     *
     * @return 处理结果
     */
    public static MenuPermission empty() { return new MenuPermission(null); }
    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public String value() { return value; }
    /**
     * 判断 is Present 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isPresent() { return value != null && !value.isBlank(); }
    @Override public boolean equals(Object o) { return o instanceof MenuPermission m && Objects.equals(value, m.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
