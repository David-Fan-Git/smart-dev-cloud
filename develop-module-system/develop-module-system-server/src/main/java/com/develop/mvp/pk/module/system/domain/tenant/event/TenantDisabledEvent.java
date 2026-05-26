package com.develop.mvp.pk.module.system.domain.tenant.event;

// Skill: AggregateRoot_Tenant_Validation_Skill — 领域事件 TenantDisabledEvent
// DDD 角色：租户禁用后发布，消费者负责禁用该租户下所有用户

import com.develop.mvp.pk.module.system.domain.user.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * Tenant Disabled Event 领域事件。
 */
public record TenantDisabledEvent(Long tenantId, LocalDateTime occurredAt) implements DomainEvent {
    /**
     * 创建 TenantDisabledEvent 实例。
     *
     * @param tenantId tenantId 参数
     */
    public TenantDisabledEvent(Long tenantId) {
        this(tenantId, LocalDateTime.now());
    }
}
