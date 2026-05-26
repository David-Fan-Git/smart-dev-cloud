package com.develop.mvp.pk.module.bpm.domain.leave.valueobject;
// DDD 角色：BPM请假单ID值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class LeaveId {
    private final Long value;
    private LeaveId(Long value) { this.value = Objects.requireNonNull(value, "请假单ID不能为空"); }
    public static LeaveId of(Long value) { return new LeaveId(value); }
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof LeaveId l && value.equals(l.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
