package com.develop.mvp.pk.module.wms.domain.inventory;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class WmsInventoryTest {

    @Test
    void create_rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> WmsInventoryFactory.create(1L, 100L, new BigDecimal("-1.00"), "负库存"));
    }

    @Test
    void setQuantity_rejectsNegativeQuantity() {
        WmsInventory inventory = WmsInventoryFactory.create(1L, 100L, BigDecimal.ZERO, null);

        assertThrows(IllegalArgumentException.class,
                () -> inventory.setQuantity(new BigDecimal("-1.00"), "负库存"));
    }
}
