---
name: configuration
description: Smart Cloud configuration system — YAML profiles, property sources, auto-config registration, custom properties, and EnvironmentPostProcessor
type: project
---

# Configuration

## Overview

Smart Cloud 使用 Spring Boot 标准配置体系，结合 Nacos 配置中心（微服务模式），通过多 profile YAML 文件管理不同环境的配置。框架通过 `AutoConfiguration.imports`、`spring.factories` 和 `@ConfigurationProperties` 实现自动配置。

## 1. Configuration File Locations

**Monolithic Mode (develop-server):**
```
develop-server/src/main/resources/
  application.yaml          # 主配置（跨环境默认值 + 多文档结构）
  application-local.yaml    # 本地开发环境
  logback-spring.xml        # 日志配置
```

**Microservices Mode (each -server module):**
```
develop-module-{name}/develop-module-{name}-server/src/main/resources/
  application.yaml          # 模块独立配置
  application-dev.yaml      # 模块开发环境配置
```

## 2. Profile System

**Activation:**
```yaml
# application.yaml default: activates local profile
spring:
  profiles:
    active: local
```

**Profile Responsibilities:**

| Profile | File | Purpose | Key Settings |
|---|---|---|---|
| local | application-local.yaml | Local development | Local DB, mock-enable=true, Nacos disabled, RPC excluded, AI vector DB excluded, captcha disabled, access-log disabled |
| dev | application-dev.yaml | Dev/Test environment | Dev DB, Nacos enabled, RPC enabled |
| (prod) | (No default file) | Production | Set via `--spring.profiles.active=prod` at startup |

## 3. Multi-Document YAML Structure

`application.yaml` uses `---` separator for multi-document YAML:

```yaml
# ===== Segment 1: Default configuration =====
spring:
  application:
    name: develop-server
  profiles:
    active: local
  main:
    allow-circular-references: true
    allow-bean-definition-overriding: true
  servlet:
    multipart:
      max-file-size: 16MB
      max-request-size: 32MB
  jackson:
    serialization:
      write-dates-as-timestamps: true
      write-date-timestamps-as-nanoseconds: false
      write-durations-as-timestamps: true
      fail-on-empty-beans: false
  cache:
    type: REDIS
    redis:
      time-to-live: 1h

server:
  servlet:
    encoding:
      enabled: true
      charset: UTF-8
      force: true

--- #################### Cloud Disabled ####################
spring:
  cloud:
    nacos:
      discovery:
        enabled: false
      config:
        enabled: false

--- #################### API Docs (SpringDoc + Knife4j) ####################
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui
  default-flat-param-object: true

knife4j:
  enable: true
  setting:
    language: zh_cn

--- #################### Flowable Workflow ####################
flowable:
  database-schema-update: true
  db-history-used: true
  check-process-definitions: false
  history-level: audit

--- #################### MyBatis Plus ####################
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: NONE  # Smart mode — auto-adapts to database type
      logic-delete-value: 1
      logic-not-delete-value: 0
    banner: false
  type-aliases-package: ${develop.info.base-package}.module.*.dal.dataobject
  encryptor:
    password: XDV71a+xqStEA3WH

mybatis-plus-join:
  banner: false
  sub-table-logic: true
```

## 4. Key Configuration Sections

### 4.1 DataSource Configuration (application-local.yaml)

```yaml
spring:
  datasource:
    druid:
      web-stat-filter:
        enabled: true
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
      filter:
        stat:
          enabled: true
          log-slow-sql: true
          slow-sql-millis: 100
          merge-sql: true
        wall:
          config:
            multi-statement-allow: true
    dynamic:
      druid:
        initial-size: 1
        min-idle: 1
        max-active: 20
        max-wait: 60000
        time-between-eviction-runs-millis: 60000
        min-evictable-idle-time-millis: 600000
        max-evictable-idle-time-millis: 1800000
        validation-query: SELECT 1 FROM DUAL
        test-while-idle: true
      primary: master
      datasource:
        master:
          url: jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true
          username: root
          password: 123456
        slave:
          lazy: true
          url: jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true&nullCatalogMeansCurrent=true
          username: root
          password: 123456
```

Uses `dynamic-datasource-spring-boot-starter` (baomidou) for multi-datasource:
- `master` primary (default `@DS("master")`)
- `slave` read-only replica (lazy loading)
- Commented alternatives: PostgreSQL, Oracle, SQL Server, DM, Kingbase, OpenGauss

### 4.2 Redis Configuration

```yaml
spring:
  cache:
    type: REDIS
    redis:
      time-to-live: 1h

--- # application-local.yaml Redis
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password:      # Empty by default
      database: 0
      timeout: 10s
      lettuce:
        pool:
          min-idle: 0
          max-idle: 8
          max-active: 16
          max-wait: -1ms
```

### 4.3 Nacos Configuration (Microservices Mode)

```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: false      # Set to true in microservices mode
        server-addr: 127.0.0.1:8848
        namespace: develop
      config:
        enabled: false      # Set to true in microservices mode
        server-addr: 127.0.0.1:8848
        namespace: develop
        group: DEFAULT_GROUP
        file-extension: yaml
```

### 4.4 XXL-Job Configuration

```yaml
xxl:
  job:
    enabled: false          # Disabled by default
    access-token: default-token
    admin:
      addresses: http://127.0.0.1:9090/xxl-job-admin
    executor:
      appname: ${spring.application.name}
      port: 9999
      log-retention-days: 30
```

### 4.5 MQ Configuration

```yaml
# RocketMQ
rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: ${spring.application.name}-producer-group

# RabbitMQ
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest

# Kafka
spring:
  kafka:
    bootstrap-servers: 127.0.0.1:9092
    consumer:
      group-id: ${spring.application.name}
```

### 4.6 Lock4j Distributed Lock

```yaml
lock4j:
  acquire-timeout: 3000    # Lock acquisition timeout (ms)
  expire: 30000            # Lock expiry (ms)
  primary-executor: com.baomidou.lock.executor.RedissonLockExecutor
```

## 5. Custom develop.* Properties

The project defines configuration properties with the `develop.` prefix:

| Property Prefix | Class | Purpose |
|---|---|---|
| `develop.web` | `WebProperties` | API prefixes: `/admin-api`, `/app-api` |
| `develop.security` | `SecurityProperties` | Token header name, token param name, mock-enable, mock-secret, permit-all-urls, passwordEncoderLength |
| `develop.tenant` | `TenantProperties` | enable, ignore-urls, ignore-visit-urls, ignore-tables, ignore-caches |
| `develop.xss` | `XssProperties` | enable, exclude-urls |
| `develop.api-encrypt` | `ApiEncryptProperties` | enable |
| `develop.access-log` | (DevelopApiLogAutoConfiguration) | enable (conditional) |
| `develop.tracer` | `TracerProperties` | enable |
| `develop.websocket` | `WebSocketProperties` | path, senderType, enable |
| `develop.dict` | (DevelopDictAutoConfiguration) | enable |
| `develop.data-permission` | (DevelopDataPermissionAutoConfiguration) | enable |
| `develop.cache` | `DevelopCacheProperties` | redis-scan-batch-size |
| `develop.protection` | (Multiple config classes) | rate-limiter.enabled, idempotent.enabled, lock.enabled, api-signature.enabled |

**Key properties detailed:**

```java
@ConfigurationProperties(prefix = "develop.security")
public class SecurityProperties {
    private String tokenHeader = "Authorization";          // HTTP header for token
    private String tokenParameter = "token";               // HTTP param for token (WebSocket fallback)
    private Boolean mockEnable = false;                    // Mock mode for local dev
    private String mockSecret = "test";                    // Mock secret key
    private List<String> permitAllUrls = Collections.emptyList(); // Permit-all URL list
    private Integer passwordEncoderLength = 4;             // BCrypt encoder strength
}
```

```java
@ConfigurationProperties(prefix = "develop.tenant")
public class TenantProperties {
    private Boolean enable = true;                         // Global enable
    private Set<String> ignoreUrls = new HashSet<>();      // URLs skipping tenant validation
    private Set<String> ignoreVisitUrls = Collections.emptySet(); // Cross-tenant visit skip
    private Set<String> ignoreTables = Collections.emptySet();    // Tables skipping tenant filter
    private Set<String> ignoreCaches = Collections.emptySet();    // Caches skipping tenant prefix
}
```

**application.yaml usage:**
```yaml
develop:
  info:
    base-package: com.develop.mvp.pk
  security:
    mock-enable: true           # Local dev only
    token-header: Authorization
    permit-all-urls:
      - /app-api/**
      - /actuator/health
  tenant:
    enable: true
    ignore-urls:
      - /rpc-api/**
      - /develop-doc/**
  websocket:
    enable: true
    path: /ws
    sender-type: redis
```

## 6. Lombok Configuration (lombok.config)

```properties
config.stopBubbling = true
lombok.tostring.callsuper=CALL
lombok.equalsandhashcode.callsuper=CALL
lombok.accessors.chain=true
```

- `chain=true`: Enable setter chaining (`obj.setName("x").setAge(18)`)
- `callsuper=CALL`: toString(), equals(), hashCode() include parent fields

## 7. @ConfigurationProperties Registration

**Method 1 — @ConfigurationPropertiesScan** (at application entry class):
```java
@ConfigurationPropertiesScan("com.develop.mvp.pk.framework")
public class SystemServerApplication { ... }
```

**Method 2 — @EnableConfigurationProperties** (at auto-config class):
```java
@EnableConfigurationProperties(SecurityProperties.class)
@AutoConfiguration
public class DevelopSecurityAutoConfiguration { ... }
```

**Method 3 — @ConfigurationProperties + @Component** (not recommended for framework components).

## 8. AutoConfiguration Registration

Framework starters register via Spring Boot 3.x standard mechanism:

**File:** `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

```
# develop-spring-boot-starter-web (8 auto-configs)
com.develop.mvp.pk.framework.apilog.config.DevelopApiLogAutoConfiguration
com.develop.mvp.pk.framework.jackson.config.DevelopJacksonAutoConfiguration
com.develop.mvp.pk.framework.swagger.config.DevelopSwaggerAutoConfiguration
com.develop.mvp.pk.framework.web.config.DevelopWebAutoConfiguration
com.develop.mvp.pk.framework.apilog.config.DevelopApiLogRpcAutoConfiguration
com.develop.mvp.pk.framework.xss.config.DevelopXssAutoConfiguration
com.develop.mvp.pk.framework.banner.config.DevelopBannerAutoConfiguration
com.develop.mvp.pk.framework.encrypt.config.DevelopApiEncryptAutoConfiguration

# develop-spring-boot-starter-security (5 auto-configs)
com.develop.mvp.pk.framework.security.config.DevelopSecurityRpcAutoConfiguration
com.develop.mvp.pk.framework.security.config.DevelopSecurityAutoConfiguration
com.develop.mvp.pk.framework.security.config.DevelopWebSecurityConfigurerAdapter
com.develop.mvp.pk.framework.operatelog.config.DevelopOperateLogConfiguration
com.develop.mvp.pk.framework.operatelog.config.DevelopOperateLogRpcAutoConfiguration

# develop-spring-boot-starter-mybatis (3 auto-configs)
com.develop.mvp.pk.framework.datasource.config.DevelopDataSourceAutoConfiguration
com.develop.mvp.pk.framework.mybatis.config.DevelopMybatisAutoConfiguration
com.develop.mvp.pk.framework.translate.config.DevelopTranslateAutoConfiguration

# develop-spring-boot-starter-redis (2 auto-configs)
com.develop.mvp.pk.framework.redis.config.DevelopRedisAutoConfiguration
com.develop.mvp.pk.framework.redis.config.DevelopCacheAutoConfiguration

# develop-spring-boot-starter-biz-tenant (2 auto-configs)
com.develop.mvp.pk.framework.tenant.config.DevelopTenantRpcAutoConfiguration
com.develop.mvp.pk.framework.tenant.config.DevelopTenantAutoConfiguration

# develop-spring-boot-starter-biz-data-permission (3 auto-configs)
com.develop.mvp.pk.framework.datapermission.config.DevelopDataPermissionAutoConfiguration
com.develop.mvp.pk.framework.datapermission.config.DevelopDeptDataPermissionAutoConfiguration
com.develop.mvp.pk.framework.datapermission.config.DevelopDataPermissionRpcAutoConfiguration

# develop-spring-boot-starter-mq (3 auto-configs)
com.develop.mvp.pk.framework.mq.redis.config.DevelopRedisMQProducerAutoConfiguration
com.develop.mvp.pk.framework.mq.redis.config.DevelopRedisMQConsumerAutoConfiguration
com.develop.mvp.pk.framework.mq.rabbitmq.config.DevelopRabbitMQAutoConfiguration

# develop-spring-boot-starter-job (2 auto-configs)
com.develop.mvp.pk.framework.quartz.config.DevelopXxlJobAutoConfiguration
com.develop.mvp.pk.framework.quartz.config.DevelopAsyncAutoConfiguration

# develop-spring-boot-starter-websocket (1 auto-config)
com.develop.mvp.pk.framework.websocket.config.DevelopWebSocketAutoConfiguration

# develop-spring-boot-starter-monitor (2 auto-configs)
com.develop.mvp.pk.framework.tracer.config.DevelopTracerAutoConfiguration
com.develop.mvp.pk.framework.tracer.config.DevelopMetricsAutoConfiguration

# develop-spring-boot-starter-protection (4 auto-configs)
com.develop.mvp.pk.framework.idempotent.config.DevelopIdempotentConfiguration
com.develop.mvp.pk.framework.lock4j.config.DevelopLock4jConfiguration
com.develop.mvp.pk.framework.ratelimiter.config.DevelopRateLimiterConfiguration
com.develop.mvp.pk.framework.signature.config.DevelopApiSignatureAutoConfiguration

# develop-spring-boot-starter-excel (2 auto-configs)
com.develop.mvp.pk.framework.dict.config.DevelopDictRpcAutoConfiguration
com.develop.mvp.pk.framework.dict.config.DevelopDictAutoConfiguration

# develop-spring-boot-starter-env (2 auto-configs)
com.develop.mvp.pk.framework.env.config.DevelopEnvWebAutoConfiguration
com.develop.mvp.pk.framework.env.config.DevelopEnvRpcAutoConfiguration
```

**Monolithic Mode — Excluded RPC AutoConfigurations** (in application-local.yaml):
```yaml
spring:
  autoconfigure:
    exclude:
      - com.develop.mvp.pk.framework.security.config.DevelopSecurityRpcAutoConfiguration
      - com.develop.mvp.pk.framework.operatelog.config.DevelopOperateLogRpcAutoConfiguration
      - com.develop.mvp.pk.framework.datapermission.config.DevelopDataPermissionRpcAutoConfiguration
      - com.develop.mvp.pk.framework.dict.config.DevelopDictRpcAutoConfiguration
      - com.develop.mvp.pk.framework.tenant.config.DevelopTenantRpcAutoConfiguration
      - com.develop.mvp.pk.framework.env.config.DevelopEnvRpcAutoConfiguration
      - com.develop.mvp.pk.framework.apilog.config.DevelopApiLogRpcAutoConfiguration
      - org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration
      - org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration
```

## 9. EnvironmentPostProcessor

Registered via `META-INF/spring.factories`:
```properties
org.springframework.boot.env.EnvironmentPostProcessor=\
com.develop.mvp.pk.framework.mybatis.config.IdTypeEnvironmentPostProcessor,\
com.develop.mvp.pk.framework.env.config.EnvEnvironmentPostProcessor
```

- `IdTypeEnvironmentPostProcessor`: Auto-detects database type and sets MyBatis Plus ID strategy (AUTO for MySQL, INPUT for Oracle/PostgreSQL/Kingbase/DM).
- `EnvEnvironmentPostProcessor`: Sets environment tag for tag-based routing (env isolation).

## 10. Logging Configuration (logback-spring.xml)

Located at `develop-server/src/main/resources/logback-spring.xml`:
- Console output with ANSI color highlighting
- File rolling policy (size + time-based)
- Per-module log level configuration
- Spring Profile adaptation (dev vs. prod)
- SkyWalking traceId injection via `[%traceId]` pattern (when agent present)

## 11. Configuration Loading Order

```
Application Startup
    |
    v
EnvironmentPostProcessor (spring.factories)
    |   - IdTypeEnvironmentPostProcessor (DB dialect auto-detect)
    |   - EnvEnvironmentPostProcessor (tag-based routing)
    v
application.yaml (main config, multi-document)
    |   - Default values, infrastructure configs
    v
application-{profile}.yaml (profile-specific)
    |   - Override defaults for local/dev/prod
    v
Nacos Config (microservices mode, dynamic refresh)
    |   - Remote config overrides local
    v
@ConfigurationProperties binding to POJOs
    |   - Type-safe property access
    v
Bean Creation (AutoConfiguration)
```

## 12. Config Change Propagation

| Change | Propagation | Zero-Downtime |
|---|---|---|
| application.yaml | Restart required | No |
| application-{profile}.yaml | Restart required | No |
| Nacos Config | Dynamic refresh | Yes |
| EnvironmentPostProcessor | Restart required | No |
| @ConfigurationProperties | Depends on `@RefreshScope` | Yes (if annotated) |

## 注意事项

- application.yaml 中 `spring.cloud.nacos.discovery.enabled: false` 和 `spring.cloud.nacos.config.enabled: false` 在单独的 YAML 文档段定义（非第一个文档段），全局默认禁用 Nacos；微服务模式下各模块需在各自的 `application-dev.yaml` 中重新启用
- `mybatis-plus.global-config.db-config.id-type: NONE` 使用智能模式，由 `IdTypeEnvironmentPostProcessor` 自动检测当前数据源类型并切换主键策略
- Lock4j 默认使用 Redisson 作为锁执行器，确保 `develop-spring-boot-starter-redis` 在类路径上
- 排除 RPC AutoConfiguration 时需检查 `application-local.yaml` 中列出的全部 7 个 RPC exclude + 2 个 AI 向量库 exclude（Qdrant, Milvus），单体模式下避免 `@FeignClient` 相关的自动配置冲突
- `develop.info.base-package` 属性作为包扫描基础路径，在 `@SpringBootApplication` 的 `scanBasePackages`、MyBatis `type-aliases-package` 和 `@MapperScan` 三处被引用
- Nacos `@RefreshScope` 动态刷新对 `@ConfigurationProperties` 类默认不生效，需显式在类上添加 `@RefreshScope` 注解
