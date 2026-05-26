---
name: TokenAuthenticationFilter
description: Gateway GlobalFilter that validates tokens via WebClient and forwards user info as headers
type: project
---

# TokenAuthenticationFilter (Gateway)

## 功能定位

这是 Gateway 网关层的 Token 认证过滤器，位于 `develop-gateway` 的 `filter.security` 包下。与前文 servlet 版的 `TokenAuthenticationFilter`（在 `security starter` 中）职责不同，它实现 `GlobalFilter + Ordered` 接口，**在网关入口处完成 Token 校验并将用户信息透传给下游微服务**。

核心职责：
- **Token 校验**：从请求中提取 Token，通过 WebClient 调用 system-server 验证
- **用户信息透传**：将校验后的用户信息（userId, userType, tenantId）通过 `login-user` 请求头 JSON 格式透传
- **不安全头移除**：在入口处移除所有 `login-user` 请求头，防止伪造
- **缓存加速**：使用 Guava LoadingCache 缓存 Token 校验结果，1 分钟 TTL
- **非阻塞架构**：所有 I/O 基于 WebFlux 响应式编程，不阻塞 Gateway 工作线程

它和 servlet 版 TokenAuthenticationFilter 的区别：

| 维度 | Servlet 版 (security starter) | Gateway 版 (此文件) |
|------|-----------------------------|---------------------|
| 基类 | `OncePerRequestFilter` | `GlobalFilter` |
| 技术栈 | Servlet | WebFlux (Reactive) |
| 服务调用 | Feign `OAuth2TokenCommonApi` | WebClient |
| 缓存 | 无本地缓存 | Guava LoadingCache (1min TTL) |
| 目的 | 设置 SecurityContext | 透传用户信息给下游 |
| 位置 | 微服务内部 | 网关入口 |

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **GatewayFilter** | 实现 `GlobalFilter + Ordered` 作为网关全局过滤器 | `filter()`, `getOrder()` |
| **Cache-Aside** | Guava LoadingCache 缓存 Token 校验结果 | `loginUserCache` + `buildAsyncReloadingCache` |
| **Reactive Chain** | 基于 WebFlux 的响应式编程模式 | `Mono<Void>` / `flatMap` / `defaultIfEmpty` |
| **Proxy** | 前置代理，将用户信息转发给下游 | 设置 `login-user` 请求头 |
| **Sentinel / Guard** | 入口处清除非法的请求头 | `SecurityFrameworkUtils.removeLoginUser()` |
| **Null Object** | 使用哨兵对象避免 null 传播 | `LOGIN_USER_EMPTY` + `defaultIfEmpty` |

## 核心逻辑流程

```
filter(exchange, chain)
  |
  +-- 步骤 0: 安全防护 - 移除所有 login-user 请求头
  |    exchange = SecurityFrameworkUtils.removeLoginUser(exchange)
  |    (防止外部请求伪造 login-user 头)
  |
  +-- 步骤 1: 提取 Token
  |    token = SecurityFrameworkUtils.obtainAuthorization(exchange)
  |    (从 Authorization header 中提取，自动去除 "Bearer " 前缀)
  |
  +-- Guard: 无 Token -> 直接放行
  |    if (StrUtil.isEmpty(token)) -> return chain.filter(exchange)
  |    (Gateway 不强制要求登录，让下游决定是否需要认证)
  |
  +-- 步骤 2: 获取 LoginUser (优先缓存)
  |    getLoginUser(exchange, token)
  |    |
  |    +-- 2.1 从缓存查询
  |    |    cacheKey = KeyValue<tenantId, token>
  |    |    localUser = loginUserCache.getIfPresent(cacheKey)
  |    |    [CACHE HIT] -> return Mono.just(localUser)
  |    |
  |    +-- 2.2 缓存 MISS: WebClient 远程调用
  |         checkAccessToken(tenantId, token)
  |           -> webClient.get()
  |              .uri(OAuth2TokenCommonApi.URL_CHECK, {accessToken -> token})
  |              .header("tenant-id", tenantId)
  |              .retrieve()
  |              .bodyToMono(String.class)
  |
  |         buildUser(body) 解析结果:
  |           -> JSON -> CommonResult<OAuth2AccessTokenCheckRespDTO>
  |           -> [成功] 构建 LoginUser
  |           -> [Token 过期/错误]: 返回 LOGIN_USER_EMPTY (哨兵)
  |
  |         loginUserCache.put(cacheKey, user) 写入缓存
  |         return Mono.just(user) 或 Mono.empty()
  |
  +-- 步骤 3: 处理认证结果
       |
       +-- 使用 defaultIfEmpty(LOGIN_USER_EMPTY) 处理空值
       |
       +-- 3.1 无用户 或 Token 过期:
       |    if (user == LOGIN_USER_EMPTY || expiresTime 已过期)
       |    -> return chain.filter(exchange)  (放行，下游决定)
       |
       +-- 3.2 有有效用户:
            +-- SecurityFrameworkUtils.setLoginUser(exchange, user)
            |    设置到 exchange 的 attribute 中
            |
            +-- exchange.mutate().request(builder ->
            |    SecurityFrameworkUtils.setLoginUserHeader(builder, user))
            |    将 user 以 JSON 设置到 login-user 请求头
            |
            +-- return chain.filter(newExchange)
```

## 关键代码剖析

### 构造方法: WebClient 初始化

```java
public TokenAuthenticationFilter(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
    // Q: 为什么不使用 OAuth2TokenApi (Feign) 进行调用？
    // A1: Spring Cloud OpenFeign 官方未内置 Reactive 的支持
    // A2: 校验 Token 的 API 需要使用 header[tenant-id] 传递租户编号
    //     暂时不想编写 RequestInterceptor 实现
    // 因此，这里采用 WebClient + lbFunction 实现负载均衡
    this.webClient = WebClient.builder().filter(lbFunction).build();
}
```

WebClient 方案 vs Feign 方案的选择：
- Gateway 基于 WebFlux（Reactive），Servlet 体系的 Feign 不兼容
- `ReactorLoadBalancerExchangeFilterFunction` 提供对 target 服务的负载均衡能力
- WebClient 的原生非阻塞 I/O 完美适配 Gateway 线程模型

### 异步缓存

```java
// 使用 buildAsyncReloadingCache 异步加载
private final LoadingCache<KeyValue<Long, String>, LoginUser> loginUserCache =
    buildAsyncReloadingCache(Duration.ofMinutes(1),
            new CacheLoader<KeyValue<Long, String>, LoginUser>() {
                @Override
                public LoginUser load(KeyValue<Long, String> token) {
                    String body = checkAccessToken(token.getKey(), token.getValue()).block();
                    return buildUser(body);
                }
            });
```

`buildAsyncReloadingCache` 是框架对 Guava Cache 的扩展，支持 `refreshAfterWrite` 语义：
- 写入后 1 分钟内直接从缓存读取
- 1 分钟后，下次访问时触发异步刷新，返回旧值（不会阻塞）
- 如果刷新失败，返回旧值（一定程度的容错）

### 哨兵对象 LOGIN_USER_EMPTY

```java
private static final LoginUser LOGIN_USER_EMPTY = new LoginUser();
```

解决两个问题：
1. `Mono.empty()` 会使 `flatMap` 跳过处理，导致返回到前端的响应为空
2. Token 过期时，需要缓存"已过期"的结果，避免每次请求都穿透到 system-server

通过 `defaultIfEmpty(LOGIN_USER_EMPTY)` 保证 `flatMap` 始终能执行。

### Token 过期判断

```java
if (user == LOGIN_USER_EMPTY ||
        user.getExpiresTime() == null || LocalDateTimeUtils.beforeNow(user.getExpiresTime())) {
    return chain.filter(finalExchange);
}
```

过期的 Token：
- `LOGIN_USER_EMPTY`：表示 Token 不存在或校验失败
- `expiresTime == null`：构造 LoginUser 失败（数据异常）
- `beforeNow(expiresTime)`：Token 已过期

### Order 设置

```java
@Override
public int getOrder() {
    return -100; // 和 Spring Security Filter 的顺序对齐
}
```

`Ordered.LOWEST_PRECEDENCE = Integer.MAX_VALUE`，`-100` 意味着优先级非常高，在绝大部分过滤器之前执行。

## 调用链

```
[Upstream - 请求入口]
  Client Request (Authorization: Bearer xxx)
    |
    +-- Gateway Handler Mapping
         |
         +-- TokenAuthenticationFilter.filter() (本类, order=-100)
              |
              +-- [安全] SecurityFrameworkUtils.removeLoginUser()
              +-- [提取] SecurityFrameworkUtils.obtainAuthorization()
              |
              +-- [远程] checkAccessToken(tenantId, token)
              |    -> WebClient GET
              |       http://system-server/rpc-api/system/oauth2/check-access-token
              |       ?accessToken=xxx
              |       Header: tenant-id=1
              |    -> Nacos 负载均衡选择 system-server 实例
              |
              +-- [缓存] loginUserCache
              |
              +-- [转发] SecurityFrameworkUtils.setLoginUserHeader()
                   -> 设置 login-user 请求头: { id, userType, tenantId, info }
                   |
                   +-- 转发到下游微服务
                   |    (system-server, bpm-server, infra-server ...)
                   |
                   +-- 下游微服务的 TokenAuthenticationFilter (servlet 版)
                        -> buildLoginUserByHeader() 直接解析
                        -> 无需再次调用 OAuth2 服务

[Downstream - 下游微服务]
  收到 login-user 请求头
    -> Servlet 版 TokenAuthenticationFilter.buildLoginUserByHeader()
    -> 直接设置 SecurityContext
    -> 零 RPC 开销
```

## 配置与条件

| 配置 | 说明 |
|------|------|
| `spring.cloud.gateway.routes[0].filters` | 路由配置中无需额外添加，此 Filter 是 GlobalFilter 自动生效 |
| `spring.cloud.loadbalancer.retry` | 负载均衡重试配置 |

| 行为条件 | 结果 |
|----------|------|
| 请求无 Authorization Header | 直接放行，不设置 login-user 头 |
| Token 有效 | 设置 login-user 头转发给下游 |
| Token 过期 | 放行，不设置 login-user 头，下游会根据接口安全配置返回 401 |
| 远程调用 system-server 失败 | loginUserCache 返回 null，放行 |

## 生产级关注点

### 1. 安全设计: 请求头移除

```java
exchange = SecurityFrameworkUtils.removeLoginUser(exchange);
```

**为什么必须在入口处移除 login-user 请求头？**
- 如果外部请求携带了伪造的 `login-user` 头（例如通过 curl 添加），下游服务会直接信任该头
- Gateway 作为唯一的入口，必须清除所有从外部传入的 `login-user` 头
- Gateway 自己校验 Token 后重新设置 `login-user` 头

### 2. WebFlux 兼容性

Gateway 基于 Spring WebFlux（Reactor），不能使用：
- Feign 客户端（基于 Servlet）
- `ThreadLocal`（WebFlux 的请求不在固定线程上执行）
- 阻塞 IO

因此使用：
- `WebClient`（响应式 HTTP 客户端）
- `TransmittableThreadLocal`（在 reactive 场景中需额外注意）
- `Mono<T>` / `Flux<T>`（响应式类型）

### 3. 缓存穿透保护

- `buildAsyncReloadingCache` + `getIfPresent`：缓存未命中时不会自动 load，而是由业务代码手动触发
- `put()`：只有在远程调用成功后才会写入缓存
- `LOGIN_USER_EMPTY`：即使 Token 过期，也缓存结果，防止对 system-server 的重复请求

### 4. 不登录即放行的策略

Gateway 层**不拒绝未携带 Token 的请求**。这是有意为之：
- 有些接口是公开的（如登录接口本身、获取验证码）
- 有些接口可能通过 IP 白名单等其他方式鉴权
- 是否要求登录由下游微服务的 `@PreAuthorize` / Security 配置决定

### 5. 超时处理

- `checkAccessToken` 使用 WebClient 的默认超时配置
- 如果 system-server 响应慢或不可用，WebClient 会超时
- 超时后返回 `Mono.empty()` -> 触发 `defaultIfEmpty` -> 放行请求
- 这意味着：**OAuth2 服务宕机时，所有请求会绕过登录校验流向下游**。根据安全策略，可能需要调整行为（比如超时后拒绝请求）

### 6. 监控建议

- 监控 `loginUserCache` 的命中率（通过 Guava Cache 的 `stats()` 方法）
- 监控 `checkAccessToken` 的延迟（对 system-server 的调用延迟直接影响整个 Gateway 的请求延迟）
- 建议对 `checkAccessToken` 的失败做告警

### 7. 日志

- 不需要对每次 Token 校验打印日志（高频操作）
- 只对降级/异常场景打印 `log.warn` 或 `log.error`
- 可通过 Gateway 的 `AccessLogFilter` 记录整体访问日志
