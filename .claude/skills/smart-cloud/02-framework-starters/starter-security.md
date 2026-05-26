---
name: starter-security
description: Token-based authentication and authorization via Spring Security — token filter, permission service, operation logging, Feign header propagation, and mock mode
type: project
---

# develop-spring-boot-starter-security

## Overview

基于 Token（无状态）的认证授权模块。整合 Spring Security，通过 `OAuth2TokenCommonApi` Feign 客户端校验 Token、`PermissionCommonApi` 鉴权。支持多终端（管理端 ADMIN=2、移动端 MEMBER=1）、Mock 模式（本地开发）、操作日志、Feign 调用身份传播。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **5 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.security` | `com.develop.mvp.pk.framework.operatelog`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-security AutoConfiguration.imports
com.develop.mvp.pk.framework.security.config.DevelopSecurityRpcAutoConfiguration
com.develop.mvp.pk.framework.security.config.DevelopSecurityAutoConfiguration
com.develop.mvp.pk.framework.security.config.DevelopWebSecurityConfigurerAdapter
com.develop.mvp.pk.framework.operatelog.config.DevelopOperateLogConfiguration
com.develop.mvp.pk.framework.operatelog.config.DevelopOperateLogRpcAutoConfiguration
```

## Core Components

### 1. DevelopSecurityAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.security.config`

Provides essential security beans:

- **TokenAuthenticationFilter** (`OncePerRequestFilter`):
  - Token extraction strategies (tried in order):
    1. **Header**: reads from `develop.security.token-header` (default: `Authorization`)
    2. **Parameter**: reads from `develop.security.token-parameter` (default: `token`, fallback for WebSocket)
    3. **Mock**: when `develop.security.mock-enable=true`, uses a mock authentication
  - Validates token via `OAuth2TokenCommonApi.checkAccessToken(token)`
  - Sets `SecurityContextHolder` with `LoginUser` on success
  - Uses `TransmittableThreadLocalSecurityContextHolderStrategy` for context propagation to child threads

- **SecurityFrameworkService** (Bean name: `"ss"`):
  ```java
  public class SecurityFrameworkServiceImpl implements SecurityFrameworkService {
      boolean hasPermission(String permission);
      boolean hasAnyPermissions(String... permissions);
      boolean hasRole(String role);
      boolean hasAnyRoles(String... roles);
      void clearLocalCache();        // Clear Guava 1-min cache
  }
  ```

- **PasswordEncoder**: `BCryptPasswordEncoder(strength)` — strength from `develop.security.passwordEncoderLength` (default: 4)
- **AuthenticationEntryPointImpl**: 401 handler (unauthenticated)
- **AccessDeniedHandlerImpl**: 403 handler (insufficient permissions)
- **SecurityContextHolder strategy**: sets to `TransmittableThreadLocalSecurityContextHolderStrategy` via `MethodInvokingFactoryBean`

### 2. DevelopWebSecurityConfigurerAdapter

Implements `SecurityFilterChain`:

```java
@AutoConfiguration
@AutoConfigureOrder(-1)   // Before Spring Security's default config
@EnableMethodSecurity(securedEnabled = true)
public class DevelopWebSecurityConfigurerAdapter {
    // Injects: WebProperties, SecurityProperties, AuthenticationEntryPoint,
    //          AccessDeniedHandler, TokenAuthenticationFilter, List<AuthorizeRequestsCustomizer>

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // - CSRF disabled (stateless token-based)
        // - SessionCreationPolicy.STATELESS
        // - Frame options disabled (for H2 console, etc.)
        // - CORS enabled (default)
        // - @PermitAll annotations auto-discovered (scans RequestMappingHandlerMapping)
        // - develop.security.permit-all-urls applied
        // - AuthorizeRequestsCustomizer beans collected — modules register custom URL rules
        // - Fallback: anyRequest().authenticated()
        // - TokenAuthenticationFilter before UsernamePasswordAuthenticationFilter
    }
}
```

**AuthorizeRequestsCustomizer**: `@FunctionalInterface`, allows each module to register additional permit-all or restricted URL patterns.

### 3. DevelopSecurityRpcAutoConfiguration

- `@EnableFeignClients` scanning `OAuth2TokenCommonApi` and `PermissionCommonApi`
- **LoginUserRequestInterceptor**: Feign `RequestInterceptor` that propagates `LoginUser` in request headers to downstream services
- Note: This is the **first auto-config in the imports list** — must be registered before DevelopSecurityAutoConfiguration

### 4. DevelopOperateLogConfiguration

**Location:** `com.develop.mvp.pk.framework.operatelog.config`

- `@EnableLogRecord(tenant = "")` — enables `mzt-log-api` for method-level operation logging
- `ILogRecordService` implementation: `LogRecordServiceImpl`

**Note:** The class name in the actual source is `DevelopOperateLogConfiguration` (NOT `DevelopOperateLogAutoConfiguration`).

### 5. DevelopOperateLogRpcAutoConfiguration

- Feign-based operation log recording (microservices mode)
- Excluded in monolithic mode via `spring.autoconfigure.exclude`

## LoginUser Model

**Location:** `com.develop.mvp.pk.framework.security.core.LoginUser`

```java
public class LoginUser {
    private Long id;
    private UserTypeEnum userType;     // MEMBER=1, ADMIN=2
    private Map<String, String> info;  // Extra information
    private Long tenantId;             // Current tenant ID
    private List<String> scopes;       // OAuth2 scopes
    private Long expiresTime;          // Token expiry timestamp
    private transient Map<String, Object> context;  // Per-request context (not serialized)
    private Long visitTenantId;        // Visited tenant (cross-tenant scenario)
}
```

## SecurityFrameworkUtils

**Location:** `com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils`

Static utility methods (based on `RequestContextHolder` / `SecurityContextHolder`):

```java
String obtainAuthorization(HttpServletRequest request);       // Extract token from header
String obtainAuthorization(HttpServletRequest request, String header, String parameter); // Multi-source token
LoginUser getLoginUser();                                     // Current LoginUser from SecurityContext
Long getLoginUserId();                                        // Current user ID
UserTypeEnum getLoginUserType();                              // Current user type
List<String> getLoginUserRoleIds();                           // Current user role IDs
boolean skipPermissionCheck();                                // Whether to skip permission check (mock mode)
```

## Configuration Properties

```yaml
develop:
  security:
    token-header: Authorization              # Token HTTP header name
    token-parameter: token                   # Token query param name (WebSocket fallback)
    mock-enable: false                       # Mock mode (local dev only — set to true)
    mock-secret: test                        # Mock secret key
    permit-all-urls:                         # URLs that bypass authentication
      - /app-api/**
      - /actuator/health
      - /develop-doc/**
    password-encoder-length: 4               # BCrypt strength (4-31, higher = more secure but slower)
```

## Filter Chain Order

```
CORS (MIN_VALUE)
  -> Trace (+1)
    -> EnvTag (+2)
      -> RequestBodyCache (MIN_VALUE+500)
        -> ApiEncrypt (+1)
          -> TenantContextFilter (-104)
            -> ApiAccessLogFilter (-103)
              -> XssFilter (-102)
                -> Spring Security Filter Chain (-100, from SecurityProperties)
                  -> TokenAuthenticationFilter (before UsernamePasswordAuthenticationFilter)
                    -> TenantSecurityFilter (-99)
                      -> FlowableFilter (-98)
                        -> DemoFilter (MAX_VALUE)
```

## Operation Log Usage

```java
// Record operation log via @LogRecord annotation
@LogRecord(
    type = "system",
    subType = "user",
    bizNo = "{{#user.id}}",
    success = "创建了用户【{{#user.nickname}}】",
    fail = "创建用户失败"
)
public void createUser(UserCreateReqVO reqVO) {
    // business logic...
}
```

## Code Examples

```java
// Method-level permission check
@PreAuthorize("@ss.hasPermission('system:user:create')")
@PostMapping("/create")
public CommonResult<Long> create(@Valid @RequestBody UserCreateReqVO reqVO) { ... }

// Role-based check
@PreAuthorize("@ss.hasAnyRoles('admin', 'super_admin')")
@DeleteMapping("/delete")
public CommonResult<Boolean> delete(@RequestParam Long id) { ... }

// Get current user
@GetMapping("/me")
public CommonResult<UserRespVO> me() {
    LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
    Long userId = SecurityFrameworkUtils.getLoginUserId();
    // ...
}
```

## 注意事项

- Feign 调用时 `LoginUserRequestInterceptor` 自动传递 LoginUser header，目标服务通过 `TokenAuthenticationFilter` 的 Header Relay 策略获取
- `DevelopWebSecurityConfigurerAdapter` 直接注册在 `AutoConfiguration.imports` 中（以 `@AutoConfiguration` 形式），而非通过 `@Import`
- 操作日志功能在 `mzt-log-api` 基础上封装，Model 类名为 `DevelopOperateLogConfiguration`（非 `DevelopOperateLogAutoConfiguration`）
- Mock 模式仅用于本地开发，生产环境必须关闭（`develop.security.mock-enable: false`），否则任何人都可绕过认证
- `context` 字段标记为 `transient`，不参与序列化，用于当前请求内的临时数据传递（如数据权限上下文）
- `@PermitAll` 同时扫描方法级和类级注解，发现后自动添加所有 HTTP 方法的免登录规则
- 权限缓存 1 分钟粒度（Guava Cache），授权变更后最多需等待 1 分钟生效；可通过 `SecurityFrameworkService.clearLocalCache()` 手动清除
