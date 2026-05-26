package com.develop.mvp.pk.module.trade.domain.tradeorder.event;

import com.develop.mvp.pk.module.trade.domain.event.DomainEvent;
import java.time.LocalDateTime;

public record TradeOrderStatusChangedEvent(Long orderId, String no, Integer oldStatus, Integer newStatus, LocalDateTime occurredAt) implements DomainEvent {
    public TradeOrderStatusChangedEvent(Long orderId, String no, Integer oldStatus, Integer newStatus) {
        this(orderId, no, oldStatus, newStatus, LocalDateTime.now());
    }
}
