package com.develop.mvp.pk.module.bpm.domain.listener.valueobject;
// DDD 角色：BPM监听器名称值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class ListenerName {
    private final String value;
    private ListenerName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("监听器名称不能为空");
        this.value = value.trim();
    }
    public static ListenerName of(String value) { return new ListenerName(value); }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof ListenerName l && value.equals(l.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
