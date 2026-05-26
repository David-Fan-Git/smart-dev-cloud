package com.develop.mvp.pk.module.mes.domain.dv.event;

import java.time.LocalDateTime;

public record MesMachineryDeletedEvent(Long machineryId, String code, String name, LocalDateTime occurredAt) implements DomainEvent {

    public MesMachineryDeletedEvent(Long machineryId, String code, String name) {
        this(machineryId, code, name, LocalDateTime.now());
    }
}
