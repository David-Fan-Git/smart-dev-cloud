---
name: OAuth2TokenServiceImpl
description: OAuth2 access/refresh token lifecycle management with Redis caching
type: project
---

# OAuth2TokenServiceImpl

## 功能定位

OAuth2TokenServiceImpl 是平台的认证令牌核心管理服务，位于 `develop-module-system` 的 `service.oauth2` 包下。它管理 OAuth2 访问令牌（Access Token）和刷新令牌（Refresh Token）的完整生命周期。

核心职责：
- **Token 创建**：用户登录时创建 Access Token + Refresh Token 对
- **Token 校验**：验证 Token 有效性（未过期、未撤销）
- **Token 刷新**：用 Refresh Token 获取新的 Access Token
- **Token 撤销**：登出或禁用用户时删除 Token
- **定时清理**：清除过期的 Refresh Token 和 Access Token
- **用户信息嵌入**：在 Token 创建时将昵称、部门 ID 等用户信息写入 LoginUser.info

它是整个认证体系的"令牌仓库"，上游依赖（`AdminAuthService`、`TokenAuthenticationFilter`）通过它完成令牌的生命周期管理。

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Lifecycle Management** | 完整管理 Token 的创建 -> 校验 -> 续期 -> 撤销 -> 清理 | 每种操作对应独立方法 |
| **Cache-Aside** | Redis 作为校验缓存，先查缓存，miss 后查 DB 并回填 | `getAccessToken()` 方法 |
| **Fallback Chain** | 校验时多种策略回退 | access token -> refresh token -> 都不存在则 401 |
| **Refresh Token Pattern** | 长期有效的刷新令牌 + 短期有效的访问令牌 | 双 Token 机制 |
| **Transactional** | 关键操作使用事务保证一致性 | `@Transactional` 注解 |

## 核心逻辑流程

### 创建 Token (createAccessToken)

```
createAccessToken(userId, userType, clientId, scopes)
  |
  +-- 1. oauth2ClientService.validOAuthClientFromCache(clientId)
  |    从缓存获取 OAuth2 客户端配置 (包括 Token 有效期)
  |
  +-- 2. createOAuth2RefreshToken(userId, userType, clientDO, scopes)
  |    +-- 2.1 IdUtil.fastSimpleUUID() 生成 32 位 UUID
  |    +-- 2.2 设置过期时间 = now + clientDO.refreshTokenValiditySeconds
  |    +-- 2.3 insert 到 DB (无 Redis 缓存)
  |    +-- 2.4 return OAuth2RefreshTokenDO
  |
  +-- 3. createOAuth2AccessToken(refreshTokenDO, clientDO)
       +-- 3.1 IdUtil.fastSimpleUUID() 生成 32 位 UUID
       +-- 3.2 buildUserInfo(userId, userType) -> {nickname, deptId}
       +-- 3.3 设置过期时间 = now + clientDO.accessTokenValiditySeconds
       +-- 3.4 租户编号: 优先从 refreshTokenDO (快照), 回退到 TenantContextHolder
       +-- 3.5 insert 到 DB
       +-- 3.6 oauth2AccessTokenRedisDAO.set(accessTokenDO) 写入 Redis 缓存
       +-- 3.7 return OAuth2AccessTokenDO
```

### 校验 Token (checkAccessToken)

```
checkAccessToken(accessToken)
  |
  +-- getAccessToken(accessToken)
  |    +-- 1. Redis 优先: oauth2AccessTokenRedisDAO.get(accessToken)
  |    |    命中: 直接返回 (包含有效期校验)
  |    |
  |    +-- 2. Redis miss: 查 MySQL
  |    |    oauth2AccessTokenMapper.selectByAccessToken(accessToken)
  |    |    命中 && 未过期: 回填 Redis, 返回
  |    |
  |    +-- 3. MySQL access_token 也不存在:
  |    |    尝试用 accessToken 参数查 Refresh Token
  |    |    oauth2RefreshTokenMapper.selectByRefreshToken(accessToken)
  |    |    -> 命中 && 未过期: convertToAccessToken(refreshTokenDO) 构造临时 accessTokenDO
  |    |    -> 回填 Redis
  |    |
  |    +-- 4. 都找不到: return null
  |
  +-- 2. 校验未过期 (DateUtils.isExpired)
  +-- 3. return OAuth2AccessTokenDO (或抛出 UNAUTHORIZED 异常)
```

### 刷新 Token (refreshAccessToken)

```
refreshAccessToken(refreshToken, clientId)
  |
  +-- @Transactional(noRollbackFor = ServiceException.class)
  |    使用 noRollbackFor: 防止刷新令牌无效时的事务回滚影响只读查询
  |
  +-- 1. 查询 RefreshTokenDO
  +-- 2. 校验 Client ID 匹配
  +-- 3. 删除旧的 AccessToken (DB + Redis)
  +-- 4. 检查 RefreshToken 是否已过期
  |    已过期: 删除 RefreshToken, throw 异常
  +-- 5. 创建新的 AccessToken (关联同一 RefreshToken)
  +-- 6. return 新 OAuth2AccessTokenDO
```

### 撤销 Token (removeAccessToken)

```
removeAccessToken(accessToken)
  |
  +-- 1. 查询 accessTokenDO (DB)
  +-- 2. 删除 accessToken (DB)
  +-- 3. 删除 Redis 缓存
  +-- 4. 删除关联的 refreshToken (DB)
  +-- 5. 删除 refreshToken 的 Redis 缓存
  +-- return 被删除的 accessTokenDO
```

## 关键代码剖析

```java
@Service
public class OAuth2TokenServiceImpl implements OAuth2TokenService {

    @Resource
    private OAuth2AccessTokenMapper oauth2AccessTokenMapper;
    @Resource
    private OAuth2RefreshTokenMapper oauth2RefreshTokenMapper;
    @Resource
    private OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;
    @Resource
    private OAuth2ClientService oauth2ClientService;
    @Resource
    @Lazy
    private AdminUserService adminUserService;
```

### RefreshToken 回退机制详解

```java
public OAuth2AccessTokenDO getAccessToken(String accessToken) {
    // 1. Redis 优先
    OAuth2AccessTokenDO accessTokenDO = oauth2AccessTokenRedisDAO.get(accessToken);
    if (accessTokenDO != null) return accessTokenDO;

    // 2. MySQL 查询 Access Token
    accessTokenDO = oauth2AccessTokenMapper.selectByAccessToken(accessToken);
    if (accessTokenDO == null) {
        // 3. 关键回退: 用 accessToken 参数查 RefreshToken 表
        // 场景: WebSocket 的 token 直接跟在 url 上，无法传递 refresh_token
        // 场景: 积木报表只允许传递 token，不允许传递 refresh_token
        OAuth2RefreshTokenDO refreshTokenDO = oauth2RefreshTokenMapper.selectByRefreshToken(accessToken);
        if (refreshTokenDO != null && !DateUtils.isExpired(refreshTokenDO.getExpiresTime())) {
            accessTokenDO = convertToAccessToken(refreshTokenDO);
        }
    }

    // 4. 回填 Redis
    if (accessTokenDO != null && !DateUtils.isExpired(accessTokenDO.getExpiresTime())) {
        oauth2AccessTokenRedisDAO.set(accessTokenDO);
    }
    return accessTokenDO;
}
```

**这个回退机制解决什么问题？**

在某些场景下（如 WebSocket 连接、集成第三方报表工具），客户端只能传递一个 token 字符串，无法同时传递 access_token 和 refresh_token。当 access_token 已过期但 refresh_token 未过期时，此回退机制允许将 refresh_token 字符串作为 access_token 参数传入，服务端自动用 refresh_token 创建一个临时的 access_token 响应。

### 用户信息构建

```java
private Map<String, String> buildUserInfo(Long userId, Integer userType) {
    if (userType.equals(UserTypeEnum.ADMIN.getValue())) {
        AdminUserDO user = adminUserService.getUser(userId);
        return MapUtil.builder(LoginUser.INFO_KEY_NICKNAME, user.getNickname())
                .put(LoginUser.INFO_KEY_DEPT_ID, StrUtil.toStringOrNull(user.getDeptId())).build();
    } else if (userType.equals(UserTypeEnum.MEMBER.getValue())) {
        return Collections.emptyMap();  // 会员暂未实现
    }
}
```

这个 `userInfo` 会存储到 AccessTokenDO 中，并在 Token 校验时通过 `OAuth2AccessTokenCheckRespDTO` 传递到 `LoginUser.info` 字段。业务代码中通过 `SecurityFrameworkUtils.getLoginUserNickname()` 或 `getLoginUserDeptId()` 直接获取，无需查 DB。

### 租户编号继承

```java
private OAuth2AccessTokenDO createOAuth2AccessToken(...) {
    // 优先从 refreshToken 获取租户编号
    // 避免 ThreadLocal 被污染时导致 tenantId 为 null
    Long tenantId = refreshTokenDO.getTenantId();
    if (tenantId == null) {
        tenantId = TenantContextHolder.getTenantId();
    }
    accessTokenDO.setTenantId(tenantId);
}
```

`refreshTokenDO.getTenantId()` 应该在 RefreshToken 创建时已经存储了当前的租户 ID。这里先取 RefreshToken 上的快照值，再回退到 ThreadLocal，是为了解决异步场景下 ThreadLocal 被污染或丢失的问题。

### 定时清理

```java
public Integer cleanRefreshToken(Integer exceedDay, Integer deleteLimit) {
    int count = 0;
    LocalDateTime expireDate = LocalDateTime.now().minusDays(exceedDay);
    // 循环删除，每次最多 deleteLimit 条
    for (int i = 0; i < Short.MAX_VALUE; i++) {
        int deleteCount = oauth2RefreshTokenMapper.deleteByExpiresTimeLt(expireDate, deleteLimit);
        count += deleteCount;
        if (deleteCount < deleteLimit) break;  // 没有更多可删的了
    }
    return count;
}
```

使用循环删除的策略，每次删除 limited 条，直到没有更多过期记录。`Short.MAX_VALUE` 作为安全上限防止无限循环。由 `TokenCleanJob` 定时任务定期调用（默认每天执行）。

## 调用链

```
[Upstream - 创建 Token]
  AdminAuthServiceImpl.login()
    -> OAuth2TokenServiceImpl.createAccessToken()
       -> OAuth2ClientService.validOAuthClientFromCache()
       -> OAuth2RefreshTokenMapper.insert()
       -> OAuth2AccessTokenMapper.insert()
       -> OAuth2AccessTokenRedisDAO.set()

[Upstream - 校验 Token]
  TokenAuthenticationFilter (servlet)
    -> OAuth2TokenCommonApi.checkAccessToken() (Feign)
       -> OAuth2TokenApiImpl.checkAccessToken()
          -> OAuth2TokenServiceImpl.checkAccessToken()
             -> getAccessToken() -> Redis -> DB -> RefreshToken fallback

[Upstream - 刷新/撤销]
  AdminAuthServiceImpl.refreshToken()
    -> OAuth2TokenServiceImpl.refreshAccessToken()
  AdminUserServiceImpl.updateUserStatus(DISABLE)
    -> OAuth2TokenServiceImpl.removeAccessToken()

[Upstream - 定时清理]
  TokenCleanJob (XXL-Job 定时任务)
    -> OAuth2TokenServiceImpl.cleanRefreshToken()
    -> OAuth2TokenServiceImpl.cleanAccessToken()

[Downstream]
  OAuth2AccessTokenMapper -> MySQL (access_token 表)
  OAuth2RefreshTokenMapper -> MySQL (refresh_token 表)
  OAuth2AccessTokenRedisDAO -> Redis (TOKEN 缓存)
  AdminUserService -> 获取用户昵称/部门信息
```

## 配置与条件

| 配置 | 来源 | 说明 |
|------|------|------|
| `client.accessTokenValiditySeconds` | OAuth2ClientDO | Access Token 有效期（秒, 默认 2 小时） |
| `client.refreshTokenValiditySeconds` | OAuth2ClientDO | Refresh Token 有效期（秒, 默认 30 天） |
| `exceedDay` | TokenCleanJob 参数 | 清理超过 N 天的过期 Token（默认 14 天） |
| `deleteLimit` | TokenCleanJob 参数 | 每次循环删除的上限（默认 100 条） |

## 生产级关注点

### 1. Redis 缓存策略

- **只缓存 AccessToken，不缓存 RefreshToken**。RefreshToken 有效期长（30 天），且使用频率低（只在刷新时使用）
- **写入时机**：创建 Token 时立即写入；MySQL miss 时查到后回填
- **删除时机**：撤销 Token 时立即删除；刷新 Token 时批量删除关联的旧 AccessToken
- **Redis vs DB 的一致性**：先删 DB 再删 Redis，如果中间失败可能导致 Redis 中的脏数据。但在 checkAccessToken 中会做有效期校验，Redis 中过期但 DB 已删的情况能正确处理

### 2. Refresh Token 回退机制的风险

允许用 Refresh Token 字符串作为 Access Token 使用，意味着 Refresh Token 的泄露等同于 Access Token 的泄露。权衡：
- 优势：完美解决 WebSocket/积木报表等无法刷新 Token 的场景
- 劣势：Refresh Token 的有效期更长，泄露风险更大
- 缓解措施：`convertToAccessToken` 构建的 accessTokenDO 同样受 `expiresTime` 控制，且每次校验都会检查

### 3. 事务设计

- `createAccessToken`: `@Transactional(rollbackFor = Exception.class)`，RefreshToken + AccessToken 在一个事务中
- `refreshAccessToken`: `@Transactional(noRollbackFor = ServiceException.class)`，刷新失败时不回滚已执行的查询
- `removeAccessToken(df)`: `@Transactional`，确保 DB 删除的原子性（Redis 操作不在事务范围内）

### 4. 并发安全

多个线程同时刷新同一个 Refresh Token 时：
1. 线程 A 删除旧 AccessToken -> 创建新 AccessToken
2. 线程 B 同时操作，可能读到已删除的 RefreshToken
3. 框架通过 `@Transactional` + MySQL 行级锁提供基本的并发保护

### 5. UUID 生成

使用 `IdUtil.fastSimpleUUID()`（Hutool 工具）生成 32 位无分隔符 UUID：
- 比标准 UUID 少了 `-` 分隔符
- 使用 Java 的 `UUID.randomUUID()` 但不包含分隔符
- 唯一性满足令牌要求

### 6. 过期 Token 清理

- **Access Token**：2 小时过期，过期后在 MySQL 中仍然存在，通过 `cleanAccessToken` 定时清理
- **Refresh Token**：30 天过期，同理通过 `cleanRefreshToken` 定时清理
- **Redis 自动过期**：Redis 中的 access token 利用 TTL 自动清理，不需要手动删除
- **清理策略**：逐批次删除（batch size = deleteLimit），避免大事务

### 7. 性能考虑

- `checkAccessToken` 是最高频的路径，每次请求都会调用
- Redis 缓存大幅降低 MySQL 压力
- 缓存命中时延迟 < 1ms（本地 Redis）
- 缓存未命中 + MySQL 查询延迟 ~ 5ms
- RefreshToken 回退路径延迟稍高，但属于少数场景

### 8. @Lazy 循环依赖

`AdminUserService` 使用 `@Lazy`：
- `OAuth2TokenServiceImpl` -> `AdminUserService` -> `AdminUserServiceImpl` -> `OAuth2TokenService`
- 如果不加 `@Lazy`，会形成循环依赖，Spring 无法启动
