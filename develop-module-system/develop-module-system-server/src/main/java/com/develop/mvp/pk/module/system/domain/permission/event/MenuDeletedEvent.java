package com.develop.mvp.pk.module.system.domain.permission.event;

import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;
import java.time.LocalDateTime;

/**
 * Menu Deleted Event 领域事件。
 */
public record MenuDeletedEvent(Long menuId, LocalDateTime occurredAt) implements DomainEvent {
    /**
     * 创建 MenuDeletedEvent 实例。
     *
     */
    public MenuDeletedEvent(Long menuId) { this(menuId, LocalDateTime.now()); }
}
