package com.develop.mvp.pk.module.pay.domain.order.valueobject;
// DDD 角色：订单状态值对象 - AggregateRoot_Pay_Skill
import java.util.Objects;
public final class OrderStatus {
    public static final OrderStatus WAITING = new OrderStatus(0);
    public static final OrderStatus SUCCESS = new OrderStatus(10);
    public static final OrderStatus REFUND = new OrderStatus(20);
    public static final OrderStatus CLOSED = new OrderStatus(30);
    private final Integer value;
    public OrderStatus(Integer value) { this.value = Objects.requireNonNull(value); }
    public Integer value() { return value; }
    public boolean isWaiting() { return value == 0; }
    public boolean isSuccess() { return value == 10; }
    public boolean isRefund() { return value == 20; }
    public boolean isClosed() { return value == 30; }
    public boolean isSuccessOrRefund() { return isSuccess() || isRefund(); }
    public boolean canSubmit() { return isWaiting(); }
    public boolean canRefund() { return isSuccess() || isRefund(); }
    public OrderStatus paid() { return SUCCESS; }
    public OrderStatus close() { return CLOSED; }
    public OrderStatus markRefund() { return REFUND; }
    @Override public boolean equals(Object o) { return o instanceof OrderStatus s && value.equals(s.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return "OrderStatus(" + value + ")"; }
}
