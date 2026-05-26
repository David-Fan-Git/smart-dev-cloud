package com.develop.mvp.pk.module.mes.dal.mysql.pro.workorder;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.pro.workorder.vo.MesProWorkOrderPageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

/**
 * MES 生产工单 Mapper
 *
 * @author David
 */
@Mapper
public interface MesProWorkOrderMapper extends BaseMapperX<MesProWorkOrderDO> {

    default PageResult<MesProWorkOrderDO> selectPage(MesProWorkOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesProWorkOrderDO>()
                .likeIfPresent(MesProWorkOrderDO::getCode, reqVO.getCode())
                .likeIfPresent(MesProWorkOrderDO::getName, reqVO.getName())
                .eqIfPresent(MesProWorkOrderDO::getType, reqVO.getType())
                .likeIfPresent(MesProWorkOrderDO::getOrderSourceCode, reqVO.getOrderSourceCode())
                .eqIfPresent(MesProWorkOrderDO::getProductId, reqVO.getProductId())
                .eqIfPresent(MesProWorkOrderDO::getClientId, reqVO.getClientId())
                .eqIfPresent(MesProWorkOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesProWorkOrderDO::getRequestDate, reqVO.getRequestDate())
                .orderByDesc(MesProWorkOrderDO::getId));
    }

    default MesProWorkOrderDO selectByCode(String code) {
        return selectOne(MesProWorkOrderDO::getCode, code);
    }

    default void updateProducedQuantity(Long id, BigDecimal incrQuantityProduced) {
        update(null, new LambdaUpdateWrapper<MesProWorkOrderDO>()
                .eq(MesProWorkOrderDO::getId, id)
                .setSql("quantity_produced = IFNULL(quantity_produced, 0) + " + incrQuantityProduced));
    }

    default Long selectCountByVendorId(Long vendorId) {
        return selectCount(MesProWorkOrderDO::getVendorId, vendorId);
    }

}
