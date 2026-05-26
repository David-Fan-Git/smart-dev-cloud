package com.develop.mvp.pk.module.system.domain.tenant.event;

// Skill: AggregateRoot_Tenant_Validation_Skill — 领域事件 TenantDeletedEvent
// DDD 角色：租户删除后发布，消费者负责清理租户关联数据

import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * Tenant Deleted Event 领域事件。
 */
public record TenantDeletedEvent(Long tenantId, String name, LocalDateTime occurredAt) implements DomainEvent {
    /**
     * 创建 TenantDeletedEvent 实例。
     *
     * @param tenantId tenantId 参数
     * @param name name 参数
     */
    public TenantDeletedEvent(Long tenantId, String name) {
        this(tenantId, name, LocalDateTime.now());
    }
}
