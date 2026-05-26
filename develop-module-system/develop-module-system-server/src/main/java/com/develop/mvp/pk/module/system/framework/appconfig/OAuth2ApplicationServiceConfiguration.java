package com.develop.mvp.pk.module.system.framework.appconfig;

import com.develop.mvp.pk.module.system.application.auth.port.inbound.AuthUseCase;
import com.develop.mvp.pk.module.system.application.oauth2.service.OAuth2ApplicationService;
import com.develop.mvp.pk.module.system.application.user.port.inbound.AdminUserUseCase;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2AccessTokenMapper;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2ApproveMapper;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2ClientMapper;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2CodeMapper;
import com.develop.mvp.pk.module.system.dal.mysql.oauth2.OAuth2RefreshTokenMapper;
import com.develop.mvp.pk.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import com.develop.mvp.pk.module.system.domain.oauth2.repository.OAuth2AccessTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Optional;

/**
 * OAuth2 Application Service Configuration 配置类。
 */
@Configuration
public class OAuth2ApplicationServiceConfiguration {

    /**
     * 执行 o Auth2 Application Service 对应的业务操作。
     *
     * @param oauth2ClientMapper oauth2ClientMapper 参数
     * @param oauth2AccessTokenMapper oauth2AccessTokenMapper 参数
     * @param oauth2RefreshTokenMapper oauth2RefreshTokenMapper 参数
     * @param oauth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO 参数
     * @param oauth2CodeMapper oauth2CodeMapper 参数
     * @param oauth2ApproveMapper oauth2ApproveMapper 参数
     * @param adminUserService adminUserService 参数
     * @param adminAuthService adminAuthService 参数
     * @param tokenRepoOpt tokenRepoOpt 参数
     * @return 处理结果
     */
    @Bean
    public OAuth2ApplicationService oAuth2ApplicationService(
            OAuth2ClientMapper oauth2ClientMapper,
            OAuth2AccessTokenMapper oauth2AccessTokenMapper,
            OAuth2RefreshTokenMapper oauth2RefreshTokenMapper,
            OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO,
            OAuth2CodeMapper oauth2CodeMapper,
            OAuth2ApproveMapper oauth2ApproveMapper,
            @Lazy AdminUserUseCase adminUserService,
            @Lazy AuthUseCase adminAuthService,
            Optional<OAuth2AccessTokenRepository> tokenRepoOpt) {
        OAuth2ApplicationService svc = new OAuth2ApplicationService(
                oauth2ClientMapper, oauth2AccessTokenMapper, oauth2RefreshTokenMapper,
                oauth2AccessTokenRedisDAO, oauth2CodeMapper, oauth2ApproveMapper,
                adminUserService, adminAuthService);
        tokenRepoOpt.ifPresent(svc::setTokenRepo);
        return svc;
    }
}
