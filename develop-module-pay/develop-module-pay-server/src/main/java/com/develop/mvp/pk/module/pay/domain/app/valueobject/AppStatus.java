package com.develop.mvp.pk.module.pay.domain.app.valueobject;
// DDD 角色：支付应用状态值对象 - AggregateRoot_Pay_Skill
import java.util.Objects;
public final class AppStatus {
    private final Integer value;
    public AppStatus(Integer value) { this.value = Objects.requireNonNull(value); }
    public Integer value() { return value; }
    public boolean isEnabled() { return value == 0; }
    public boolean isDisabled() { return value == 1; }
    @Override public boolean equals(Object o) { return o instanceof AppStatus s && value.equals(s.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return "AppStatus(" + value + ")"; }
}
