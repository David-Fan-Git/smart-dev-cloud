package com.develop.mvp.pk.module.system.domain.permission.event;

import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;
import java.time.LocalDateTime;

/**
 * Role Created Event 领域事件。
 */
public record RoleCreatedEvent(Long roleId, String code, LocalDateTime occurredAt) implements DomainEvent {
    /**
     * 创建 RoleCreatedEvent 实例。
     *
     * @param roleId roleId 参数
     */
    public RoleCreatedEvent(Long roleId, String code) { this(roleId, code, LocalDateTime.now()); }
}
