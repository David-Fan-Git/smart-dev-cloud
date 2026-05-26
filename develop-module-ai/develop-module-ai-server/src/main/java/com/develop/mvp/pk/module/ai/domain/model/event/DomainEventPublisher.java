package com.develop.mvp.pk.module.ai.domain.model.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
