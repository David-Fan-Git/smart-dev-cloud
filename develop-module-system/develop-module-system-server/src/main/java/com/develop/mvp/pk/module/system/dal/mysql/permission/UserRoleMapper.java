package com.develop.mvp.pk.module.system.dal.mysql.permission;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.module.system.dal.dataobject.permission.UserRoleDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * User Role Mapper 持久化 Mapper。
 */
@Mapper
public interface UserRoleMapper extends BaseMapperX<UserRoleDO> {

    /**
     * 查询 select List By User Id 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    default List<UserRoleDO> selectListByUserId(Long userId) {
        return selectList(UserRoleDO::getUserId, userId);
    }

    /**
     * 删除 delete List By User Id And Role Id Ids 对应的数据。
     *
     * @param userId userId 参数
     * @param roleIds roleIds 参数
     */
    default void deleteListByUserIdAndRoleIdIds(Long userId, Collection<Long> roleIds) {
        delete(new LambdaQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO::getUserId, userId)
                .in(UserRoleDO::getRoleId, roleIds));
    }

    /**
     * 删除 delete List By User Id 对应的数据。
     *
     * @param userId userId 参数
     */
    default void deleteListByUserId(Long userId) {
        delete(new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getUserId, userId));
    }

    /**
     * 删除 delete List By Role Id 对应的数据。
     *
     * @param roleId roleId 参数
     */
    default void deleteListByRoleId(Long roleId) {
        delete(new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getRoleId, roleId));
    }

    /**
     * 查询 select List By Role Ids 对应的数据。
     *
     * @param roleIds roleIds 参数
     * @return 处理结果
     */
    default List<UserRoleDO> selectListByRoleIds(Collection<Long> roleIds) {
        return selectList(UserRoleDO::getRoleId, roleIds);
    }

}
