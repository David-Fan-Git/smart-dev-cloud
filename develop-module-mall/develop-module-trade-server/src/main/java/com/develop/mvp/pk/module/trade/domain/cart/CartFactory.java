package com.develop.mvp.pk.module.trade.domain.cart;

// Skill: AggregateRoot_Cart_Validation_Skill

import com.develop.mvp.pk.module.trade.domain.cart.valueobject.CartId;

public final class CartFactory {
    private CartFactory() {}

    public static Cart create(Long id, Long userId, Long spuId, Long skuId, Integer count, Boolean selected) {
        return new Cart(id != null ? CartId.of(id) : null, userId, spuId, skuId, count, selected);
    }

    public static Cart reconstitute(Long id, Long userId, Long spuId, Long skuId, Integer count, Boolean selected) {
        return new Cart(CartId.of(id), userId, spuId, skuId, count, selected);
    }
}
