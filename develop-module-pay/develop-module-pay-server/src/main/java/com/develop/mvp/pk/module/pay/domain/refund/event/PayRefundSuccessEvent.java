package com.develop.mvp.pk.module.pay.domain.refund.event;
// DDD 角色：退款成功事件 - AggregateRoot_Pay_Skill
public record PayRefundSuccessEvent(Long refundId, String no, Long orderId, Integer refundPrice) {}
