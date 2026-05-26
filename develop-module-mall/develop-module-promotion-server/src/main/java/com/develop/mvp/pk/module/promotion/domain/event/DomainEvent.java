package com.develop.mvp.pk.module.promotion.domain.event;

import java.time.LocalDateTime;

/**
 * 促销域领域事件基接口
 */
public interface DomainEvent {
    LocalDateTime occurredAt();
}
