package com.develop.mvp.pk.module.system.domain.user.event;

// Skill: AggregateRoot_User_Validation_Skill — 领域事件发布器接口
// DDD 角色：领域层接口，由基础设施层（Spring ApplicationEventPublisher）实现
// 验收标准 AC10/AC11：禁用/删除用户时发布领域事件，由订阅者消费

/**
 * Domain Event Publisher 领域事件。
 */
public interface DomainEventPublisher {
    /**
     * 发送 publish 对应的消息。
     *
     * @param event event 参数
     */
    void publish(DomainEvent event);
}
