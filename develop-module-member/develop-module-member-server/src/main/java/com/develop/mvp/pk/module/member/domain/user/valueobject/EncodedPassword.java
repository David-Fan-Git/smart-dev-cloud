package com.develop.mvp.pk.module.member.domain.user.valueobject;

import cn.hutool.core.util.StrUtil;
import java.util.Objects;

// Skill: AggregateRoot_MemberUser_Skill — 值对象 EncodedPassword（BCrypt密文）
public final class EncodedPassword {
    private final String value;
    public EncodedPassword(String value) {
        if (StrUtil.isBlank(value)) throw new IllegalArgumentException("加密密码不能为空");
        this.value = value;
    }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof EncodedPassword p && Objects.equals(value, p.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
