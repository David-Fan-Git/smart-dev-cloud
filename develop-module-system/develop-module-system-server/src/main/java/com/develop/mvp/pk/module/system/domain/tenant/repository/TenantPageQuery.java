package com.develop.mvp.pk.module.system.domain.tenant.repository;

// Skill: AggregateRoot_Tenant_Validation_Skill — 仓储查询对象 TenantPageQuery
// DDD 角色：封装租户分页查询条件

import java.time.LocalDateTime;

/**
 * Tenant Page Query 查询条件对象。
 */
public record TenantPageQuery(
        String name,
        String contactName,
        String contactMobile,
        Integer status,
        LocalDateTime[] createTime,
        Integer pageNo,
        Integer pageSize
) {}
