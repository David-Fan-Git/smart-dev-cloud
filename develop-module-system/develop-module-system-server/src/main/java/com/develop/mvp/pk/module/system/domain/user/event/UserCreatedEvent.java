package com.develop.mvp.pk.module.system.domain.user.event;

// Skill: AggregateRoot_User_Validation_Skill — UserCreatedEvent
// 触发时机：用户创建成功后

import java.time.LocalDateTime;

/**
 * User Created Event 领域事件。
 */
public record UserCreatedEvent(Long userId, String username, Long tenantId, LocalDateTime occurredAt) implements DomainEvent {
    /**
     * 创建 UserCreatedEvent 实例。
     *
     * @param userId userId 参数
     * @param username username 参数
     * @param tenantId tenantId 参数
     */
    public UserCreatedEvent(Long userId, String username, Long tenantId) {
        this(userId, username, tenantId, LocalDateTime.now());
    }
}
