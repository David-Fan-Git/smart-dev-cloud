package com.develop.mvp.pk.module.system.application.permission.port.inbound;

// DDD 角色：入站端口 — 定义 Menu 聚合的用例边界，供 Controller/API/跨服务调用
// Hexagonal-Lite：入站端口接口，应用服务实现此接口

import com.develop.mvp.pk.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.menu.MenuSaveVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO;

import java.util.Collection;
import java.util.List;
/**
 * Menu 聚合的入站用例端口。
 */
public interface MenuUseCase {

    /**
     * 创建 create Menu 对应的数据。
     *
     * @param createReqVO createReqVO 参数
     * @return 处理结果
     */
    Long createMenu(MenuSaveVO createReqVO);

    /**
     * 更新 update Menu 对应的数据。
     *
     * @param updateReqVO updateReqVO 参数
     */
    void updateMenu(MenuSaveVO updateReqVO);

    /**
     * 删除 delete Menu 对应的数据。
     *
     * @param id id 参数
     */
    void deleteMenu(Long id);

    /**
     * 删除 delete Menu List 对应的数据。
     *
     * @param ids ids 参数
     */
    void deleteMenuList(List<Long> ids);

    /**
     * 查询 get Menu List 对应的数据。
     *
     * @return 处理结果
     */
    List<MenuDO> getMenuList();

    /**
     * 查询 get Menu List By Tenant 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    List<MenuDO> getMenuListByTenant(MenuListReqVO reqVO);

    /**
     * 过滤 filter Disable Menus 对应的数据。
     *
     * @param menuList menuList 参数
     * @return 处理结果
     */
    List<MenuDO> filterDisableMenus(List<MenuDO> menuList);

    /**
     * 查询 get Menu List 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    List<MenuDO> getMenuList(MenuListReqVO reqVO);

    /**
     * 查询 get Menu Id List By Permission From Cache 对应的数据。
     *
     * @param permission permission 参数
     * @return 处理结果
     */
    List<Long> getMenuIdListByPermissionFromCache(String permission);

    /**
     * 查询 get Menu 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    MenuDO getMenu(Long id);

    /**
     * 查询 get Menu List 对应的数据。
     *
     * @param ids ids 参数
     * @return 处理结果
     */
    List<MenuDO> getMenuList(Collection<Long> ids);
}
