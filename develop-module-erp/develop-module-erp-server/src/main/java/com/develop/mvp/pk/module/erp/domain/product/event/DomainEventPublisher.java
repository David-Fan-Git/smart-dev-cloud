package com.develop.mvp.pk.module.erp.domain.product.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
