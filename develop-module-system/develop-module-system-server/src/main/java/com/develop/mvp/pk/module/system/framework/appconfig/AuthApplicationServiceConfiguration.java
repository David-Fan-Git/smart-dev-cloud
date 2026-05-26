package com.develop.mvp.pk.module.system.framework.appconfig;

import com.anji.captcha.service.CaptchaService;
import com.develop.mvp.pk.module.system.api.sms.SmsCodeApi;
import com.develop.mvp.pk.module.system.application.auth.service.AuthApplicationService;
import com.develop.mvp.pk.module.system.application.logger.service.LoggerApplicationService;
import com.develop.mvp.pk.module.system.application.member.service.MemberApplicationService;
import com.develop.mvp.pk.module.system.application.oauth2.port.inbound.OAuth2UseCase;
import com.develop.mvp.pk.module.system.application.social.port.inbound.SocialUseCase;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auth Application Service Configuration 配置类。
 */
@Configuration
public class AuthApplicationServiceConfiguration {

    /**
     * 处理 auth Application Service 对应的认证流程。
     *
     * @param userService userService 参数
     * @param loggerApplicationService loggerApplicationService 参数
     * @param oauth2TokenService oauth2TokenService 参数
     * @param socialUseCase socialUseCase 参数
     * @param memberApplicationService memberApplicationService 参数
     * @param validator validator 参数
     * @param captchaService captchaService 参数
     * @param smsCodeApi smsCodeApi 参数
     * @param captchaEnable captchaEnable 参数
     * @return 处理结果
     */
    @Bean
    public AuthApplicationService authApplicationService(
            AdminUserUseCase userService,
            LoggerApplicationService loggerApplicationService,
            OAuth2UseCase oauth2TokenService,
            SocialUseCase socialUseCase,
            MemberApplicationService memberApplicationService,
            Validator validator,
            CaptchaService captchaService,
            SmsCodeApi smsCodeApi,
            @Value("${develop.captcha.enable:true}") Boolean captchaEnable) {
        AuthApplicationService svc = new AuthApplicationService(userService, loggerApplicationService,
                oauth2TokenService, socialUseCase, memberApplicationService,
                validator, captchaService, smsCodeApi);
        svc.setCaptchaEnable(captchaEnable);
        return svc;
    }
}
