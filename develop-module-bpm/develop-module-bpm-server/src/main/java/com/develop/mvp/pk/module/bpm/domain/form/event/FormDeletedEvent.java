package com.develop.mvp.pk.module.bpm.domain.form.event;
// DDD 角色：BPM表单删除事件 - AggregateRoot_Bpm_Skill
import java.time.LocalDateTime;
public record FormDeletedEvent(Long formId, LocalDateTime occurredAt) implements FormDomainEvent {
    public FormDeletedEvent(Long formId) { this(formId, LocalDateTime.now()); }
}
