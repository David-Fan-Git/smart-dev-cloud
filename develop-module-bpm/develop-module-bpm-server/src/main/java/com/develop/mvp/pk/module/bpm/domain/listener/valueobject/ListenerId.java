package com.develop.mvp.pk.module.bpm.domain.listener.valueobject;
// DDD 角色：BPM监听器ID值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class ListenerId {
    private final Long value;
    private ListenerId(Long value) { this.value = Objects.requireNonNull(value, "监听器ID不能为空"); }
    public static ListenerId of(Long value) { return new ListenerId(value); }
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof ListenerId l && value.equals(l.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
