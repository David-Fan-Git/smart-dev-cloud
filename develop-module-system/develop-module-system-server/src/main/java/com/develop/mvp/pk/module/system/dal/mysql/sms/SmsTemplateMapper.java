package com.develop.mvp.pk.module.system.dal.mysql.sms;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.template.SmsTemplatePageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsTemplateDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Sms Template Mapper 持久化 Mapper。
 */
@Mapper
public interface SmsTemplateMapper extends BaseMapperX<SmsTemplateDO> {

    /**
     * 查询 select By Code 对应的数据。
     *
     * @param code code 参数
     * @return 处理结果
     */
    default SmsTemplateDO selectByCode(String code) {
        return selectOne(SmsTemplateDO::getCode, code);
    }

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<SmsTemplateDO> selectPage(SmsTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SmsTemplateDO>()
                .eqIfPresent(SmsTemplateDO::getType, reqVO.getType())
                .eqIfPresent(SmsTemplateDO::getStatus, reqVO.getStatus())
                .likeIfPresent(SmsTemplateDO::getCode, reqVO.getCode())
                .likeIfPresent(SmsTemplateDO::getContent, reqVO.getContent())
                .likeIfPresent(SmsTemplateDO::getApiTemplateId, reqVO.getApiTemplateId())
                .eqIfPresent(SmsTemplateDO::getChannelId, reqVO.getChannelId())
                .betweenIfPresent(SmsTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SmsTemplateDO::getId));
    }

    /**
     * 查询 select Count By Channel Id 对应的数据。
     *
     * @param channelId channelId 参数
     * @return 处理结果
     */
    default Long selectCountByChannelId(Long channelId) {
        return selectCount(SmsTemplateDO::getChannelId, channelId);
    }

}
