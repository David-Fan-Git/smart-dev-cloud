package com.develop.mvp.pk.module.trade.domain.aftersale;

// Skill: AggregateRoot_AfterSale_Validation_Skill

import com.develop.mvp.pk.module.trade.domain.aftersale.valueobject.AfterSaleId;
import java.time.LocalDateTime;

public final class AfterSaleFactory {
    private AfterSaleFactory() {}

    public static AfterSale create(Long id, String no, Long userId, Long orderId, Long orderItemId,
                                    Long spuId, Long skuId, Integer count, Integer type,
                                    String reason, String description, String[] proofPictures,
                                    Integer status, Integer refundPrice) {
        return new AfterSale(id != null ? AfterSaleId.of(id) : null, no, userId, orderId, orderItemId,
                spuId, skuId, count, type, reason, description, proofPictures,
                status, refundPrice, null, null, null, null, null, null);
    }

    public static AfterSale reconstitute(Long id, String no, Long userId, Long orderId, Long orderItemId,
                                          Long spuId, Long skuId, Integer count, Integer type,
                                          String reason, String description, String[] proofPictures,
                                          Integer status, Integer refundPrice, String rejectReason,
                                          String payChannelCode, Long payRefundId,
                                          LocalDateTime auditTime, LocalDateTime refuseTime,
                                          LocalDateTime refundTime) {
        return new AfterSale(AfterSaleId.of(id), no, userId, orderId, orderItemId,
                spuId, skuId, count, type, reason, description, proofPictures,
                status, refundPrice, rejectReason, payChannelCode, payRefundId,
                auditTime, refuseTime, refundTime);
    }
}
