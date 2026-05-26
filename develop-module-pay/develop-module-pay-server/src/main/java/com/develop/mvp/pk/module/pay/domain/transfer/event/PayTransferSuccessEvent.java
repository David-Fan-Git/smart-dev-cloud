package com.develop.mvp.pk.module.pay.domain.transfer.event;
// DDD 角色：转账成功事件 - AggregateRoot_Pay_Skill
public record PayTransferSuccessEvent(Long transferId, String no, Long appId, Integer price) {}
