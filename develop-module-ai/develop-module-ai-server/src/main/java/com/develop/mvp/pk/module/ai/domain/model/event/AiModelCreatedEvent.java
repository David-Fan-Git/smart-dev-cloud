package com.develop.mvp.pk.module.ai.domain.model.event;

import java.time.LocalDateTime;

public record AiModelCreatedEvent(Long modelId, String name, String model,
                                  String platform, LocalDateTime occurredAt) implements DomainEvent {

    public AiModelCreatedEvent(Long modelId, String name, String model, String platform) {
        this(modelId, name, model, platform, LocalDateTime.now());
    }
}
