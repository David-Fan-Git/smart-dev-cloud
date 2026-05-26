package com.develop.mvp.pk.module.trade.domain.tradeorder;

// Skill: AggregateRoot_TradeOrder_Validation_Skill — 工厂 TradeOrderFactory

import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.OrderItem;
import com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject.TradeOrderId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class TradeOrderFactory {
    private TradeOrderFactory() {}

    public static TradeOrder create(Long id, String no, Integer type, Integer terminal, Long userId,
                                     String userIp, String userRemark,
                                     Integer totalPrice, Integer discountPrice, Integer deliveryPrice,
                                     Integer adjustPrice, Integer payPrice,
                                     Integer deliveryType, String receiverName, String receiverMobile,
                                     Integer receiverAreaId, String receiverDetailAddress,
                                     List<OrderItem> items) {
        return new TradeOrder(id != null ? TradeOrderId.of(id) : null, no, type, terminal, userId,
                userIp, userRemark, null, null, null, null, null, null, false,
                null, null, false, null, null, totalPrice, discountPrice, deliveryPrice,
                adjustPrice, payPrice, deliveryType, null, null, null, null,
                receiverName, receiverMobile, receiverAreaId, receiverDetailAddress,
                null, null, null, 0, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, items);
    }

    public static TradeOrder reconstitute(Long id, String no, Integer type, Integer terminal, Long userId,
                                           String userIp, String userRemark, Integer status, Integer productCount,
                                           LocalDateTime finishTime, LocalDateTime cancelTime, Integer cancelType,
                                           String remark, Boolean commentStatus, Long brokerageUserId,
                                           Long payOrderId, Boolean payStatus, LocalDateTime payTime,
                                           String payChannelCode, Integer totalPrice, Integer discountPrice,
                                           Integer deliveryPrice, Integer adjustPrice, Integer payPrice,
                                           Integer deliveryType, Long logisticsId, String logisticsNo,
                                           LocalDateTime deliveryTime, LocalDateTime receiveTime,
                                           String receiverName, String receiverMobile, Integer receiverAreaId,
                                           String receiverDetailAddress, Long pickUpStoreId, String pickUpVerifyCode,
                                           Integer refundStatus, Integer refundPrice,
                                           Long couponId, Integer couponPrice, Integer usePoint, Integer pointPrice,
                                           Integer givePoint, Integer refundPoint, Integer vipPrice,
                                           Map<Long, Integer> giveCouponTemplateCounts, List<Long> giveCouponIds,
                                           Long seckillActivityId, Long bargainActivityId, Long bargainRecordId,
                                           Long combinationActivityId, Long combinationHeadId,
                                           Long combinationRecordId, Long pointActivityId,
                                           List<OrderItem> items) {
        return new TradeOrder(TradeOrderId.of(id), no, type, terminal, userId,
                userIp, userRemark, status, productCount, finishTime, cancelTime, cancelType,
                remark, commentStatus, brokerageUserId, payOrderId, payStatus, payTime,
                payChannelCode, totalPrice, discountPrice, deliveryPrice, adjustPrice, payPrice,
                deliveryType, logisticsId, logisticsNo, deliveryTime, receiveTime,
                receiverName, receiverMobile, receiverAreaId, receiverDetailAddress,
                pickUpStoreId, pickUpVerifyCode, refundStatus, refundPrice,
                couponId, couponPrice, usePoint, pointPrice, givePoint, refundPoint, vipPrice,
                giveCouponTemplateCounts, giveCouponIds, seckillActivityId, bargainActivityId,
                bargainRecordId, combinationActivityId, combinationHeadId, combinationRecordId,
                pointActivityId, items);
    }
}
