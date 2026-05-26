package com.develop.mvp.pk.module.infra.domain.config.event;

// DDD 角色：配置更新完成后发布
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record ConfigUpdatedEvent(Long configId, String configKey, LocalDateTime occurredAt) implements DomainEvent {
    public ConfigUpdatedEvent(Long configId, String configKey) {
        this(configId, configKey, LocalDateTime.now());
    }
}
