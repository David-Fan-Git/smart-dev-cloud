---
name: SecurityFrameworkServiceImpl
description: Bean "ss" that provides permission and role checking with caching, used in @PreAuthorize
type: project
---

# SecurityFrameworkServiceImpl

## 功能定位

SecurityFrameworkServiceImpl 是权限校验的门面服务，位于 `develop-spring-boot-starter-security` 的 `core.service` 包下。它以 Bean 名 `"ss"` 注册到 Spring 容器，**专门用于 `@PreAuthorize("@ss.hasPermission(...)")` 注解的 SpEL 表达式调用**。

核心职责：
- **权限校验**：判断当前用户是否拥有指定权限标识（`system:user:create` 等）
- **角色校验**：判断当前用户是否拥有指定角色标识（`system-admin` 等）
- **Scope 校验**：判断当前用户的 OAuth2 授权范围是否覆盖指定 scope
- **跨租户跳过**：跨租户访问时自动跳过权限校验

它封装了对 `PermissionCommonApi`（Feign 客户端）的远程调用，并引入 Guava LoadingCache 缓存，降低对 system-server 服务的调用压力。

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Facade** | 封装 PermissionCommonApi 的远程调用，提供简洁的校验接口 | `hasAnyPermissions()`, `hasAnyRoles()`, `hasAnyScopes()` |
| **Decorator** | 在 PermissionCommonApi 之上叠加 Guava LoadingCache 缓存层 | `hasAnyPermissionsCache.get()` |
| **Strategy** | 不同校验维度独立处理路径 | permission / role / scope 各自独立的方法 |
| **Proxy / Cache-Aside** | 先查缓存，miss 后调远程，结果回填 | `LoadingCache` + `CacheLoader` |
| **Guard Clause** | 前置条件快速返回 | `skipPermissionCheck()` -> true; `userId == null` -> false |

## 核心逻辑流程

```
hasAnyPermissions(String... permissions)
  |
  +-- Guard 1: skipPermissionCheck()
  |    LoginUser.getVisitTenantId() != null && != LoginUser.getTenantId()
  |    跨租户访问 -> return true (无法进行权限校验)
  |
  +-- Guard 2: getLoginUserId() == null
  |    未登录 -> return false
  |
  +-- hasAnyPermissionsCache.get(KeyValue<userId, permissions>)
       |
       +-- [Cache HIT] 返回缓存值
       +-- [Cache MISS] CacheLoader.load():
       |    -> permissionApi.hasAnyPermissions(userId, permissions)
       |    -> 返回 CommonResult<Boolean>
       |    -> getCheckedData() 解包
       |    -> 写入缓存 (TTL: 1 分钟, maxSize: 1024)
       |
       +-- return boolean

hasAnyRoles(String... roles)
  -- 与上述流程完全相同，使用 hasAnyRolesCache --
  -- 调用 permissionApi.hasAnyRoles(userId, roles) --

hasAnyScopes(String... scopes)
  |
  +-- Guard 1: skipPermissionCheck() -> return true
  +-- Guard 2: getLoginUser() == null -> return false
  |
  +-- 不走远程调用，直接从 LoginUser.getScopes() 判断
  |    CollUtil.containsAny(user.getScopes(), Arrays.asList(scope))
  +-- return boolean
```

## 关键代码剖析

```java
@AllArgsConstructor
public class SecurityFrameworkServiceImpl implements SecurityFrameworkService {

    private final PermissionCommonApi permissionApi;

    // 权限缓存: LoadingCache<KeyValue<userId, permissions>, Boolean>
    // TTL = 1 分钟，maxSize = 1024
    private final LoadingCache<KeyValue<Long, List<String>>, Boolean> hasAnyPermissionsCache =
            buildCache(Duration.ofMinutes(1L),
                    new CacheLoader<>() {
                        public Boolean load(KeyValue<Long, List<String>> key) {
                            return permissionApi.hasAnyPermissions(key.getKey(),
                                    key.getValue().toArray(new String[0])).getCheckedData();
                        }
                    });

    // 角色缓存: 结构同上
    private final LoadingCache<KeyValue<Long, List<String>>, Boolean> hasAnyRolesCache = ...;

    @Override
    public boolean hasAnyPermissions(String... permissions) {
        if (skipPermissionCheck()) { return true; }
        Long userId = getLoginUserId();
        if (userId == null) { return false; }
        return hasAnyPermissionsCache.get(new KeyValue<>(userId, Arrays.asList(permissions)));
    }

    @Override
    public boolean hasAnyScopes(String... scope) {
        if (skipPermissionCheck()) { return true; }
        LoginUser user = SecurityFrameworkUtils.getLoginUser();
        if (user == null) { return false; }
        return CollUtil.containsAny(user.getScopes(), Arrays.asList(scope));
    }
}
```

### 缓存设计剖析

**为什么用 Guava LoadingCache 而不是 Redis?**

- 权限校验是高频操作（每个接口都可能调用 `@PreAuthorize`）
- 权限变更频率低，1 分钟内的不一致可以接受
- LoadingCache 是本地堆内缓存，零网络开销，延迟极低
- 支持自动的 `refreshAfterWrite` / `expireAfterWrite` 策略

**缓存 Key 设计**:
```java
KeyValue<Long, List<String>>  // userId + 权限/角色标识列表
```
- 使用 `KeyValue` 复合键（框架内定义，实现了 `equals`/`hashCode`）
- 注意：`List<String>` 作为 key 的一部分，其内容顺序很重要

**TTL 选择 1 分钟的原因**:
- 太短（如 10s）：缓存命中率低，失去缓存意义
- 太长（如 5min）：用户权限变更后，最长 5 分钟不生效
- 1 分钟是权衡：用户授权后在 1 分钟内生效，同时大幅降低 RPC 频率

**为什么不需要缓存 Scope？**

Scope 信息直接存储在 `LoginUser.scopes` 中，而 LoginUser 已经存储在 SecurityContext（线程级），无需缓存层。

### skipPermissionCheck() 详解

```java
public static boolean skipPermissionCheck() {
    LoginUser loginUser = getLoginUser();
    if (loginUser == null) { return false; }
    if (loginUser.getVisitTenantId() == null) { return false; }
    return ObjUtil.notEqual(loginUser.getVisitTenantId(), loginUser.getTenantId());
}
```

当跨租户访问时（如租户 A 的管理员查看租户 B 的数据），由于权限数据都是租户隔离的，跨租户场景下无法做权限校验，因此直接跳过。

## 调用链

```
[Upstream]
  @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    -> Spring AOP (MethodSecurityInterceptor)
       -> SecurityFrameworkServiceImpl.hasAnyPermissions()
          |
          +-- [缓存命中] 直接返回
          +-- [缓存未命中]
               -> PermissionCommonApi (Feign 客户端)
                  -> HTTP 调用 system-server
                     -> PermissionApiImpl (REST 端点)
                        -> PermissionService.hasAnyPermissions()
                           -> AdminUserMapper / RoleMapper / MenuMapper
                              -> MySQL 查询

[Downstream Callers]
  - SecurityFrameworkService (接口) -- 也可直接在业务代码中注入使用
  - SecurityFrameworkUtils (工具类) -- 不经过缓存，直接读 SecurityContext
```

## 配置与条件

| 条件 | 行为 |
|------|------|
| `skipPermissionCheck() == true` | 跨租户访问，跳过权限校验，返回 true |
| `getLoginUserId() == null` | 用户未登录，返回 false |
| 缓存 TTL 内 | 返回缓存结果，不触发 RPC |
| 缓存 TTL 过期 | 下次访问触发 CacheLoader.load()，RPC 调用 |

### Bean 注册

该 Bean 在 `DevelopWebSecurityConfigurerAdapter` 中手动注册为 `"ss"`:
```java
@Bean("ss")
public SecurityFrameworkService securityFrameworkService() {
    return new SecurityFrameworkServiceImpl(permissionApi);
}
```

## 生产级关注点

### 1. 缓存穿透与安全

- **LoadingCache 天然防穿透**：同一个 key 的并发请求只执行一次 `load()`，其他线程等待结果
- 异常处理：如果 `permissionApi` 调用失败抛出异常，`LoadingCache` 会向上传播异常，调用方收到 `UncheckedExecutionException`

### 2. 性能考虑

- 每次 `@PreAuthorize` 调用都执行一次缓存 get，但只在 cache miss 时产生 Feign RPC
- `List<String>` 作为 key 的一部分，`Arrays.asList()` 生成的 list 是固定大小的 `ArrayList`，其 `equals` 实现会逐元素比较
- 建议：将权限/角色列表合理分组，避免 `@PreAuthorize` 传递过多参数

### 3. 线程安全

- `LoadingCache` 内部使用 `ConcurrentHashMap`，线程安全
- `SecurityFrameworkServiceImpl` 是无状态的（`permissionApi` 是 final 字段），线程安全
- `skipPermissionCheck()` 从 `SecurityContextHolder` 获取，该上下文与请求线程绑定

### 4. 与 PermissionApiImpl 的区别

| 类 | 角色 | 路径 |
|------|------|------|
| `SecurityFrameworkServiceImpl` | Feign 客户端调用方 | 在 security starter 中 |
| `PermissionApiImpl` | Feign 服务端提供方 | 在 system-server 中 |

SecurityFrameworkServiceImpl 是**消费者**，PermissionApiImpl 是**生产者**。

### 5. 监控与日志

- 缓存命中率没有显式暴露为 metrics（可通过 JXM 查看 LoadingCache 统计）
- `permissionApi` 调用失败时由 Feign 客户端记录错误日志
- 权限校验结果不具备日志，避免日志过多
