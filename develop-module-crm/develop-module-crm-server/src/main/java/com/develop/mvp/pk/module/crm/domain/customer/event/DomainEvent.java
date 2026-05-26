package com.develop.mvp.pk.module.crm.domain.customer.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
