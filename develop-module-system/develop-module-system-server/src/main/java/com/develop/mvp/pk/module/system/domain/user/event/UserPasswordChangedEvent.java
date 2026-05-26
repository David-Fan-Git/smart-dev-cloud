package com.develop.mvp.pk.module.system.domain.user.event;

// Skill: AggregateRoot_User_Validation_Skill — UserPasswordChangedEvent
// 触发时机：密码变更成功后

import java.time.LocalDateTime;

/**
 * User Password Changed Event 领域事件。
 */
public record UserPasswordChangedEvent(Long userId, LocalDateTime occurredAt) implements DomainEvent {
    /**
     * 创建 UserPasswordChangedEvent 实例。
     *
     * @param userId userId 参数
     */
    public UserPasswordChangedEvent(Long userId) {
        this(userId, LocalDateTime.now());
    }
}
