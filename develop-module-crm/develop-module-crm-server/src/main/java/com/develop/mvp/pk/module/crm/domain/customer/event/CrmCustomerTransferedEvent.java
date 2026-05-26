package com.develop.mvp.pk.module.crm.domain.customer.event;

import java.time.LocalDateTime;

public record CrmCustomerTransferedEvent(Long customerId, Long fromUserId, Long toUserId, LocalDateTime occurredAt) implements DomainEvent {
    public CrmCustomerTransferedEvent(Long customerId, Long fromUserId, Long toUserId) {
        this(customerId, fromUserId, toUserId, LocalDateTime.now());
    }
}
