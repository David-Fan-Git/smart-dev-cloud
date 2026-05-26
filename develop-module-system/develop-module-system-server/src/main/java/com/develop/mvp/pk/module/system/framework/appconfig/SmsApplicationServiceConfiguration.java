package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import com.develop.mvp.pk.module.system.application.sms.service.SmsApplicationService;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsChannelMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsCodeMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsLogMapper;
import com.develop.mvp.pk.module.system.dal.mysql.sms.SmsTemplateMapper;
import com.develop.mvp.pk.module.system.domain.sms.repository.SmsChannelRepository;
import com.develop.mvp.pk.module.system.framework.sms.config.SmsCodeProperties;
import com.develop.mvp.pk.module.system.framework.sms.core.client.SmsClientFactory;
import com.develop.mvp.pk.module.system.mq.producer.sms.SmsProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sms Application Service Configuration 配置类。
 */
@Configuration
public class SmsApplicationServiceConfiguration {

    /**
     * 执行 sms Application Service 对应的业务操作。
     *
     * @param channelRepo channelRepo 参数
     * @param smsClientFactory smsClientFactory 参数
     * @param smsChannelMapper smsChannelMapper 参数
     * @param smsTemplateMapper smsTemplateMapper 参数
     * @param smsLogMapper smsLogMapper 参数
     * @param smsCodeMapper smsCodeMapper 参数
     * @param smsCodeProperties smsCodeProperties 参数
     * @param adminUserService adminUserService 参数
     * @param memberApplicationService memberApplicationService 参数
     * @param smsProducer smsProducer 参数
     * @return 处理结果
     */
    @Bean
    public SmsApplicationService smsApplicationService(
            SmsChannelRepository channelRepo,
            SmsClientFactory smsClientFactory,
            SmsChannelMapper smsChannelMapper,
            SmsTemplateMapper smsTemplateMapper,
            SmsLogMapper smsLogMapper,
            SmsCodeMapper smsCodeMapper,
            SmsCodeProperties smsCodeProperties,
            AdminUserUseCase adminUserService,
            MemberApplicationService memberApplicationService,
            SmsProducer smsProducer) {
        return new SmsApplicationService(channelRepo, smsClientFactory, smsChannelMapper,
                smsTemplateMapper, smsLogMapper, smsCodeMapper, smsCodeProperties,
                adminUserService, memberApplicationService, smsProducer);
    }
}
