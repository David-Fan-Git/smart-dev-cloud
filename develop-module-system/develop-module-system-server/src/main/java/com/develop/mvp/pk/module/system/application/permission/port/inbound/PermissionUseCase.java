package com.develop.mvp.pk.module.system.application.permission.port.inbound;

// DDD 角色：入站端口 — 定义 Permission 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleDO;

import java.util.Collection;
import java.util.List;
import java.util.Set;
/**
 * Permission 聚合的入站用例端口。
 */
public interface PermissionUseCase {

    /**
     * 执行 assign Role Menu 对应的业务操作。
     *
     * @param roleId roleId 参数
     * @param menuIds menuIds 参数
     */
    void assignRoleMenu(Long roleId, Set<Long> menuIds);

    /**
     * 执行 process Role Deleted 对应的业务操作。
     *
     * @param roleId roleId 参数
     */
    void processRoleDeleted(Long roleId);

    /**
     * 执行 process Menu Deleted 对应的业务操作。
     *
     * @param menuId menuId 参数
     */
    void processMenuDeleted(Long menuId);

    /**
     * 查询 get Role Menu Ids 对应的数据。
     *
     * @param roleId roleId 参数
     * @return 处理结果
     */
    Set<Long> getRoleMenuIds(Long roleId);

    /**
     * 查询 get Role Menu Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    Set<Long> getRoleMenuIds(Collection<Long> roleIds);

    /**
     * 查询 get Menu Role Ids 对应的数据。
     *
     * @param menuId menuId 参数
     * @return 处理结果
     */
    Set<Long> getMenuRoleIds(Long menuId);

    /**
     * 执行 assign User Role 对应的业务操作。
     *
     * @param userId userId 参数
     * @param roleIds roleIds 参数
     */
    void assignUserRole(Long userId, Set<Long> roleIds);

    /**
     * 执行 process User Deleted 对应的业务操作。
     *
     * @param userId userId 参数
     */
    void processUserDeleted(Long userId);

    /**
     * 查询 get User Role Ids 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    Set<Long> getUserRoleIds(Long userId);

    /**
     * 查询 get User Ids By Role Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    Set<Long> getUserIdsByRoleIds(Collection<Long> roleIds);

    // ---- Additional methods from PermissionApplicationService ----

    /**
     * 判断 has Any Permissions 对应的条件是否成立。
     *
     * @param userId userId 参数
     * @param permissions permissions 参数
     * @return 处理结果
     */
    boolean hasAnyPermissions(Long userId, String... permissions);

    /**
     * 判断 has Any Roles 对应的条件是否成立。
     *
     * @param userId userId 参数
     * @param roles roles 参数
     * @return 处理结果
     */
    boolean hasAnyRoles(Long userId, String... roles);

    /**
     * 查询 get Role Menu List By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     * @return 处理结果
     */
    Set<Long> getRoleMenuListByRoleId(Long roleId);

    /**
     * 查询 get Role Menu List By Role Id 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    Set<Long> getRoleMenuListByRoleId(Collection<Long> roleIds);

    /**
     * 查询 get Menu Role Id List By Menu Id From Cache 对应的数据。
     *
     * @param menuId menuId 参数
     * @return 处理结果
     */
    Set<Long> getMenuRoleIdListByMenuIdFromCache(Long menuId);

    /**
     * 查询 get User Role Id List By User Id 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    Set<Long> getUserRoleIdListByUserId(Long userId);

    /**
     * 查询 get User Role Id List By User Id From Cache 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    Set<Long> getUserRoleIdListByUserIdFromCache(Long userId);

    /**
     * 查询 get User Role Id List By Role Id 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    Set<Long> getUserRoleIdListByRoleId(Collection<Long> roleIds);

    /**
     * 执行 assign Role Data Scope 对应的业务操作。
     *
     * @param roleId roleId 参数
     * @param dataScope dataScope 参数
     * @param dataScopeDeptIds dataScopeDeptIds 参数
     */
    void assignRoleDataScope(Long roleId, Integer dataScope, Set<Long> dataScopeDeptIds);

    /**
     * 查询 get Dept Data Permission 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    DeptDataPermissionRespDTO getDeptDataPermission(Long userId);

    /**
     * 查询 get Enable User Role List By User Id From Cache 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    List<RoleDO> getEnableUserRoleListByUserIdFromCache(Long userId);
}
