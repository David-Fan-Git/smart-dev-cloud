package com.develop.mvp.pk.module.member.domain.user.valueobject;

import java.time.LocalDateTime;
import java.util.Objects;

// Skill: AggregateRoot_MemberUser_Skill — 值对象 LoginRecord
public final class LoginRecord {
    private final String loginIp;
    private final LocalDateTime loginDate;
    public LoginRecord(String loginIp, LocalDateTime loginDate) {
        this.loginIp = loginIp;
        this.loginDate = Objects.requireNonNull(loginDate, "登录日期不能为空");
    }
    public String loginIp() { return loginIp; }
    public LocalDateTime loginDate() { return loginDate; }
}
