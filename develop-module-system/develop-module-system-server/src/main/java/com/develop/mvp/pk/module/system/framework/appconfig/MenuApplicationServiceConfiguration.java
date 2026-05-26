package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.permission.service.MenuApplicationService;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import com.develop.mvp.pk.module.system.dal.mysql.permission.MenuMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Menu Application Service Configuration 配置类。
 */
@Configuration
public class MenuApplicationServiceConfiguration {

    /**
     * 执行 menu Application Service 对应的业务操作。
     *
     * @param menuMapper menuMapper 参数
     * @param permissionUseCase permissionUseCase 参数
     * @param tenantUseCase tenantUseCase 参数
     * @return 处理结果
     */
    @Bean
    public MenuApplicationService menuApplicationService(
            MenuMapper menuMapper,
            @Lazy PermissionUseCase permissionUseCase,
            @Lazy TenantUseCase tenantUseCase) {
        return new MenuApplicationService(menuMapper, permissionUseCase, tenantUseCase);
    }
}
