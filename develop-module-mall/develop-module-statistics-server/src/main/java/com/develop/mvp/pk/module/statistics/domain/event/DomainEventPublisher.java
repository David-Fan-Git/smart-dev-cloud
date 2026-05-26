package com.develop.mvp.pk.module.statistics.domain.event;

/**
 * 统计域领域事件发布器接口
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
