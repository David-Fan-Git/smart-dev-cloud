package com.develop.mvp.pk.module.pay.domain.app;
// DDD 角色：支付应用工厂 - AggregateRoot_Pay_Skill
public class PayAppFactory {
    public static PayApp create(Long id, String name, String appKey) {
        PayApp app = new PayApp(id, name);
        app.appKey(appKey).status(0); // 默认启用
        return app;
    }
    public static PayApp restore(Long id, String name, String appKey, Integer status,
                                  String remark, String orderNotifyUrl, String refundNotifyUrl, String transferNotifyUrl) {
        PayApp app = new PayApp(id, name);
        app.appKey(appKey).status(status).remark(remark);
        app.orderNotifyUrl(orderNotifyUrl).refundNotifyUrl(refundNotifyUrl).transferNotifyUrl(transferNotifyUrl);
        return app;
    }
}
