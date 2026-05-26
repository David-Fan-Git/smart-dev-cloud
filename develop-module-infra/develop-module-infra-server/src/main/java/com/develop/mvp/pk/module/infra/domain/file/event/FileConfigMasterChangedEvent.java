package com.develop.mvp.pk.module.infra.domain.file.event;

// DDD 角色：Master 文件配置变更后发布
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record FileConfigMasterChangedEvent(Long id, String name, LocalDateTime occurredAt) implements DomainEvent {
    public FileConfigMasterChangedEvent(Long id, String name) {
        this(id, name, LocalDateTime.now());
    }
}
