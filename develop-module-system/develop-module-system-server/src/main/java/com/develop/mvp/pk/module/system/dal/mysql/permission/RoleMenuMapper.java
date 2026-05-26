package com.develop.mvp.pk.module.system.dal.mysql.permission;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.RoleMenuDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * Role Menu Mapper 持久化 Mapper。
 */
@Mapper
public interface RoleMenuMapper extends BaseMapperX<RoleMenuDO> {

    /**
     * 查询 select List By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     * @return 处理结果
     */
    default List<RoleMenuDO> selectListByRoleId(Long roleId) {
        return selectList(RoleMenuDO::getRoleId, roleId);
    }

    /**
     * 查询 select List By Role Id 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    default List<RoleMenuDO> selectListByRoleId(Collection<Long> roleIds) {
        return selectList(RoleMenuDO::getRoleId, roleIds);
    }

    /**
     * 查询 select List By Menu Id 对应的数据。
     *
     * @param menuId menuId 参数
     * @return 处理结果
     */
    default List<RoleMenuDO> selectListByMenuId(Long menuId) {
        return selectList(RoleMenuDO::getMenuId, menuId);
    }

    /**
     * 删除 delete List By Role Id And Menu Ids 对应的数据。
     *
     * @param roleId roleId 参数
     * @param menuIds menuIds 参数
     */
    default void deleteListByRoleIdAndMenuIds(Long roleId, Collection<Long> menuIds) {
        delete(new LambdaQueryWrapper<RoleMenuDO>()
                .eq(RoleMenuDO::getRoleId, roleId)
                .in(RoleMenuDO::getMenuId, menuIds));
    }

    /**
     * 删除 delete List By Menu Id 对应的数据。
     *
     * @param menuId menuId 参数
     */
    default void deleteListByMenuId(Long menuId) {
        delete(new LambdaQueryWrapper<RoleMenuDO>().eq(RoleMenuDO::getMenuId, menuId));
    }

    /**
     * 删除 delete List By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     */
    default void deleteListByRoleId(Long roleId) {
        delete(new LambdaQueryWrapper<RoleMenuDO>().eq(RoleMenuDO::getRoleId, roleId));
    }

}
