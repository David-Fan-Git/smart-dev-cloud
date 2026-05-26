package com.develop.mvp.pk.module.system.domain.user.event;

import java.time.LocalDateTime;

// Skill: AggregateRoot_User_Validation_Skill — 领域事件基类
/**
 * Domain Event 领域事件。
 */
public interface DomainEvent {
    /**
     * 执行 occurred At 对应的业务操作。
     *
     * @return 处理结果
     */
    LocalDateTime occurredAt();
}
