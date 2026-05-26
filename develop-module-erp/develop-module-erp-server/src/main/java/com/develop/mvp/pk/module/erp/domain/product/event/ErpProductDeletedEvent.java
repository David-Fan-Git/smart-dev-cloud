package com.develop.mvp.pk.module.erp.domain.product.event;

import java.time.LocalDateTime;

public record ErpProductDeletedEvent(Long productId, String name, LocalDateTime occurredAt) implements DomainEvent {
    public ErpProductDeletedEvent(Long productId, String name) {
        this(productId, name, LocalDateTime.now());
    }
}
