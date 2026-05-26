package com.develop.mvp.pk.module.mes.dal.mysql.dv.checkplan;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanPageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 点检保养方案 Mapper
 *
 * @author David
 */
@Mapper
public interface MesDvCheckPlanMapper extends BaseMapperX<MesDvCheckPlanDO> {

    default PageResult<MesDvCheckPlanDO> selectPage(MesDvCheckPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesDvCheckPlanDO>()
                .likeIfPresent(MesDvCheckPlanDO::getCode, reqVO.getCode())
                .likeIfPresent(MesDvCheckPlanDO::getName, reqVO.getName())
                .eqIfPresent(MesDvCheckPlanDO::getType, reqVO.getType())
                .eqIfPresent(MesDvCheckPlanDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesDvCheckPlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MesDvCheckPlanDO::getId));
    }

    default MesDvCheckPlanDO selectByCode(String code) {
        return selectOne(MesDvCheckPlanDO::getCode, code);
    }

}
