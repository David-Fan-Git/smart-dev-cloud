package com.develop.mvp.pk.module.bpm.domain.usergroup.valueobject;
// DDD 角色：BPM用户组ID值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class UserGroupId {
    private final Long value;
    private UserGroupId(Long value) { this.value = Objects.requireNonNull(value, "用户组ID不能为空"); }
    public static UserGroupId of(Long value) { return new UserGroupId(value); }
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof UserGroupId u && value.equals(u.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
