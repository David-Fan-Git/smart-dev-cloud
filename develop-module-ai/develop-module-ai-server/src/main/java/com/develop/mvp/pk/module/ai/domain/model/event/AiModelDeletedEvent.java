package com.develop.mvp.pk.module.ai.domain.model.event;

import java.time.LocalDateTime;

public record AiModelDeletedEvent(Long modelId, String name, String model,
                                  LocalDateTime occurredAt) implements DomainEvent {

    public AiModelDeletedEvent(Long modelId, String name, String model) {
        this(modelId, name, model, LocalDateTime.now());
    }
}
