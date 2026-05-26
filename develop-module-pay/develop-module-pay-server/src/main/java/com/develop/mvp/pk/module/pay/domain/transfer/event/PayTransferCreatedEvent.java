package com.develop.mvp.pk.module.pay.domain.transfer.event;
// DDD 角色：转账单创建事件 - AggregateRoot_Pay_Skill
public record PayTransferCreatedEvent(Long transferId, String no, Long appId, String merchantTransferId, Integer price) {}
