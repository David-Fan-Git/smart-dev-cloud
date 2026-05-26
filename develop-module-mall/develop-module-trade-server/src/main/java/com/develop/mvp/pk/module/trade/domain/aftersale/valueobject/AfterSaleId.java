package com.develop.mvp.pk.module.trade.domain.aftersale.valueobject;

import java.util.Objects;

public final class AfterSaleId {
    private final Long value;

    private AfterSaleId(Long value) { this.value = Objects.requireNonNull(value, "售后编号不能为空"); }
    public static AfterSaleId of(Long value) { return new AfterSaleId(value); }
    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AfterSaleId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "AfterSaleId{" + value + '}'; }
}
