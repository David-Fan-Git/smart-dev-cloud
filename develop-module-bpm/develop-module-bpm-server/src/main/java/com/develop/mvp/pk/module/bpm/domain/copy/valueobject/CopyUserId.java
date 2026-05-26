package com.develop.mvp.pk.module.bpm.domain.copy.valueobject;
// DDD 角色：BPM抄送用户ID值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class CopyUserId {
    private final Long value;
    private CopyUserId(Long value) { this.value = Objects.requireNonNull(value); }
    public static CopyUserId of(Long value) { return new CopyUserId(value); }
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof CopyUserId c && value.equals(c.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
