package com.develop.mvp.pk.module.system.dal.mysql.permission;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.permission.vo.menu.MenuListReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.MenuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Menu Mapper 持久化 Mapper。
 */
@Mapper
public interface MenuMapper extends BaseMapperX<MenuDO> {

    /**
     * 查询 select By Parent Id And Name 对应的数据。
     *
     * @param parentId parentId 参数
     * @param name name 参数
     * @return 处理结果
     */
    default MenuDO selectByParentIdAndName(Long parentId, String name) {
        return selectOne(MenuDO::getParentId, parentId, MenuDO::getName, name);
    }

    /**
     * 查询 select Count By Parent Id 对应的数据。
     *
     * @param parentId parentId 参数
     * @return 处理结果
     */
    default Long selectCountByParentId(Long parentId) {
        return selectCount(MenuDO::getParentId, parentId);
    }

    /**
     * 查询 select List 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default List<MenuDO> selectList(MenuListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<MenuDO>()
                .likeIfPresent(MenuDO::getName, reqVO.getName())
                .eqIfPresent(MenuDO::getStatus, reqVO.getStatus()));
    }

    /**
     * 查询 select List By Permission 对应的数据。
     *
     * @param permission permission 参数
     * @return 处理结果
     */
    default List<MenuDO> selectListByPermission(String permission) {
        return selectList(MenuDO::getPermission, permission);
    }

    /**
     * 查询 select By Component Name 对应的数据。
     *
     * @param componentName componentName 参数
     * @return 处理结果
     */
    default MenuDO selectByComponentName(String componentName) {
        return selectOne(MenuDO::getComponentName, componentName);
    }

}
