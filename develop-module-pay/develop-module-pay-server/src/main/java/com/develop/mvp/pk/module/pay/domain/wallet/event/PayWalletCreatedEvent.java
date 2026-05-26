package com.develop.mvp.pk.module.pay.domain.wallet.event;
// DDD 角色：钱包创建事件 - AggregateRoot_Pay_Skill
public record PayWalletCreatedEvent(Long walletId, Long userId, Integer userType) {}
