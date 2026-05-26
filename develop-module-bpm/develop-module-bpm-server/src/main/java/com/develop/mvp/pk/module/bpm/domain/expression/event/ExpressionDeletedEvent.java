package com.develop.mvp.pk.module.bpm.domain.expression.event;
// DDD 角色：BPM表达式删除事件 - AggregateRoot_Bpm_Skill
import java.time.LocalDateTime;
public record ExpressionDeletedEvent(Long expressionId, LocalDateTime occurredAt) implements ExpressionDomainEvent {
    public ExpressionDeletedEvent(Long expressionId) { this(expressionId, LocalDateTime.now()); }
}
