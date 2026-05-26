package com.develop.mvp.pk.module.product.domain.productspu.event;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 领域事件 ProductSpuStatusChangedEvent
// DDD 角色：商品 SPU 状态变更后发布

import com.develop.mvp.pk.module.product.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record ProductSpuStatusChangedEvent(Long spuId, Integer oldStatus, Integer newStatus, LocalDateTime occurredAt) implements DomainEvent {
    public ProductSpuStatusChangedEvent(Long spuId, Integer oldStatus, Integer newStatus) {
        this(spuId, oldStatus, newStatus, LocalDateTime.now());
    }
}
