package com.develop.mvp.pk.module.system.framework.justauth.config;

import com.develop.mvp.pk.module.system.framework.justauth.core.AuthRequestFactory;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import com.xkcoding.justauth.support.cache.RedisStateCache;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * JustAuth 配置类 TODO David：等 justauth 1.4.1 版本发布！！！
 *
 * @author David
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({JustAuthProperties.class})
public class DevelopJustAuthConfiguration {

    /**
     * 处理 auth Request Factory 对应的认证流程。
     *
     * @param properties properties 参数
     * @param authStateCache authStateCache 参数
     * @return 处理结果
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "justauth",
            value = {"enabled"},
            havingValue = "true",
            matchIfMissing = true
    )
    public AuthRequestFactory authRequestFactory(JustAuthProperties properties, AuthStateCache authStateCache) {
        return new AuthRequestFactory(properties, authStateCache);
    }

    /**
     * 处理 auth State Cache 对应的认证流程。
     *
     * @param justAuthRedisCacheTemplate justAuthRedisCacheTemplate 参数
     * @param justAuthProperties justAuthProperties 参数
     * @return 处理结果
     */
    @Bean
    public AuthStateCache authStateCache(RedisTemplate<String, String> justAuthRedisCacheTemplate,
                                         JustAuthProperties justAuthProperties) {
        return new RedisStateCache(justAuthRedisCacheTemplate, justAuthProperties.getCache());
    }

}
