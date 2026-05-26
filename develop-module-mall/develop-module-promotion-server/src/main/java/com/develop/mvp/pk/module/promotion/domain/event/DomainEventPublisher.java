package com.develop.mvp.pk.module.promotion.domain.event;

/**
 * 促销域领域事件发布器接口
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
