package com.develop.mvp.pk.module.system.application.tenant.port.inbound;

// DDD 角色：入站端口 — 定义 Tenant 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantInfoHandler;
import com.develop.mvp.pk.module.system.application.tenant.service.TenantMenuHandler;
import com.develop.mvp.pk.module.system.domain.tenant.Tenant;
import com.develop.mvp.pk.module.system.domain.tenant.repository.TenantPageQuery;

import java.util.List;
import java.util.Set;
/**
 * Tenant 聚合的入站用例端口。
 */
public interface TenantUseCase {

    /**
     * 创建 create Tenant 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param contactName contactName 参数
     * @param contactMobile contactMobile 参数
     * @param status status 参数
     * @param websites websites 参数
     * @param packageId packageId 参数
     * @param expireTime expireTime 参数
     * @param accountCount accountCount 参数
     * @param username username 参数
     * @param password password 参数
     * @return 处理结果
     */
    Long createTenant(Long id, String name, String contactName, String contactMobile,
                      Integer status, List<String> websites, Long packageId,
                      java.time.LocalDateTime expireTime, Integer accountCount,
                      String username, String password);

    /**
     * 更新 update Tenant 对应的数据。
     *
     * @param id id 参数
     * @param name name 参数
     * @param contactName contactName 参数
     * @param contactMobile contactMobile 参数
     * @param status status 参数
     * @param websites websites 参数
     * @param packageId packageId 参数
     * @param expireTime expireTime 参数
     * @param accountCount accountCount 参数
     */
    void updateTenant(Long id, String name, String contactName, String contactMobile,
                      Integer status, List<String> websites, Long packageId,
                      java.time.LocalDateTime expireTime, Integer accountCount);

    /**
     * 删除 delete Tenant 对应的数据。
     *
     * @param id id 参数
     */
    void deleteTenant(Long id);

    /**
     * 删除 delete Tenant List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteTenantList(List<Long> ids);

    /**
     * 查询 get Tenant 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Tenant getTenant(Long id);

    /**
     * 查询 get And Validate Tenant 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Tenant getAndValidateTenant(Long id);

    /**
     * 执行 valid Tenant 对应的业务操作。
     *
     * @param id id 参数
     */
    void validTenant(Long id);

    /**
     * 查询 get Tenant By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    Tenant getTenantByName(String name);

    /**
     * 查询 get Tenant By Website 对应的数据。
     *
     * @param website website 参数
     * @return 处理结果
     */
    Tenant getTenantByWebsite(String website);

    /**
     * 查询 get Tenant Page 对应的数据。
     *
     * @param query query 参数
     * @return 处理结果
     */
    PageResult<Tenant> getTenantPage(TenantPageQuery query);

    /**
     * 查询 get Tenant Domain List By Status 对应的数据。
     *
     * @param statusCode statusCode 参数
     * @return 处理结果
     */
    List<Tenant> getTenantDomainListByStatus(Integer statusCode);

    /**
     * 查询 get Tenant Domain List By Package Id 对应的数据。
     *
     * @param packageId packageId 参数
     * @return 处理结果
     */
    List<Tenant> getTenantDomainListByPackageId(Long packageId);

    /**
     * 查询 get Tenant Count By Package Id 对应的数据。
     *
     * @param packageId packageId 参数
     * @return 处理结果
     */
    Long getTenantCountByPackageId(Long packageId);

    /**
     * 查询 get Tenant Id List 对应的数据。
     *
     * @return 处理结果
     */
    List<Long> getTenantIdList();

    /**
     * 更新 update Tenant Role Menu 对应的数据。
     *
     * @param tenantId tenantId 参数
     * @param menuIds menuIds 参数
     */
    void updateTenantRoleMenu(Long tenantId, Set<Long> menuIds);

    /**
     * 执行 handle Tenant Info 对应的业务操作。
     *
     * @param handler handler 参数
     */
    void handleTenantInfo(TenantInfoHandler handler);

    /**
     * 执行 handle Tenant Menu 对应的业务操作。
     *
     * @param handler handler 参数
     */
    void handleTenantMenu(TenantMenuHandler handler);
}
