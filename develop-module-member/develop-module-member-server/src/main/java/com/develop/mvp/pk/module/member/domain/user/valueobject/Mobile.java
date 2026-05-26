package com.develop.mvp.pk.module.member.domain.user.valueobject;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

// Skill: AggregateRoot_MemberUser_Skill — 值对象 Mobile
public final class Mobile {
    private final String value;
    public Mobile(String value) {
        if (StrUtil.isBlank(value)) throw new IllegalArgumentException("手机号不能为空");
        this.value = value.trim();
    }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof Mobile m && Objects.equals(value, m.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
