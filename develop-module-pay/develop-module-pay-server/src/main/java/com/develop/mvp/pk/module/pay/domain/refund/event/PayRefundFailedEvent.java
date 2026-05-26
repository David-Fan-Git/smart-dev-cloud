package com.develop.mvp.pk.module.pay.domain.refund.event;
// DDD 角色：退款失败事件 - AggregateRoot_Pay_Skill
public record PayRefundFailedEvent(Long refundId, String no, String errorCode, String errorMsg) {}
