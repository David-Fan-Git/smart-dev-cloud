package com.develop.mvp.pk.module.system.dal.mysql.social;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.socail.vo.user.SocialUserPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.social.SocialUserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Social User Mapper 持久化 Mapper。
 */
@Mapper
public interface SocialUserMapper extends BaseMapperX<SocialUserDO> {

    /**
     * 查询 select By Type And Code An State 对应的数据。
     *
     * @param type type 参数
     * @param code code 参数
     * @param state state 参数
     * @return 处理结果
     */
    default SocialUserDO selectByTypeAndCodeAnState(Integer type, String code, String state) {
        return selectOne(SocialUserDO::getType, type,
                SocialUserDO::getCode, code,
                SocialUserDO::getState, state);
    }

    /**
     * 查询 select By Type And Openid 对应的数据。
     *
     * @param type type 参数
     * @param openid openid 参数
     * @return 处理结果
     */
    default SocialUserDO selectByTypeAndOpenid(Integer type, String openid) {
        return selectFirstOne(SocialUserDO::getType, type,
                SocialUserDO::getOpenid, openid);
    }

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<SocialUserDO> selectPage(SocialUserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialUserDO>()
                .eqIfPresent(SocialUserDO::getType, reqVO.getType())
                .likeIfPresent(SocialUserDO::getNickname, reqVO.getNickname())
                .likeIfPresent(SocialUserDO::getOpenid, reqVO.getOpenid())
                .betweenIfPresent(SocialUserDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SocialUserDO::getId));
    }

}
