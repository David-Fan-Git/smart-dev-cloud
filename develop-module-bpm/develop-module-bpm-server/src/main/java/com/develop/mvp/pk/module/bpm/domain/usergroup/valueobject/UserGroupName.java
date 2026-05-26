package com.develop.mvp.pk.module.bpm.domain.usergroup.valueobject;
// DDD 角色：BPM用户组名称值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class UserGroupName {
    private final String value;
    private UserGroupName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("用户组名称不能为空");
        this.value = value.trim();
    }
    public static UserGroupName of(String value) { return new UserGroupName(value); }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof UserGroupName u && value.equals(u.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
