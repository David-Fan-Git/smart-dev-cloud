package com.develop.mvp.pk.module.bpm.domain.form.valueobject;
// DDD 角色：BPM表单ID值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class FormId {
    private final Long value;
    private FormId(Long value) { this.value = Objects.requireNonNull(value, "表单ID不能为空"); }
    public static FormId of(Long value) { return new FormId(value); }
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof FormId f && value.equals(f.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
