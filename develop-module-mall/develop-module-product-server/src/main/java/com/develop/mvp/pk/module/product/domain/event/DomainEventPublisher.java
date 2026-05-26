package com.develop.mvp.pk.module.product.domain.event;

/**
 * 商品域领域事件发布器接口
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
