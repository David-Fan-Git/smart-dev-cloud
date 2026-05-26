package com.develop.mvp.pk.module.pay.domain.order.event;
// DDD 角色：支付订单关闭事件 - AggregateRoot_Pay_Skill
public record PayOrderClosedEvent(Long orderId, String reason) {}
