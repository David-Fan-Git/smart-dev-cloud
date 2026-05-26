package com.develop.mvp.pk.module.system.dal.mysql.social;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialUserBindDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Social User Bind Mapper 持久化 Mapper。
 */
@Mapper
public interface SocialUserBindMapper extends BaseMapperX<SocialUserBindDO> {

    /**
     * 删除 delete By User Type And User Id And Social Type 对应的数据。
     *
     * @param userType userType 参数
     * @param userId userId 参数
     * @param socialType socialType 参数
     */
    default void deleteByUserTypeAndUserIdAndSocialType(Integer userType, Long userId, Integer socialType) {
        delete(new LambdaQueryWrapperX<SocialUserBindDO>()
                .eq(SocialUserBindDO::getUserType, userType)
                .eq(SocialUserBindDO::getUserId, userId)
                .eq(SocialUserBindDO::getSocialType, socialType));
    }

    /**
     * 删除 delete By User Type And Social User Id 对应的数据。
     *
     * @param userType userType 参数
     * @param socialUserId socialUserId 参数
     */
    default void deleteByUserTypeAndSocialUserId(Integer userType, Long socialUserId) {
        delete(new LambdaQueryWrapperX<SocialUserBindDO>()
                .eq(SocialUserBindDO::getUserType, userType)
                .eq(SocialUserBindDO::getSocialUserId, socialUserId));
    }

    /**
     * 查询 select By User Type And Social User Id 对应的数据。
     *
     * @param userType userType 参数
     * @param socialUserId socialUserId 参数
     * @return 处理结果
     */
    default SocialUserBindDO selectByUserTypeAndSocialUserId(Integer userType, Long socialUserId) {
        return selectOne(SocialUserBindDO::getUserType, userType,
                SocialUserBindDO::getSocialUserId, socialUserId);
    }

    /**
     * 查询 select List By User Id And User Type 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    default List<SocialUserBindDO> selectListByUserIdAndUserType(Long userId, Integer userType) {
        return selectList(new LambdaQueryWrapperX<SocialUserBindDO>()
                .eq(SocialUserBindDO::getUserId, userId)
                .eq(SocialUserBindDO::getUserType, userType));
    }

    /**
     * 查询 select By User Id And User Type And Social Type 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param socialType socialType 参数
     * @return 处理结果
     */
    default SocialUserBindDO selectByUserIdAndUserTypeAndSocialType(Long userId, Integer userType, Integer socialType) {
        return selectOne(new LambdaQueryWrapperX<SocialUserBindDO>()
                .eq(SocialUserBindDO::getUserId, userId)
                .eq(SocialUserBindDO::getUserType, userType)
                .eq(SocialUserBindDO::getSocialType, socialType));
    }

}
