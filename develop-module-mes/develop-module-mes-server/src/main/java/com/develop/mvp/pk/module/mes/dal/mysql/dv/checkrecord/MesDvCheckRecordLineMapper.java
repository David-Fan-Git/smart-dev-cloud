package com.develop.mvp.pk.module.mes.dal.mysql.dv.checkrecord;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.dv.checkrecord.vo.line.MesDvCheckRecordLinePageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.dv.checkrecord.MesDvCheckRecordLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 设备点检记录明细 Mapper
 *
 * @author David
 */
@Mapper
public interface MesDvCheckRecordLineMapper extends BaseMapperX<MesDvCheckRecordLineDO> {

    default PageResult<MesDvCheckRecordLineDO> selectPage(MesDvCheckRecordLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesDvCheckRecordLineDO>()
                .eqIfPresent(MesDvCheckRecordLineDO::getRecordId, reqVO.getRecordId())
                .orderByDesc(MesDvCheckRecordLineDO::getId));
    }

    default List<MesDvCheckRecordLineDO> selectListByRecordId(Long recordId) {
        return selectList(MesDvCheckRecordLineDO::getRecordId, recordId);
    }

    default int deleteByRecordId(Long recordId) {
        return delete(MesDvCheckRecordLineDO::getRecordId, recordId);
    }

}
