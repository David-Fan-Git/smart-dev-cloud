package com.develop.mvp.pk.module.product.domain.productbrand.event;

// Skill: AggregateRoot_ProductBrand_Validation_Skill — 领域事件 ProductBrandStatusChangedEvent
// DDD 角色：商品品牌状态变更后发布

import com.develop.mvp.pk.module.product.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record ProductBrandStatusChangedEvent(Long brandId, Integer oldStatus, Integer newStatus, LocalDateTime occurredAt) implements DomainEvent {
    public ProductBrandStatusChangedEvent(Long brandId, Integer oldStatus, Integer newStatus) {
        this(brandId, oldStatus, newStatus, LocalDateTime.now());
    }
}
