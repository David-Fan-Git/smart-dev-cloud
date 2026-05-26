package com.develop.mvp.pk.module.bpm.domain.copy.event;
// DDD 角色：BPM抄送创建事件 - AggregateRoot_Bpm_Skill
import java.time.LocalDateTime;
public record CopyCreatedEvent(Long copyId, Long userId, String processInstanceId, LocalDateTime occurredAt) implements CopyDomainEvent {
    public CopyCreatedEvent(Long copyId, Long userId, String processInstanceId) { this(copyId, userId, processInstanceId, LocalDateTime.now()); }
}
