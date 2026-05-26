package com.develop.mvp.pk.module.trade.domain.event;

import java.time.LocalDateTime;

/**
 * 交易域领域事件基接口
 */
public interface DomainEvent {
    LocalDateTime occurredAt();
}
