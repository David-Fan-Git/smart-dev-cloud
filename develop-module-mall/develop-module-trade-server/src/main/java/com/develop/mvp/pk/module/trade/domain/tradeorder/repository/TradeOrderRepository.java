package com.develop.mvp.pk.module.trade.domain.tradeorder.repository;

// Skill: AggregateRoot_TradeOrder_Validation_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.trade.domain.tradeorder.TradeOrder;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.TradeOrderId;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeOrderRepository {
    TradeOrder save(TradeOrder order);
    TradeOrder findById(TradeOrderId id);
    TradeOrder findByNo(String no);
    List<TradeOrder> findByUserId(Long userId);
    List<TradeOrder> findByStatus(Integer status);
    PageResult<TradeOrder> findPage(Long userId, Integer status, String no,
                                     LocalDateTime[] createTime, Integer pageNo, Integer pageSize);
    long countByStatus(Integer status);
    long count();
}
