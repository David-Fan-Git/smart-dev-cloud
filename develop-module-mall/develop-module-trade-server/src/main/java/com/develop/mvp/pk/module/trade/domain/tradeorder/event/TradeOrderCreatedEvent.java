package com.develop.mvp.pk.module.trade.domain.tradeorder.event;

import com.develop.mvp.pk.module.trade.domain.event.DomainEvent;
import java.time.LocalDateTime;

public record TradeOrderCreatedEvent(Long orderId, String no, Long userId, Integer payPrice, LocalDateTime occurredAt) implements DomainEvent {
    public TradeOrderCreatedEvent(Long orderId, String no, Long userId, Integer payPrice) {
        this(orderId, no, userId, payPrice, LocalDateTime.now());
    }
}
