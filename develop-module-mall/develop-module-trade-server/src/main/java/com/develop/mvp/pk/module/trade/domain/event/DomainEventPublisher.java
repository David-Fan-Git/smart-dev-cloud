package com.develop.mvp.pk.module.trade.domain.event;

/**
 * 交易域领域事件发布器接口
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
