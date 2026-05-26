package com.develop.mvp.pk.module.trade.domain.cart.repository;

// Skill: AggregateRoot_Cart_Validation_Skill

import com.develop.mvp.pk.module.trade.domain.cart.Cart;
import com.develop.mvp.pk.module.trade.domain.cart.valueobject.CartId;

import java.util.List;

public interface CartRepository {
    Cart save(Cart cart);
    void delete(CartId id);
    void deleteByUserId(Long userId);
    Cart findById(CartId id);
    List<Cart> findByUserId(Long userId);
    Cart findByUserIdAndSkuId(Long userId, Long skuId);
    void deleteSelectedByUserId(Long userId);
    void selectAllByUserId(Long userId);
    void unselectAllByUserId(Long userId);
}
