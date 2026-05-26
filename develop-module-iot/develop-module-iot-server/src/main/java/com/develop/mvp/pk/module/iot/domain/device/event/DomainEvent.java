package com.develop.mvp.pk.module.iot.domain.device.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
