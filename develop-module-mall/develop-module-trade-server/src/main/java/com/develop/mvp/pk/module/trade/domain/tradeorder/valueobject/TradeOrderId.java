package com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject;

import java.util.Objects;

public final class TradeOrderId {
    private final Long value;

    private TradeOrderId(Long value) { this.value = Objects.requireNonNull(value, "订单编号不能为空"); }
    public static TradeOrderId of(Long value) { return new TradeOrderId(value); }
    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradeOrderId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "TradeOrderId{" + value + '}'; }
}
