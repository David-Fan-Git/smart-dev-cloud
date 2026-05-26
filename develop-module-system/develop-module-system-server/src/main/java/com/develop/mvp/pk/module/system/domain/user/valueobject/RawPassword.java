package com.develop.mvp.pk.module.system.domain.user.valueobject;

// Skill: AggregateRoot_User_Validation_Skill — 值对象 RawPassword
// DDD 角色：不可变值对象，封装明文密码（仅在创建/修改密码时短暂存在）
// 不变式 I04：密码永远以 BCrypt 密文存储
// 验收标准 AC04：final 字段，无 setter

import com.develop.mvp.pk.framework.common.exception.ServiceException;

import java.util.Objects;

import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.USER_PASSWORD_FAILED;

/**
 * Raw Password 值对象。
 */
public final class RawPassword {

    private final String rawValue;

    /**
     * 创建 RawPassword 实例。
     *
     * @param rawValue rawValue 参数
     * @param validateLength validateLength 参数
     */
    private RawPassword(String rawValue, boolean validateLength) {
        if (rawValue == null || (validateLength && rawValue.length() < 6)) {
            throw new ServiceException(USER_PASSWORD_FAILED.getCode(), "密码长度不能小于6位");
        }
        this.rawValue = rawValue;
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param rawValue rawValue 参数
     * @return 处理结果
     */
    public static RawPassword of(String rawValue) {
        return new RawPassword(rawValue, true);
    }

    /**
     * 执行 for Verification 对应的业务操作。
     *
     * @param rawValue rawValue 参数
     * @return 处理结果
     */
    public static RawPassword forVerification(String rawValue) {
        return new RawPassword(rawValue, false);
    }

    /** 仅 PasswordEncoder 实现类可读取明文值进行加密/比对 */
    public String rawValue() {
        return rawValue;
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
        if (!(o instanceof RawPassword that)) return false;
        return rawValue.equals(that.rawValue);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() {
        return Objects.hash(rawValue);
    }
}
