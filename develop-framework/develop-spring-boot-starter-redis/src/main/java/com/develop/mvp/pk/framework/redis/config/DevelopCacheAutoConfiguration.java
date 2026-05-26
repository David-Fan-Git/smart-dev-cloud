package com.develop.mvp.pk.framework.redis.config;

import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.redis.core.TimeoutRedisCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static com.develop.mvp.pk.framework.redis.config.DevelopRedisAutoConfiguration.buildRedisSerializer;

/**
 * Spring Cache 的 Redis 自动配置入口。
 * <p>
 * 本类负责把 Spring Cache 抽象落到 Redis 上：业务方法使用 {@code @Cacheable}、{@code @CachePut}、
 * {@code @CacheEvict} 等注解时，真正的缓存读写由这里创建的 {@link RedisCacheManager} 完成。
 * <p>
 * 它和 {@link RedisTemplate} 的定位不同：RedisTemplate 面向显式 Redis 命令；
 * CacheManager 面向注解驱动缓存，统一处理缓存名称、key 前缀、序列化、空值缓存和默认过期时间。
 * 本类还通过 {@link TimeoutRedisCacheManager} 支持框架自定义的缓存过期策略扩展。
 */
@AutoConfiguration
@EnableConfigurationProperties({CacheProperties.class, DevelopCacheProperties.class})
@EnableCaching
public class DevelopCacheAutoConfiguration {

    /**
     * 创建 Spring Cache 使用的 Redis 缓存配置。
     * <p>
     * 该配置决定所有 Redis Cache 的基础行为：缓存 key 前缀格式、value 序列化方式、默认 TTL、
     * 是否缓存 null 值、是否启用 key 前缀等。业务方法进入 Spring Cache 后，
     * CacheManager 会基于这些规则把缓存项写入 Redis。
     * <p>
     * 这里参考 Spring Boot 默认 RedisCacheConfiguration 的创建逻辑，但将 key 前缀统一为单冒号风格，
     * 并复用框架 Redis JSON 序列化器，保证缓存 value 与 RedisTemplate 的对象序列化习惯保持一致。
     */
    @Bean
    @Primary
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // 使用单冒号分隔缓存前缀和缓存名称，避免默认双冒号在 Redis 可视化工具中显示不友好；该格式也兼容历史 Issue 修复。
        config = config.computePrefixWith(cacheName -> {
            String keyPrefix = cacheProperties.getRedis().getKeyPrefix();
            if (StringUtils.hasText(keyPrefix)) {
                keyPrefix = keyPrefix.lastIndexOf(StrUtil.COLON) == -1 ? keyPrefix + StrUtil.COLON : keyPrefix;
                return keyPrefix + cacheName + StrUtil.COLON;
            }
            return cacheName + StrUtil.COLON;
        });
        // Cache value 使用框架统一的 JSON 序列化方式，便于对象缓存和排查。
        config = config.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(buildRedisSerializer()));

        // 应用 Spring Boot 标准 spring.cache.redis.* 配置：默认过期时间、是否缓存空值、是否启用 key 前缀。
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }

    /**
     * 创建基于 Redis 的 Spring CacheManager。
     * <p>
     * 注解式缓存不会直接使用业务注入的 {@link RedisTemplate} 执行命令，而是通过 {@link RedisCacheManager}
     * 根据缓存名称获取 Cache，再由 Cache 读写 Redis。本方法仅复用 RedisTemplate 中的连接工厂，
     * 确保 Spring Cache 和显式 RedisTemplate 操作连接到同一套 Redis。
     * <p>
     * {@link RedisCacheWriter#nonLockingRedisCacheWriter(RedisConnectionFactory, BatchStrategies.BatchStrategy)}
     * 使用非锁写入方式，并按 {@link DevelopCacheProperties#getRedisScanBatchSize()} 配置批量 scan 策略。
     * 返回的 {@link TimeoutRedisCacheManager} 在基础配置之上承载框架自定义的缓存超时能力。
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate<String, Object> redisTemplate,
                                               RedisCacheConfiguration redisCacheConfiguration,
                                               DevelopCacheProperties developCacheProperties) {
        // 从 RedisTemplate 复用连接工厂，保证注解缓存与显式 Redis 操作访问同一个 Redis 连接来源。
        RedisConnectionFactory connectionFactory = Objects.requireNonNull(redisTemplate.getConnectionFactory());
        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory,
                BatchStrategies.scan(developCacheProperties.getRedisScanBatchSize()));
        // 使用支持超时策略扩展的 CacheManager，统一承接 Spring Cache 注解产生的缓存读写。
        return new TimeoutRedisCacheManager(cacheWriter, redisCacheConfiguration);
    }

}
