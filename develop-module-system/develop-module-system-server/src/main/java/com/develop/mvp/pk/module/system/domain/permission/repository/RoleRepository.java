package com.develop.mvp.pk.module.system.domain.permission.repository;

// Skill: AggregateRoot_Role_Menu_Skill — 仓储接口 RoleRepository
// 验收标准 AC05：领域层接口，不 import MyBatis

import com.develop.mvp.pk.module.system.domain.permission.Role;
import com.develop.mvp.pk.module.system.domain.permission.valueobject.*;
import com.develop.mvp.pk.framework.common.pojo.PageResult;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Role Repository 领域仓储接口。
 */
public interface RoleRepository {
    /**
     * 创建 save 对应的数据。
     *
     * @param role role 参数
     * @return 处理结果
     */
    Role save(Role role);
    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(RoleId id);
    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Role findById(RoleId id);
    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<Role> findByIds(Collection<RoleId> ids);
    /**
     * 查询 find By Status 对应的数据。
     *
     * @param statuses statuses 参数
     * @return 处理结果
     */
    List<Role> findByStatus(Collection<Integer> statuses);
    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    List<Role> findAll();
    /**
     * 查询 find Page 对应的数据。
     *
     * @param name name 参数
     * @param code code 参数
     * @param status status 参数
     * @param createTime createTime 参数
     * @param pageNo pageNo 参数
     * @param pageSize pageSize 参数
     * @return 处理结果
     */
    PageResult<Role> findPage(String name, String code, Integer status,
                              LocalDateTime[] createTime, Integer pageNo, Integer pageSize);
    /**
     * 查询 find By Name 对应的数据。
     *
     * @param name name 参数
     * @return 处理结果
     */
    Optional<Role> findByName(String name);
    /**
     * 查询 find By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    Optional<Role> findByCode(String code);
}
