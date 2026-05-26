package com.develop.mvp.pk.module.wms.domain.inventory.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WmsInventoryStockChangedEvent(Long inventoryId, Long skuId, Long warehouseId,
                                            BigDecimal oldQuantity, BigDecimal newQuantity,
                                            BigDecimal changeAmount,
                                            LocalDateTime occurredAt) implements DomainEvent {

    public WmsInventoryStockChangedEvent(Long inventoryId, Long skuId, Long warehouseId,
                                          BigDecimal oldQuantity, BigDecimal newQuantity,
                                          BigDecimal changeAmount) {
        this(inventoryId, skuId, warehouseId, oldQuantity, newQuantity, changeAmount, LocalDateTime.now());
    }
}
