package com.develop.mvp.pk.module.system.domain.permission.repository;

// Skill: AggregateRoot_Role_Menu_Skill — 仓储接口 RoleMenuRepository
// DDD 角色：管理角色-菜单关联的仓储

import java.util.*;

/**
 * Role Menu Repository 领域仓储接口。
 */
public interface RoleMenuRepository {
    /**
     * 执行 assign 对应的业务操作。
     *
     * @param roleId roleId 参数
     * @param menuIds menuIds 参数
     */
    void assign(Long roleId, Set<Long> menuIds);
    /**
     * 查询 find By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     * @return 处理结果
     */
    Set<Long> findByRoleId(Long roleId);
    /**
     * 查询 find By Role Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    Set<Long> findByRoleIds(Collection<Long> roleIds);
    /**
     * 查询 find By Menu Id 对应的数据。
     *
     * @param menuId menuId 参数
     * @return 处理结果
     */
    Set<Long> findByMenuId(Long menuId);
    /**
     * 删除 delete By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     */
    void deleteByRoleId(Long roleId);
    /**
     * 删除 delete By Menu Id 对应的数据。
     *
     * @param menuId menuId 参数
     */
    void deleteByMenuId(Long menuId);
}
