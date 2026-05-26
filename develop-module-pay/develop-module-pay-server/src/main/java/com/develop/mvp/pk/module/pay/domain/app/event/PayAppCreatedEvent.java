package com.develop.mvp.pk.module.pay.domain.app.event;
// DDD 角色：支付应用创建事件 - AggregateRoot_Pay_Skill
public record PayAppCreatedEvent(Long appId, String appKey, String name) {}
