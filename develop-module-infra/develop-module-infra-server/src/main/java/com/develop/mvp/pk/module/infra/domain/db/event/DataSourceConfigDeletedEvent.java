package com.develop.mvp.pk.module.infra.domain.db.event;

// DDD 角色：数据源配置删除后发布
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record DataSourceConfigDeletedEvent(Long id, String name, LocalDateTime occurredAt) implements DomainEvent {
    public DataSourceConfigDeletedEvent(Long id, String name) {
        this(id, name, LocalDateTime.now());
    }
}
