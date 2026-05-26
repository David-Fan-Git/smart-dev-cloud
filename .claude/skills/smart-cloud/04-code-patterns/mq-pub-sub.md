---
name: mq-pub-sub
description: MQ publish/subscribe — Redis Pub/Sub and Stream modes, RocketMQ/RabbitMQ/Kafka switching, retry, dead letter, exactly-once patterns
type: project
---

# MQ 发布订阅模式

## 概述

通过 `develop-spring-boot-starter-mq` 抽象层统一实现。默认使用 **Redis Pub/Sub**（零额外中间件）。切换 RocketMQ / RabbitMQ / Kafka 只需修改配置。支持两种消息模式：**Channel**（Pub/Sub，默认）和 **Stream**（可靠消息，Redis 5.0+）。

## 1. 定义消息体

```java
package com.develop.mvp.pk.module.{module}.mq.message;

import com.develop.mvp.pk.framework.mq.core.message.AbstractMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {领域} 消息（Channel 模式 — Pub/Sub）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class {Domain}Message extends AbstractMessage {

    private Long id;
    private String name;
    private Integer status;
}
```

## 2. 生产者

```java
package com.develop.mvp.pk.module.{module}.service.{domain}.impl;

import com.develop.mvp.pk.framework.mq.redis.core.RedisMQTemplate;
import com.develop.mvp.pk.module.{module}.mq.message.{Domain}Message;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
@Validated
public class {Domain}ServiceImpl implements {Domain}Service {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    @Override
    public Long create{Domain}({Domain}SaveReqVO createReqVO) {
        // 1. 业务逻辑（写入数据库）
        {Domain}DO entity = ...;
        {domain}Mapper.insert(entity);

        // 2. 发送消息（业务完成后发送）
        {Domain}Message message = new {Domain}Message();
        message.setId(entity.getId());
        message.setName(entity.getName());
        redisMQTemplate.send(message);

        return entity.getId();
    }
}
```

## 3. 消费者（Channel 模式 — Pub/Sub）

```java
package com.develop.mvp.pk.module.{module}.mq.consumer;

import com.develop.mvp.pk.framework.mq.core.pubsub.AbstractChannelMessageListener;
import com.develop.mvp.pk.module.{module}.mq.message.{Domain}Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class {Domain}Consumer extends AbstractChannelMessageListener<{Domain}Message> {

    @Override
    public void onMessage({Domain}Message message) {
        log.info("[onMessage][收到 {领域} 消息：{}]", message.getId());
        try {
            // 业务处理：如更新缓存、发送通知
            processMessage(message);
        } catch (Exception e) {
            log.error("[onMessage][处理失败：{}]", message.getId(), e);
            // Channel 模式：消息已丢失（Pub/Sub 无重试机制）
            // 如需可靠投递，使用 Stream 模式
        }
    }

    private void processMessage({Domain}Message message) {
        // 业务处理逻辑
    }
}
```

## 4. Stream 模式（可靠消息，推荐用于重要场景）

```java
// Stream 消息体
@Data
@EqualsAndHashCode(callSuper = true)
public class {Domain}StreamMessage extends AbstractStreamMessage {

    private Long id;
    private String name;

    @Override
    public String getStreamKey() {
        return "stream:{domain}"; // Redis Stream Key
    }
}

// Stream 消费者（支持 ACK 和消费者组）
@Component
@Slf4j
public class {Domain}StreamConsumer extends AbstractStreamMessageListener<{Domain}StreamMessage> {

    @Override
    public void onMessage({Domain}StreamMessage message) {
        log.info("[onMessage][收到 {领域} Stream 消息：{}]", message.getId());
        // Stream 模式：消费成功后自动 ACK
        // 未 ACK 的消息会被消费者组重新投递
    }
}
```

## 5. 消息重试策略

```java
// Stream 模式下，处理失败可抛出异常触发重试
@Override
public void onMessage({Domain}StreamMessage message) {
    try {
        processMessage(message);
    } catch (Exception e) {
        log.error("[onMessage][处理失败：{}]", message.getId(), e);
        // 不抛出异常 = 消费成功（消息被 ACK）
        // 抛出异常 = 消费失败（消息保留在 PEL 中，下次重新投递）
        throw new RuntimeException("处理失败，需要重试", e);
    }
}

// 手动确认（适用于需要精细控制的场景）
@Override
public void onMessage({Domain}StreamMessage message) {
    boolean success = tryProcess(message);
    // 处理成功 → 自动 ACK
    // 处理失败 → 不 ACK，消息留在 PEL 待下次投递
}
```

## 6. 配置切换

```yaml
# application.yaml — Spring Boot 自动配置

# === Redis（默认）===
spring:
  redis:
    host: ${redis.host:127.0.0.1}
    port: ${redis.port:6379}

# === RocketMQ（取消注释并引入 rocketmq-spring-boot-starter）===
# rocketmq:
#   name-server: 127.0.0.1:9876
#   producer:
#     group: ${spring.application.name}

# === RabbitMQ ===
# spring:
#   rabbitmq:
#     host: 127.0.0.1
#     port: 5672

# === Kafka ===
# spring:
#   kafka:
#     bootstrap-servers: 127.0.0.1:9092
#     consumer:
#       properties:
#         spring.json.trusted.packages: com.develop.mvp.pk
```

## 消息模式对比

| 模式 | 消息可靠性 | 消费者组 | ACK 机制 | 适用场景 |
|---|---|---|---|---|
| Channel (Pub/Sub) | 无（丢失不重试） | 无 | 无 | 缓存刷新、非关键通知 |
| Stream | 高（PEL 保证） | 支持 | 自动/手动 ACK | 订单状态变更、积分变更 |

## 关键点

1. **消息体必须继承 `AbstractMessage`**（Channel）或 `AbstractStreamMessage`（Stream）
2. **消费者必须 `@Component`** 注册为 Spring Bean
3. **`redisMQTemplate.send(message)`** — 发送消息的唯入口，自动序列化为 JSON
4. **默认使用 Redis Pub/Sub** — 零额外依赖
5. **Stream 模式（Redis 5.0+）** 保证可靠投递，消费者组支持 ACK 和重试
6. **租户上下文自动传播** — Redis Pub/Sub 消费者携带生产者租户上下文
7. **发送时机**：业务数据入库成功后发送，不发送失败的业务

## 常见错误

- 消息体未继承 `AbstractMessage` — 框架不识别，不会发送
- 消费者未加 `@Component` — 不注册监听
- `onMessage()` 异常未捕获 — Channel 模式消息丢失，Stream 模式无限重试
- 修改消息体字段后旧消费者仍在运行 — 反序列化失败。Stream 模式建议加版本号
- 在事务中未提交就发送消息 — 事务回滚但消息已发出
- 重要业务场景使用 Channel 模式而非 Stream 模式 — 消息可能丢失
