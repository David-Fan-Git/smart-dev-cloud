---
name: starter-redis
description: Redis caching with JSON serialization, TTL-aware TimeoutRedisCacheManager, Redisson distributed locks, and cache property configuration
type: project
---

# develop-spring-boot-starter-redis

## Overview

Redis 缓存与分布式协同基础模块。提供统一的 `RedisTemplate` 配置（JSON 序列化，含 Java 8 时间类型支持）、基于 TTL 后缀注解的 `TimeoutRedisCacheManager`、以及 Redisson 分布式锁自动配置。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **2 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.redis`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-redis AutoConfiguration.imports
com.develop.mvp.pk.framework.redis.config.DevelopRedisAutoConfiguration
com.develop.mvp.pk.framework.redis.config.DevelopCacheAutoConfiguration
```

## Core Components

### 1. DevelopRedisAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.redis.config`

- **RedisTemplate<String, Object>**:
  - `@AutoConfiguration(before = RedissonAutoConfigurationV2.class)` — ensures custom template takes priority
  - Key serialization: `RedisSerializer.string()` (StringRedisSerializer)
  - Value serialization: `RedisSerializer.json()` with `JavaTimeModule` registered (Jackson's generic JSON serializer, not `Jackson2JsonRedisSerializer` directly)
  - Hash serialization: same as value (JSON)
  - `JavaTimeModule` enables proper `LocalDateTime`, `LocalDate`, `LocalTime` serialization

**Key Detail:**
```java
public static RedisSerializer<?> buildRedisSerializer() {
    RedisSerializer<Object> json = RedisSerializer.json();
    // Add JavaTimeModule for Java 8 time types
    ObjectMapper objectMapper = (ObjectMapper) ReflectUtil.getFieldValue(json, "mapper");
    objectMapper.registerModules(new JavaTimeModule());
    return json;
}
```

This uses `RedisSerializer.json()` (Jackson's generic JSON serializer) instead of manually constructing `Jackson2JsonRedisSerializer`, ensuring better type handling with `@class` type information in the serialized JSON.

### 2. DevelopCacheAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.redis.config`

- `@EnableCaching` — enables Spring's `@Cacheable`, `@CacheEvict`, `@CachePut` support
- **TimeoutRedisCacheManager** (extends `RedisCacheManager`):
  - Parses TTL suffix from `@Cacheable(cacheNames = "key#30m")`:
    - `d` — days (e.g., `#1d`)
    - `h` — hours (e.g., `#2h`)
    - `m` — minutes (e.g., `#30m`)
    - `s` — seconds (e.g., `#90s`)
  - Falls back to default TTL (`spring.cache.redis.time-to-live`) when no suffix present
- Primary `RedisCacheConfiguration` with default serializer config

## Configuration Properties

```yaml
spring:
  cache:
    type: REDIS
    redis:
      time-to-live: 1h              # Default TTL for all caches

develop:
  cache:
    redis-scan-batch-size: 100      # Redis SCAN batch size for cache cleanup

# Redis connection (Lettuce)
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password:
      database: 0
      timeout: 10s
      lettuce:
        pool:
          min-idle: 0
          max-idle: 8
          max-active: 16
          max-wait: -1ms

# Redisson (separate connection)
spring:
  redis:
    redisson:
      config: classpath:redisson.yaml   # Or inline config
```

## Redisson Integration

Redisson is auto-configured by `RedissonAutoConfigurationV2` (from `redisson-spring-boot-starter`). It uses a separate connection pool from Lettuce and provides:

- **Distributed locks** (RLock): `RedissonLockExecutor` (Lock4j default)
- **Rate limiter** (RRateLimiter): used by starter-protection
- **Semaphore**, **CountDownLatch**, etc.
- Redisson config can be defined in YAML or via `redisson.yaml` file

## Code Examples

```java
// Using RedisTemplate directly
@Autowired
private RedisTemplate<String, Object> redisTemplate;

public void cacheUser(Long id, UserDO user) {
    redisTemplate.opsForValue().set("user:" + id, user, 30, TimeUnit.MINUTES);
}

// Using @Cacheable with TTL suffix
@Cacheable(cacheNames = "user_cache#2h", key = "#id")
public UserDO getUser(Long id) {
    return userMapper.selectById(id);
}

// Distributed lock using Lock4j
@Lock4j(name = "order:pay:", keys = "#orderId", expire = 30000, acquireTimeout = 3000)
public void pay(Long orderId) { ... }

// Redisson native lock
@Autowired
private RedissonClient redissonClient;

public void business(Long id) {
    RLock lock = redissonClient.getLock("business:" + id);
    if (lock.tryLock(3, 30, TimeUnit.SECONDS)) {
        try {
            // Business logic
        } finally {
            lock.unlock();
        }
    }
}
```

## 注意事项

- `TimeoutRedisCacheManager` 的 TTL 后缀解析仅作用于 `cacheNames` 属性，`@Cacheable(key = ...)` 中的 key 不支持 TTL 后缀。正确用法：`@Cacheable(cacheNames = "cache#30m", key = "#id")`
- `RedisSerializer.json()` 序列化的值携带 `@class` 类型信息（全限定类名），反序列化时通过该信息恢复具体类型。如果类型变更或类名改变，需清理缓存或处理反序列化异常
- Redisson 与 Lettuce 可以共存（Redisson 使用独立连接），但不建议在业务代码中混用两种客户端，统一使用一种客户端降低维护成本
- Redis Cluster 模式下 `@Cacheable` 的 TTL 后缀仍有效，但 `SCAN` 命令需谨慎使用（可能阻塞集群节点）
- `JavaTimeModule` 的注册解决了 `LocalDateTime` 序列化为数组（`[2026,5,23,10,30,0]`）而非时间戳的问题
- 序列化值中包含 `@class` 信息会导致缓存占用比预期更大（约+30%），对内存敏感的场景可考虑自定义序列化器指定具体类型
- MQ 模块（starter-mq）依赖 Redis Stream/ Pub/Sub 时，确保 Redis 配置支持 stream 功能（Redis 5.0+）
