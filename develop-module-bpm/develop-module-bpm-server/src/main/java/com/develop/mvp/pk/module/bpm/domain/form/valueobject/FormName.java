package com.develop.mvp.pk.module.bpm.domain.form.valueobject;
// DDD 角色：BPM表单名称值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class FormName {
    private final String value;
    private FormName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("表单名称不能为空");
        this.value = value.trim();
    }
    public static FormName of(String value) { return new FormName(value); }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof FormName f && value.equals(f.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
