package com.develop.mvp.pk.module.product.domain.productcategory.event;

// Skill: AggregateRoot_ProductCategory_Validation_Skill — 领域事件 ProductCategoryStatusChangedEvent
// DDD 角色：商品分类状态变更后发布

import com.develop.mvp.pk.module.product.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record ProductCategoryStatusChangedEvent(Long categoryId, Integer oldStatus, Integer newStatus, LocalDateTime occurredAt) implements DomainEvent {
    public ProductCategoryStatusChangedEvent(Long categoryId, Integer oldStatus, Integer newStatus) {
        this(categoryId, oldStatus, newStatus, LocalDateTime.now());
    }
}
