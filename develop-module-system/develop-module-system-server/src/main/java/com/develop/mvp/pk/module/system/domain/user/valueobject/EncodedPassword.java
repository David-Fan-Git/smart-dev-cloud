package com.develop.mvp.pk.module.system.domain.user.valueobject;

// Skill: AggregateRoot_User_Validation_Skill — 值对象 EncodedPassword
// DDD 角色：不可变值对象，封装 BCrypt 加密后的密码密文
// 不变式 I04：密码永远以 BCrypt 密文存储，不可明文回读
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

/**
 * Encoded Password 值对象。
 */
public final class EncodedPassword {

    private final String encodedValue;

    /**
     * 创建 EncodedPassword 实例。
     *
     * @param encodedValue encodedValue 参数
     */
    private EncodedPassword(String encodedValue) {
        this.encodedValue = Objects.requireNonNull(encodedValue, "加密密码不能为空");
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param encodedValue encodedValue 参数
     * @return 处理结果
     */
    public static EncodedPassword of(String encodedValue) {
        return new EncodedPassword(encodedValue);
    }

    /**
     * 执行 encoded Value 对应的业务操作。
     *
     * @return 处理结果
     */
    public String encodedValue() {
        return encodedValue;
    }

    /** 用于持久化，仅仓储实现可调用 */
    public String toStoreValue() {
        return encodedValue;
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
        if (!(o instanceof EncodedPassword that)) return false;
        return encodedValue.equals(that.encodedValue);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() {
        return Objects.hash(encodedValue);
    }

    /**
     * 执行 to String 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public String toString() {
        return "[ENCRYPTED]";
    }
}
