package com.develop.mvp.pk.module.pay.domain.order.event;
// DDD 角色：支付订单创建事件 - AggregateRoot_Pay_Skill
public record PayOrderCreatedEvent(Long orderId, String no, Long appId, String merchantOrderId, Integer price) {}
