package com.develop.mvp.pk.module.infra.domain.file.event;

// DDD 角色：文件配置删除后发布
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record FileConfigDeletedEvent(Long id, String name, LocalDateTime occurredAt) implements DomainEvent {
    public FileConfigDeletedEvent(Long id, String name) {
        this(id, name, LocalDateTime.now());
    }
}
