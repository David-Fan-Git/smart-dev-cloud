package com.develop.mvp.pk.module.system.domain.user.event;

// Skill: AggregateRoot_User_Validation_Skill — UserLoggedInEvent
// 触发时机：用户登录信息更新后（原 updateUserLogin L185-187）

import java.time.LocalDateTime;

/**
 * User Logged In Event 领域事件。
 */
public record UserLoggedInEvent(Long userId, String loginIp, LocalDateTime loginDate, LocalDateTime occurredAt) implements DomainEvent {
    /**
     * 创建 UserLoggedInEvent 实例。
     *
     * @param userId userId 参数
     * @param loginIp loginIp 参数
     * @param loginDate loginDate 参数
     */
    public UserLoggedInEvent(Long userId, String loginIp, LocalDateTime loginDate) {
        this(userId, loginIp, loginDate, LocalDateTime.now());
    }
}
