package com.develop.mvp.pk.module.system.domain.permission.repository;

// Skill: AggregateRoot_Role_Menu_Skill — 仓储接口 MenuRepository

import com.develop.mvp.pk.module.system.domain.permission.Menu;
import com.develop.mvp.pk.module.system.domain.permission.valueobject.MenuId;

import java.util.*;

/**
 * Menu Repository 领域仓储接口。
 */
public interface MenuRepository {
    /**
     * 创建 save 对应的数据。
     *
     * @param menu menu 参数
     * @return 处理结果
     */
    Menu save(Menu menu);
    /**
     * 删除 delete 对应的数据。
     *
     * @param id id 参数
     */
    void delete(MenuId id);
    /**
     * 查询 find By Id 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    Menu findById(MenuId id);
    /**
     * 查询 find By Ids 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<Menu> findByIds(Collection<MenuId> ids);
    /**
     * 查询 find All 对应的数据。
     *
     * @return 处理结果
     */
    List<Menu> findAll();
    /**
     * 查询 find By Permission 对应的数据。
     *
     * @param permission permission 参数
     * @return 处理结果
     */
    List<Menu> findByPermission(String permission);
    /**
     * 查询 find By Parent Id And Name 对应的数据。
     *
     * @param parentId parentId 参数
     * @param name name 参数
     * @return 处理结果
     */
    Optional<Menu> findByParentIdAndName(Long parentId, String name);
    /**
     * 查询 find By Component Name 对应的数据。
     *
     * @param componentName componentName 参数
     * @return 处理结果
     */
    Optional<Menu> findByComponentName(String componentName);
    /**
     * 查询 count By Parent Id 对应的数据。
     *
     * @param parentId parentId 参数
     * @return 处理结果
     */
    long countByParentId(MenuId parentId);
}
