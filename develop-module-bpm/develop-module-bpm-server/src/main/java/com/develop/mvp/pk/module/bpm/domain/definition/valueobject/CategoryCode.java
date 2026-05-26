package com.develop.mvp.pk.module.bpm.domain.definition.valueobject;
// DDD 角色：BPM流程分类编码值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class CategoryCode {
    private final String value;
    private CategoryCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("分类编码不能为空");
        }
        this.value = value.trim();
    }
    public static CategoryCode of(String value) { return new CategoryCode(value); }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof CategoryCode c && value.equals(c.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
