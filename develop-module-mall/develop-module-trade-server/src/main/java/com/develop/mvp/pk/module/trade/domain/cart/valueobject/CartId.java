package com.develop.mvp.pk.module.trade.domain.cart.valueobject;

// Skill: AggregateRoot_Cart_Validation_Skill

import java.util.Objects;

public final class CartId {
    private final Long value;

    private CartId(Long value) { this.value = Objects.requireNonNull(value, "购物车编号不能为空"); }
    public static CartId of(Long value) { return new CartId(value); }
    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "CartId{" + value + '}'; }
}
