package com.develop.mvp.pk.module.infra.domain.config.event;

// DDD 角色：配置删除后发布
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record ConfigDeletedEvent(Long configId, String configKey, LocalDateTime occurredAt) implements DomainEvent {
    public ConfigDeletedEvent(Long configId, String configKey) {
        this(configId, configKey, LocalDateTime.now());
    }
}
