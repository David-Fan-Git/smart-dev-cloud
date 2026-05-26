package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantPackageApplicationService;
import com.develop.mvp.pk.module.system.dal.mysql.tenant.TenantPackageMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Tenant Package Application Service Configuration 配置类。
 */
@Configuration
public class TenantPackageApplicationServiceConfiguration {

    /**
     * 执行 tenant Package Application Service 对应的业务操作。
     *
     * @param tenantPackageMapper tenantPackageMapper 参数
     * @param tenantUseCase tenantUseCase 参数
     * @return 处理结果
     */
    @Bean
    public TenantPackageApplicationService tenantPackageApplicationService(
            TenantPackageMapper tenantPackageMapper,
            @Lazy TenantUseCase tenantUseCase) {
        return new TenantPackageApplicationService(tenantPackageMapper, tenantUseCase);
    }
}
