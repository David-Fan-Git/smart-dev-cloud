package com.develop.mvp.pk.module.bpm.domain.expression.valueobject;
// DDD 角色：BPM表达式ID值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class ExpressionId {
    private final Long value;
    private ExpressionId(Long value) { this.value = Objects.requireNonNull(value, "表达式ID不能为空"); }
    public static ExpressionId of(Long value) { return new ExpressionId(value); }
    public Long value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof ExpressionId e && value.equals(e.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
