package com.develop.mvp.pk.module.system.domain.permission.valueobject;

import java.util.Objects;

/**
 * Menu Name 值对象。
 */
public final class MenuName {
    private final String value;
    /**
     * 创建 MenuName 实例。
     *
     * @param value value 参数
     */
    private MenuName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("菜单名称不能为空");
        this.value = value;
    }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static MenuName of(String value) { return new MenuName(value); }
    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof MenuName m && value.equals(m.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
