---
name: starter-web
description: Web layer auto-configuration — global exception handling, Jackson, Swagger/Knife4j, XSS sanitization, API access log, API encryption, and banner
type: project
---

# develop-spring-boot-starter-web

## Overview

Web 基础自动配置模块。所有 REST 服务的入口层，涵盖全局异常处理、响应统一包装、JSON 序列化、API 文档（SpringDoc + Knife4j）、XSS 过滤、API 访问日志、API 加解密过滤器及启动横幅。通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **8 个自动配置类**。

**Package:** `com.develop.mvp.pk.framework.web|jackson|swagger|xss|apilog|encrypt|banner`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-web AutoConfiguration.imports (order matters)
com.develop.mvp.pk.framework.apilog.config.DevelopApiLogAutoConfiguration   # API access log filter
com.develop.mvp.pk.framework.jackson.config.DevelopJacksonAutoConfiguration   # Jackson ObjectMapper config
com.develop.mvp.pk.framework.swagger.config.DevelopSwaggerAutoConfiguration   # SpringDoc + Knife4j
com.develop.mvp.pk.framework.web.config.DevelopWebAutoConfiguration          # Core: exception handler, CORS, body cache
com.develop.mvp.pk.framework.apilog.config.DevelopApiLogRpcAutoConfiguration  # Feign-based API logging
com.develop.mvp.pk.framework.xss.config.DevelopXssAutoConfiguration          # XSS sanitization
com.develop.mvp.pk.framework.banner.config.DevelopBannerAutoConfiguration    # Console banner
com.develop.mvp.pk.framework.encrypt.config.DevelopApiEncryptAutoConfiguration # API request/response encryption
```

## Core Components

### 1. DevelopWebAutoConfiguration (Core Web Config)

**Location:** `com.develop.mvp.pk.framework.web.config`

Provides:

- **GlobalExceptionHandler** (`@RestControllerAdvice`):
  - Maps 15+ exception types to proper HTTP status codes:
    - `ServiceException` -> biz error (code/msg preserved)
    - `ConstraintViolationException`, `MethodArgumentNotValidException` -> 400
    - `NoHandlerFoundException`, `HttpRequestMethodNotSupportedException` -> 404
    - `AccessDeniedException` -> 403
    - `AuthenticationException` -> 401
    - `BindException`, `MessageNotReadableException` -> 400
    - `DataSourceException`, `TransactionException` -> 500
    - `Exception` (fallback) -> 500
  - Logging: WARN level with stack trace for 500s, DEBUG for 400s
  - Integrates with `ApiErrorLogCommonApi` for async error log persistence

- **GlobalResponseBodyHandler**: implements `ResponseBodyAdvice`, wraps all responses into `CommonResult` (skips `CommonResult` itself and `String` type)

- **WebMvcRegistrations**: dynamic path prefix registration via `RequestMappingHandlerMapping.setPathPrefixes()`
  - `/admin-api/` -> `**.controller.admin.**`
  - `/app-api/` -> `**.controller.app.**`
  - Configurable via `WebProperties` (prefix = `develop.web`)

- **CorsFilter**: `@Order(WebFilterOrderEnum.CORS_FILTER)` — allows all origins, headers, methods (dev-friendly)
- **CacheRequestBodyFilter**: `@Order(WebFilterOrderEnum.REQUEST_BODY_CACHE_FILTER)` — wraps `HttpServletRequest` for repeatable body reading (needed by ApiAccessLog, XSS, Encrypt filters)
- **DemoFilter**: `@Order(WebFilterOrderEnum.DEMO_FILTER)` — blocks mutating operations in demo mode (`ConditionalOnProperty: develop.demo=true`)
- **RestTemplate** x2: standard `RestTemplate` + `@LoadBalanced RestTemplate` for service-discovery-aware HTTP calls

### 2. DevelopJacksonAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.jackson.config`

- Registers custom serializers:
  - `NumberSerializer`: Long -> String (JS precision safety)
  - `TimestampLocalDateTimeSerializer` / `TimestampLocalDateTimeDeserializer`: epoch millis
  - LocalDate / LocalTime ISO formatters
- Initializes `JsonUtils` with the configured `ObjectMapper`

### 3. DevelopSwaggerAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.swagger.config` (NOT jackson)

- SpringDoc OpenAPI 3.0 + Knife4j 4.5.0
- Global security scheme: `Authorization` header
- Global parameter: `tenant-id` header
- Custom `operationId` format: `ClassName_methodName`
- Static helper: `buildGroupedOpenApi(group, pathPrefix)` for module-level API groupings
- Conditions: `springdoc.api-docs.enabled=true` (default), enables/disables via config

**Knife4jOpenApiCustomizer**: customizes OpenAPI metadata for Knife4j UI.

### 4. DevelopXssAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.xss.config`

- **JsoupXssCleaner**: HTML sanitization with whitelist strategy (removes script/on* attributes)
- **XssStringJsonDeserializer**: interceptor at Jackson deserialization for JSON body fields
- **XssFilter**: `@Order(WebFilterOrderEnum.XSS_FILTER = -102)` — intercepts all form/query params
- Conditional: `@ConditionalOnProperty(prefix = "develop.xss", name = "enable", matchIfMissing = true)`
- Config: `develop.xss.enable`, `develop.xss.exclude-urls`

**Edge Cases:**
- XSS filter applies to String fields only — numbers, booleans, dates pass through untouched
- Rich text fields (TinyMCE, CKEditor) should be excluded via `develop.xss.exclude-urls` or excluded rules
- JSON deserialization XSS filter runs only when Jackson is used (not for `@RequestParam` directly)

### 5. DevelopApiLogAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.apilog.config`

- **ApiAccessLogFilter**: `@Order(WebFilterOrderEnum.API_ACCESS_LOG_FILTER = -103)` — records every API request
- Logs: HTTP method, URL, parameters, request body, response, execution time, user ID, tenant ID
- Asynchronous persistence via `ApiAccessLogCommonApi` Feign client
- Conditional: `develop.access-log.enable=true` (default: enabled)
- Adds `ApiAccessLogInterceptor` to MVC interceptor registry for Controller-level timing

**Performance:**
- Filter order -103 ensures it runs after RequestBodyCache (-2147483148), so request body is available
- Asynchronous logging (non-blocking) prevents API slowdown
- Sensitive fields logging can be avoided via `@ApiAccessLog(sensitiveFields = {"password", "token"})`

### 6. DevelopApiLogRpcAutoConfiguration

- Feign-based API logging for RPC calls (used in microservices mode)
- Logs Feign invocations with request/response details
- Conditional: auto-enabled when Feign is present, excluded in monolithic mode via `spring.autoconfigure.exclude`

### 7. DevelopApiEncryptAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.encrypt.config`

- **ApiEncryptFilter**: `@Order(WebFilterOrderEnum.API_ENCRYPT_FILTER)` — request/response encryption
- Conditional: `@ConditionalOnProperty(prefix = "develop.api-encrypt", name = "enable", havingValue = "true")` (default: disabled)
- Config: `develop.api-encrypt.enable`, `develop.api-encrypt.*`
- Encrypts response body, decrypts request body based on annotation on Controller methods

### 8. DevelopBannerAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.banner.config`

- Prints ASCII art banner to console at startup
- Application name, version, active profiles
- Config: `spring.main.banner-mode=console` (default)

## Filter Chain Order

```
CORS (Integer.MIN_VALUE)
  -> TraceFilter (CORS+1, from starter-monitor)
    -> EnvTagFilter (CORS+2, from starter-env)
      -> RequestBodyCache (Integer.MIN_VALUE+500)
        -> ApiEncryptFilter (REQUEST_BODY_CACHE_FILTER+1)
          -> OrderedRequestContextFilter (-105, Spring Boot)
            -> TenantContextFilter (-104, from starter-tenant)
              -> ApiAccessLogFilter (-103)
                -> XssFilter (-102)
                  -> Spring Security Filter Chain (-100)
                    -> TenantSecurityFilter (-99)
                      -> FlowableFilter (-98)
                        -> DemoFilter (Integer.MAX_VALUE)
```

## Configuration Properties

```yaml
develop:
  web:
    admin-api:
      prefix: /admin-api
      controller: '**.controller.admin.**'
    app-api:
      prefix: /app-api
      controller: '**.controller.app.**'
  xss:
    enable: true                    # Default: enabled
    exclude-urls:                   # URLs to skip XSS filtering
      - /admin-api/system/notice/*
  api-encrypt:
    enable: false                   # Default: disabled
  access-log:
    enable: true                    # Default: enabled
  demo: false                       # Demo mode
```

## Code Examples

```java
// Module-level Swagger grouping
@Bean
public GroupedOpenApi systemGroupedOpenApi() {
    return DevelopSwaggerAutoConfiguration.buildGroupedOpenApi("system", "system");
}

// Global exception — works automatically
@GetMapping("/user/{id}")
public CommonResult<UserVO> get(@PathVariable Long id) {
    UserDO user = userService.get(id);
    if (user == null) {
        throw new ServiceException(ErrorCodeConstants.USER_NOT_FOUND);
    }
    return CommonResult.success(UserConvert.INSTANCE.convert(user));
}
```

## 注意事项

- `GlobalResponseBodyHandler` 包装响应时，**必须排除** `CommonResult` 自身（否则包装两次）和 `String` 类型（String 需特殊处理，否则会转为 `{"code":0,"msg":"","data":"xxx"}` 而非纯字符串）
- XSS 过滤仅对字符串字段生效；富文本场景（如通知公告、富文本编辑器内容）需通过 `develop.xss.exclude-urls` 配置绕过
- API 日志过滤器中应避免记录敏感字段（密码、Token），通过 `@ApiAccessLog(sensitiveFields)` 配置
- Knife4j 与 SpringDoc 版本严格匹配，依赖由 `develop-dependencies` BOM 统一管理；不可随意升级单方版本
- `CacheRequestBodyFilter` 是多个过滤器（ApiAccessLog, Xss, ApiEncrypt）能够读取请求体的前提，其顺序必须在这些过滤器之前
- 全局异常处理器中 `ServiceException` 使用 WARN 级别日志（非 ERROR），避免业务异常污染 ERROR 告警
- `DemoFilter` 仅在 `develop.demo=true` 时启用，生产环境切勿开启（会阻止所有 POST/PUT/DELETE 操作）
