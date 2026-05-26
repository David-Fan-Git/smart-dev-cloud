package com.develop.mvp.pk.module.report.domain.goview.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
