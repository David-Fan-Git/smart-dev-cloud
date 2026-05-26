package com.develop.mvp.pk.module.pay.domain.order.event;
// DDD 角色：支付订单退款事件 - AggregateRoot_Pay_Skill
public record PayOrderRefundedEvent(Long orderId, Integer refundPrice, Integer totalRefundPrice) {}
