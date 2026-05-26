package com.develop.mvp.pk.module.system.framework.sms.core.client.impl;

import com.develop.mvp.pk.module.system.framework.sms.core.client.SmsClient;
import com.develop.mvp.pk.module.system.framework.sms.core.property.SmsChannelProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 短信客户端的抽象类，提供模板方法，减少子类的冗余代码
 *
 * @author David
 * @since 2021/2/1 9:28
 */
@Slf4j
public abstract class AbstractSmsClient implements SmsClient {

    /**
     * 短信渠道配置
     */
    protected volatile SmsChannelProperties properties;

    /**
     * 创建 AbstractSmsClient 实例。
     *
     * @param properties properties 参数
     */
    public AbstractSmsClient(SmsChannelProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化
     */
    public final void init() {
        log.debug("[init][配置({}) 初始化完成]", properties);
    }

    /**
     * 执行 refresh 对应的业务操作。
     *
     * @param properties properties 参数
     */
    public final void refresh(SmsChannelProperties properties) {
        // 判断是否更新
        if (properties.equals(this.properties)) {
            return;
        }
        log.info("[refresh][配置({})发生变化，重新初始化]", properties);
        this.properties = properties;
        // 初始化
        this.init();
    }

    /**
     * 查询 get Id 对应的数据。
     *
     * @return 处理结果
     */
    @Override
    public Long getId() {
        return properties.getId();
    }

}
