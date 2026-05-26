package com.develop.mvp.pk.module.infra.domain.file.event;

// DDD 角色：文件配置创建后发布
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record FileConfigCreatedEvent(Long id, String name, LocalDateTime occurredAt) implements DomainEvent {
    public FileConfigCreatedEvent(Long id, String name) {
        this(id, name, LocalDateTime.now());
    }
}
