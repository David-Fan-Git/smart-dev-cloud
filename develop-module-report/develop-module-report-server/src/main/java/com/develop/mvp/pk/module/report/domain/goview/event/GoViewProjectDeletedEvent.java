package com.develop.mvp.pk.module.report.domain.goview.event;

import java.time.LocalDateTime;

public record GoViewProjectDeletedEvent(Long projectId, String name, LocalDateTime occurredAt) implements DomainEvent {
    public GoViewProjectDeletedEvent(Long projectId, String name) {
        this(projectId, name, LocalDateTime.now());
    }
}
