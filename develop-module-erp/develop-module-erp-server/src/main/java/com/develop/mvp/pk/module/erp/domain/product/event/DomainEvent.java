package com.develop.mvp.pk.module.erp.domain.product.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
