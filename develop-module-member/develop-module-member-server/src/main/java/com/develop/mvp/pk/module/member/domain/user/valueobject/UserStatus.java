package com.develop.mvp.pk.module.member.domain.user.valueobject;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import java.util.Objects;

// Skill: AggregateRoot_MemberUser_Skill — 值对象 UserStatus
public final class UserStatus {
    public static final Integer ENABLE = CommonStatusEnum.ENABLE.getStatus();
    public static final Integer DISABLE = CommonStatusEnum.DISABLE.getStatus();
    private final Integer code;
    public UserStatus(Integer code) {
        if (code == null || (!code.equals(ENABLE) && !code.equals(DISABLE)))
            throw new IllegalArgumentException("状态只能是 ENABLE 或 DISABLE: " + code);
        this.code = code;
    }
    public static UserStatus enabled() { return new UserStatus(ENABLE); }
    public static UserStatus disabled() { return new UserStatus(DISABLE); }
    public Integer code() { return code; }
    public boolean isEnabled() { return code.equals(ENABLE); }
    @Override public boolean equals(Object o) { return o instanceof UserStatus s && Objects.equals(code, s.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
