package com.develop.mvp.pk.module.iot.domain.device.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
