package com.develop.mvp.pk.module.pay.domain.transfer.event;
// DDD 角色：转账关闭事件 - AggregateRoot_Pay_Skill
public record PayTransferClosedEvent(Long transferId, String no, String errorCode, String errorMsg) {}
