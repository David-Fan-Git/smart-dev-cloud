package com.develop.mvp.pk.module.pay.domain.app.event;
// DDD 角色：支付应用状态更新事件 - AggregateRoot_Pay_Skill
public record PayAppStatusUpdatedEvent(Long appId, Integer oldStatus, Integer newStatus) {}
