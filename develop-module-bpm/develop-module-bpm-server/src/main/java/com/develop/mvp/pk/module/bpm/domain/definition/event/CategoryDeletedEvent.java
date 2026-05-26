package com.develop.mvp.pk.module.bpm.domain.definition.event;
// DDD 角色：BPM流程分类删除事件 - AggregateRoot_Bpm_Skill
import java.time.LocalDateTime;

public record CategoryDeletedEvent(Long categoryId, LocalDateTime occurredAt) implements CategoryDomainEvent {
    public CategoryDeletedEvent(Long categoryId) { this(categoryId, LocalDateTime.now()); }
}
