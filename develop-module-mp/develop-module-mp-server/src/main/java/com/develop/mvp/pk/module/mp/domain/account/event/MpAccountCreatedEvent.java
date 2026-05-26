package com.develop.mvp.pk.module.mp.domain.account.event;

import java.time.LocalDateTime;

public record MpAccountCreatedEvent(Long accountId, String name, String appId, LocalDateTime occurredAt) implements DomainEvent {
    public MpAccountCreatedEvent(Long accountId, String name, String appId) {
        this(accountId, name, appId, LocalDateTime.now());
    }
}
