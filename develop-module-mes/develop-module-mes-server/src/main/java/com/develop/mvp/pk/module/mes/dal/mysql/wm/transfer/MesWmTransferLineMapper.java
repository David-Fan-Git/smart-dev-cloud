package com.develop.mvp.pk.module.mes.dal.mysql.wm.transfer;

import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.mes.dal.dataobject.wm.transfer.MesWmTransferLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 转移单行 Mapper
 */
@Mapper
public interface MesWmTransferLineMapper extends BaseMapperX<MesWmTransferLineDO> {

    default List<MesWmTransferLineDO> selectListByTransferId(Long transferId) {
        return selectList(new LambdaQueryWrapperX<MesWmTransferLineDO>()
                .eq(MesWmTransferLineDO::getTransferId, transferId)
                .orderByAsc(MesWmTransferLineDO::getId));
    }

    default void deleteByTransferId(Long transferId) {
        delete(MesWmTransferLineDO::getTransferId, transferId);
    }

}
