package com.develop.mvp.pk.module.statistics.domain.productstatistics.valueobject;

import java.util.Objects;

public final class ProductStatisticsId {
    private final Long value;

    private ProductStatisticsId(Long value) { this.value = Objects.requireNonNull(value, "统计编号不能为空"); }
    public static ProductStatisticsId of(Long value) { return new ProductStatisticsId(value); }
    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductStatisticsId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
}
