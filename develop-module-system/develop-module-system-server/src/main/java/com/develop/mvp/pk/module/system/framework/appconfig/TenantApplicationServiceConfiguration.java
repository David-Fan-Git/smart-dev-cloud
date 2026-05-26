package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.framework.tenant.config.TenantProperties;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.MenuUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.RoleUseCase;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantApplicationService;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantPackageUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.domain.tenant.repository.TenantRepository;
import com.develop.mvp.pk.module.system.domain.tenant.service.TenantUniquenessChecker;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tenant Application Service Configuration 配置类。
 */
@Configuration
public class TenantApplicationServiceConfiguration {

    /**
     * 执行 tenant Application Service 对应的业务操作。
     *
     * @param tenantRepository tenantRepository 参数
     * @param uniquenessChecker uniquenessChecker 参数
     * @param eventPublisher eventPublisher 参数
     * @param tenantPackageService tenantPackageService 参数
     * @param adminUserService adminUserService 参数
     * @param roleService roleService 参数
     * @param permissionService permissionService 参数
     * @param menuService menuService 参数
     * @param tenantProperties tenantProperties 参数
     * @return 处理结果
     */
    @Bean
    public TenantApplicationService tenantApplicationService(
            TenantRepository tenantRepository,
            TenantUniquenessChecker uniquenessChecker,
            DomainEventPublisher eventPublisher,
            TenantPackageUseCase tenantPackageService,
            AdminUserUseCase adminUserService,
            RoleUseCase roleService,
            PermissionUseCase permissionService,
            MenuUseCase menuService,
            @Autowired(required = false) TenantProperties tenantProperties) {
        TenantApplicationService svc = new TenantApplicationService(
                tenantRepository, uniquenessChecker, eventPublisher,
                tenantPackageService, adminUserService, roleService,
                permissionService, menuService);
        if (tenantProperties != null) {
            svc.setTenantProperties(tenantProperties);
        }
        return svc;
    }
}
