package com.develop.mvp.pk.module.system.domain.user.valueobject;

// Skill: AggregateRoot_User_Validation_Skill — 值对象 LoginRecord
// DDD 角色：不可变值对象，记录用户登录的 IP 和时间
// 验收标准 AC16：User 聚合提供 recordLogin(LoginRecord) 方法
// 验收标准 AC04：final 字段，无 setter

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Login Record 值对象。
 */
public final class LoginRecord {

    private final String loginIp;
    private final LocalDateTime loginDate;

    /**
     * 创建 LoginRecord 实例。
     *
     * @param loginIp loginIp 参数
     * @param loginDate loginDate 参数
     */
    private LoginRecord(String loginIp, LocalDateTime loginDate) {
        this.loginIp = loginIp;
        this.loginDate = Objects.requireNonNull(loginDate, "登录时间不能为空");
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param loginIp loginIp 参数
     * @return 处理结果
     */
    public static LoginRecord of(String loginIp) {
        return new LoginRecord(loginIp, LocalDateTime.now());
    }

    /**
     * 执行 of 对应的业务操作。
     *
     * @param loginIp loginIp 参数
     * @param loginDate loginDate 参数
     * @return 处理结果
     */
    public static LoginRecord of(String loginIp, LocalDateTime loginDate) {
        return new LoginRecord(loginIp, loginDate);
    }

    /**
     * 处理 login Ip 对应的认证流程。
     *
     * @return 处理结果
     */
    public String loginIp() { return loginIp; }
    /**
     * 处理 login Date 对应的认证流程。
     *
     * @return 处理结果
     */
    public LocalDateTime loginDate() { return loginDate; }

    /**
     * 执行 equals 对应的业务操作。
     *
     * @param o o 参数
     * @return 处理结果
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginRecord that)) return false;
        return Objects.equals(loginIp, that.loginIp) && loginDate.equals(that.loginDate);
    }

    /**
     * 判断 hash Code 对应的条件是否成立。
     *
     * @return 处理结果
     */
    @Override
    public int hashCode() { return Objects.hash(loginIp, loginDate); }

    /**
     * 执行 to String 对应的业务操作。
     *
     * @return 处理结果
     */
    @Override
    public String toString() {
        return "LoginRecord{ip='" + loginIp + "', date=" + loginDate + '}';
    }
}
