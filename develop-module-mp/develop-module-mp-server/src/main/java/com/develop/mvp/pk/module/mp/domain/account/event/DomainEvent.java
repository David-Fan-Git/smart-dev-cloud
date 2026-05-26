package com.develop.mvp.pk.module.mp.domain.account.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredAt();
}
