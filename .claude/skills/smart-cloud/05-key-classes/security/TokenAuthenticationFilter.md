---
name: TokenAuthenticationFilter
description: Token authentication filter that validates access tokens and sets SecurityContext
type: project
---

# TokenAuthenticationFilter

## 功能定位

TokenAuthenticationFilter 是 Spring Security 过滤器链中的核心认证组件，位于 `develop-spring-boot-starter-security` 模块的 `core.filter` 包下。它继承 `OncePerRequestFilter`，确保每次请求只执行一次过滤逻辑。

职责是**从 HTTP 请求中提取并验证 Token，将认证用户信息（LoginUser）设置到 SecurityContext 中**。它支撑平台的三种认证模式，形成一个**降级链**：

1. **Header 透传模式**：Gateway 已经完成认证，通过 `login-user` 请求头透传用户 JSON
2. **Token 校验模式**：直接访问的场景（如 Nginx -> 服务），从 Authorization Header 提取 Token 并调用 OAuth2 服务校验
3. **Mock 调试模式**：本地开发环境，通过配置模拟任意用户登录

位置在体系中的关系：
- `TokenAuthenticationFilter` (servlet, security starter) -- 用于微服务内部，基于 Servlet
- `TokenAuthenticationFilter` (webflux, gateway) -- 用于网关层，基于 WebFlux
- 两套实现，不同的技术栈，相同的认证语义

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Chain of Responsibility (Filter)** | 嵌入 Spring Security 过滤器链，通过 OncePerRequestFilter 实现 | `extends OncePerRequestFilter` + `doFilterInternal()` |
| **Strategy / Fallback Chain** | 三种认证策略依次尝试，首胜即止，逐级降级 | Header -> Token -> Mock 的 `if (loginUser == null)` 链 |
| **Facade** | 封装 Token 校验、用户构建、异常处理等复杂逻辑 | 委托给 `OAuth2TokenCommonApi`、`GlobalExceptionHandler` |
| **DTO** | 通过 `OAuth2AccessTokenCheckRespDTO` 传递校验结果 | Feign 调用返回 `CommonResult<OAuth2AccessTokenCheckRespDTO>` |

## 核心逻辑流程

```
doFilterInternal(request, response, chain)
  |
  +-- Strategy 1: buildLoginUserByHeader(request)
  |    读取 header["login-user"]
  |    -> URLDecoder.decode (解决中文乱码)
  |    -> JsonUtils.parseObject -> LoginUser
  |    -> 校验 userType 与请求路径匹配 (如果非 null)
  |    -> 成功返回 LoginUser，失败抛 AccessDeniedException
  |
  +-- IF loginUser == null (Strategy 1 未命中):
  |    SecurityFrameworkUtils.obtainAuthorization(request)
  |    -> 从 securityProperties 配置的 header 或 query parameter 获取 token
  |    -> 自动去除 "Bearer " 前缀
  |    -> 如果 token 为空，跳过
  |
  |    +-- Strategy 2: buildLoginUserByToken(token, userType)
  |    |   调用 oauth2TokenApi.checkAccessToken(token) (Feign -> system-server)
  |    |   -> 返回 null 表示 token 无效 (ServiceException 被吞掉)
  |    |   -> 返回有效数据:
  |    |       校验 UserType 与路径类型匹配 (admin-api / app-api)
  |    |       构建 LoginUser(id, userType, info, tenantId, scopes, expiresTime)
  |    |
  |    +-- IF loginUser == null (Strategy 2 未命中):
  |         Strategy 3: mockLoginUser(request, token, userType)
  |          仅在 securityProperties.getMockEnable() == true 时生效
  |          检查 token 是否以 mockSecret 开头
  |          提取 userId -> 构建 Mock LoginUser
  |          注意: 线上环境必须关闭!
  |
  +-- IF loginUser != null:
  |    SecurityFrameworkUtils.setLoginUser(loginUser, request)
  |    -> 创建 UsernamePasswordAuthenticationToken
  |    -> 设置到 SecurityContextHolder
  |    -> 额外设置到 request attribute (供 ApiAccessLogFilter 使用)
  |
  +-- chain.doFilter(request, response)
      继续过滤器链
```

## 关键代码剖析

```java
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final GlobalExceptionHandler globalExceptionHandler;
    private final OAuth2TokenCommonApi oauth2TokenApi;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 策略1: 从 Header 直接构建 (Gateway 已认证过的透传场景)
        LoginUser loginUser = buildLoginUserByHeader(request);

        // 策略2 && 策略3: Token 校验 + Mock
        if (loginUser == null) {
            String token = SecurityFrameworkUtils.obtainAuthorization(request,
                    securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
            if (StrUtil.isNotEmpty(token)) {
                Integer userType = WebFrameworkUtils.getLoginUserType(request);
                try {
                    loginUser = buildLoginUserByToken(token, userType);
                    if (loginUser == null) {
                        loginUser = mockLoginUser(request, token, userType);
                    }
                } catch (Throwable ex) {
                    // 将异常包装为 CommonResult 写入响应，中断过滤链
                    CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                    ServletUtils.writeJSON(response, result);
                    return;
                }
            }
        }

        if (loginUser != null) {
            SecurityFrameworkUtils.setLoginUser(loginUser, request);
        }
        chain.doFilter(request, response);
    }
}
```

关键细节：

1. **UserType 比对逻辑**（在 `buildLoginUserByToken` 和 `buildLoginUserByHeader` 中均有）:
   ```java
   if (userType != null && ObjectUtil.notEqual(accessToken.getUserType(), userType)) {
       throw new AccessDeniedException("错误的用户类型");
   }
   ```
   - `userType` 从 `WebFrameworkUtils.getLoginUserType(request)` 获取，通过路径前缀解析: `/admin-api/*` -> 1, `/app-api/*` -> 2
   - `userType == null` 时表示路径无类型要求（如 `/ws/*` WebSocket 连接），跳过比对

2. **Token 校验异常处理**:
   - `ServiceException` (如 token 过期/不存在) 被 catch 后返回 `null`，而非抛出异常。这是有意为之——让无需登录的接口继续处理
   - 其他异常（如网络超时）通过 `GlobalExceptionHandler.allExceptionHandler` 写入 JSON 响应并 `return`，中断链

3. **Mock 模式实现**:
   ```java
   private LoginUser mockLoginUser(HttpServletRequest request, String token, Integer userType) {
       if (!securityProperties.getMockEnable()) { return null; }
       if (!token.startsWith(securityProperties.getMockSecret())) { return null; }
       Long userId = Long.valueOf(token.substring(securityProperties.getMockSecret().length()));
       return new LoginUser().setId(userId).setUserType(userType)
               .setTenantId(WebFrameworkUtils.getTenantId(request));
   }
   ```
   Token 格式: `{mockSecret}{userId}`，直接构造 LoginUser 而不经过 OAuth2 服务

4. **setLoginUser 双写机制**:
   ```java
   public static void setLoginUser(LoginUser loginUser, HttpServletRequest request) {
       Authentication authentication = buildAuthentication(loginUser, request);
       SecurityContextHolder.getContext().setAuthentication(authentication);
       // 额外设置到 request，解决 Filter 顺序问题
       if (request != null) {
           WebFrameworkUtils.setLoginUserId(request, loginUser.getId());
           WebFrameworkUtils.setLoginUserType(request, loginUser.getUserType());
       }
   }
   ```
   - SecurityContextHolder: 标准 Spring Security 认证上下文
   - request attribute: 供 `ApiAccessLogFilter` 访问日志记录，因为该 Filter 在 Security Filter 链之前执行，无法从 SecurityContext 读取

## 调用链

```
[Upstream]
  Http Request
    -> Spring Security Filter Chain
       -> TokenAuthenticationFilter.doFilterInternal()
          |
          +-- [入口] 直接由 OncePerRequestFilter 调用
          +-- [异常时] GlobalExceptionHandler.allExceptionHandler()
          |    -> 将异常转为 CommonResult JSON 写入响应
          |
          +-- [Token 校验] OAuth2TokenCommonApi.checkAccessToken()
          |    -> Feign 远程调用 system-server
          |    -> OAuth2TokenServiceImpl.checkAccessToken()
          |       -> Redis 查询 Token -> DB 回填缓存
          |
          +-- [设置用户] SecurityFrameworkUtils.setLoginUser()
               -> SecurityContextHolder + request attribute

[Downstream]
  -> 后续 Filter (如 AuthorizationFilter)
  -> Controller
```

### 下游影响

当认证通过时，下游可以全局获取用户信息:
- `SecurityFrameworkUtils.getLoginUser()` -> 从 SecurityContext 获取
- `SecurityFrameworkUtils.getLoginUserId()` -> 获取当前用户 ID
- `SecurityFrameworkUtils.getLoginUserDeptId()` -> 从 `LoginUser.info.deptId` 获取
- `WebFrameworkUtils.getLoginUserId(request)` -> 从 request attribute 获取

## 配置与条件

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `develop.security.token-header` | `Authorization` | Token 的 HTTP Header 名 |
| `develop.security.token-parameter` | `token` | Token 的 Query 参数名 |
| `develop.security.mock-enable` | `false` | 是否启用 Mock 模式 |
| `develop.security.mock-secret` | (无默认) | Mock 模式的密钥前缀 |

该类的 bean 创建由 `DevelopWebSecurityConfigurerAdapter` 自动配置完成:
```java
// DevelopWebSecurityConfigurerAdapter 中添加
http.addFilterBefore(tokenAuthenticationFilter, LogoutFilter.class);
```

## 生产级关注点

### 1. 线程安全性

`TokenAuthenticationFilter` 是无状态的（只有 `final` 字段），天生线程安全。但 `SecurityContextHolder` 使用 `InheritableThreadLocal` 策略（而非默认的 `ThreadLocal`），结合 `TransmittableThreadLocalSecurityContextHolderStrategy`，确保异步场景下上下文正确传播。

### 2. 性能考虑

- **Header 透传是最优路径**：Gateway 已认证的场景下，仅做 JSON 反序列化 + URL 解码，不产生 RPC 调用
- **Token 校验产生 Feign 调用**：每次请求都会调用 system-server 的 checkAccessToken 接口（除非启用缓存，但当前版本未在此 Filter 层缓存）
- **避免重复解码**：Token 提取时自动去除 `Bearer ` 前缀，URL 解码仅对 Header 中的 JSON 执行

### 3. 错误处理

- **Server 端的 ServiceException 被吞掉**：返回 `null` 让请求继续，由 @PreAuthorize 或权限注解决定是否拒绝
- **其他异常写入 JSON 响应**：通过 `GlobalExceptionHandler.allExceptionHandler` 统一处理，返回标准 `CommonResult` 格式
- **响应已提交的处理**：如果 response 已经被 `chain.doFilter()` 提交，再次调用 `ServletUtils.writeJSON` 会抛 `IllegalStateException`，但标准的 Filter 链中不会出现此问题

### 4. 安全考量

- **Mock 模式必须关闭**：线上环境 `develop.security.mock-enable=false`，否则攻击者可以构造 `{mockSecret}{任意userId}` 模拟任意用户
- **Header 伪造防护**：Gateway 层的 `TokenAuthenticationFilter` 在入口处移除了 `login-user` 请求头，防止外部请求伪造用户信息
- **UserType 校验**: 防止 admin-api 的接口被 app 端 token 访问（或反过来）

### 5. 监控与日志

- 异常场景下通过 `GlobalExceptionHandler` 记录错误日志
- Token 校验失败时（`ServiceException`）不记录日志，避免无效刷新令牌等场景刷日志
- 当前版本未在此 Filter 显式添加 access log，但后续 Filter 和 `ApiAccessLogFilter` 会记录
