package com.develop.mvp.pk.module.bpm.domain.listener.event;
// DDD 角色：BPM监听器删除事件 - AggregateRoot_Bpm_Skill
import java.time.LocalDateTime;
public record ListenerDeletedEvent(Long listenerId, LocalDateTime occurredAt) implements ListenerDomainEvent {
    public ListenerDeletedEvent(Long listenerId) { this(listenerId, LocalDateTime.now()); }
}
