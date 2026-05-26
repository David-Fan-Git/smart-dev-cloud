package com.develop.mvp.pk.module.erp.domain.product.valueobject;

import java.util.Objects;

public final class ErpProductId {
    private final Long value;

    private ErpProductId(Long value) {
        this.value = Objects.requireNonNull(value, "productId不能为空");
    }

    public static ErpProductId of(Long value) { return new ErpProductId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErpProductId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "ErpProductId{" + value + '}'; }
}
