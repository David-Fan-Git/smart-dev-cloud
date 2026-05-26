package com.develop.mvp.pk.module.pay.domain.order.valueobject;
// DDD 角色：订单金额值对象 - AggregateRoot_Pay_Skill
import java.util.Objects;
public final class OrderPrice {
    private final Integer cents;
    public OrderPrice(Integer cents) {
        if (cents == null || cents < 0) throw new IllegalArgumentException("Price must be non-negative");
        this.cents = cents;
    }
    public Integer cents() { return cents; }
    public boolean isZero() { return cents == 0; }
    public OrderPrice add(Integer other) { return new OrderPrice(cents + other); }
    public OrderPrice subtract(Integer other) {
        if (other > cents) throw new IllegalArgumentException("Insufficient price");
        return new OrderPrice(cents - other);
    }
    public boolean canRefund(Integer refundAmount) { return refundAmount <= cents; }
    @Override public boolean equals(Object o) { return o instanceof OrderPrice p && cents.equals(p.cents); }
    @Override public int hashCode() { return Objects.hash(cents); }
    @Override public String toString() { return "OrderPrice(" + cents + "cents)"; }
}
