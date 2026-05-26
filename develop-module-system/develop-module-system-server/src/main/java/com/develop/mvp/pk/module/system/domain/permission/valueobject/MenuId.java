package com.develop.mvp.pk.module.system.domain.permission.valueobject;

import java.util.Objects;

/**
 * Menu Id 值对象。
 */
public final class MenuId {
    public static final Long ROOT_ID = 0L;
    private final Long value;
    /**
     * 创建 MenuId 实例。
     *
     * @param value value 参数
     */
    private MenuId(Long value) { this.value = Objects.requireNonNull(value); }
    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static MenuId of(Long value) { return new MenuId(value); }
    /**
     * 执行 root 对应的业务操作。
     *
     * @return 处理结果
     */
    public static MenuId root() { return new MenuId(ROOT_ID); }
    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long value() { return value; }
    /**
     * 判断 is Root 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isRoot() { return ROOT_ID.equals(value); }
    @Override public boolean equals(Object o) { return o instanceof MenuId m && value.equals(m.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
