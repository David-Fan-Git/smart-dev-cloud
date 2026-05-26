package com.develop.mvp.pk.module.system.framework.appconfig;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.binarywang.spring.starter.wxjava.miniapp.properties.WxMaProperties;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import com.develop.mvp.pk.module.system.application.social.service.SocialApplicationService;
import com.develop.mvp.pk.module.system.dal.mysql.social.SocialClientMapper;
import com.develop.mvp.pk.module.system.dal.mysql.social.SocialUserBindMapper;
import com.develop.mvp.pk.module.system.dal.mysql.social.SocialUserMapper;
import com.develop.mvp.pk.module.system.framework.justauth.core.AuthRequestFactory;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Social Application Service Configuration 配置类。
 */
@Configuration
public class SocialApplicationServiceConfiguration {

    /**
     * 执行 social Application Service 对应的业务操作。
     *
     * @param authRequestFactory authRequestFactory 参数
     * @param wxMpService wxMpService 参数
     * @param wxMpProperties wxMpProperties 参数
     * @param stringRedisTemplate stringRedisTemplate 参数
     * @param wxMaService wxMaService 参数
     * @param wxMaProperties wxMaProperties 参数
     * @param socialClientMapper socialClientMapper 参数
     * @param socialUserBindMapper socialUserBindMapper 参数
     * @param socialUserMapper socialUserMapper 参数
     * @param envVersion envVersion 参数
     * @param miniprogramState miniprogramState 参数
     * @return 处理结果
     */
    @Bean
    public SocialApplicationService socialApplicationService(
            @Autowired(required = false) AuthRequestFactory authRequestFactory,
            WxMpService wxMpService,
            WxMpProperties wxMpProperties,
            StringRedisTemplate stringRedisTemplate,
            WxMaService wxMaService,
            WxMaProperties wxMaProperties,
            SocialClientMapper socialClientMapper,
            SocialUserBindMapper socialUserBindMapper,
            SocialUserMapper socialUserMapper,
            @Value("${develop.wxa-code.env-version:release}") String envVersion,
            @Value("${develop.wxa-subscribe-message.miniprogram-state:formal}") String miniprogramState) {
        return new SocialApplicationService(
                authRequestFactory, wxMpService, wxMpProperties,
                stringRedisTemplate, wxMaService, wxMaProperties,
                socialClientMapper, socialUserBindMapper, socialUserMapper,
                envVersion, miniprogramState);
    }
}
