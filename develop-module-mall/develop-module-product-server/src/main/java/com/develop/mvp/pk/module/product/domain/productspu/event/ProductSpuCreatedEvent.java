package com.develop.mvp.pk.module.product.domain.productspu.event;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 领域事件 ProductSpuCreatedEvent
// DDD 角色：商品 SPU 创建成功后发布

import com.develop.mvp.pk.module.product.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record ProductSpuCreatedEvent(Long spuId, String name, LocalDateTime occurredAt) implements DomainEvent {
    public ProductSpuCreatedEvent(Long spuId, String name) {
        this(spuId, name, LocalDateTime.now());
    }
}
