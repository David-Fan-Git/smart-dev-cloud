package com.develop.mvp.pk.module.system.infrastructure.tenant.persistence;

// Skill: AggregateRoot_Tenant_Validation_Skill — 领域服务实现 TenantUniquenessCheckerImpl
// DDD 角色：TenantUniquenessChecker 的实现，委托 TenantRepository 检查唯一性
// 验收标准 AC10：唯一性校验通过领域服务接口完成

import com.develop.mvp.pk.module.system.domain.tenant.repository.TenantRepository;
import com.develop.mvp.pk.module.system.domain.tenant.service.TenantUniquenessChecker;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.TenantId;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.TenantName;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tenant Uniqueness Checker Impl 类。
 */
@Component
public class TenantUniquenessCheckerImpl implements TenantUniquenessChecker {

    private final TenantRepository tenantRepository;

    /**
     * 创建 TenantUniquenessCheckerImpl 实例。
     *
     * @param tenantRepository tenantRepository 参数
     */
    public TenantUniquenessCheckerImpl(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /**
     * 判断 is Name Unique 对应的条件是否成立。
     *
     * @param name name 参数
     * @param excludeId excludeId 参数
     * @return 处理结果
     */
    @Override
    public boolean isNameUnique(TenantName name, TenantId excludeId) {
        return tenantRepository.findByName(name)
                .map(t -> excludeId != null && t.id().equals(excludeId))
                .orElse(true);
    }

    /**
     * 判断 is Website Unique 对应的条件是否成立。
     *
     * @param website website 参数
     * @param excludeId excludeId 参数
     * @return 处理结果
     */
    @Override
    public boolean isWebsiteUnique(String website, TenantId excludeId) {
        List<com.develop.mvp.pk.module.system.domain.tenant.Tenant> tenants =
                tenantRepository.findByWebsite(website);
        if (tenants.isEmpty()) return true;
        if (excludeId == null) return false;
        return tenants.stream().allMatch(t -> t.id().equals(excludeId));
    }
}
