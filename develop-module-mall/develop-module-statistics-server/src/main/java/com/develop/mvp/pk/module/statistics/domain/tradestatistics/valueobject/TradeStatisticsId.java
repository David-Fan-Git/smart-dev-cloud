package com.develop.mvp.pk.module.statistics.domain.tradestatistics.valueobject;

import java.util.Objects;

public final class TradeStatisticsId {
    private final Long value;

    private TradeStatisticsId(Long value) { this.value = Objects.requireNonNull(value, "交易统计编号不能为空"); }
    public static TradeStatisticsId of(Long value) { return new TradeStatisticsId(value); }
    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradeStatisticsId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
}
