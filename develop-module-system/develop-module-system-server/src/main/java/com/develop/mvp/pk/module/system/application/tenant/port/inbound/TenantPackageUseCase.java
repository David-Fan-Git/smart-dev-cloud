package com.develop.mvp.pk.module.system.application.tenant.port.inbound;

// DDD 角色：入站端口 — 定义 TenantPackage 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.packages.TenantPackagePageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.tenant.vo.packages.TenantPackageSaveReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.tenant.TenantPackageDO;

import java.util.List;
/**
 * TenantPackage 聚合的入站用例端口。
 */
public interface TenantPackageUseCase {

    /**
     * 创建 create Tenant Package 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createTenantPackage(TenantPackageSaveReqVO createReqVO);

    /**
     * 更新 update Tenant Package 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateTenantPackage(TenantPackageSaveReqVO updateReqVO);

    /**
     * 删除 delete Tenant Package 对应的数据。
     *
     * @param id id 参数
     */
    void deleteTenantPackage(Long id);

    /**
     * 删除 delete Tenant Package List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteTenantPackageList(List<Long> ids);

    /**
     * 查询 get Tenant Package 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    TenantPackageDO getTenantPackage(Long id);

    /**
     * 查询 get Tenant Package Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    PageResult<TenantPackageDO> getTenantPackagePage(TenantPackagePageReqVO pageReqVO);

    /**
     * 执行 valid Tenant Package 对应的业务操作。
     *
     * @param id id 参数
     * @return 处理结果
     */
    TenantPackageDO validTenantPackage(Long id);

    /**
     * 查询 get Tenant Package List By Status 对应的数据。
     *
     * @param status status 参数
     * @return 处理结果
     */
    List<TenantPackageDO> getTenantPackageListByStatus(Integer status);

    /**
     * 校验 validate Tenant Package Name Unique 对应的业务规则。
     *
     * @param id id 参数
     * @param name name 参数
     */
    void validateTenantPackageNameUnique(Long id, String name);
}
