package com.develop.mvp.pk.module.mp.domain.account.event;

import java.time.LocalDateTime;

public record MpAccountDeletedEvent(Long accountId, String name, LocalDateTime occurredAt) implements DomainEvent {
    public MpAccountDeletedEvent(Long accountId, String name) {
        this(accountId, name, LocalDateTime.now());
    }
}
