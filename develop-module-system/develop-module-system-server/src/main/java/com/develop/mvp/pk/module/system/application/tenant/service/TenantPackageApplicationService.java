package com.develop.mvp.pk.module.system.application.tenant.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantPackageUseCase;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.packages.TenantPackagePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.packages.TenantPackageSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantPackageDO;
import com.develop.mvp.pk.module.system.domain.tenant.Tenant;
import com.develop.mvp.pk.module.system.dal.mysql.tenant.TenantPackageMapper;
import com.develop.mvp.pk.module.system.application.tenant.port.inbound.TenantUseCase;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

/**
 * Tenant Package Application Service 应用服务。
 */
@Validated
public class TenantPackageApplicationService implements TenantPackageUseCase {

    private final TenantPackageMapper tenantPackageMapper;
    private final TenantUseCase tenantUseCase;

    /**
     * 创建 TenantPackageApplicationService 实例。
     *
     * @param tenantPackageMapper tenantPackageMapper 参数
     * @param tenantUseCase tenantUseCase 参数
     */
    public TenantPackageApplicationService(TenantPackageMapper tenantPackageMapper,
                                           @Lazy TenantUseCase tenantUseCase) {
        this.tenantPackageMapper = tenantPackageMapper;
        this.tenantUseCase = tenantUseCase;
    }

    /**
     * 创建 create Tenant Package 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    public Long createTenantPackage(TenantPackageSaveReqVO createReqVO) {
        validateTenantPackageNameUnique(null, createReqVO.getName());
        TenantPackageDO tenantPackage = BeanUtils.toBean(createReqVO, TenantPackageDO.class);
        tenantPackageMapper.insert(tenantPackage);
        return tenantPackage.getId();
    }

    /**
     * 更新 update Tenant Package 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    @DSTransactional
    public void updateTenantPackage(TenantPackageSaveReqVO updateReqVO) {
        TenantPackageDO tenantPackage = validateTenantPackageExists(updateReqVO.getId());
        validateTenantPackageNameUnique(updateReqVO.getId(), updateReqVO.getName());
        TenantPackageDO updateObj = BeanUtils.toBean(updateReqVO, TenantPackageDO.class);
        tenantPackageMapper.updateById(updateObj);
        if (!CollUtil.isEqualList(tenantPackage.getMenuIds(), updateReqVO.getMenuIds())) {
            List<Tenant> tenants = tenantUseCase.getTenantDomainListByPackageId(updateReqVO.getId());
            tenants.forEach(tenant -> tenantUseCase.updateTenantRoleMenu(tenant.id().value(), updateReqVO.getMenuIds()));
        }
    }

    /**
     * 删除 delete Tenant Package 对应的数据。
     *
     * @param id id 参数
     */
    public void deleteTenantPackage(Long id) {
        validateTenantPackageExists(id);
        validateTenantUsed(id);
        tenantPackageMapper.deleteById(id);
    }

    /**
     * 删除 delete Tenant Package List 对应的数据。
     *
     * @param ids ids 参数
     */
    public void deleteTenantPackageList(List<Long> ids) {
        for (Long id : ids) {
            if (tenantUseCase.getTenantCountByPackageId(id) > 0) {
                throw exception(TENANT_PACKAGE_USED);
            }
        }
        tenantPackageMapper.deleteByIds(ids);
    }

    /**
     * 查询 get Tenant Package 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public TenantPackageDO getTenantPackage(Long id) {
        return tenantPackageMapper.selectById(id);
    }

    /**
     * 查询 get Tenant Package Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    public PageResult<TenantPackageDO> getTenantPackagePage(TenantPackagePageReqVO pageReqVO) {
        return tenantPackageMapper.selectPage(pageReqVO);
    }

    /**
     * 执行 valid Tenant Package 对应的业务操作。
     *
     * @param id id 参数
     * @return 处理结果
     */
    public TenantPackageDO validTenantPackage(Long id) {
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(id);
        if (tenantPackage == null) {
            throw exception(TENANT_PACKAGE_NOT_EXISTS);
        }
        if (tenantPackage.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) {
            throw exception(TENANT_PACKAGE_DISABLE, tenantPackage.getName());
        }
        return tenantPackage;
    }

    /**
     * 查询 get Tenant Package List By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    public List<TenantPackageDO> getTenantPackageListByStatus(Integer status) {
        return tenantPackageMapper.selectListByStatus(status);
    }

    /**
     * 校验 validate Tenant Package Exists 对应的业务规则。
     *
     * @param id id 参数
     * @return 处理结果
     */
    private TenantPackageDO validateTenantPackageExists(Long id) {
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(id);
        if (tenantPackage == null) {
            throw exception(TENANT_PACKAGE_NOT_EXISTS);
        }
        return tenantPackage;
    }

    /**
     * 校验 validate Tenant Used 对应的业务规则。
     *
     * @param id id 参数
     */
    private void validateTenantUsed(Long id) {
        if (tenantUseCase.getTenantCountByPackageId(id) > 0) {
            throw exception(TENANT_PACKAGE_USED);
        }
    }

    /**
     * 校验 validate Tenant Package Name Unique 对应的业务规则。
     *
     * @param id id 参数
     * @param name name 参数
     */
    @VisibleForTesting
    public void validateTenantPackageNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        TenantPackageDO tenantPackage = tenantPackageMapper.selectByName(name);
        if (tenantPackage == null) {
            return;
        }
        if (id == null) {
            throw exception(TENANT_PACKAGE_NAME_DUPLICATE);
        }
        if (!tenantPackage.getId().equals(id)) {
            throw exception(TENANT_PACKAGE_NAME_DUPLICATE);
        }
    }
}
