---
name: starter-mq
description: Message queue abstraction based on Redis Stream (primary) with Redis Pub/Sub and RabbitMQ support — message model, producer/consumer auto-config, and tenant propagation
type: project
---

# develop-spring-boot-starter-mq

## Overview

消息队列统一抽象模块。基于 **Redis Stream**（主推，支持消息持久化）、**Redis Pub/Sub**（向后兼容）和 **RabbitMQ** 三种后端实现。提供分层消息模型（Stream/Channel）、`RedisMQTemplate` 发送模板、消息拦截器链以及多租户透传。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **3 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.mq`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-mq AutoConfiguration.imports
com.develop.mvp.pk.framework.mq.redis.config.DevelopRedisMQProducerAutoConfiguration
com.develop.mvp.pk.framework.mq.redis.config.DevelopRedisMQConsumerAutoConfiguration
com.develop.mvp.pk.framework.mq.rabbitmq.config.DevelopRabbitMQAutoConfiguration
```

Note: Only Redis and RabbitMQ backends have auto-configuration classes. RocketMQ and Kafka are NOT registered via AutoConfiguration.imports (they require manual configuration if needed).

## Message Model Hierarchy

Redis 消息使用**双层抽象**: `AbstractRedisMessage` -> `AbstractRedisStreamMessage` / `AbstractRedisChannelMessage`。

### AbstractRedisMessage (Base)

```java
@Data
public abstract class AbstractRedisMessage {
    private Map<String, String> headers = new HashMap<>();

    public String getHeader(String key) { return headers.get(key); }
    public void addHeader(String key, String value) { headers.put(key, value); }
}
```

### AbstractRedisStreamMessage (推荐 — Redis Stream)

```java
public abstract class AbstractRedisStreamMessage extends AbstractRedisMessage {
    @JsonIgnore
    public String getStreamKey() {
        return getClass().getSimpleName();  // Default: class name as stream key
    }
}
```

Features:
- Redis Stream (Redis 5.0+) — **消息持久化**，Consumer Group 支持
- 消息不会丢失（持久化到 Redis RDB/AOF）
- 支持消费者组（多实例负载均衡）
- 支持待处理列表（Pending List）和消息重试

### AbstractRedisChannelMessage (Legacy — Redis Pub/Sub)

```java
public abstract class AbstractRedisChannelMessage extends AbstractRedisMessage {
    @JsonIgnore
    public String getChannel() {
        return getClass().getSimpleName();  // Default: class name as channel
    }
}
```

Caveats:
- Redis Pub/Sub — **消息不持久化**，丢失后无法恢复
- 广播模式：所有订阅者都收到消息
- 适用于非关键消息（如缓存刷新通知）

## Core Components

### 1. DevelopRedisMQProducerAutoConfiguration

- **RedisMQTemplate**: message sending template
  - `send(channel, message)` — sends to Redis Pub/Sub channel
  - `sendStream(streamKey, message)` — sends to Redis Stream
  - `stream` mode is the default and recommended for production
- Interceptor chain: supports `RedisMessageInterceptor` for pre-send/post-send hooks
  - `TenantRedisMessageInterceptor`: injects `tenantId` into message headers

### 2. DevelopRedisMQConsumerAutoConfiguration

- Registers `RedisMessageListenerContainer` for Pub/Sub listeners
- Registers stream consumers via scheduled polling
- Supports interceptors on consumer side for context restoration
- **RedisPendingMessageResendJob**: scheduled job to resend pending Stream messages (error handling)
- **RedisStreamMessageCleanupJob**: scheduled job to clean up consumed Stream messages (prevents unbounded growth)

### 3. DevelopRabbitMQAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.mq.rabbitmq.config`

- RabbitMQ connection factory and template configuration
- Queue, Exchange, Binding declarations
- Message listener container setup
- Tenant context propagation via message headers

### Listeners

```java
// Redis Pub/Sub listener
public abstract class AbstractRedisChannelMessageListener<T extends AbstractRedisChannelMessage> {
    public abstract void onMessage(T message);
}

// Redis Stream listener
public abstract class AbstractRedisStreamMessageListener<T extends AbstractRedisStreamMessage> {
    public abstract void onMessage(T message);
}
```

## Configuration

```yaml
# MQ type is implicit based on which auto-configs are registered.
# Redis is the default and primary implementation.
# RabbitMQ auto-config is registered alongside Redis.

develop:
  mq:
    redis:
      channel-prefix: smart_cloud:       # Channel name prefix (Pub/Sub)
      stream-prefix: smart_cloud:        # Stream key prefix (Stream)
      pending-message-resend-interval: 60000  # Pending message resend interval (ms)
      stream-message-cleanup-interval: 300000 # Stream cleanup interval (ms)

rabbitmq:
  host: localhost
  port: 5672
  virtual-host: /
  username: guest
  password: guest
```

## Code Examples

```java
// 1. Define message (Stream — Recommended)
@Data
public class OrderCreateMessage extends AbstractRedisStreamMessage {
    private Long orderId;
    private Long userId;
    private Integer amount;
}

// 2. Send message
@Autowired
private RedisMQTemplate mqTemplate;

public void createOrder(OrderCreateReqVO reqVO) {
    // ... business logic
    OrderCreateMessage message = new OrderCreateMessage();
    message.setOrderId(order.getId());
    message.setUserId(order.getUserId());
    message.setAmount(order.getAmount());
    mqTemplate.sendStream(message);  // Uses stream key = class simple name
}

// 3. Consume message
@Component
public class OrderCreateMessageListener extends AbstractRedisStreamMessageListener<OrderCreateMessage> {
    @Override
    public void onMessage(OrderCreateMessage message) {
        try {
            orderService.handleOrderCreated(message.getOrderId());
        } catch (Exception e) {
            log.error("[onMessage][orderId({}) 处理失败]", message.getOrderId(), e);
            // Exception prevents ACK — message will be retried via Pending List
        }
    }
}
```

## Edge Case Handling

| Scenario | Behavior | Mitigation |
|---|---|---|
| Redis Stream message processing fails | Exception thrown -> message stays in Pending List | `RedisPendingMessageResendJob` retries after interval |
| Consumer crashes mid-processing | Message stays in Pending List (not acknowledged) | Auto-resend after consumer restarts |
| Memory growth from accumulated Stream messages | Unbounded if not cleaned | `RedisStreamMessageCleanupJob` auto-trims consumed messages |
| Redis Pub/Sub message lost (no subscribers) | Message silently dropped | Use Stream mode for critical messages |
| Tenant context missing in async consumer | TenantContextHolder is empty | `TenantRedisMessageInterceptor` restores from message headers |

## 注意事项

- **Redis Pub/Sub 模式不支持消息持久化**，消息丢失后无法恢复。关键业务场景（如订单、支付）**必须使用 Redis Stream** 或 RabbitMQ
- 项目 MQ 模块当前仅注册了 **Redis**（Stream + Pub/Sub）和 **RabbitMQ** 的 AutoConfiguration。RocketMQ 和 Kafka 如需使用需手动创建 AutoConfiguration 并注册到 `AutoConfiguration.imports`
- 消息监听器方法必须使用 try-catch 包裹业务逻辑；异常将阻止消息确认（Stream 模式），消息会留在 Pending List 中等待 `RedisPendingMessageResendJob` 重试
- MQ 拦截器负责租户传播：发送方自动将 `tenantId` 注入消息头，消费方通过 `TenantRedisMessageInterceptor` 恢复上下文。Redis Stream 和 Pub/Sub 都支持
- 同一 Stream 的多个消费者实例会通过 Consumer Group 实现负载均衡（每条消息只被一个实例消费），不同于 Pub/Sub 的广播模式
- Redis Stream 是 Redis 5.0+ 功能，部署环境需确认 Redis 版本 >= 5.0
- `AbstractRedisStreamMessage` 和 `AbstractRedisChannelMessage` 的 `getStreamKey()`/`getChannel()` 默认返回类名（`getClass().getSimpleName()`），如需自定义 key 可覆写该方法
