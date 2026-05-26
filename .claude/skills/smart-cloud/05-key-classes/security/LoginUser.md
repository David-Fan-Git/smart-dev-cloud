---
name: LoginUser
description: Value object holding authenticated user info stored in SecurityContext
type: project
---

# LoginUser

## 功能定位

LoginUser 是平台的认证上下文核心值对象，位于 `develop-spring-boot-starter-security` 的 `core` 包下。它存储在 `SecurityContextHolder` 中，是**整个请求生命周期内获取当前用户信息的唯一入口**。

核心职责：
- **存储用户身份**：用户 ID、用户类型、租户编号、过期时间
- **存储授权范围**：OAuth2 Scopes（权限范围，如 `read`, `write`）
- **存储额外信息**：`info` Map 存储昵称、部门 ID 等运行时信息
- **请求级缓存**：`context` Map 提供基于 LoginUser 维度的临时缓存，避免在一次请求中重复远程调用
- **跨租户标识**：`visitTenantId` 标记跨租户访问场景

与其他模块的关系：
- **TokenAuthenticationFilter** 创建并设置 LoginUser
- **SecurityFrameworkUtils** 存取当前用户
- **DeptDataPermissionRule** 利用 `context` 缓存部门数据权限结果
- **SecurityFrameworkServiceImpl** 读取 `scopes` 做 Scope 校验
- **各业务 Service** 读取 `userId`, `tenantId` 等

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Value Object** | 纯数据容器，无业务行为 | `@Data` + getter/setter |
| **Context Object** | 请求维度的上下文数据缓存 | `context` Map + `setContext()`/`getContext()` |
| **DTO** | 跨模块传递认证信息 | Gateway -> 业务服务通过 JSON 序列化 |
| **Memento** | 暂存计算密集型结果 | 部门数据权限结果缓存到 context |

## 核心逻辑流程

### LoginUser 生命周期

```
[创建]
  TokenAuthenticationFilter 解析 Token/Header
    -> new LoginUser()
    -> setter 链式调用: .setId().setUserType().setTenantId()...
    -> 通过 OAuth2AccessTokenCheckRespDTO 构建

[设置]
  SecurityFrameworkUtils.setLoginUser(loginUser, request)
    -> SecurityContextHolder.getContext().setAuthentication(authentication)
       (包装为 UsernamePasswordAuthenticationToken)
    -> request.setAttribute("LOGIN_USER_ID", loginUser.getId())
       (双重写入，供 ApiAccessLogFilter 使用)

[使用]
  同一请求中的任意组件通过 SecurityFrameworkUtils 获取:
    -> getLoginUser() -> 从 SecurityContext 获取
    -> getLoginUserId() -> .getId()
    -> getLoginUserNickname() -> .getInfo().get("nickname")
    -> getLoginUserDeptId() -> .getInfo().get("deptId")

[缓存利用]
  DeptDataPermissionRule:
    -> loginUser.getContext(CONTEXT_KEY, DeptDataPermissionRespDTO.class)
    -> MISS: 远程调用 + setContext()
    -> HIT: 直接使用

[清理]
  请求结束:
    -> SecurityContextHolder.clearContext()
    -> 由 SecurityContextHolderStrategy 自动清理
```

## 关键代码剖析

```java
@Data
public class LoginUser {

    // ========== 用户身份信息 ==========
    private Long id;                          // 用户编号 (主键)
    private Integer userType;                 // 用户类型 (UserTypeEnum: 1=管理员, 2=会员)
    private Map<String, String> info;         // 额外信息 {nickname, deptId}
    private Long tenantId;                    // 租户编号
    private List<String> scopes;              // OAuth2 授权范围
    private LocalDateTime expiresTime;        // 过期时间

    // ========== 请求上下文 (不持久化) ==========
    @JsonIgnore
    private Map<String, Object> context;      // 请求级缓存
    private Long visitTenantId;               // 跨租户访问时的目标租户编号
}
```

### info 字段详解

```java
// 常量定义
public static final String INFO_KEY_NICKNAME = "nickname";
public static final String INFO_KEY_DEPT_ID = "deptId";

// 构建逻辑 (在 OAuth2TokenServiceImpl.buildUserInfo 中)
Map<String, String> info = MapUtil.builder(
    LoginUser.INFO_KEY_NICKNAME, user.getNickname()
).put(LoginUser.INFO_KEY_DEPT_ID, StrUtil.toStringOrNull(user.getDeptId())).build();
```

`info` 字段的作用：
- `nickname`：页面展示用户昵称，避免每次都查数据库
- `deptId`：数据权限过滤需要，避免从数据库重新查询
- 可通过 `MapUtil.getStr(loginUser.getInfo(), LOGIN_USER_HEADER)` 便捷读取

### context 缓存机制

```java
@JsonIgnore
private Map<String, Object> context;

public void setContext(String key, Object value) {
    if (context == null) {
        context = new HashMap<>();  // 懒初始化
    }
    context.put(key, value);
}

public <T> T getContext(String key, Class<T> type) {
    return MapUtil.get(context, key, type);
}
```

使用场景示例（DeptDataPermissionRule）：
```java
DeptDataPermissionRespDTO deptDataPermission =
    loginUser.getContext(CONTEXT_KEY, DeptDataPermissionRespDTO.class);
if (deptDataPermission == null) {
    deptDataPermission = permissionApi.getDeptDataPermission(loginUser.getId()).getCheckedData();
    loginUser.setContext(CONTEXT_KEY, deptDataPermission);  // 缓存
}
```

**为什么 context 用 @JsonIgnore？**
- LoginUser 在 Gateway 转发时会被序列化为 JSON 放入 Header（`login-user` 请求头）
- context 是运行时数据，不应被序列化传递
- 接收端重新创建自己的 LoginUser 实例，context 为空，按需重新计算

### visitTenantId 的作用

```java
// SecurityFrameworkUtils.skipPermissionCheck()
public static boolean skipPermissionCheck() {
    LoginUser loginUser = getLoginUser();
    if (loginUser == null || loginUser.getVisitTenantId() == null) { return false; }
    return ObjUtil.notEqual(loginUser.getVisitTenantId(), loginUser.getTenantId());
}
```

当 `visitTenantId != tenantId` 时，表示当前用户在跨租户访问其他租户的数据。由于权限数据是租户隔离的，跨租户场景下无法进行权限校验，所有 `@PreAuthorize` 检查都返回 true。

## 调用链

### 上游创建者

| 创建者 | 场景 | 数据来源 |
|--------|------|----------|
| TokenAuthenticationFilter (servlet) | Header 透传 | request header[login-user] JSON 反序列化 |
| TokenAuthenticationFilter (servlet) | Token 校验 | OAuth2TokenCommonApi.checkAccessToken Feign 调用 |
| TokenAuthenticationFilter (servlet) | Mock 模式 | 根据 mockSecret + userId 构造 |
| TokenAuthenticationFilter (gateway) | Gateway 层 | WebClient 调用 checkAccessToken 接口 |

### 下游消费者

| 消费者 | 读取内容 | 用途 |
|--------|----------|------|
| SecurityFrameworkServiceImpl | id, scopes, visitTenantId | 权限/角色/Scope 校验 |
| DeptDataPermissionRule | id, context | 部门数据权限计算与缓存 |
| SecurityFrameworkUtils | 全部 | 提供 getLoginUser/getLoginUserId 等静态方法 |
| 业务 Service | id, tenantId | 数据创建者、数据权限过滤 |
| ApiAccessLogFilter | id, userType | 记录操作日志的用户信息 |

## 配置与条件

| 字段 | 设置时机 | 说明 |
|------|----------|------|
| `id` | Token 校验时从 tokenInfo 获取 | 非空 |
| `userType` | Token 校验时从 tokenInfo 获取 | UserTypeEnum.ADMIN=1, MEMBER=2 |
| `info` | OAuth2TokenServiceImpl.buildUserInfo() | nickname + deptId |
| `tenantId` | Token 校验时从 tokenInfo 获取 | 多租户场景 |
| `scopes` | Token 校验时从 tokenInfo 获取 | OAuth2 授权范围 |
| `expiresTime` | Token 校验时从 tokenInfo 获取 | 用于判断 Token 是否有效 |
| `context` | 业务组件运行时写入 | 懒初始化，@JsonIgnore |
| `visitTenantId` | 跨租户操作时由业务代码设置 | 控制权限跳过 |

## 生产级关注点

### 1. 线程安全

- **读多写少**：LoginUser 在一次请求内基本是只读的（除了 context 写入）
- **context 非线程安全**：`HashMap` 不是线程安全的，但同一个请求的所有操作在单一线程中执行，不需要并发保护
- **跨线程问题**：`SecurityContextHolder` 默认使用 `ThreadLocal`，在异步线程中丢失。框架使用 `TransmittableThreadLocalSecurityContextHolderStrategy` 代替默认策略，确保线程池场景下上下文传递

### 2. 序列化/反序列化

- Gateway 序列化场景：`LoginUser` -> JSON -> `login-user` Header -> 业务服务接收
- `@JsonIgnore` 保证 context 不被序列化
- 反序列化需要标准的无参构造器 + setter（`@Data` 提供）
- `expiresTime` 为 `LocalDateTime` 类型，Jackson 需要 `jsr310` 模块支持

### 3. 内存使用

- 单次请求一个 LoginUser 实例，内存占用很小
- `context` 中的缓存数据随请求结束被 GC
- `info` Map 通常只有 2 个条目
- 无内存泄漏风险（只要 SecurityContext 在请求结束时被清理）

### 4. 安全考量

- `@JsonIgnore` 防止 context 中的敏感数据被泄露到其他服务
- `LoginUser` 不应包含密码等敏感信息
- Scopes 做业务鉴权时应当与用户身份结合使用

### 5. Gateway 版本的 LoginUser

Gateway 模块有自己的简化版 `LoginUser`，位于 `develop-gateway` 模块，区别如下：
- 无 `context` 字段
- 无 `visitTenantId` 字段
- 序列化后通过 Header 转发

### 6. 调试提示

`info` 字段内容可通过 `SecurityFrameworkUtils.getLoginUserNickname()` / `getLoginUserDeptId()` 直接获取，无需从 `LoginUser.info` 手动解析 Map。
