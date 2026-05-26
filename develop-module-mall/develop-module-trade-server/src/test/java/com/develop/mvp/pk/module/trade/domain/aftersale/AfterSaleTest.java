package com.develop.mvp.pk.module.trade.domain.aftersale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class AfterSaleTest {

    @Test
    void create_allowsTransientId() {
        AfterSale afterSale = AfterSaleFactory.create(null, "AS202605230001", 1L, 2L, 3L,
                4L, 5L, 1, 10, "不想要了", "description", new String[0], 10, 100);

        assertNull(afterSale.id());
    }
}
