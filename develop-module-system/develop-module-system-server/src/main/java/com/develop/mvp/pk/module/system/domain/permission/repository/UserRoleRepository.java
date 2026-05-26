package com.develop.mvp.pk.module.system.domain.permission.repository;

// Skill: AggregateRoot_Role_Menu_Skill — 仓储接口 UserRoleRepository
// DDD 角色：管理用户-角色关联的仓储

import java.util.*;

/**
 * User Role Repository 领域仓储接口。
 */
public interface UserRoleRepository {
    /**
     * 执行 assign 对应的业务操作。
     *
     * @param userId userId 参数
     * @param roleIds roleIds 参数
     */
    void assign(Long userId, Set<Long> roleIds);
    /**
     * 查询 find By User Id 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    Set<Long> findByUserId(Long userId);
    /**
     * 查询 find By Role Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    Set<Long> findByRoleIds(Collection<Long> roleIds);
    /**
     * 删除 delete By User Id 对应的数据。
     *
     * @param userId userId 参数
     */
    void deleteByUserId(Long userId);
    /**
     * 删除 delete By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     */
    void deleteByRoleId(Long roleId);
}
