package com.develop.mvp.pk.module.trade.domain.tradeorder;

import com.develop.mvp.pk.module.trade.enums.order.TradeOrderStatusEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradeOrderTest {

    @Test
    void create_allowsTransientId() {
        TradeOrder order = TradeOrderFactory.create(null, "NO202605230001", 1, 20, 1L,
                "127.0.0.1", null, 100, 0, 0, 0, 100, 1,
                "张三", "13800138000", 110000, "北京市朝阳区", List.of());

        assertNull(order.id());
    }

    @Test
    void updateReceiver_allowsOnlyUndeliveredStatus() {
        TradeOrder unpaid = createOrder(1L, TradeOrderStatusEnum.UNPAID.getStatus());
        TradeOrder undelivered = createOrder(2L, TradeOrderStatusEnum.UNDELIVERED.getStatus());

        assertThrows(IllegalStateException.class,
                () -> unpaid.updateReceiver("张三", "13800138000", 110000, "北京市朝阳区"));
        assertDoesNotThrow(() -> undelivered.updateReceiver("李四", "13800138001", 110000, "北京市海淀区"));
    }

    private TradeOrder createOrder(Long id, Integer status) {
        return TradeOrderFactory.reconstitute(id, "NO202605230001", 1, 20, 1L,
                "127.0.0.1", null, status, 1, null, null, null,
                null, false, null, null, false, null, null,
                100, 0, 0, 0, 100, 1, null, null,
                null, null, "张三", "13800138000", 110000, "北京市朝阳区",
                null, null, 0, 0, null, 0, 0, 0,
                0, 0, 0, null, null, null, null, null,
                null, null, null, null, List.of());
    }
}
