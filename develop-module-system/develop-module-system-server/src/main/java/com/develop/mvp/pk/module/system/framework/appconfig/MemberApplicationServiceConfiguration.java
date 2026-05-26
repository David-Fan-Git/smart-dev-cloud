package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.member.port.outbound.MemberUserGateway;
import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Member Application Service Configuration 配置类。
 */
@Configuration
public class MemberApplicationServiceConfiguration {

    /**
     * 执行 member Application Service 对应的业务操作。
     *
     * @param memberUserGateway memberUserGateway 参数
     * @return 处理结果
     */
    @Bean
    public MemberApplicationService memberApplicationService(MemberUserGateway memberUserGateway) {
        return new MemberApplicationService(memberUserGateway);
    }
}
