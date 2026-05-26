# develop-framework Core Starter Comments Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `develop-framework` 核心 Starter 的主流程入口补充初学者友好的专业注释，帮助读者理解自动配置、请求链路、上下文传递、数据访问、缓存、MQ、RPC、租户和数据权限流程。

**Architecture:** 本计划只修改 Java 注释，不改变任何运行逻辑、方法签名、注解参数、Bean 名称或配置键。按 Starter 分组处理：先补自动配置入口，再补 Filter/AOP/Interceptor/Context/Template 等流程节点，最后通过 diff 和 Maven 编译验证行为不变。

**Tech Stack:** Java 17, Spring Boot 3.5.x, Spring Security, MyBatis Plus, Redis/Redisson, Spring Cloud OpenFeign, Maven.

---

## File Structure

### 设计依据

- 设计文档：`docs/superpowers/specs/2026-05-26-develop-framework-core-starter-comments-design.md`
- 注释策略：只解释流程、职责边界、触发时机、上下游协作和顺序/条件原因。
- 禁止事项：不重构、不改行为、不移动文件、不新增测试代码、不修改 README/SVG。

### 修改文件清单

#### Web Starter

- Modify: `develop-framework/develop-spring-boot-starter-web/src/main/java/com/develop/mvp/pk/framework/web/config/DevelopWebAutoConfiguration.java`
  - 说明 Web Starter 如何装配 MVC、异常、响应体、CORS 与过滤器。
- Modify: `develop-framework/develop-spring-boot-starter-web/src/main/java/com/develop/mvp/pk/framework/apilog/config/DevelopApiLogAutoConfiguration.java`
  - 说明 API 访问日志和错误日志链路如何进入请求处理流程。
- Modify: `develop-framework/develop-spring-boot-starter-web/src/main/java/com/develop/mvp/pk/framework/xss/config/DevelopXssAutoConfiguration.java`
  - 说明 XSS 条件装配、过滤器和请求包装器的作用顺序。
- Modify: `develop-framework/develop-spring-boot-starter-web/src/main/java/com/develop/mvp/pk/framework/encrypt/config/DevelopApiEncryptAutoConfiguration.java`
  - 说明 API 加解密过滤链和请求/响应包装器的入口位置。

#### Security Starter

- Modify: `develop-framework/develop-spring-boot-starter-security/src/main/java/com/develop/mvp/pk/framework/security/config/DevelopSecurityAutoConfiguration.java`
  - 说明安全链路主装配入口、Token 过滤器、异常处理器和 SecurityContext 策略。
- Modify: `develop-framework/develop-spring-boot-starter-security/src/main/java/com/develop/mvp/pk/framework/security/core/filter/TokenAuthenticationFilter.java`
  - 说明 Token 如何解析为登录态并写入 Spring Security 上下文。
- Modify: `develop-framework/develop-spring-boot-starter-security/src/main/java/com/develop/mvp/pk/framework/security/core/context/TransmittableThreadLocalSecurityContextHolderStrategy.java`
  - 说明登录上下文为什么要支持跨线程传递。
- Modify: `develop-framework/develop-spring-boot-starter-security/src/main/java/com/develop/mvp/pk/framework/security/config/DevelopSecurityRpcAutoConfiguration.java`
  - 说明 RPC 场景下登录用户信息如何透传。

#### MyBatis Starter

- Modify: `develop-framework/develop-spring-boot-starter-mybatis/src/main/java/com/develop/mvp/pk/framework/datasource/config/DevelopDataSourceAutoConfiguration.java`
  - 说明数据源、事务和 Druid 监控过滤器的启动装配边界。
- Modify: `develop-framework/develop-spring-boot-starter-mybatis/src/main/java/com/develop/mvp/pk/framework/mybatis/config/DevelopMybatisAutoConfiguration.java`
  - 说明 MyBatis Plus 插件、分页、类型处理器和 SQL 拦截链。
- Modify: `develop-framework/develop-spring-boot-starter-mybatis/src/main/java/com/develop/mvp/pk/framework/translate/config/DevelopTranslateAutoConfiguration.java`
  - 说明数据翻译能力如何挂接到框架层。

#### Redis Starter

- Modify: `develop-framework/develop-spring-boot-starter-redis/src/main/java/com/develop/mvp/pk/framework/redis/config/DevelopRedisAutoConfiguration.java`
  - 说明 RedisTemplate、序列化和 Redis 基础能力入口。
- Modify: `develop-framework/develop-spring-boot-starter-redis/src/main/java/com/develop/mvp/pk/framework/redis/config/DevelopCacheAutoConfiguration.java`
  - 说明 CacheManager、缓存过期策略和 Redis 缓存链路。

#### MQ Starter

- Modify: `develop-framework/develop-spring-boot-starter-mq/src/main/java/com/develop/mvp/pk/framework/mq/redis/config/DevelopRedisMQProducerAutoConfiguration.java`
  - 说明 Redis MQ 生产者和拦截器链如何装配。
- Modify: `develop-framework/develop-spring-boot-starter-mq/src/main/java/com/develop/mvp/pk/framework/mq/redis/config/DevelopRedisMQConsumerAutoConfiguration.java`
  - 说明 Redis MQ 消费端如何启动监听和消费。
- Modify: `develop-framework/develop-spring-boot-starter-mq/src/main/java/com/develop/mvp/pk/framework/mq/rabbitmq/config/DevelopRabbitMQAutoConfiguration.java`
  - 说明 RabbitMQ 分支和条件装配职责。
- Modify: `develop-framework/develop-spring-boot-starter-mq/src/main/java/com/develop/mvp/pk/framework/mq/redis/core/RedisMQTemplate.java`
  - 说明业务侧发消息入口、消息封装和发布动作。

#### RPC Starter

- Modify: `develop-framework/develop-spring-boot-starter-rpc/src/main/java/com/develop/mvp/pk/framework/rpc/package-info.java`
  - 说明 RPC Starter 的包级职责边界：只提供远程调用基础能力，不承载业务规则。

#### Tenant Starter

- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/config/DevelopTenantAutoConfiguration.java`
  - 说明租户总装配入口如何串联 Web、Security、DB、MQ、Redis、RPC。
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/context/TenantContextHolder.java`
  - 说明租户上下文的写入、读取和清理规则。
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/web/TenantContextWebFilter.java`
  - 说明 HTTP 请求进入时如何识别租户并放入上下文。
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/aop/TenantIgnoreAspect.java`
  - 说明 `@TenantIgnore` 如何临时关闭租户隔离。
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/db/TenantDatabaseInterceptor.java`
  - 说明数据库层租户隔离如何参与 SQL 处理。
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/rpc/TenantRequestInterceptor.java`
  - 说明 Feign/RPC 请求如何透传租户编号。

#### Data Permission Starter

- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/config/DevelopDataPermissionAutoConfiguration.java`
  - 说明数据权限总装配入口如何串联规则、注解 AOP 和 MyBatis 拦截器。
- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/core/aop/DataPermissionAnnotationAdvisor.java`
  - 说明哪些方法会被数据权限注解切面拦截。
- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/core/aop/DataPermissionAnnotationInterceptor.java`
  - 说明注解拦截后如何写入和恢复数据权限上下文。
- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/core/aop/DataPermissionContextHolder.java`
  - 说明数据权限上下文的 ThreadLocal 生命周期。
- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/core/rpc/DataPermissionRequestInterceptor.java`
  - 说明跨服务调用时如何透传数据权限标记。

---

### Task 1: Baseline Scan and Guardrails

**Files:**
- Read: `docs/superpowers/specs/2026-05-26-develop-framework-core-starter-comments-design.md`
- Inspect: all files listed in File Structure

- [ ] **Step 1: Confirm working tree context**

Run:

```bash
git status --short
```

Expected: existing user changes may be present. Do not stage or overwrite unrelated files.

- [ ] **Step 2: Read the design spec**

Read:

```text
docs/superpowers/specs/2026-05-26-develop-framework-core-starter-comments-design.md
```

Expected: confirm the work is comment-only and focused on core Starter flow entries.

- [ ] **Step 3: Inspect selected source files before editing**

Read each file listed in the File Structure section before editing it.

Expected: identify existing JavaDoc style, existing comments, bean names, method signatures, annotations, and control-flow ordering.

- [ ] **Step 4: Apply comment-only rule**

Before each edit, check that the intended change is one of:

```text
- class-level JavaDoc
- method-level JavaDoc
- short inline comment explaining non-obvious ordering, context propagation, or condition
```

Expected: no code statements, annotations, imports, signatures, configuration keys, Bean names, or logic branches are changed.

### Task 2: Web Starter Flow Comments

**Files:**
- Modify: `develop-framework/develop-spring-boot-starter-web/src/main/java/com/develop/mvp/pk/framework/web/config/DevelopWebAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-web/src/main/java/com/develop/mvp/pk/framework/apilog/config/DevelopApiLogAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-web/src/main/java/com/develop/mvp/pk/framework/xss/config/DevelopXssAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-web/src/main/java/com/develop/mvp/pk/framework/encrypt/config/DevelopApiEncryptAutoConfiguration.java`

- [ ] **Step 1: Read Web Starter files**

Read the four Web files listed above.

Expected: understand which beans register MVC config, filters, interceptors, wrappers, CORS, exception handlers, access logs, XSS and encryption behavior.

- [ ] **Step 2: Add class-level JavaDoc for Web auto-configuration entries**

Add JavaDoc that explains each class in this style:

```java
/**
 * Web 基础能力的自动配置入口。
 *
 * <p>初学者可以把这个类看作 HTTP 请求进入业务 Controller 前的“装配清单”：
 * 它把 MVC 扩展、全局异常、统一返回、跨域和通用过滤器注册到 Spring 容器中。
 * 这些 Bean 是否生效取决于 Spring Boot 的自动配置条件和调用方是否引入本 Starter。</p>
 */
```

Expected: wording must match actual class responsibility after reading the file; do not copy this exact text to unrelated classes if responsibilities differ.

- [ ] **Step 3: Add method-level comments for filter/interceptor registration methods**

For methods that register `FilterRegistrationBean`, `Interceptor`, wrapper filter, CORS or exception components, add concise JavaDoc like:

```java
/**
 * 注册请求链路中的通用过滤器。
 *
 * <p>过滤器运行在 Controller 之前，适合处理请求包装、跨域、日志或安全前置逻辑。
 * 如果这里调整顺序，可能影响后续 Starter 读取请求体、请求头或上下文的时机。</p>
 */
```

Expected: explain trigger position and ordering risk only where relevant.

- [ ] **Step 4: Check Web Starter diff**

Run:

```bash
git diff -- develop-framework/develop-spring-boot-starter-web
```

Expected: diff contains only comment changes in the four selected files.

### Task 3: Security Starter Flow Comments

**Files:**
- Modify: `develop-framework/develop-spring-boot-starter-security/src/main/java/com/develop/mvp/pk/framework/security/config/DevelopSecurityAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-security/src/main/java/com/develop/mvp/pk/framework/security/core/filter/TokenAuthenticationFilter.java`
- Modify: `develop-framework/develop-spring-boot-starter-security/src/main/java/com/develop/mvp/pk/framework/security/core/context/TransmittableThreadLocalSecurityContextHolderStrategy.java`
- Modify: `develop-framework/develop-spring-boot-starter-security/src/main/java/com/develop/mvp/pk/framework/security/config/DevelopSecurityRpcAutoConfiguration.java`

- [ ] **Step 1: Read Security Starter files**

Read the four Security files listed above.

Expected: understand authentication filter registration, token checking, login user context, security exception handlers and RPC propagation.

- [ ] **Step 2: Add class-level JavaDoc for authentication flow**

Use class-level JavaDoc to explain:

```text
- DevelopSecurityAutoConfiguration: security chain assembly point
- TokenAuthenticationFilter: per-request token parsing point
- TransmittableThreadLocalSecurityContextHolderStrategy: cross-thread security context strategy
- DevelopSecurityRpcAutoConfiguration: remote-call user context propagation point
```

Expected: comments show “请求携带 token → Filter 校验 → SecurityContext 保存登录态 → 业务代码读取登录用户 → RPC 可透传必要身份信息”。

- [ ] **Step 3: Add method comments for token parsing and context lifecycle**

For the token filter and context strategy methods, explain when context is created, read, replaced or cleared.

Example comment pattern:

```java
/**
 * 每个 HTTP 请求都会先尝试从请求头解析访问令牌。
 *
 * <p>解析成功后，登录用户会写入 Spring Security 上下文；后续 Controller、权限判断和操作日志
 * 都通过同一个上下文读取当前用户。解析失败不能留下半初始化状态，否则会污染同线程后续请求。</p>
 */
```

Expected: comments emphasize lifecycle and cleanup without changing filter behavior.

- [ ] **Step 4: Check Security Starter diff**

Run:

```bash
git diff -- develop-framework/develop-spring-boot-starter-security
```

Expected: diff contains only comment changes in the four selected files.

### Task 4: MyBatis and Redis Starter Flow Comments

**Files:**
- Modify: `develop-framework/develop-spring-boot-starter-mybatis/src/main/java/com/develop/mvp/pk/framework/datasource/config/DevelopDataSourceAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-mybatis/src/main/java/com/develop/mvp/pk/framework/mybatis/config/DevelopMybatisAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-mybatis/src/main/java/com/develop/mvp/pk/framework/translate/config/DevelopTranslateAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-redis/src/main/java/com/develop/mvp/pk/framework/redis/config/DevelopRedisAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-redis/src/main/java/com/develop/mvp/pk/framework/redis/config/DevelopCacheAutoConfiguration.java`

- [ ] **Step 1: Read MyBatis and Redis files**

Read the five files listed above.

Expected: identify data source initialization, MyBatis interceptor chain, data translation hook, RedisTemplate setup and CacheManager setup.

- [ ] **Step 2: Add MyBatis class and method comments**

Add comments explaining:

```text
- DataSource auto-configuration is the database connection and transaction foundation.
- MyBatis auto-configuration is the SQL execution extension point.
- Interceptor order matters because tenant/data-permission/pagination plugins may all participate in SQL processing.
- Translation auto-configuration enriches returned data but should not own business rules.
```

Expected: comments help beginners understand “业务 Mapper 调用 → MyBatis 插件链 → 数据库”。

- [ ] **Step 3: Add Redis and Cache comments**

Add comments explaining:

```text
- RedisTemplate is the low-level Redis operation entry.
- Serializer configuration determines how Java objects are written to Redis.
- CacheManager is the Spring Cache abstraction entry; business code usually sees @Cacheable, not Redis commands directly.
- Expiration/default cache behavior belongs to the framework layer, while cache usage decisions belong to business modules.
```

Expected: comments distinguish Redis operations from Spring Cache abstraction.

- [ ] **Step 4: Check MyBatis and Redis diff**

Run:

```bash
git diff -- develop-framework/develop-spring-boot-starter-mybatis develop-framework/develop-spring-boot-starter-redis
```

Expected: diff contains only comment changes in the five selected files.

### Task 5: MQ and RPC Starter Flow Comments

**Files:**
- Modify: `develop-framework/develop-spring-boot-starter-mq/src/main/java/com/develop/mvp/pk/framework/mq/redis/config/DevelopRedisMQProducerAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-mq/src/main/java/com/develop/mvp/pk/framework/mq/redis/config/DevelopRedisMQConsumerAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-mq/src/main/java/com/develop/mvp/pk/framework/mq/rabbitmq/config/DevelopRabbitMQAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-mq/src/main/java/com/develop/mvp/pk/framework/mq/redis/core/RedisMQTemplate.java`
- Modify: `develop-framework/develop-spring-boot-starter-rpc/src/main/java/com/develop/mvp/pk/framework/rpc/package-info.java`

- [ ] **Step 1: Read MQ and RPC files**

Read the five files listed above.

Expected: understand producer/consumer auto-configuration, Redis MQ template, RabbitMQ conditional branch and RPC package boundary.

- [ ] **Step 2: Add MQ producer and consumer comments**

Add comments explaining:

```text
- Producer auto-configuration prepares the template business code uses to publish messages.
- Consumer auto-configuration prepares listener-side infrastructure.
- Interceptors decorate messages before send or around consume, but should not contain caller-specific business decisions.
- Redis and RabbitMQ are alternative infrastructure implementations behind the same messaging idea.
```

Expected: beginners can distinguish “发消息入口” from “消费消息入口”。

- [ ] **Step 3: Add RedisMQTemplate comments**

Add class and key method comments explaining:

```text
- RedisMQTemplate is the direct send entry used by producers.
- The template converts framework message objects into Redis Stream operations.
- Message construction should remain generic; business payload meaning belongs to caller modules.
```

Expected: no send logic is changed.

- [ ] **Step 4: Add RPC package-level comments**

Update `package-info.java` with concise package JavaDoc explaining:

```java
/**
 * RPC 基础能力包。
 *
 * <p>本 Starter 只提供远程调用的技术支撑，例如 Feign 相关依赖、请求头透传和调用边界约定。
 * 具体调用哪个业务 API、如何编排业务流程，应由业务模块的 API 契约和应用服务决定。</p>
 */
package com.develop.mvp.pk.framework.rpc;
```

Expected: package statement remains unchanged.

- [ ] **Step 5: Check MQ and RPC diff**

Run:

```bash
git diff -- develop-framework/develop-spring-boot-starter-mq develop-framework/develop-spring-boot-starter-rpc
```

Expected: diff contains only comment changes in selected files.

### Task 6: Tenant Starter Flow Comments

**Files:**
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/config/DevelopTenantAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/context/TenantContextHolder.java`
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/web/TenantContextWebFilter.java`
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/aop/TenantIgnoreAspect.java`
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/db/TenantDatabaseInterceptor.java`
- Modify: `develop-framework/develop-spring-boot-starter-biz-tenant/src/main/java/com/develop/mvp/pk/framework/tenant/core/rpc/TenantRequestInterceptor.java`

- [ ] **Step 1: Read Tenant files**

Read the six files listed above.

Expected: understand tenant context lifecycle across HTTP request, AOP ignore scope, database interceptor and RPC propagation.

- [ ] **Step 2: Add tenant auto-configuration comments**

Add class and bean method comments explaining:

```text
- Tenant auto-configuration is the assembly point for tenant support.
- Web filter identifies tenant at request entry.
- Context holder stores tenant ID during the current execution.
- Database interceptor applies tenant isolation to SQL.
- RPC interceptor propagates tenant ID to downstream services.
- Ignore aspect creates temporary bypass scopes for framework/system operations.
```

Expected: comments describe collaboration across components without changing bean definitions.

- [ ] **Step 3: Add context lifecycle comments**

For `TenantContextHolder` and `TenantContextWebFilter`, explain:

```text
- where tenant ID is read from
- why context must be cleared after request
- why ThreadLocal is safe only when lifecycle is controlled
- how missing tenant differs from explicitly ignored tenant
```

Expected: comments make cleanup and ignore semantics clear.

- [ ] **Step 4: Add DB/RPC/AOP comments**

For `TenantDatabaseInterceptor`, `TenantRequestInterceptor`, and `TenantIgnoreAspect`, explain:

```text
- SQL interception protects data isolation at persistence boundary.
- RPC interception keeps downstream service calls in the same tenant scope.
- @TenantIgnore is a narrow bypass, not a global disable switch.
```

Expected: comments emphasize boundary and risk.

- [ ] **Step 5: Check Tenant diff**

Run:

```bash
git diff -- develop-framework/develop-spring-boot-starter-biz-tenant
```

Expected: diff contains only comment changes in selected files.

### Task 7: Data Permission Starter Flow Comments

**Files:**
- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/config/DevelopDataPermissionAutoConfiguration.java`
- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/core/aop/DataPermissionAnnotationAdvisor.java`
- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/core/aop/DataPermissionAnnotationInterceptor.java`
- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/core/aop/DataPermissionContextHolder.java`
- Modify: `develop-framework/develop-spring-boot-starter-biz-data-permission/src/main/java/com/develop/mvp/pk/framework/datapermission/core/rpc/DataPermissionRequestInterceptor.java`

- [ ] **Step 1: Read Data Permission files**

Read the five files listed above.

Expected: understand annotation advisor, interceptor, context holder, MyBatis interceptor registration and RPC propagation.

- [ ] **Step 2: Add auto-configuration comments**

Add comments explaining:

```text
- DataPermission auto-configuration connects annotation interception, rule factories and MyBatis SQL processing.
- Business methods declare data permission requirements through annotations or framework conventions.
- Query execution reads the data permission context later at persistence boundary.
```

Expected: beginners understand why AOP and MyBatis both appear in the same Starter.

- [ ] **Step 3: Add AOP and context comments**

For advisor/interceptor/context holder, explain:

```text
- Advisor decides which methods enter the data permission flow.
- Interceptor writes the current method's permission rules into context before invocation.
- Context must be restored or cleared after invocation to avoid leaking rules to nested or later calls.
```

Expected: comments emphasize stack/lifecycle behavior if present in implementation.

- [ ] **Step 4: Add RPC propagation comments**

For `DataPermissionRequestInterceptor`, explain:

```text
- request interceptor carries data permission markers across Feign/RPC boundaries when necessary.
- downstream services still apply their own query-time restrictions.
```

Expected: comments do not imply RPC bypasses downstream permission checks.

- [ ] **Step 5: Check Data Permission diff**

Run:

```bash
git diff -- develop-framework/develop-spring-boot-starter-biz-data-permission
```

Expected: diff contains only comment changes in selected files.

### Task 8: Final Verification

**Files:**
- Inspect: all modified files

- [ ] **Step 1: Review complete diff for comment-only changes**

Run:

```bash
git diff -- develop-framework/develop-spring-boot-starter-web develop-framework/develop-spring-boot-starter-security develop-framework/develop-spring-boot-starter-mybatis develop-framework/develop-spring-boot-starter-redis develop-framework/develop-spring-boot-starter-mq develop-framework/develop-spring-boot-starter-rpc develop-framework/develop-spring-boot-starter-biz-tenant develop-framework/develop-spring-boot-starter-biz-data-permission
```

Expected: only JavaDoc or inline comment changes appear.

- [ ] **Step 2: Compile framework module**

Run:

```bash
mvn compile -pl develop-framework -am
```

Expected: build succeeds. If it fails for environment or historical reasons, capture the first actionable failure and run the narrower affected module compile command.

- [ ] **Step 3: Fallback compile commands if needed**

If full framework compile fails before reaching modified modules, run targeted compile commands:

```bash
mvn compile -pl develop-framework/develop-spring-boot-starter-web -am
mvn compile -pl develop-framework/develop-spring-boot-starter-security -am
mvn compile -pl develop-framework/develop-spring-boot-starter-mybatis -am
mvn compile -pl develop-framework/develop-spring-boot-starter-redis -am
mvn compile -pl develop-framework/develop-spring-boot-starter-mq -am
mvn compile -pl develop-framework/develop-spring-boot-starter-rpc -am
mvn compile -pl develop-framework/develop-spring-boot-starter-biz-tenant -am
mvn compile -pl develop-framework/develop-spring-boot-starter-biz-data-permission -am
```

Expected: each targeted module either compiles or produces a recorded failure unrelated to comment-only changes.

- [ ] **Step 4: Summarize verification**

Report:

```text
- Modified starter groups
- Verification command(s) run
- Result of each command
- Confirmation that diff is comment-only
- Any pre-existing or environment failures
```

Expected: no claim of success without command output.
