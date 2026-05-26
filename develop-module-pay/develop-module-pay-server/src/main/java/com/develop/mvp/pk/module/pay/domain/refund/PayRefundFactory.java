package com.develop.mvp.pk.module.pay.domain.refund;
// DDD 角色：退款单工厂 - AggregateRoot_Pay_Skill
import java.time.LocalDateTime;
public class PayRefundFactory {
    public static PayRefund create(Long id, String no, Long appId, Long orderId, String orderNo,
                                    Long channelId, String channelCode, String merchantOrderId,
                                    String merchantRefundId, String notifyUrl, Integer payPrice,
                                    Integer refundPrice, String reason, String userIp, String channelOrderNo) {
        PayRefund refund = new PayRefund(id, no);
        refund.appId(appId).orderId(orderId).orderNo(orderNo)
                .channelId(channelId).channelCode(channelCode)
                .merchantOrderId(merchantOrderId).merchantRefundId(merchantRefundId)
                .notifyUrl(notifyUrl).payPrice(payPrice).refundPrice(refundPrice)
                .reason(reason).userIp(userIp).channelOrderNo(channelOrderNo)
                .status(0); // WAITING
        return refund;
    }
    public static PayRefund restore(Long id, String no, Long appId, Long channelId, String channelCode,
                                     Long orderId, String orderNo, Long userId, Integer userType,
                                     String merchantOrderId, String merchantRefundId, String notifyUrl,
                                     Integer status, Integer payPrice, Integer refundPrice, String reason,
                                     String userIp, String channelOrderNo, String channelRefundNo,
                                     LocalDateTime successTime) {
        PayRefund refund = new PayRefund(id, no);
        refund.appId(appId).channelId(channelId).channelCode(channelCode)
                .orderId(orderId).orderNo(orderNo).userId(userId).userType(userType)
                .merchantOrderId(merchantOrderId).merchantRefundId(merchantRefundId).notifyUrl(notifyUrl)
                .status(status).payPrice(payPrice).refundPrice(refundPrice)
                .reason(reason).userIp(userIp).channelOrderNo(channelOrderNo)
                .channelRefundNo(channelRefundNo).successTime(successTime);
        return refund;
    }
}
