package com.develop.mvp.pk.module.mes.domain.dv.event;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
