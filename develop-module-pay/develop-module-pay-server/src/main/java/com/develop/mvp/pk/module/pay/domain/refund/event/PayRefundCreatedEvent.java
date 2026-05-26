package com.develop.mvp.pk.module.pay.domain.refund.event;
// DDD 角色：退款单创建事件 - AggregateRoot_Pay_Skill
public record PayRefundCreatedEvent(Long refundId, String no, Long orderId, Integer refundPrice) {}
