package com.develop.mvp.pk.module.pay.domain.order.event;
// DDD 角色：支付订单支付成功事件 - AggregateRoot_Pay_Skill
public record PayOrderPaidEvent(Long orderId, String no, String channelOrderNo, Long extensionId) {}
