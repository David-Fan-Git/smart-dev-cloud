package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.notify.service.NotifyApplicationService;
import com.develop.mvp.pk.module.system.dal.mysql.notify.NotifyMessageMapper;
import com.develop.mvp.pk.module.system.dal.mysql.notify.NotifyTemplateMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Notify Application Service Configuration 配置类。
 */
@Configuration
public class NotifyApplicationServiceConfiguration {

    /**
     * 执行 notify Application Service 对应的业务操作。
     *
     * @param notifyMessageMapper notifyMessageMapper 参数
     * @param notifyTemplateMapper notifyTemplateMapper 参数
     * @return 处理结果
     */
    @Bean
    public NotifyApplicationService notifyApplicationService(
            NotifyMessageMapper notifyMessageMapper,
            NotifyTemplateMapper notifyTemplateMapper) {
        return new NotifyApplicationService(notifyMessageMapper, notifyTemplateMapper);
    }
}
