package com.develop.mvp.pk.module.crm.domain.customer.event;

import java.time.LocalDateTime;

public record CrmCustomerDeletedEvent(Long customerId, String name, LocalDateTime occurredAt) implements DomainEvent {
    public CrmCustomerDeletedEvent(Long customerId, String name) {
        this(customerId, name, LocalDateTime.now());
    }
}
