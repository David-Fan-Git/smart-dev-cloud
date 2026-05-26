package com.develop.mvp.pk.module.pay.domain.channel.event;
// DDD 角色：支付渠道更新事件 - AggregateRoot_Pay_Skill
public record PayChannelUpdatedEvent(Long channelId, Long appId) {}
