package com.develop.mvp.pk.module.member.domain.user.valueobject;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

// Skill: AggregateRoot_MemberUser_Skill — 值对象 RawPassword（明文密码，长度>=6）
public final class RawPassword {
    private final String value;
    public RawPassword(String value) {
        if (StrUtil.isEmpty(value) || value.length() < 6) throw new IllegalArgumentException("密码长度不能小于6位");
        this.value = value;
    }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof RawPassword p && Objects.equals(value, p.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
