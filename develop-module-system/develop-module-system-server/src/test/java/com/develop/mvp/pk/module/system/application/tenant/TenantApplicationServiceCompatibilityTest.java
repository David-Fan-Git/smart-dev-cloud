package com.develop.mvp.pk.module.system.application.tenant;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.tenant.config.TenantProperties;
import com.develop.mvp.pk.framework.tenant.core.context.TenantContextHolder;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantApplicationService;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantInfoHandler;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantMenuHandler;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantPackageApplicationService;
import com.develop.mvp.pk.module.system.application.user.service.AdminUserApplicationService;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.tenant.TenantSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantDO;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantPackageDO;
import com.develop.mvp.pk.module.system.dal.mysql.tenant.TenantMapper;
import com.develop.mvp.pk.module.system.domain.user.event.DomainEventPublisher;
import com.develop.mvp.pk.module.system.enums.permission.RoleCodeEnum;
import com.develop.mvp.pk.module.system.enums.permission.RoleTypeEnum;
import com.develop.mvp.pk.module.system.infrastructure.tenant.persistence.TenantRepositoryImpl;
import com.develop.mvp.pk.module.system.infrastructure.tenant.persistence.TenantUniquenessCheckerImpl;
import com.develop.mvp.pk.module.system.application.permission.service.MenuApplicationService;
import com.develop.mvp.pk.module.system.application.permission.service.PermissionApplicationService;
import com.develop.mvp.pk.module.system.application.permission.service.RoleApplicationService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.develop.mvp.pk.framework.common.util.collection.SetUtils.asSet;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static com.develop.mvp.pk.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.*;
import static com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantDO.PACKAGE_ID_SYSTEM;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Import({TenantApplicationService.class, TenantRepositoryImpl.class, TenantUniquenessCheckerImpl.class})
class TenantApplicationServiceCompatibilityTest extends BaseDbUnitTest {

    @Resource
    private TenantApplicationService tenantService;

    @Resource
    private TenantMapper tenantMapper;

    @MockitoBean
    private TenantProperties tenantProperties;
    @MockitoBean
    private TenantPackageApplicationService tenantPackageService;
    @MockitoBean
    private AdminUserApplicationService userService;
    @MockitoBean
    private RoleApplicationService roleService;
    @MockitoBean
    private MenuApplicationService menuService;
    @MockitoBean
    private PermissionApplicationService permissionService;
    @MockitoBean
    private DomainEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        TenantContextHolder.clear();
    }

    @Test
    void testGetTenantIdList() {
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setId(1L));
        tenantMapper.insert(tenant);

        List<Long> result = tenantService.getTenantIdList();

        assertEquals(Collections.singletonList(1L), result);
    }

    @Test
    void testValidTenant_notExists() {
        assertServiceException(() -> tenantService.validTenant(randomLongId()), TENANT_NOT_EXISTS);
    }

    @Test
    void testValidTenant_disable() {
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setId(1L).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        tenantMapper.insert(tenant);

        assertServiceException(() -> tenantService.validTenant(1L), TENANT_DISABLE, tenant.getName());
    }

    @Test
    void testValidTenant_expired() {
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setId(1L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setExpireTime(buildTime(2020, 2, 2)));
        tenantMapper.insert(tenant);

        assertServiceException(() -> tenantService.validTenant(1L), TENANT_EXPIRE, tenant.getName());
    }

    @Test
    void testValidTenant_success() {
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setId(1L).setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setExpireTime(LocalDateTime.now().plusDays(1)));
        tenantMapper.insert(tenant);

        tenantService.validTenant(1L);
    }

    @Test
    void testCreateTenant() {
        TenantPackageDO tenantPackage = randomPojo(TenantPackageDO.class, o -> o.setId(100L));
        when(tenantPackageService.validTenantPackage(eq(100L))).thenReturn(tenantPackage);
        when(roleService.createRole(argThat(role -> {
            assertEquals(RoleCodeEnum.TENANT_ADMIN.getName(), role.getName());
            assertEquals(RoleCodeEnum.TENANT_ADMIN.getCode(), role.getCode());
            assertEquals(0, role.getSort());
            assertEquals("系统自动生成", role.getRemark());
            return true;
        }), eq(RoleTypeEnum.SYSTEM.getType()))).thenReturn(200L);
        when(userService.createUser(argThat(user -> {
            assertEquals("yunai", user.getUsername());
            assertEquals("yuanma", user.getPassword());
            assertEquals("David", user.getNickname());
            assertEquals("15601691300", user.getMobile());
            return true;
        }))).thenReturn(300L);
        TenantSaveReqVO reqVO = randomPojo(TenantSaveReqVO.class, o -> {
            o.setContactName("David");
            o.setContactMobile("15601691300");
            o.setPackageId(100L);
            o.setStatus(randomCommonStatus());
            o.setWebsites(singletonList("https://www.iocoder.cn"));
            o.setUsername("yunai");
            o.setPassword("yuanma");
        }).setId(null);

        Long tenantId = tenantService.createTenant(reqVO);

        assertNotNull(tenantId);
        TenantDO tenant = tenantMapper.selectById(tenantId);
        assertPojoEquals(reqVO, tenant, "id");
        assertEquals(300L, tenant.getContactUserId());
        verify(permissionService).assignRoleMenu(eq(200L), same(tenantPackage.getMenuIds()));
        verify(permissionService).assignUserRole(eq(300L), eq(singleton(200L)));
    }

    @Test
    void testUpdateTenant_success() {
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setStatus(randomCommonStatus()));
        tenantMapper.insert(dbTenant);
        TenantSaveReqVO reqVO = randomPojo(TenantSaveReqVO.class, o -> {
            o.setId(dbTenant.getId());
            o.setStatus(randomCommonStatus());
            o.setWebsites(singletonList(randomString()));
        });
        TenantPackageDO tenantPackage = randomPojo(TenantPackageDO.class,
                o -> o.setMenuIds(asSet(200L, 201L)));
        when(tenantPackageService.validTenantPackage(eq(reqVO.getPackageId()))).thenReturn(tenantPackage);
        RoleDO role100 = randomPojo(RoleDO.class, o -> o.setId(100L).setCode(RoleCodeEnum.TENANT_ADMIN.getCode()));
        role100.setTenantId(dbTenant.getId());
        RoleDO role101 = randomPojo(RoleDO.class, o -> o.setId(101L));
        role101.setTenantId(dbTenant.getId());
        when(roleService.getRoleList()).thenReturn(asList(role100, role101));
        when(permissionService.getRoleMenuListByRoleId(eq(101L))).thenReturn(asSet(201L, 202L));

        tenantService.updateTenant(reqVO);

        TenantDO tenant = tenantMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, tenant);
        verify(permissionService).assignRoleMenu(eq(100L), eq(asSet(200L, 201L)));
        verify(permissionService).assignRoleMenu(eq(101L), eq(asSet(201L)));
    }

    @Test
    void testUpdateTenant_notExists() {
        TenantSaveReqVO reqVO = randomPojo(TenantSaveReqVO.class);

        assertServiceException(() -> tenantService.updateTenant(reqVO), TENANT_NOT_EXISTS);
    }

    @Test
    void testUpdateTenant_system() {
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setPackageId(PACKAGE_ID_SYSTEM));
        tenantMapper.insert(dbTenant);
        TenantSaveReqVO reqVO = randomPojo(TenantSaveReqVO.class, o -> o.setId(dbTenant.getId()));

        assertServiceException(() -> tenantService.updateTenant(reqVO), TENANT_CAN_NOT_UPDATE_SYSTEM);
    }

    @Test
    void testDeleteTenant_success() {
        TenantDO dbTenant = randomPojo(TenantDO.class,
                o -> o.setStatus(randomCommonStatus()));
        tenantMapper.insert(dbTenant);

        tenantService.deleteTenant(dbTenant.getId());

        assertNull(tenantMapper.selectById(dbTenant.getId()));
    }

    @Test
    void testDeleteTenant_notExists() {
        assertServiceException(() -> tenantService.deleteTenant(randomLongId()), TENANT_NOT_EXISTS);
    }

    @Test
    void testDeleteTenant_system() {
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setPackageId(PACKAGE_ID_SYSTEM));
        tenantMapper.insert(dbTenant);

        assertServiceException(() -> tenantService.deleteTenant(dbTenant.getId()), TENANT_CAN_NOT_UPDATE_SYSTEM);
    }

    @Test
    void testGetTenant() {
        TenantDO dbTenant = randomPojo(TenantDO.class);
        tenantMapper.insert(dbTenant);

        TenantDO result = tenantService.getTenantDo(dbTenant.getId());

        assertPojoEquals(result, dbTenant);
    }

    @Test
    void testGetTenantPage() {
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> {
            o.setName("David");
            o.setContactName("David");
            o.setContactMobile("15601691300");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2020, 12, 12));
        });
        tenantMapper.insert(dbTenant);
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setName(randomString())));
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setContactName(randomString())));
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setContactMobile(randomString())));
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setCreateTime(buildTime(2021, 12, 12))));
        TenantPageReqVO reqVO = new TenantPageReqVO();
        reqVO.setName("David");
        reqVO.setContactName("David");
        reqVO.setContactMobile("1560");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2020, 12, 1, 2020, 12, 24));

        PageResult<TenantDO> pageResult = tenantService.getTenantPage(reqVO);

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbTenant, pageResult.getList().get(0));
    }

    @Test
    void testGetTenantByName() {
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setName("David"));
        tenantMapper.insert(dbTenant);

        TenantDO result = tenantService.getTenantDoByName("David");

        assertPojoEquals(result, dbTenant);
    }

    @Test
    @Disabled
    void testGetTenantByWebsite() {
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setWebsites(singletonList("https://www.iocoder.cn")));
        tenantMapper.insert(dbTenant);

        TenantDO result = tenantService.getTenantDoByWebsite("https://www.iocoder.cn");

        assertPojoEquals(result, dbTenant);
    }

    @Test
    void testGetTenantListByPackageId() {
        TenantDO dbTenant1 = randomPojo(TenantDO.class, o -> o.setPackageId(1L));
        tenantMapper.insert(dbTenant1);
        TenantDO dbTenant2 = randomPojo(TenantDO.class, o -> o.setPackageId(2L));
        tenantMapper.insert(dbTenant2);

        List<TenantDO> result = tenantService.getTenantListByPackageId(1L);

        assertEquals(1, result.size());
        assertPojoEquals(dbTenant1, result.get(0));
    }

    @Test
    void testGetTenantCountByPackageId() {
        TenantDO dbTenant1 = randomPojo(TenantDO.class, o -> o.setPackageId(1L));
        tenantMapper.insert(dbTenant1);
        TenantDO dbTenant2 = randomPojo(TenantDO.class, o -> o.setPackageId(2L));
        tenantMapper.insert(dbTenant2);

        Long count = tenantService.getTenantCountByPackageId(1L);

        assertEquals(1, count);
    }

    @Test
    void testHandleTenantInfo_disable() {
        TenantInfoHandler handler = mock(TenantInfoHandler.class);
        when(tenantProperties.getEnable()).thenReturn(false);

        tenantService.handleTenantInfo(handler);

        verify(handler, never()).handle(any());
    }

    @Test
    void testHandleTenantInfo_success() {
        TenantInfoHandler handler = mock(TenantInfoHandler.class);
        when(tenantProperties.getEnable()).thenReturn(true);
        TenantDO dbTenant = randomPojo(TenantDO.class);
        tenantMapper.insert(dbTenant);
        TenantContextHolder.setTenantId(dbTenant.getId());

        tenantService.handleTenantInfo(handler);

        verify(handler).handle(argThat(argument -> {
            assertPojoEquals(dbTenant, argument);
            return true;
        }));
    }

    @Test
    void testHandleTenantMenu_disable() {
        TenantMenuHandler handler = mock(TenantMenuHandler.class);
        when(tenantProperties.getEnable()).thenReturn(false);

        tenantService.handleTenantMenu(handler);

        verify(handler, never()).handle(any());
    }

    @Test
    void testHandleTenantMenu_system() {
        TenantMenuHandler handler = mock(TenantMenuHandler.class);
        when(tenantProperties.getEnable()).thenReturn(true);
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setPackageId(PACKAGE_ID_SYSTEM));
        tenantMapper.insert(dbTenant);
        TenantContextHolder.setTenantId(dbTenant.getId());
        when(menuService.getMenuList()).thenReturn(Arrays.asList(randomPojo(MenuDO.class, o -> o.setId(100L)),
                randomPojo(MenuDO.class, o -> o.setId(101L))));

        tenantService.handleTenantMenu(handler);

        verify(handler).handle(asSet(100L, 101L));
    }

    @Test
    void testHandleTenantMenu_normal() {
        TenantMenuHandler handler = mock(TenantMenuHandler.class);
        when(tenantProperties.getEnable()).thenReturn(true);
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setPackageId(200L));
        tenantMapper.insert(dbTenant);
        TenantContextHolder.setTenantId(dbTenant.getId());
        when(tenantPackageService.getTenantPackage(eq(200L))).thenReturn(randomPojo(TenantPackageDO.class,
                o -> o.setMenuIds(asSet(100L, 101L))));

        tenantService.handleTenantMenu(handler);

        verify(handler).handle(asSet(100L, 101L));
    }
}
