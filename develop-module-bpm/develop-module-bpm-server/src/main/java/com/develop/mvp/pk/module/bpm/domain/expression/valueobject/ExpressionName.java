package com.develop.mvp.pk.module.bpm.domain.expression.valueobject;
// DDD 角色：BPM表达式名称值对象 - AggregateRoot_Bpm_Skill
import java.util.Objects;

public final class ExpressionName {
    private final String value;
    private ExpressionName(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("表达式名称不能为空");
        this.value = value.trim();
    }
    public static ExpressionName of(String value) { return new ExpressionName(value); }
    public String value() { return value; }
    @Override public boolean equals(Object o) { return o instanceof ExpressionName e && value.equals(e.value); }
    @Override public int hashCode() { return Objects.hash(value); }
}
