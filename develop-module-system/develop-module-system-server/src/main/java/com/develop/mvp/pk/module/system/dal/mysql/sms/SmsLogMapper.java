package com.develop.mvp.pk.module.system.dal.mysql.sms;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.system.controller.admin.sms.vo.log.SmsLogPageReqVO;
import com.develop.mvp.pk.module.system.dal.dataobject.sms.SmsLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Sms Log Mapper 持久化 Mapper。
 */
@Mapper
public interface SmsLogMapper extends BaseMapperX<SmsLogDO> {

    /**
     * 查询 select Page 对应的数据。
     *
     * @param reqVO reqVO 参数
     * @return 处理结果
     */
    default PageResult<SmsLogDO> selectPage(SmsLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SmsLogDO>()
                .eqIfPresent(SmsLogDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(SmsLogDO::getTemplateId, reqVO.getTemplateId())
                .likeIfPresent(SmsLogDO::getMobile, reqVO.getMobile())
                .eqIfPresent(SmsLogDO::getSendStatus, reqVO.getSendStatus())
                .betweenIfPresent(SmsLogDO::getSendTime, reqVO.getSendTime())
                .eqIfPresent(SmsLogDO::getReceiveStatus, reqVO.getReceiveStatus())
                .betweenIfPresent(SmsLogDO::getReceiveTime, reqVO.getReceiveTime())
                .orderByDesc(SmsLogDO::getId));
    }

    /**
     * 查询 select By Api Serial No 对应的数据。
     *
     * @param apiSerialNo apiSerialNo 参数
     * @return 处理结果
     */
    default SmsLogDO selectByApiSerialNo(String apiSerialNo) {
        return selectOne(SmsLogDO::getApiSerialNo, apiSerialNo);
    }

}
