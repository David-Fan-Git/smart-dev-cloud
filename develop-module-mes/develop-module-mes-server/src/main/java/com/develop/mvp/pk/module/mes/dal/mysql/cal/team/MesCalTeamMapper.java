package com.develop.mvp.pk.module.mes.dal.mysql.cal.team;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.cal.team.vo.MesCalTeamPageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 班组 Mapper
 *
 * @author David
 */
@Mapper
public interface MesCalTeamMapper extends BaseMapperX<MesCalTeamDO> {

    default PageResult<MesCalTeamDO> selectPage(MesCalTeamPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesCalTeamDO>()
                .likeIfPresent(MesCalTeamDO::getCode, reqVO.getCode())
                .likeIfPresent(MesCalTeamDO::getName, reqVO.getName())
                .eqIfPresent(MesCalTeamDO::getCalendarType, reqVO.getCalendarType())
                .orderByDesc(MesCalTeamDO::getId));
    }

    default MesCalTeamDO selectByCode(String code) {
        return selectOne(MesCalTeamDO::getCode, code);
    }

}
