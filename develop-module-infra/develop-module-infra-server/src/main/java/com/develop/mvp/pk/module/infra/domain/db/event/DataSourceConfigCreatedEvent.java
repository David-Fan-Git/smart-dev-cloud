package com.develop.mvp.pk.module.infra.domain.db.event;

// DDD 角色：数据源配置创建后发布
import com.develop.mvp.pk.module.infra.domain.event.DomainEvent;

import java.time.LocalDateTime;

public record DataSourceConfigCreatedEvent(Long id, String name, LocalDateTime occurredAt) implements DomainEvent {
    public DataSourceConfigCreatedEvent(Long id, String name) {
        this(id, name, LocalDateTime.now());
    }
}
