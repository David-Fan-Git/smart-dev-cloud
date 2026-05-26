package com.develop.mvp.pk.framework.redis.config;

import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 自动配置入口。
 * <p>
 * 本类负责提供面向业务代码直接读写 Redis 的 {@link RedisTemplate}。
 * 它位于 Redis 连接工厂之上、业务缓存工具之下：连接工厂负责连接 Redis 服务，
 * {@link RedisTemplate} 负责把 Java 对象序列化成 Redis 能存储的 key/value，并执行 get、set、hash 等命令。
 * <p>
 * 与 Spring Cache 不同，RedisTemplate 是“命令式操作工具”，适合业务代码显式控制 Redis key、数据结构和读写时机；
 * Spring Cache 则面向 {@code @Cacheable}、{@code @CacheEvict} 等注解，由 {@code CacheManager} 统一管理缓存名称和过期策略。
 */
@AutoConfiguration(before = RedissonAutoConfigurationV2.class) // 先注册框架自定义 RedisTemplate，供后续 Redis/Redisson 相关自动配置优先复用
public class DevelopRedisAutoConfiguration {

    /**
     * 创建框架统一的 RedisTemplate。
     * <p>
     * key 和 hash key 使用字符串序列化，便于在 Redis 控制台中直接阅读和排查；
     * value 和 hash value 使用 Jackson JSON 序列化，便于保存普通 Java 对象。
     * 业务代码直接注入该 Bean 时，读写的是明确的 Redis 数据结构，不会自动套用 Spring Cache 的缓存名称、前缀或 TTL 规则。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 对象，它是业务代码直接执行 Redis 命令的主要入口。
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂，具体底层客户端由 Spring Data Redis 根据依赖和配置适配。
        template.setConnectionFactory(factory);
        // Redis key 保持字符串形式，方便人工定位和跨语言访问。
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        // Redis value 使用 Jackson JSON，支持保存对象并兼顾可读性。
        template.setValueSerializer(buildRedisSerializer());
        template.setHashValueSerializer(buildRedisSerializer());
        return template;
    }

    /**
     * 构建框架统一的 Redis JSON 序列化器。
     * <p>
     * Spring Data Redis 默认 JSON 序列化器内部使用 Jackson。这里额外注册 {@link JavaTimeModule}，
     * 让 {@code LocalDateTime} 等 Java 8 时间类型可以被正确序列化和反序列化，避免时间字段写入 Redis 时出现不兼容格式。
     */
    public static RedisSerializer<?> buildRedisSerializer() {
        RedisSerializer<Object> json = RedisSerializer.json();
        // 为 Redis JSON 序列化器补充 Java 8 时间类型支持。
        ObjectMapper objectMapper = (ObjectMapper) ReflectUtil.getFieldValue(json, "mapper");
        objectMapper.registerModules(new JavaTimeModule());
        return json;
    }

}
