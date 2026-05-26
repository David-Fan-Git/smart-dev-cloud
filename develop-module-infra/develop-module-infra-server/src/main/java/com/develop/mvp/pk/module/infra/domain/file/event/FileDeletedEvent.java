package com.develop.mvp.pk.module.infra.domain.file.event;

// DDD 角色：文件删除后发布
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record FileDeletedEvent(Long id, String path, LocalDateTime occurredAt) implements DomainEvent {
    public FileDeletedEvent(Long id, String path) {
        this(id, path, LocalDateTime.now());
    }
}
