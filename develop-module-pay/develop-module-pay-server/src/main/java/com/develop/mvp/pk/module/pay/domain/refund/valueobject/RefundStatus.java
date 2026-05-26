package com.develop.mvp.pk.module.pay.domain.refund.valueobject;
// DDD 角色：退款状态值对象 - AggregateRoot_Pay_Skill
import java.util.Objects;
public final class RefundStatus {
    public static final RefundStatus WAITING = new RefundStatus(0);
    public static final RefundStatus SUCCESS = new RefundStatus(10);
    public static final RefundStatus FAILURE = new RefundStatus(20);
    private final Integer value;
    public RefundStatus(Integer value) { this.value = Objects.requireNonNull(value); }
    public Integer value() { return value; }
    public boolean isWaiting() { return value == 0; }
    public boolean isSuccess() { return value == 10; }
    public boolean isFailure() { return value == 20; }
    public boolean isTerminal() { return isSuccess() || isFailure(); }
    @Override public boolean equals(Object o) { return o instanceof RefundStatus s && value.equals(s.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return "RefundStatus(" + value + ")"; }
}
