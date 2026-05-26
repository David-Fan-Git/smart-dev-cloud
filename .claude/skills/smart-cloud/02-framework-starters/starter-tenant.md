---
name: starter-tenant
description: SaaS multi-tenant support — tenant context propagation across Web, DB, Redis, MQ, Job, and RPC layers with TransmittableThreadLocal
type: project
---

# develop-spring-boot-starter-biz-tenant

## Overview

SaaS 多租户基础模块。通过 `TransmittableThreadLocal`（TTL）在全链路透传租户 ID，覆盖 **Web 请求拦截、MyBatis Plus SQL 拦截、Redis Key 隔离、MQ 消息路由、XXL-Job 多租户执行、Feign 调用传播**六大层面。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **2 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.tenant`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-biz-tenant AutoConfiguration.imports
com.develop.mvp.pk.framework.tenant.config.DevelopTenantRpcAutoConfiguration
com.develop.mvp.pk.framework.tenant.config.DevelopTenantAutoConfiguration
```

## Core Components

### 1. TenantContextHolder

**Location:** `com.develop.mvp.pk.framework.tenant.core.context`

- Uses `TransmittableThreadLocal<Long>` for tenant ID storage
- Auto-propagates to thread pools, `@Async` methods, and any `TtlRunnable`/`TtlCallable` wrapped tasks
- `TransmittableThreadLocal` ensures context is passed to child threads even when thread pool reuses threads

```java
public class TenantContextHolder {
    static void setTenantId(Long tenantId);
    static Long getTenantId();
    static void clear();
}
```

### 2. Web Layer

- **TenantContextWebFilter**: extracts `tenant-id` from request header, sets into `TenantContextHolder`
  - Order: `WebFilterOrderEnum.TENANT_CONTEXT_FILTER` (-104)
  - Must run after `RequestContextFilter` (-105) and before `ApiAccessLogFilter` (-103)

- **TenantVisitContextInterceptor**: validates that the visitor has access in the current tenant scope
  - Skips URLs in `develop.tenant.ignore-visit-urls`

- **TenantSecurityWebFilter**: verifies that the tenant exists and is enabled
  - Order: `WebFilterOrderEnum.TENANT_SECURITY_FILTER` (-99)
  - Runs after Spring Security's filter chain (-100)

### 3. DB Layer (TenantLineInnerInterceptor)

MyBatis Plus 多租户 SQL 拦截，插入到 `MybatisPlusInterceptor` 的 **pos=0**（最优先执行）:

```java
// Auto-appended WHERE clause:
WHERE tenant_id = <current tenant ID from TenantContextHolder>
```

- Only applies to tables that have a `tenant_id` column (entities extending `TenantBaseDO`)
- **Ignored tables**: listed in `develop.tenant.ignore-tables` configuration
- **Skip via annotation**: `@TenantIgnore` on Controller/Mapper method — skips tenant filter entirely
- **Easy-trans integration**: automatically detects translation queries and skips tenant filter

### 4. Redis Layer (TenantRedisCacheManager)

- `@Primary` — replaces the default RedisCacheManager
- All cache keys are automatically prefixed with `{tenantId}://`:
  ```
  Without tenant: "user:1"
  With tenant:    "1://user:1"
  ```
- Prevents cache collisions between tenants
- **Ignored caches**: listed in `develop.tenant.ignore-caches` configuration (e.g., `captcha_cache`, `access_token_cache`)
- Affects all `@Cacheable`, `@CacheEvict`, `@CachePut` annotations

### 5. MQ Layer

- **TenantRedisMessageInterceptor**: Redis pub/sub + stream message interceptor
  - On message send: injects `tenantId` into message headers
  - On message receive: restores `TenantContextHolder` from message headers before invoking listener
- RabbitMQ and RocketMQ: tenants are propagated via message headers in respective initializers
- Ensures consumer-side processing runs in the correct tenant context

### 6. Job Layer (TenantJobAspect)

- AOP aspect for `@TenantJob` annotation on `@XxlJob` methods
- `@TenantJob`: iterates over all active tenants, executes the job once per tenant
  - Sets `TenantContextHolder` before each tenant's execution
  - Serial execution (one tenant at a time) to avoid resource contention
- `@TenantIgnore`: skips tenant context setup (executes once without tenant scope)

### 7. RPC Layer

- **TenantRequestInterceptor**: Feign `RequestInterceptor`
  - Adds `tenant-id` header to all outbound Feign requests
  - Downstream services' `TenantContextWebFilter` extracts and restores from the header
- Part of `DevelopTenantRpcAutoConfiguration` (excluded in monolithic mode)

### 8. Annotations

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantJob {
    // No attributes — serves as marker for TenantJobAspect
}

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantIgnore {
    // Skips tenant SQL interceptor and context setup
}
```

### 9. TenantBaseDO

**Location:** `com.develop.mvp.pk.framework.tenant.core.db`

```java
public abstract class TenantBaseDO extends BaseDO {
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
```

## Configuration Properties

```yaml
develop:
  tenant:
    enable: true                          # Global switch
    ignore-urls:                          # URLs skipping tenant validation
      - /admin-api/system/auth/**
    ignore-visit-urls:                    # URLs skipping visit validation
      - /app-api/**
    ignore-tables:                        # Tables skipping SQL tenant filter
      - system_config
      - system_dict_data
    ignore-caches:                        # Caches skipping tenant key prefix
      - captcha_cache
      - access_token_cache
```

## Code Examples

```java
// Entity — extends TenantBaseDO for automatic multi-tenancy
@Data
@TableName("system_operate_log")
public class OperateLogDO extends TenantBaseDO { ... }

// Skip tenant filter on specific endpoint
@TenantIgnore
@GetMapping("/config")
public CommonResult<ConfigVO> getConfig() { ... }

// Multi-tenant scheduled job
@Component
public class OrderStatisticsJob {
    @TenantJob  // Executes once per tenant
    @XxlJob("orderStatistics")
    public void execute() {
        // TenantContextHolder is set by TenantJobAspect
        List<OrderStatisticVO> stats = orderService.statistic();
        XxlJobHelper.handleSuccess(JsonUtils.toJsonString(stats));
    }
}

// Manual tenant context
public void someMethod() {
    Long tenantId = TenantContextHolder.getTenantId();
    // ... or set temporarily
    try {
        TenantContextHolder.setTenantId(1L);
        // execute scoped logic
    } finally {
        TenantContextHolder.clear();
    }
}
```

## 注意事项

- `TenantLineInnerInterceptor` 必须插入到 `MybatisPlusInterceptor` 的 **pos=0**（最优先），确保在分页、数据权限和其他所有 SQL 拦截之前追加租户条件
- 缓存 Key 前缀隔离后，跨租户清理缓存需要通过 `{tenantId}:*` 模式匹配或显式指定忽略缓存列表
- `@TenantJob` 在所有租户上**串行执行**，租户数量大时需关注执行总耗时；建议将耗时长的批量租户任务分批执行或自行实现并行版本
- easy-trans 翻译时自动排除租户拦截（通过堆栈检测判断翻译线程），避免字典、部门名称等翻译查询被租户条件过滤
- 关闭 `develop.tenant.enable=false` 时，所有租户相关拦截器跳过，`TenantContextHolder.getTenantId()` 返回 null
- `@TenantIgnore` 可以标注在 Controller 方法上（跳过过滤器/拦截器）或 Mapper 方法上（跳过 SQL 过滤）
- `TenantRedisCacheManager` 的 Key 前缀格式为 `{tenantId}://`，注意 Redis 管理工具中查找缓存时需加上前缀
- 配置属性类名称为 `TenantProperties`（而非 `DevelopTenantProperties`），package: `com.develop.mvp.pk.framework.tenant.config`
