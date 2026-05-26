package com.develop.mvp.pk.module.system.application.tenant.service;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantDO;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantPackageDO;
import com.develop.mvp.pk.module.system.dal.mysql.tenant.TenantMapper;
import com.develop.mvp.pk.module.system.domain.tenant.event.TenantCreatedEvent;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEventPublisher;
import com.develop.mvp.pk.module.system.enums.permission.RoleCodeEnum;
import com.develop.mvp.pk.module.system.enums.permission.RoleTypeEnum;
import com.develop.mvp.pk.module.system.infrastructure.tenant.persistence.TenantRepositoryImpl;
import com.develop.mvp.pk.module.system.infrastructure.tenant.persistence.TenantUniquenessCheckerImpl;
import com.develop.mvp.pk.module.system.application.permission.service.MenuApplicationService;
import com.develop.mvp.pk.module.system.application.permission.service.PermissionApplicationService;
import com.develop.mvp.pk.module.system.application.permission.service.RoleApplicationService;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantPackageApplicationService;
import com.develop.mvp.pk.module.system.application.user.service.AdminUserApplicationService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({TenantApplicationService.class, TenantRepositoryImpl.class, TenantUniquenessCheckerImpl.class})
class TenantApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private TenantApplicationService tenantApplicationService;
    @Resource
    private TenantMapper tenantMapper;

    @MockitoBean
    private DomainEventPublisher eventPublisher;
    @MockitoBean
    private TenantPackageApplicationService tenantPackageService;
    @MockitoBean
    private AdminUserApplicationService adminUserService;
    @MockitoBean
    private RoleApplicationService roleService;
    @MockitoBean
    private PermissionApplicationService permissionService;
    @MockitoBean
    private MenuApplicationService menuService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.clear();
    }

    @Test
    void createTenantGeneratesIdAndBackfillsContactUser() {
        TenantPackageDO tenantPackage = new TenantPackageDO()
                .setId(100L)
                .setMenuIds(Set.of(10L, 20L));
        when(tenantPackageService.validTenantPackage(100L)).thenReturn(tenantPackage);
        when(roleService.createRole(argThat(role -> {
            assertEquals(RoleCodeEnum.TENANT_ADMIN.getName(), role.getName());
            assertEquals(RoleCodeEnum.TENANT_ADMIN.getCode(), role.getCode());
            assertEquals(0, role.getSort());
            assertEquals("系统自动生成", role.getRemark());
            return true;
        }), eq(RoleTypeEnum.SYSTEM.getType()))).thenReturn(200L);
        when(adminUserService.createUser(argThat(user -> {
            assertEquals("tenantAdmin", user.getUsername());
            assertEquals("password", user.getPassword());
            assertEquals("David", user.getNickname());
            assertEquals("15601691300", user.getMobile());
            return true;
        }))).thenReturn(300L);

        Long tenantId = tenantApplicationService.createTenant(
                null, "testTenant", "David", "15601691300",
                CommonStatusEnum.ENABLE.getStatus(), java.util.List.of("www.example.com"), 100L,
                LocalDateTime.now().plusDays(1), 10, "tenantAdmin", "password");

        assertNotNull(tenantId);
        TenantDO tenant = tenantMapper.selectById(tenantId);
        assertNotNull(tenant);
        assertEquals("testTenant", tenant.getName());
        assertEquals(300L, tenant.getContactUserId());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), tenant.getStatus());
        assertEquals(java.util.List.of("www.example.com"), tenant.getWebsites());
        verify(permissionService).assignRoleMenu(200L, tenantPackage.getMenuIds());
        verify(permissionService).assignUserRole(300L, singleton(200L));
        verify(eventPublisher).publish(isA(TenantCreatedEvent.class));
    }
}
