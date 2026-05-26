package com.develop.mvp.pk.module.wms.domain.inventory.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
