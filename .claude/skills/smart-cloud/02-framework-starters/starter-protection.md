---
name: starter-protection
description: Service resilience — distributed rate limiting, idempotency, Lock4j distributed locking, and API signature verification, all backed by Redis/Redisson
type: project
---

# develop-spring-boot-starter-protection

## Overview

服务治理与防护模块。提供四大核心能力: **分布式限流**（RateLimiter）、**接口幂等性**（Idempotent）、**分布式锁**（Lock4j 集成）、**API 签名校验**（ApiSignature）。全部基于 Redisson + Redis 实现，通过注解声明式使用。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **4 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.idempotent|lock4j|ratelimiter|signature`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-protection AutoConfiguration.imports
com.develop.mvp.pk.framework.idempotent.config.DevelopIdempotentConfiguration
com.develop.mvp.pk.framework.lock4j.config.DevelopLock4jConfiguration
com.develop.mvp.pk.framework.ratelimiter.config.DevelopRateLimiterConfiguration
com.develop.mvp.pk.framework.signature.config.DevelopApiSignatureAutoConfiguration
```

## Core Components

### 1. Distributed Rate Limiting — RateLimiterAspect

**Location:** `com.develop.mvp.pk.framework.ratelimiter`

AOP aspect intercepting `@RateLimiter` annotated methods.

**Key Resolver Strategies (5 built-in):**

| Resolver | Key Source | Use Case |
|---|---|---|
| `DefaultRateLimiterKeyResolver` | Fixed value | Global API rate limit |
| `UserRateLimiterKeyResolver` | Current user ID | Per-user throttling (e.g., SMS send) |
| `ClientIpRateLimiterKeyResolver` | Request client IP | Per-IP throttling (e.g., brute force prevention) |
| `ServerNodeRateLimiterKeyResolver` | Current service node ID | Per-instance rate limit |
| `ExpressionRateLimiterKeyResolver` | SpEL expression | Custom key logic |

**Backend:** Redisson `RRateLimiter` (token bucket algorithm)

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    String name() default "";                    // Rate limiter name (Redis key)
    int count() default 100;                     // Max requests
    int time() default 60;                       // Time window (seconds)
    KeyResolver keyResolver() default KeyResolver.DEFAULT;
}
```

### 2. Idempotency — IdempotentAspect

**Location:** `com.develop.mvp.pk.framework.idempotent`

AOP aspect intercepting `@Idempotent` annotated methods.

**Key Resolver Strategies (3 built-in):**

| Resolver | Key Source |
|---|---|
| `DefaultIdempotentKeyResolver` | Fixed value (e.g., `createOrder`) |
| `UserIdIdempotentKeyResolver` | Current user ID + method name |
| `ExpressionIdempotentKeyResolver` | SpEL expression (e.g., `#reqVO.orderNo`) |

**Backend:** Redis SET NX + TTL (`IdempotentRedisDAO`)
- Duplicate request returns `CommonResult.error(REPEATED_REQUESTS)` with code `REPEATED_REQUESTS`
- TTL-based automatic lock release

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String name() default "";                    // Idempotent key name
    long timeout() default 3;                    // Idempotent window (seconds)
    KeyResolver keyResolver() default KeyResolver.DEFAULT;
}
```

### 3. Distributed Lock — Lock4j Integration

**Location:** `com.develop.mvp.pk.framework.lock4j`

Uses `com.baomidou:lock4j-redisson-spring-boot-starter`.

**@Lock4j annotation:**
```java
@Lock4j(name = "order:pay:", keys = {"#orderId"}, expire = 30000, acquireTimeout = 3000)
public void pay(Long orderId) { ... }
```

**DefaultLockFailureStrategy**: throws `ServiceException` when lock acquisition fails.

**DevelopLock4jConfiguration:**
```java
@AutoConfiguration(before = LockAutoConfiguration.class)
@ConditionalOnClass(name = "com.baomidou.lock.annotation.Lock4j")
public class DevelopLock4jConfiguration {
    @Bean
    public DefaultLockFailureStrategy lockFailureStrategy() {
        return new DefaultLockFailureStrategy();
    }
}
```

### 4. API Signature — ApiSignatureAspect

**Location:** `com.develop.mvp.pk.framework.signature`

AOP aspect intercepting `@ApiSignature` annotated methods (for open API / callback endpoints).

**Flow:**
1. Client sends `timestamp + nonce + sign` in request headers
2. Server validates:
   - `timestamp` within allowed window (default 5 minutes)
   - `nonce` not used before (cached in Redis via `ApiSignatureRedisDAO`)
   - `sign` matches HMAC-SHA256 of sorted parameters with shared secret
3. Replay attack prevention: `nonce` entries expire after the time window

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSignature {
    long timeout() default 300;          // Signature validity (seconds)
}
```

## Configuration Properties

```yaml
develop:
  protection:
    rate-limiter:
      enabled: true                     # Enable rate limiter
    idempotent:
      enabled: true                     # Enable idempotency
    lock:
      enabled: true                     # Enable Lock4j
    api-signature:
      enabled: true                     # Enable API signature
      secret-key: your-secret-key       # Shared secret for HMAC-SHA256

# Lock4j global config
lock4j:
  acquire-timeout: 3000                 # Lock acquisition timeout (ms)
  expire: 30000                         # Lock expiry (ms)
  primary-executor: com.baomidou.lock.executor.RedissonLockExecutor
```

## Code Examples

```java
// Rate limiting — per-user, 10 requests per minute
@RateLimiter(name = "sendSms", count = 10, time = 60,
             keyResolver = RateLimiter.KeyResolver.USER)
@PostMapping("/send-sms")
public CommonResult<Boolean> sendSms(@RequestParam String mobile) { ... }

// Idempotency — prevent duplicate order submission
@Idempotent(name = "createOrder", timeout = 5,
            keyResolver = Idempotent.KeyResolver.USER)
@PostMapping("/create")
public CommonResult<Long> create(@Valid @RequestBody OrderCreateReqVO reqVO) { ... }

// Distributed lock — prevent concurrent account deduction
@Lock4j(name = "account:deduct:", keys = {"#accountId"}, expire = 10000)
public void deduct(Long accountId, Integer amount) {
    accountService.deduct(accountId, amount);
}

// API signature — open API callback validation
@ApiSignature(timeout = 600)
@PostMapping("/open/callback")
public CommonResult<String> callback(@RequestBody CallbackReqVO reqVO) { ... }
```

## Edge Cases and Error States

| Scenario | Behavior | Mitigation |
|---|---|---|
| Redis unavailable during rate limiting | `RateLimiterAspect` throws exception | Configurable fallback (deny or allow via Sentinel) |
| Redis connection timeout during idempotency check | `IdempotentAspect` fails open (allows request) | May cause duplicate — ensure downstream handles it |
| Lock acquisition timeout | `DefaultLockFailureStrategy` throws `ServiceException` (lock contention) | Check `acquireTimeout` setting relative to expected hold time |
| Nonce cache full (API Signature) | TTL auto-cleans old entries | Default 5-min window, adjust via `@ApiSignature.timeout()` |
| Distributed lock expired while holding | Lock released prematurely, concurrent access possible | Set `expire` to 5-10x expected execution time |

## Performance Guidance

| Feature | Performance Impact | Recommendation |
|---|---|---|
| Rate limiter (RRateLimiter) | 1 Redis call per check | ~1ms per check, negligible |
| Idempotent (SET NX) | 1 Redis call | Same as above |
| Lock4j (Redisson lock) | 1-3 Redis calls + watchdog | Minimize lock hold time |
| API Signature (HMAC + Redis nonce) | 1 HMAC + 1 Redis call | Cache nonce checking in high-QPS scenarios |

## 注意事项

- 限流和幂等都依赖 Redis，Redis 不可用时相关功能降级（根据 Sentinel 配置决定放行或拒绝）；生产环境务必部署 Redis 高可用（主从/集群）
- `@Idempotent` 的 `timeout` 建议设置为接口正常响应时间的 2-3 倍；过长影响正常重试（用户需等待超时），过短则无法拦截真正重复的请求
- `@Lock4j` 的 `expire` 是锁持有时间（毫秒），超过自动释放。务必设置合理的 `expire`（建议 5-10x 正常执行时间 + buffer），防止死锁
- `expire` 应大于接口最大执行时间；Redisson 的 watchdog 机制会自动续期，但注解方式使用 Lock4j 时 watchdog 行为取决于底层实现
- API 签名需调用方和服务端约定相同的 `secret-key`、签名算法（HMAC-SHA256）和参数排序规则（字典序）
- nonce 防重放依赖 Redis，`ApiSignatureRedisDAO` 使用独立 Key 前缀避免与其他缓存冲突
- `DevelopIdempotentConfiguration` 和 `DevelopRateLimiterConfiguration` 是 `Configuration` 类型（非 `AutoConfiguration`），`DevelopLock4jConfiguration` 使用 `@ConditionalOnClass` 确保 Lock4j 在类路径上时才激活
