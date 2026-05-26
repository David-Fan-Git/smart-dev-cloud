package com.develop.mvp.pk.module.crm.domain.customer.event;

import java.time.LocalDateTime;

public record CrmCustomerCreatedEvent(Long customerId, String name, Long ownerUserId, LocalDateTime occurredAt) implements DomainEvent {
    public CrmCustomerCreatedEvent(Long customerId, String name, Long ownerUserId) {
        this(customerId, name, ownerUserId, LocalDateTime.now());
    }
}
