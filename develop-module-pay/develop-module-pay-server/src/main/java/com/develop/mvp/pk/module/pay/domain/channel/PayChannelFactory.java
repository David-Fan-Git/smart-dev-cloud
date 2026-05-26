package com.develop.mvp.pk.module.pay.domain.channel;
// DDD 角色：支付渠道工厂 - AggregateRoot_Pay_Skill
public class PayChannelFactory {
    public static PayChannel create(Long id, String code, Long appId, Double feeRate, Object config) {
        PayChannel channel = new PayChannel(id, code, appId);
        channel.feeRate(feeRate).config(config).status(0);
        return channel;
    }
    public static PayChannel restore(Long id, String code, Long appId, Integer status,
                                      Double feeRate, String remark, Object config) {
        PayChannel channel = new PayChannel(id, code, appId);
        channel.status(status).feeRate(feeRate).remark(remark).config(config);
        return channel;
    }
}
