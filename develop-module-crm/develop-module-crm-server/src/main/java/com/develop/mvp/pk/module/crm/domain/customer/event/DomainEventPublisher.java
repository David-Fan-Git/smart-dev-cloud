package com.develop.mvp.pk.module.crm.domain.customer.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
