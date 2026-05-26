package com.develop.mvp.pk.module.mes.dal.mysql.pro.workorder;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.pro.workorder.vo.bom.MesProWorkOrderBomPageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderBomDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 生产工单 BOM Mapper
 *
 * @author David
 */
@Mapper
public interface MesProWorkOrderBomMapper extends BaseMapperX<MesProWorkOrderBomDO> {

    default PageResult<MesProWorkOrderBomDO> selectPage(MesProWorkOrderBomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesProWorkOrderBomDO>()
                .eqIfPresent(MesProWorkOrderBomDO::getWorkOrderId, reqVO.getWorkOrderId())
                .orderByDesc(MesProWorkOrderBomDO::getId));
    }

    default List<MesProWorkOrderBomDO> selectListByWorkOrderId(Long workOrderId) {
        return selectList(MesProWorkOrderBomDO::getWorkOrderId, workOrderId);
    }

    default void deleteByWorkOrderId(Long workOrderId) {
        delete(MesProWorkOrderBomDO::getWorkOrderId, workOrderId);
    }

}
