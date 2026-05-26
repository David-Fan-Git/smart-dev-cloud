package com.develop.mvp.pk.module.pay.domain.channel.event;
// DDD 角色：支付渠道创建事件 - AggregateRoot_Pay_Skill
public record PayChannelCreatedEvent(Long channelId, String code, Long appId) {}
