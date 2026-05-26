package com.develop.mvp.pk.module.bpm.domain.copy.valueobject;
// DDD 角色：BPM流程抄送ID值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class CopyId {
    private final Long value;
    private CopyId(Long value) { this.value = Objects.requireNonNull(value, "抄送ID不能为空"); }
    public static CopyId of(Long value) { return new CopyId(value); }
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof CopyId c && value.equals(c.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
