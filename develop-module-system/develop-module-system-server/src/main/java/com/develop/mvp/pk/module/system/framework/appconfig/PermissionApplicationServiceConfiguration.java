package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.dept.port.inbound.DeptUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.MenuUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.PermissionUseCase;
import com.develop.mvp.pk.module.system.application.permission.port.inbound.RoleUseCase;
import com.develop.mvp.pk.module.system.application.permission.service.PermissionApplicationService;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.domain.permission.repository.RoleMenuRepository;
import com.develop.mvp.pk.module.system.domain.permission.repository.UserRoleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Permission Application Service Configuration 配置类。
 */
@Configuration
public class PermissionApplicationServiceConfiguration {

    /**
     * 执行 permission Application Service 对应的业务操作。
     *
     * @param userRoleRepository userRoleRepository 参数
     * @param roleMenuRepository roleMenuRepository 参数
     * @param roleUseCase roleUseCase 参数
     * @param menuUseCase menuUseCase 参数
     * @param deptUseCase deptUseCase 参数
     * @param adminUserUseCase adminUserUseCase 参数
     * @return 处理结果
     */
    @Bean
    public PermissionApplicationService permissionApplicationService(
            UserRoleRepository userRoleRepository,
            RoleMenuRepository roleMenuRepository,
            @Lazy RoleUseCase roleUseCase,
            @Lazy MenuUseCase menuUseCase,
            DeptUseCase deptUseCase,
            @Lazy AdminUserUseCase adminUserUseCase) {
        return new PermissionApplicationService(
                userRoleRepository, roleMenuRepository,
                roleUseCase, menuUseCase,
                deptUseCase, adminUserUseCase);
    }
}
