package com.develop.mvp.pk.module.infra.domain.event;

// DDD 角色：领域事件发布器接口，由基础设施层实现
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
