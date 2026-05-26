package com.develop.mvp.pk.module.mes.domain.dv.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
