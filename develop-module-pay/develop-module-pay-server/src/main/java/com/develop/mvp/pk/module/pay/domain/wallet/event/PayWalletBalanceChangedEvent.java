package com.develop.mvp.pk.module.pay.domain.wallet.event;
// DDD 角色：钱包余额变更事件 - AggregateRoot_Pay_Skill
public record PayWalletBalanceChangedEvent(Long walletId, Integer changeAmount, Integer newBalance,
                                            Integer bizType, String bizId, String title) {}
