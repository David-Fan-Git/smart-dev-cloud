package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.mail.service.MailApplicationService;
import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.dal.mysql.mail.MailAccountMapper;
import com.develop.mvp.pk.module.system.dal.mysql.mail.MailLogMapper;
import com.develop.mvp.pk.module.system.dal.mysql.mail.MailTemplateMapper;
import com.develop.mvp.pk.module.system.domain.mail.repository.MailAccountRepository;
import com.develop.mvp.pk.module.system.domain.mail.repository.MailTemplateRepository;
import com.develop.mvp.pk.module.system.mq.producer.mail.MailProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mail Application Service Configuration 配置类。
 */
@Configuration
public class MailApplicationServiceConfiguration {

    /**
     * 执行 mail Application Service 对应的业务操作。
     *
     * @param mailAccountMapper mailAccountMapper 参数
     * @param mailTemplateMapper mailTemplateMapper 参数
     * @param mailLogMapper mailLogMapper 参数
     * @param adminUserService adminUserService 参数
     * @param memberApplicationService memberApplicationService 参数
     * @param mailProducer mailProducer 参数
     * @param accountRepo accountRepo 参数
     * @param templateRepo templateRepo 参数
     * @return 处理结果
     */
    @Bean
    public MailApplicationService mailApplicationService(
            MailAccountMapper mailAccountMapper,
            MailTemplateMapper mailTemplateMapper,
            MailLogMapper mailLogMapper,
            AdminUserUseCase adminUserService,
            MemberApplicationService memberApplicationService,
            MailProducer mailProducer,
            @Autowired(required = false) MailAccountRepository accountRepo,
            @Autowired(required = false) MailTemplateRepository templateRepo) {
        MailApplicationService svc = new MailApplicationService(
                mailAccountMapper, mailTemplateMapper, mailLogMapper,
                adminUserService, memberApplicationService, mailProducer);
        if (accountRepo != null) {
            svc.setAccountRepo(accountRepo);
        }
        if (templateRepo != null) {
            svc.setTemplateRepo(templateRepo);
        }
        return svc;
    }
}
