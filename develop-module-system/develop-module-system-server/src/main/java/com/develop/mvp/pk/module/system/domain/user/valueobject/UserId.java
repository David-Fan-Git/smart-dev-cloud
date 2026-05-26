package com.develop.mvp.pk.module.system.domain.user.valueobject;

// Skill: AggregateRoot_User_Validation_Skill — 聚合根标识 UserId
// DDD 角色：不可变值对象，作为 User 聚合根的唯一标识

import java.util.Objects;

/**
 * User Id 值对象。
 */
public final class UserId {

    private final Long value;

    /**
     * 创建 UserId 实例。
     *
     * @param value value 参数
     */
    private UserId(Long value) {
        this.value = Objects.requireNonNull(value, "用户ID不能为空");
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param value value 参数
     * @return 处理结果
     */
    public static UserId of(Long value) {
        return new UserId(value);
    }

    /**
     * 执行 value 对应的业务操作。
     *
     * @return 处理结果
     */
    public Long value() { return value; }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId that)) return false;
        return value.equals(that.value);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() { return Objects.hash(value); }

    /**
     * 执行 to String 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public String toString() { return "UserId{" + value + '}'; }
}
