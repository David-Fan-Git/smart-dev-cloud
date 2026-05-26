package com.develop.mvp.pk.module.wms.domain.inventory.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
