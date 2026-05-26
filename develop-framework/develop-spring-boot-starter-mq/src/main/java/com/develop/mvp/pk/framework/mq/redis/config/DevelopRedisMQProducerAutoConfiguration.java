package com.develop.mvp.pk.framework.mq.redis.config;

import com.develop.mvp.pk.framework.mq.redis.core.RedisMQTemplate;
import com.develop.mvp.pk.framework.mq.redis.core.interceptor.RedisMessageInterceptor;
import com.develop.mvp.pk.framework.redis.config.DevelopRedisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * Redis 消息队列生产者自动配置。
 *
 * <p>该配置在基础 Redis 自动配置完成之后生效，负责把 Spring Data Redis 提供的
 * {@link StringRedisTemplate} 包装成业务侧可直接注入的 {@link RedisMQTemplate}。
 * 业务代码发送 Redis Pub/Sub 或 Redis Stream 消息时，不需要直接操作底层 Redis API，
 * 统一通过模板完成序列化、发送以及发送前后的拦截扩展。</p>
 *
 * @author David
 */
@Slf4j
@AutoConfiguration(after = DevelopRedisAutoConfiguration.class)
public class DevelopRedisMQProducerAutoConfiguration {

    /**
     * 装配 Redis MQ 发送模板。
     *
     * <p>这里保留传入的 {@link StringRedisTemplate} 作为真实发送通道，并把容器中已有的
     * {@link RedisMessageInterceptor} 注册到模板中。后续业务调用模板发送消息时，模板会在
     * Redis Pub/Sub 或 Redis Stream 发布前后回调这些拦截器，用于补充租户等横切上下文。</p>
     *
     * @param redisTemplate Redis 字符串模板，由基础 Redis Starter 提供
     * @param interceptors 消息拦截器列表，由 Spring 容器收集
     * @return 业务侧统一使用的 Redis MQ 模板
     */
    @Bean
    public RedisMQTemplate redisMQTemplate(StringRedisTemplate redisTemplate,
                                           List<RedisMessageInterceptor> interceptors) {
        RedisMQTemplate redisMQTemplate = new RedisMQTemplate(redisTemplate);
        // 添加拦截器
        interceptors.forEach(redisMQTemplate::addInterceptor);
        return redisMQTemplate;
    }

}
