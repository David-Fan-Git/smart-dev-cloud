package com.develop.mvp.pk.module.bpm.domain.definition.valueobject;
// DDD 角色：BPM流程分类ID值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class CategoryId {
    private final Long value;
    private CategoryId(Long value) { this.value = Objects.requireNonNull(value, "分类ID不能为空"); }
    public static CategoryId of(Long value) { return new CategoryId(value); }
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof CategoryId c && value.equals(c.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
