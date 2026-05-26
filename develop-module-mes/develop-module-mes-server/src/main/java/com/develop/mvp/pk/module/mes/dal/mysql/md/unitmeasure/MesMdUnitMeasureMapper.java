package com.develop.mvp.pk.module.mes.dal.mysql.md.unitmeasure;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.md.unitmeasure.vo.MesMdUnitMeasurePageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 计量单位 Mapper
 *
 * @author David
 */
@Mapper
public interface MesMdUnitMeasureMapper extends BaseMapperX<MesMdUnitMeasureDO> {

    default PageResult<MesMdUnitMeasureDO> selectPage(MesMdUnitMeasurePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesMdUnitMeasureDO>()
                .likeIfPresent(MesMdUnitMeasureDO::getCode, reqVO.getCode())
                .likeIfPresent(MesMdUnitMeasureDO::getName, reqVO.getName())
                .eqIfPresent(MesMdUnitMeasureDO::getStatus, reqVO.getStatus())
                .orderByDesc(MesMdUnitMeasureDO::getId));
    }

    default MesMdUnitMeasureDO selectByCode(String code) {
        return selectOne(MesMdUnitMeasureDO::getCode, code);
    }

    default List<MesMdUnitMeasureDO> selectListByStatus(Integer status) {
        return selectList(MesMdUnitMeasureDO::getStatus, status);
    }

    default Long selectCountByPrimaryId(Long primaryId) {
        return selectCount(MesMdUnitMeasureDO::getPrimaryId, primaryId);
    }

}
