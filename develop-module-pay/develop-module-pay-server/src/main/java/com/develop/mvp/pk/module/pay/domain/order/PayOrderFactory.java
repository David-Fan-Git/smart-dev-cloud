package com.develop.mvp.pk.module.pay.domain.order;
// DDD 角色：支付订单工厂 - AggregateRoot_Pay_Skill
import com.develop.mvp.pk.module.pay.domain.order.valueobject.OrderStatus;
import java.time.LocalDateTime;
public class PayOrderFactory {
    public static PayOrder create(Long id, String no, Long appId, String merchantOrderId,
                                   String subject, Integer price) {
        PayOrder order = new PayOrder(id, no);
        order.appId(appId).merchantOrderId(merchantOrderId).subject(subject)
                .price(price).status(OrderStatus.WAITING.value()).refundPrice(0);
        return order;
    }
    public static PayOrder restore(Long id, String no, Long appId, Long channelId, String channelCode,
                                    Long userId, Integer userType, String merchantOrderId, String subject,
                                    String body, String notifyUrl, String userIp, Integer price,
                                    Double channelFeeRate, Integer channelFeePrice, Integer status,
                                    LocalDateTime expireTime, LocalDateTime successTime, Long extensionId,
                                    Integer refundPrice, String channelUserId, String channelOrderNo) {
        PayOrder order = new PayOrder(id, no);
        order.appId(appId).channelId(channelId).channelCode(channelCode)
                .userId(userId).userType(userType)
                .merchantOrderId(merchantOrderId).subject(subject).body(body).notifyUrl(notifyUrl)
                .userIp(userIp).price(price).channelFeeRate(channelFeeRate).channelFeePrice(channelFeePrice)
                .status(status).expireTime(expireTime).successTime(successTime).extensionId(extensionId)
                .refundPrice(refundPrice).channelUserId(channelUserId).channelOrderNo(channelOrderNo);
        return order;
    }
}
