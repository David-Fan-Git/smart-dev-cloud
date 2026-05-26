package com.develop.mvp.pk.module.system.domain.tenant.event;

// Skill: AggregateRoot_Tenant_Validation_Skill — 领域事件 TenantCreatedEvent
// DDD 角色：租户创建成功后发布，携带 tenantId 和 name

import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * Tenant Created Event 领域事件。
 */
public record TenantCreatedEvent(Long tenantId, String name, LocalDateTime occurredAt) implements DomainEvent {
    /**
     * 创建 TenantCreatedEvent 实例。
     *
     * @param tenantId tenantId 参数
     * @param name name 参数
     */
    public TenantCreatedEvent(Long tenantId, String name) {
        this(tenantId, name, LocalDateTime.now());
    }
}
