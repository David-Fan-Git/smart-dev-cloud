package com.develop.mvp.pk.module.infra.domain.event;

// DDD 角色：领域事件基类
import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
