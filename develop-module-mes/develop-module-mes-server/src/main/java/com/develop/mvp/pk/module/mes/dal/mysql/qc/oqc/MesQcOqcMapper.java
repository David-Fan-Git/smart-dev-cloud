package com.develop.mvp.pk.module.mes.dal.mysql.qc.oqc;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.qc.oqc.vo.MesQcOqcPageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.qc.oqc.MesQcOqcDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 出货检验单（OQC） Mapper
 *
 * @author David
 */
@Mapper
public interface MesQcOqcMapper extends BaseMapperX<MesQcOqcDO> {

    default PageResult<MesQcOqcDO> selectPage(MesQcOqcPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesQcOqcDO>()
                .likeIfPresent(MesQcOqcDO::getCode, reqVO.getCode())
                .eqIfPresent(MesQcOqcDO::getClientId, reqVO.getClientId())
                .likeIfPresent(MesQcOqcDO::getBatchCode, reqVO.getBatchCode())
                .eqIfPresent(MesQcOqcDO::getItemId, reqVO.getItemId())
                .eqIfPresent(MesQcOqcDO::getCheckResult, reqVO.getCheckResult())
                .orderByDesc(MesQcOqcDO::getId));
    }

    default MesQcOqcDO selectByCode(String code) {
        return selectOne(MesQcOqcDO::getCode, code);
    }

}
