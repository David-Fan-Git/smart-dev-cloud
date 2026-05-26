package com.develop.mvp.pk.module.wms.domain.inventory.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WmsInventoryCreatedEvent(Long inventoryId, Long skuId, Long warehouseId,
                                       BigDecimal quantity, LocalDateTime occurredAt) implements DomainEvent {

    public WmsInventoryCreatedEvent(Long inventoryId, Long skuId, Long warehouseId, BigDecimal quantity) {
        this(inventoryId, skuId, warehouseId, quantity, LocalDateTime.now());
    }
}
