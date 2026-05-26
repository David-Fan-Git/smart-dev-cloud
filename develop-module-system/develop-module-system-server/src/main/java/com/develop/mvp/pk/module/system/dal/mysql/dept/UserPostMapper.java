package com.develop.mvp.pk.module.system.dal.mysql.dept;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.dal.dataobject.dept.UserPostDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * User Post Mapper 持久化 Mapper。
 */
@Mapper
public interface UserPostMapper extends BaseMapperX<UserPostDO> {

    /**
     * 查询 select List By User Id 对应的数据。
     *
     * @param userId userId 参数
     * @return 处理结果
     */
    default List<UserPostDO> selectListByUserId(Long userId) {
        return selectList(UserPostDO::getUserId, userId);
    }

    /**
     * 删除 delete By User Id And Post Id 对应的数据。
     *
     * @param userId userId 参数
     * @param postIds postIds 参数
     */
    default void deleteByUserIdAndPostId(Long userId, Collection<Long> postIds) {
        delete(new LambdaQueryWrapperX<UserPostDO>()
                .eq(UserPostDO::getUserId, userId)
                .in(UserPostDO::getPostId, postIds));
    }

    /**
     * 查询 select List By Post Ids 对应的数据。
     *
     * @param postIds postIds 参数
     * @return 处理结果
     */
    default List<UserPostDO> selectListByPostIds(Collection<Long> postIds) {
        return selectList(UserPostDO::getPostId, postIds);
    }

    /**
     * 删除 delete By User Id 对应的数据。
     *
     * @param userId userId 参数
     */
    default void deleteByUserId(Long userId) {
        delete(Wrappers.lambdaUpdate(UserPostDO.class).eq(UserPostDO::getUserId, userId));
    }
}
