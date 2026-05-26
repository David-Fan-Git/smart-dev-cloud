package com.develop.mvp.pk.module.system.dal.mysql.social;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.client.SocialClientPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialClientDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Social Client Mapper 持久化 Mapper。
 */
@Mapper
public interface SocialClientMapper extends BaseMapperX<SocialClientDO> {

    /**
     * 查询 select By Social Type And User Type 对应的数据。
     *
     * @param socialType socialType 参数
     * @param userType userType 参数
     * @return 处理结果
     */
    default SocialClientDO selectBySocialTypeAndUserType(Integer socialType, Integer userType) {
        return selectOne(SocialClientDO::getSocialType, socialType,
                SocialClientDO::getUserType, userType);
    }

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<SocialClientDO> selectPage(SocialClientPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialClientDO>()
                .likeIfPresent(SocialClientDO::getName, reqVO.getName())
                .eqIfPresent(SocialClientDO::getSocialType, reqVO.getSocialType())
                .eqIfPresent(SocialClientDO::getUserType, reqVO.getUserType())
                .likeIfPresent(SocialClientDO::getClientId, reqVO.getClientId())
                .eqIfPresent(SocialClientDO::getStatus, reqVO.getStatus())
                .orderByDesc(SocialClientDO::getId));
    }

}
