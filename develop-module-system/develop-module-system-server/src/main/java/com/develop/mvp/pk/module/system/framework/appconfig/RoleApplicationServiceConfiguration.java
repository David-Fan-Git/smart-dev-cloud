package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.permission.service.RoleApplicationService;
import com.develop.mvp.pk.module.system.dal.mysql.permission.RoleMapper;
import com.develop.mvp.pk.module.system.domain.permission.repository.RoleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Role Application Service Configuration 配置类。
 */
@Configuration
public class RoleApplicationServiceConfiguration {

    /**
     * 执行 role Application Service 对应的业务操作。
     *
     * @param roleRepository roleRepository 参数
     * @param roleMapper roleMapper 参数
     * @param permissionUseCase permissionUseCase 参数
     * @return 处理结果
     */
    @Bean
    public RoleApplicationService roleApplicationService(
            RoleRepository roleRepository,
            RoleMapper roleMapper,
            @Lazy PermissionUseCase permissionUseCase) {
        return new RoleApplicationService(roleRepository, roleMapper, permissionUseCase);
    }
}
