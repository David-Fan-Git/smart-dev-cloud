---
name: starter-rpc
description: Feign-based inter-service RPC with LoadBalancer, dual HTTP/Feign contract pattern, LoginUser propagation, and per-module Feign client configuration
type: project
---

# develop-spring-boot-starter-rpc

## Overview

微服务间远程调用模块。基于 Spring Cloud OpenFeign，提供声明式 RPC 接口定义规范、LoadBalancer 负载均衡、LoginUser 上下文透传（Feign 请求拦截器），以及独特的"双面契约"模式——同一接口同时服务于 HTTP REST 和 Feign RPC 消费者。**无 AutoConfiguration 注册**（由各业务模块的 `@EnableFeignClients` 手动按需启用）。

**Package base:** `com.develop.mvp.pk.framework.rpc`

## AutoConfiguration

This starter does not register any auto-configuration classes in `AutoConfiguration.imports`. Instead:

1. Each business module that needs Feign clients creates its own `@Configuration` with `@EnableFeignClients(clients = {...})`
2. `DevelopSecurityRpcAutoConfiguration` from starter-security enables Feign for security APIs
3. Other `DevelopXxxRpcAutoConfiguration` classes in each starter enable Feign for their respective APIs

## Core Components

### 1. @FeignClient Interface Convention

**Defined in `-api` module:**

```java
@FeignClient(name = ApiConstants.NAME, contextId = "userApi",
             url = "${develop.feign.system-server:}")
public interface UserApi {
    @GetMapping(RpcConstants.RPC_API_PREFIX + "/user/get")
    CommonResult<UserRespDTO> getUser(@RequestParam("id") Long id);
}
```

Key points:
- `name`: must match the target service's `spring.application.name`
- `contextId`: prevents Feign client Bean name collision in the same application context (important in monolithic mode)
- `url`: SpEL expression allows direct URL override via config (empty = use service discovery)
- All method parameters must have explicitly named `@RequestParam("value")` — compiled bytecode loses parameter names otherwise

### 2. Dual Contract Pattern (HTTP + Feign)

The same interface serves both REST consumers (through HTTP) and Feign consumers (through RPC):

```java
// -api module: Feign interface definition (contract only)
public interface UserApi {
    @GetMapping("/rpc-api/user/get")
    CommonResult<UserRespDTO> getUser(@RequestParam("id") Long id);
}

// -server module: Implementation (doubles as REST endpoint)
@RestController
public class UserApiImpl implements UserApi {
    @Override
    public CommonResult<UserRespDTO> getUser(Long id) {
        UserDO user = userService.get(id);
        return success(UserConvert.INSTANCE.convert(user));
    }
}
```

This pattern means:
- When used as Feign client: calling `userApi.get(id)` triggers HTTP call to the remote service
- When used as local call (monolithic mode): since Feign is excluded, the `@RestController` implementation handles the call directly within the same JVM

### 3. RpcConstants

**Location:** `com.develop.mvp.pk.framework.common.enums.RpcConstants`

```java
public class RpcConstants {
    public static final String RPC_API_PREFIX = "/rpc-api";
    // Service name constants (for reference in @FeignClient name)
    public static final String SYSTEM_SERVER = "system-server";
    public static final String INFRA_SERVER = "infra-server";
    public static final String MEMBER_SERVER = "member-server";
}
```

The `/rpc-api` prefix serves as a namespace separator — Nginx/gateway routing and security filters can distinguish RPC endpoints from public REST endpoints by this prefix.

### 4. LoadBalancer Integration

- Uses `spring-cloud-starter-loadbalancer` (Spring Cloud's official load balancer)
- Replaces Netflix Ribbon (deprecated)
- Strategies: Round Robin (default), Random, etc.
- Nacos service discovery integration

### 5. LoginUserRequestInterceptor

**Location:** `com.develop.mvp.pk.framework.security.core.interceptor`

- Implements `RequestInterceptor`
- On Feign request: reads `LoginUser` from current thread's `SecurityContextHolder`
- Writes user info into request headers (e.g., `login-user-id`, `login-user-type`)
- Downstream service's `TokenAuthenticationFilter` reads these headers to reconstruct `LoginUser`
- Ensures user identity is propagated across the service call chain

### 6. Per-Module Feign Configuration

Each module that provides Feign APIs exposes a dedicated configuration class:

```java
@Configuration
@EnableFeignClients(clients = {UserApi.class, DeptApi.class, RoleApi.class, PostApi.class})
public class SystemRpcConfiguration {}
```

The `-api` module's `ApiConstants` class is used by both the provider and consumer:

```java
public class ApiConstants {
    public static final String NAME = "system-server";
    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/system";
    public static final String VERSION = "1.0.0";
}
```

## Configuration Properties

```yaml
develop:
  feign:
    system-server:              # Direct URL override (empty = service discovery)
    infra-server:

spring:
  cloud:
    loadbalancer:
      enabled: true
    nacos:
      discovery:
        enabled: true           # Required for service-discovery-based Feign

feign:
  client:
    config:
      default:
        connectTimeout: 5000    # Connection timeout (ms)
        readTimeout: 10000      # Read timeout (ms)
        loggerLevel: BASIC      # Log level: NONE, BASIC, HEADERS, FULL
  compression:
    request:
      enabled: true             # Compress Feign requests (gzip)
      mime-types: application/json,application/xml
      min-request-size: 2048
    response:
      enabled: true             # Compress Feign responses (gzip)
```

## Code Examples

```java
// -api module interface definition
public interface DeptApi {
    @GetMapping(RpcConstants.RPC_API_PREFIX + "/dept/list-by-ids")
    CommonResult<List<DeptRespDTO>> getDeptList(@RequestParam("ids") Collection<Long> ids);
}

// -server module implementation
@RestController
public class DeptApiImpl implements DeptApi {
    @Override
    public CommonResult<List<DeptRespDTO>> getDeptList(Collection<Long> ids) {
        List<DeptDO> list = deptService.getList(ids);
        return success(DeptConvert.INSTANCE.convertList(list));
    }
}

// Consumer side
@Service
public class OrderService {
    @Resource
    private DeptApi deptApi;

    public void processOrder(Long deptId) {
        CommonResult<List<DeptRespDTO>> result = deptApi.getDeptList(Arrays.asList(deptId));
        List<DeptRespDTO> depts = result.getCheckedData();  // Auto-unwrap, throws on non-0 code
    }
}
```

## Production Concerns

| Concern | Recommendation |
|---|---|
| Feign call timeout | Set `readTimeout` based on the slowest endpoint in the target service; for batch operations, consider async Feign |
| Circuit breaking | Integrate with Sentinel via `spring.cloud.openfeign.sentinel.enabled=true` |
| Retry | Avoid automatic retry on non-idempotent endpoints (POST/PUT/DELETE). For GET: `feign.client.config.default.retryer` |
| Connection pool | Use Apache HttpClient or OkHttp as Feign HTTP client: `feign.httpclient.enabled=true` or `feign.okhttp.enabled=true` |
| Logging | `BASIC` in production (URL + status); `HEADERS` in dev for debugging; `FULL` only in dev (may log sensitive data) |
| Monolithic mode | Exclude `spring-cloud-starter-openfeign` from starter-rpc dependency in develop-server/pom.xml |

## 注意事项

- `RpcConstants.RPC_API_PREFIX = "/rpc-api"` 前缀用于区分 RPC 接口和普通 REST 接口，避免安全过滤器（如 TokenAuthenticationFilter）对 RPC 端点应用不必要的拦截
- `@FeignClient` 的 `url` 使用 SpEL 表达式（`${develop.feign.system-server:}`），本地开发可通过配置文件直连指定服务跳过 Nacos 服务发现
- `result.getCheckedData()` 在 code 非 0 时抛 `ServiceException`，简化调用方错误处理；使用时注意此操作会消费 CommonResult 对象（调用后原 result 仍可访问但不应再使用）
- Feign 接口的方法签名必须与 `@GetMapping/@PostMapping` 完全一致；`@RequestParam` 必须显式指定 value（如 `@RequestParam("ids")`），否则参数名在编译后丢失导致 400 错误
- 模块级 Feign 配置类（`XxxRpcConfiguration`）需通过 `@Import`、`@ComponentScan` 或模块自身的 `@EnableFeignClients` 被 Spring 扫描注册
- 高并发场景建议启用 Feign 的请求/响应压缩（`feign.compression.request.enabled=true`）和连接池（`feign.httpclient.enabled=true` 或 `feign.okhttp.enabled=true`）
- 单体模式下需排除 `spring-cloud-starter-openfeign` 依赖（通过 starter-rpc 的 exclusion），否则 `@FeignClient` 接口会尝试连接远程服务并失败
- `contextId` 是解决同一服务多 Feign 接口 Bean 名称冲突的关键属性，特别是单体模式下多个 Api 接口被同一 JVM 加载时
