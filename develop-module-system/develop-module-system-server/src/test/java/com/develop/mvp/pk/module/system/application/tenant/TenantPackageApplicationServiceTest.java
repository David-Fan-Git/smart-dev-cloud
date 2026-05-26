package com.develop.mvp.pk.module.system.application.tenant;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantPackageApplicationService;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.packages.TenantPackagePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.packages.TenantPackageSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantPackageDO;
import com.develop.mvp.pk.module.system.dal.mysql.tenant.TenantPackageMapper;
import com.develop.mvp.pk.module.system.domain.tenant.Tenant;
import com.develop.mvp.pk.module.system.domain.tenant.valueobject.TenantId;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;
import java.util.List;

import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static com.develop.mvp.pk.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static com.develop.mvp.pk.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.develop.mvp.pk.framework.test.core.util.AssertUtils.assertServiceException;
import static com.develop.mvp.pk.framework.test.core.util.RandomUtils.*;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(TenantPackageApplicationService.class)
class TenantPackageApplicationServiceTest extends BaseDbUnitTest {

    @Resource
    private TenantPackageApplicationService tenantPackageService;

    @Resource
    private TenantPackageMapper tenantPackageMapper;

    @MockitoBean
    private TenantUseCase tenantUseCase;

    @Test
    void testCreateTenantPackage_success() {
        TenantPackageSaveReqVO reqVO = randomPojo(TenantPackageSaveReqVO.class,
                o -> o.setStatus(randomCommonStatus()))
                .setId(null);

        Long tenantPackageId = tenantPackageService.createTenantPackage(reqVO);

        assertNotNull(tenantPackageId);
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(tenantPackageId);
        assertPojoEquals(reqVO, tenantPackage, "id");
    }

    @Test
    void testUpdateTenantPackage_success() {
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class,
                o -> o.setStatus(randomCommonStatus()));
        tenantPackageMapper.insert(dbTenantPackage);
        TenantPackageSaveReqVO reqVO = randomPojo(TenantPackageSaveReqVO.class, o -> {
            o.setId(dbTenantPackage.getId());
            o.setStatus(randomCommonStatus());
        });
        Long tenantId01 = randomLongId();
        Long tenantId02 = randomLongId();
        Tenant tenant1 = mock(Tenant.class);
        when(tenant1.id()).thenReturn(TenantId.of(tenantId01));
        Tenant tenant2 = mock(Tenant.class);
        when(tenant2.id()).thenReturn(TenantId.of(tenantId02));
        when(tenantUseCase.getTenantDomainListByPackageId(eq(reqVO.getId()))).thenReturn(
                asList(tenant1, tenant2));

        tenantPackageService.updateTenantPackage(reqVO);

        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, tenantPackage);
        verify(tenantUseCase).updateTenantRoleMenu(eq(tenantId01), eq(reqVO.getMenuIds()));
        verify(tenantUseCase).updateTenantRoleMenu(eq(tenantId02), eq(reqVO.getMenuIds()));
    }

    @Test
    void testUpdateTenantPackage_notExists() {
        TenantPackageSaveReqVO reqVO = randomPojo(TenantPackageSaveReqVO.class);

        assertServiceException(() -> tenantPackageService.updateTenantPackage(reqVO), TENANT_PACKAGE_NOT_EXISTS);
    }

    @Test
    void testDeleteTenantPackage_success() {
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class);
        tenantPackageMapper.insert(dbTenantPackage);
        Long id = dbTenantPackage.getId();
        when(tenantUseCase.getTenantCountByPackageId(eq(id))).thenReturn(0L);

        tenantPackageService.deleteTenantPackage(id);

        assertNull(tenantPackageMapper.selectById(id));
    }

    @Test
    void testDeleteTenantPackage_notExists() {
        Long id = randomLongId();

        assertServiceException(() -> tenantPackageService.deleteTenantPackage(id), TENANT_PACKAGE_NOT_EXISTS);
    }

    @Test
    void testDeleteTenantPackage_used() {
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class);
        tenantPackageMapper.insert(dbTenantPackage);
        Long id = dbTenantPackage.getId();
        when(tenantUseCase.getTenantCountByPackageId(eq(id))).thenReturn(1L);

        assertServiceException(() -> tenantPackageService.deleteTenantPackage(id), TENANT_PACKAGE_USED);
    }

    @Test
    void testGetTenantPackagePage() {
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class, o -> {
            o.setName("David");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setRemark("源码解析");
            o.setCreateTime(buildTime(2022, 10, 10));
        });
        tenantPackageMapper.insert(dbTenantPackage);
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setName("源码")));
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setRemark("解析")));
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage, o -> o.setCreateTime(buildTime(2022, 11, 11))));
        TenantPackagePageReqVO reqVO = new TenantPackagePageReqVO();
        reqVO.setName("David");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("源码");
        reqVO.setCreateTime(buildBetweenTime(2022, 10, 9, 2022, 10, 11));

        PageResult<TenantPackageDO> pageResult = tenantPackageService.getTenantPackagePage(reqVO);

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbTenantPackage, pageResult.getList().get(0));
    }

    @Test
    void testValidTenantPackage_success() {
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class,
                o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        tenantPackageMapper.insert(dbTenantPackage);

        TenantPackageDO result = tenantPackageService.validTenantPackage(dbTenantPackage.getId());

        assertPojoEquals(dbTenantPackage, result);
    }

    @Test
    void testValidTenantPackage_notExists() {
        Long id = randomLongId();

        assertServiceException(() -> tenantPackageService.validTenantPackage(id), TENANT_PACKAGE_NOT_EXISTS);
    }

    @Test
    void testValidTenantPackage_disable() {
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class,
                o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        tenantPackageMapper.insert(dbTenantPackage);

        assertServiceException(() -> tenantPackageService.validTenantPackage(dbTenantPackage.getId()),
                TENANT_PACKAGE_DISABLE, dbTenantPackage.getName());
    }

    @Test
    void testGetTenantPackage() {
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class);
        tenantPackageMapper.insert(dbTenantPackage);

        TenantPackageDO result = tenantPackageService.getTenantPackage(dbTenantPackage.getId());

        assertPojoEquals(result, dbTenantPackage);
    }

    @Test
    void testGetTenantPackageListByStatus() {
        TenantPackageDO dbTenantPackage = randomPojo(TenantPackageDO.class,
                o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        tenantPackageMapper.insert(dbTenantPackage);
        tenantPackageMapper.insert(cloneIgnoreId(dbTenantPackage,
                o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        List<TenantPackageDO> list = tenantPackageService.getTenantPackageListByStatus(
                CommonStatusEnum.ENABLE.getStatus());

        assertEquals(1, list.size());
        assertPojoEquals(dbTenantPackage, list.get(0));
    }
}
