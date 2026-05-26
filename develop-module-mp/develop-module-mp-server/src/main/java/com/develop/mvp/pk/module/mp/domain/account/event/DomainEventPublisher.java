package com.develop.mvp.pk.module.mp.domain.account.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
