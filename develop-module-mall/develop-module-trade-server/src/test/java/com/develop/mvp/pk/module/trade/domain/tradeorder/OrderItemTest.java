package com.develop.mvp.pk.module.trade.domain.tradeorder;

import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.OrderItem;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.OrderItemProperty;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderItemTest {

    @Test
    void create_usesDomainPropertyValueObject() {
        OrderItemProperty property = new OrderItemProperty(1L, "颜色", 2L, "红色");

        OrderItem item = new OrderItem(1L, 2L, 3L, 4L, "商品", 5L,
                List.of(property), "pic.png", 1, false, 100, 0, 0, 0,
                100, 0, 0, 0, 0, 0, null, 0);

        assertEquals(List.of(property), item.properties());
    }
}
