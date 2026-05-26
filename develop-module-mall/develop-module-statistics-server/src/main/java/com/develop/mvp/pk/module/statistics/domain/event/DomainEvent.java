package com.develop.mvp.pk.module.statistics.domain.event;

import java.time.LocalDateTime;

/**
 * 统计域领域事件基接口
 */
public interface DomainEvent {
    LocalDateTime occurredAt();
}
