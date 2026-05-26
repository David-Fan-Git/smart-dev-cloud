package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.infra.api.config.ConfigApi;
import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import com.develop.mvp.pk.module.system.application.oauth2.port.inbound.OAuth2UseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.UserUseCase;
import com.develop.mvp.pk.module.system.application.user.service.AdminUserApplicationService;
import com.develop.mvp.pk.module.system.dal.mysql.dept.UserPostMapper;
import com.develop.mvp.pk.module.system.dal.mysql.user.AdminUserMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Admin User Application Service Configuration 配置类。
 */
@Configuration
public class AdminUserApplicationServiceConfiguration {

    /**
     * 执行 admin User Application Service 对应的业务操作。
     *
     * @param userMapper userMapper 参数
     * @param userApplicationService userApplicationService 参数
     * @param deptUseCase deptUseCase 参数
     * @param postUseCase postUseCase 参数
     * @param permissionService permissionService 参数
     * @param passwordEncoder passwordEncoder 参数
     * @param tenantService tenantService 参数
     * @param oauth2TokenService oauth2TokenService 参数
     * @param userPostMapper userPostMapper 参数
     * @param configApi configApi 参数
     * @return 处理结果
     */
    @Bean
    public AdminUserApplicationService adminUserApplicationService(
            AdminUserMapper userMapper,
            UserUseCase userApplicationService,
            DeptUseCase deptUseCase,
            DeptUseCase postUseCase,
            @Lazy PermissionUseCase permissionService,
            PasswordEncoder passwordEncoder,
            @Lazy TenantUseCase tenantService,
            @Lazy OAuth2UseCase oauth2TokenService,
            UserPostMapper userPostMapper,
            ConfigApi configApi) {
        return new AdminUserApplicationService(userMapper, userApplicationService,
                deptUseCase, postUseCase, permissionService, passwordEncoder,
                tenantService, oauth2TokenService, userPostMapper, configApi);
    }
}
