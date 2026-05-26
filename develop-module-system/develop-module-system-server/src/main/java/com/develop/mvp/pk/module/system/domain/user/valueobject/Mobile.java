package com.develop.mvp.pk.module.system.domain.user.valueobject;

// Skill: AggregateRoot_User_Validation_Skill — 值对象 Mobile
// 不变式 I03：手机号在同一租户内不可重复（UserUniquenessChecker 保证）
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

/**
 * Mobile 值对象。
 */
public final class Mobile {

    private final String value;

    /**
     * 创建 Mobile 实例。
     *
     * @param value value 参数
     */
    private Mobile(String value) {
        this.value = (value == null || value.isBlank()) ? null : value.trim();
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static Mobile of(String value) {
        return new Mobile(value);
    }

    /**
     * 执行 empty 对应的业务操作。
     *
     * @return 处理结果
     */
    public static Mobile empty() {
        return new Mobile(null);
    }

    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public String value() {
        return value;
    }

    /**
     * 判断 is Present 对应的条件是否成立。
     *
     * @return 处理结果
     */
    public boolean isPresent() {
        return value != null;
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
        if (!(o instanceof Mobile that)) return false;
        return Objects.equals(value, that.value);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * 执行 to String 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public String toString() {
        return value;
    }
}
