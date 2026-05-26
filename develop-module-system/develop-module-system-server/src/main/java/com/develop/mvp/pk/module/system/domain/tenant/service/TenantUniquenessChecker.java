package com.develop.mvp.pk.module.system.domain.tenant.service;

// Skill: AggregateRoot_Tenant_Validation_Skill — 领域服务接口 TenantUniquenessChecker
// DDD 角色：检查租户名称/域名在全局的唯一性，需要跨聚合查询故定义为领域服务
// 不变式 I01/I02：名称/域名全局唯一
// 验收标准 AC10：通过领域服务接口完成唯一性校验，不直接在聚合根内查 DB

import com.develop.mvp.pk.module.system.domain.tenant.valueobject.TenantId;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.TenantName;

/**
 * Tenant Uniqueness Checker 领域服务。
 */
public interface TenantUniquenessChecker {
    /** 规则 R02：检查租户名称在全局是否唯一（排除指定ID） */
    boolean isNameUnique(TenantName name, TenantId excludeId);

    /** 规则 R03：检查域名在全局是否唯一（排除指定ID） */
    boolean isWebsiteUnique(String website, TenantId excludeId);
}
