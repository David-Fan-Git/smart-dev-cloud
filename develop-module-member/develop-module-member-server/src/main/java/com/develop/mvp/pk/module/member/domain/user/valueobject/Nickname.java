package com.develop.mvp.pk.module.member.domain.user.valueobject;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

// Skill: AggregateRoot_MemberUser_Skill — 值对象 Nickname
public final class Nickname {
    private final String value;
    public Nickname(String value) {
        if (StrUtil.isBlank(value)) throw new IllegalArgumentException("昵称不能为空");
        this.value = value.trim();
    }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof Nickname n && Objects.equals(value, n.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
