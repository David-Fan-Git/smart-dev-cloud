package com.develop.mvp.pk.module.trade.domain.cart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class CartTest {

    @Test
    void create_allowsTransientId() {
        Cart cart = CartFactory.create(null, 1L, 2L, 3L, 1, true);

        assertNull(cart.id());
    }
}
