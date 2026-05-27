package com.develop.mvp.pk.module.system.infrastructure.user.messaging;

// Skill: AggregateRoot_User_Validation_Skill — 基础设施适配器
// DDD 角色：DomainEventPublisher 的实现，委托 Spring ApplicationEventPublisher

import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring Domain Event Publisher 类。
 */
@Component("systemDomainEventPublisher")
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher springPublisher;

    /**
     * 创建 SpringDomainEventPublisher 实例。
     *
     * @param springPublisher springPublisher 参数
     */
    public SpringDomainEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    /**
     * 发送 publish 对应的消息。
     *
     * @param event event 参数
     */
    @Override
    public void publish(DomainEvent event) {
        springPublisher.publishEvent(event);
    }
}
