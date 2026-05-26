package com.develop.mvp.pk.module.promotion.domain.seckill.event;

// Skill: AggregateRoot_SeckillActivity_Validation_Skill — 领域事件

import com.develop.mvp.pk.module.promotion.domain.event.DomainEvent;
import java.time.LocalDateTime;

public record SeckillActivityStatusChangedEvent(Long activityId, Integer oldStatus, Integer newStatus, LocalDateTime occurredAt) implements DomainEvent {
    public SeckillActivityStatusChangedEvent(Long activityId, Integer oldStatus, Integer newStatus) {
        this(activityId, oldStatus, newStatus, LocalDateTime.now());
    }
}
