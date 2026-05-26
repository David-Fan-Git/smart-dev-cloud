package com.develop.mvp.pk.module.system.dal.mysql.oauth2;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.dal.dataobject.oauth2.OAuth2ApproveDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * OAuth2 Approve Mapper 持久化 Mapper。
 */
@Mapper
public interface OAuth2ApproveMapper extends BaseMapperX<OAuth2ApproveDO> {

    /**
     * 更新 update 对应的数据。
     *
     * @param updateObj updateObj 参数
     * @return 处理结果
     */
    default int update(OAuth2ApproveDO updateObj) {
        return update(updateObj, new LambdaQueryWrapperX<OAuth2ApproveDO>()
                .eq(OAuth2ApproveDO::getUserId, updateObj.getUserId())
                .eq(OAuth2ApproveDO::getUserType, updateObj.getUserType())
                .eq(OAuth2ApproveDO::getClientId, updateObj.getClientId())
                .eq(OAuth2ApproveDO::getScope, updateObj.getScope()));
    }

    /**
     * 查询 select List By User Id And User Type And Client Id 对应的数据。
     *
     * @param userId userId 参数
     * @param userType userType 参数
     * @param clientId clientId 参数
     * @return 处理结果
     */
    default List<OAuth2ApproveDO> selectListByUserIdAndUserTypeAndClientId(Long userId, Integer userType, String clientId) {
        return selectList(new LambdaQueryWrapperX<OAuth2ApproveDO>()
                .eq(OAuth2ApproveDO::getUserId, userId)
                .eq(OAuth2ApproveDO::getUserType, userType)
                .eq(OAuth2ApproveDO::getClientId, clientId));
    }

}
