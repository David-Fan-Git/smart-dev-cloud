package com.develop.mvp.pk.module.mes.dal.mysql.wm.productreceipt;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.controller.admin.wm.productreceipt.vo.line.MesWmProductReceiptLinePageReqVO;
import com.develop.mvp.pk.module.mes.dal.dataobject.wm.productreceipt.MesWmProductReceiptLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 产品收货单行 Mapper
 */
@Mapper
public interface MesWmProductReceiptLineMapper extends BaseMapperX<MesWmProductReceiptLineDO> {

    default PageResult<MesWmProductReceiptLineDO> selectPage(MesWmProductReceiptLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmProductReceiptLineDO>()
                .eqIfPresent(MesWmProductReceiptLineDO::getReceiptId, reqVO.getReceiptId())
                .orderByDesc(MesWmProductReceiptLineDO::getId));
    }

    default List<MesWmProductReceiptLineDO> selectListByRecptId(Long receiptId) {
        return selectList(MesWmProductReceiptLineDO::getReceiptId, receiptId);
    }

    default void deleteByRecptId(Long receiptId) {
        delete(MesWmProductReceiptLineDO::getReceiptId, receiptId);
    }

}
