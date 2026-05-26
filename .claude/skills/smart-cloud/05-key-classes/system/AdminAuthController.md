---
name: AdminAuthController
description: System authentication controller providing login, logout, token refresh, SMS/social login
type: project
---

# AdminAuthController

## 功能定位

AuthController 是管理后台的认证入口 Controller，位于 `develop-module-system` 的 `controller.admin.auth` 包下。注意：实际类名为 `AuthController`，前缀 `Admin` 仅用于标记其为管理后台 Controller。

核心职责是**提供管理后台全部认证相关的 REST API**：
- **密码登录**：账号密码认证，创建 OAuth2 Token
- **短信登录**：手机号 + 验证码登录
- **社交登录**：微信/钉钉等第三方授权登录
- **令牌管理**：登录/登出/刷新 Token
- **权限信息**：获取登录用户的菜单树、角色、权限标识
- **注册/重置密码**：自助注册和密码重置

它是 `develop-module-system` 对外暴露的认证入口，所有其他模块（包括 Gateway 和前端）都通过此 Controller 完成用户的身份认证。

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Facade** | 编排多个 Service 完成认证流程 | 注入 6 个 Service + 1 个 Properties |
| **Convert (Mapper)** | DO -> VO 转换 | `AuthConvert.INSTANCE.convert()` MapStruct 转换器 |
| **Strategy** | 不同登录方式独立处理路径 | password login / SMS login / social login 独立端点 |
| **PermitAll** | 公开访问控制 | `@PermitAll` 标记无需认证的端点 |

## 核心逻辑流程

### 账号密码登录 (`POST /system/auth/login`)

```
AuthController.login(reqVO)
  |
  +-- @PermitAll 无需认证
  +-- @Valid 校验请求参数
  |
  +-- authService.login(reqVO)           [AdminAuthService]
       |
       +-- 1. 校验验证码 (如已开启)
       +-- 2. AdminUserService.getUserByUsername(username)
       +-- 3. 校验密码 BCryptPasswordEncoder.matches(password, user.password)
       +-- 4. OAuth2TokenService.createAccessToken(userId, ADMIN, clientId, scopes)
       |    +-- 4.1 校验 OAuth2Client
       |    +-- 4.2 创建 RefreshToken (UUID, 持久化 DB)
       |    +-- 4.3 创建 AccessToken (UUID, 持久化 DB + Redis)
       |    +-- 4.4 buildUserInfo() 获取 nickname + deptId -> info Map
       |
       +-- 5. 记录登录日志 (LoginLogService.createLoginLog)
       +-- 6. 更新用户最后登录时间和 IP
       +-- 7. 返回 AuthLoginRespVO(token, refreshToken, userInfo)
```

### 短信登录 (`POST /system/auth/sms-login`)

```
authService.smsLogin(reqVO)
  |
  +-- 1. 校验短信验证码 (校验 code 与手机号匹配)
  +-- 2. 查询手机号对应用户 (AdminUserService.getUserByMobile)
  +-- 3. 如不存在则自动注册 (可配置)
  +-- 4. 创建 OAuth2 Token (同密码登录)
  +-- 5. 记录登录日志
```

### 社交登录 (`POST /system/auth/social-login`)

两步流程:

**第一步**: `GET /system/auth/social-auth-redirect?type=wechat&redirectUri=xxx`
```
socialClientService.getAuthorizeUrl(type, ADMIN, redirectUri)
  -> 返回社交平台的授权 URL
  -> 前端重定向到该 URL 进行授权
```

**第二步**: `POST /system/auth/social-login`
```
authService.socialLogin(reqVO)
  -> 社交平台回调带 code
  -> 通过 code 换取 access_token
  -> 获取社交用户唯一标识 (openId)
  -> 查询已绑定的平台用户
  -> 创建 OAuth2 Token
```

### 获取权限信息 (`GET /system/auth/get-permission-info`)

```
getPermissionInfo()
  |
  +-- @DataPermission(enable = false)
  |    关闭数据权限，防止 SQL 级联过滤导致查不到数据
  |
  +-- 1. AdminUserService.getUser(getLoginUserId())
  |    如果用户不存在，返回 null
  |
  +-- 2. PermissionService.getUserRoleIdListByUserId(userId)
  |    获取用户的角色 ID 集合 (Set<Long>)
  |    如果为空，返回空权限信息
  |
  +-- 3. RoleService.getRoleList(roleIds)
  |    获取角色详情，移除禁用的角色
  |
  +-- 4. PermissionService.getRoleMenuListByRoleId(roleIds)
  |    获取角色的菜单 ID 集合
  |    如果为空，返回空权限信息
  |
  +-- 5. MenuService.getMenuList(menuIds)
  |    获取菜单详情
  |    -> filterDisableMenus() 移除禁用菜单
  |
  +-- 6. AuthConvert.INSTANCE.convert(user, roles, menuList)
       -> 组装 AuthPermissionInfoRespVO
       -> 包含: 用户信息、角色标识、权限标识、菜单树
```

### 登出流程 (`POST /system/auth/logout`)

```
AuthController.logout(request)
  |
  +-- 从请求中提取 token (Header/Parameter)
  +-- authService.logout(token, LOGOUT_SELF)
  |    -> 从 DB 和 Redis 中删除 accessToken + refreshToken
  |    -> 记录登出日志
  +-- 返回 success(true)
```

### 刷新 Token (`POST /system/auth/refresh-token`)

```
authService.refreshToken(refreshToken)
  |
  +-- 查询 refreshToken (MySQL)
  +-- 校验 Client 匹配
  +-- 删除旧 accessToken (DB + Redis)
  +-- 检查 refreshToken 是否过期
  +-- 创建新 accessToken (关联同一 refreshToken)
  +-- 返回新的 AuthLoginRespVO
```

## 关键代码剖析

```java
@Tag(name = "管理后台 - 认证")
@RestController
@RequestMapping("/system/auth")
@Validated
@Slf4j
public class AuthController {

    @Resource
    private AdminAuthService authService;
    @Resource
    private AdminUserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private SocialClientService socialClientService;
    @Resource
    private SecurityProperties securityProperties;

    @PostMapping("/login")
    @PermitAll
    public CommonResult<AuthLoginRespVO> login(@RequestBody @Valid AuthLoginReqVO reqVO) {
        return success(authService.login(reqVO));
    }

    @PostMapping("/logout")
    @PermitAll
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        }
        return success(true);
    }

    @GetMapping("/get-permission-info")
    @DataPermission(enable = false)
    public CommonResult<AuthPermissionInfoRespVO> getPermissionInfo() {
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(getLoginUserId());
        if (CollUtil.isEmpty(roleIds)) {
            return success(AuthConvert.INSTANCE.convert(user, Collections.emptyList(), Collections.emptyList()));
        }
        List<RoleDO> roles = roleService.getRoleList(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus()));
        // 菜单权限...
        return success(AuthConvert.INSTANCE.convert(user, roles, menuList));
    }
}
```

### @DataPermission(enable = false) 的作用

`get-permission-info` 方法标记 `@DataPermission(enable = false)` 的原因：
- 在查询用户时，如果开启了数据权限，MyBatis Plus 会追加 `tenant_id = ?` 或 `dept_id IN (...)` 条件
- 权限信息的查询不应该受到数据权限的过滤，否则可能出现"用户因数据权限看不到自己的角色"的问题
- 参考 issue：https://t.zsxq.com/LHnrp

## 调用链

```
[Upstream] 前端浏览器 / 移动端 App / Postman
  |
  +-- Gateway TokenAuthenticationFilter (Gateway 层 Token 校验)
       |
       +-- AuthController (本类)
            |
            +-- AdminAuthService         -> 认证业务逻辑 (核心)
            +-- AdminUserService         -> 用户查询
            +-- PermissionService        -> 角色/菜单/权限
            +-- RoleService              -> 角色列表查询
            +-- MenuService              -> 菜单树查询
            +-- SocialClientService      -> 社交登录授权 URL
            |
            +-- [间接]
                 +-- OAuth2TokenService  -> Token 创建/刷新/删除
                 +-- LoginLogService     -> 登录登出日志
                 +-- CaptchaService      -> 验证码校验
```

## 配置与条件

### 端点权限矩阵

| 端点 | 方法 | @PermitAll | 权限标识 | 说明 |
|------|------|------------|----------|------|
| `/login` | POST | Yes | - | 账号密码登录 |
| `/logout` | POST | Yes | - | 登出 |
| `/refresh-token` | POST | Yes | - | 刷新令牌 |
| `/get-permission-info` | GET | No | 需认证 | 获取菜单权限(登录后) |
| `/sms-login` | POST | Yes | - | 短信验证码登录 |
| `/send-sms-code` | POST | Yes | - | 发送短信验证码 |
| `/register` | POST | Yes | - | 注册用户 |
| `/reset-password` | POST | Yes | - | 重置密码 |
| `/social-auth-redirect` | GET | Yes | - | 社交授权跳转 |
| `/social-login` | POST | Yes | - | 社交登录 |

### 业务配置项

| 配置 | 说明 | 影响端点 |
|------|------|----------|
| `develop.security.token-header` | Token Header 名 | logout |
| `develop.security.token-parameter` | Token Parameter 名 | logout |
| `system.user.register-enabled` | 是否开放注册 | register |
| 验证码开关 | 系统配置 | login, sms-login |

## 生产级关注点

### 1. 安全防护

- **密码暴力破解防护**：可通过叠加 `@RateLimiter` 限流注解（如短信登录接口）防止接口被刷
- **验证码校验**：密码登录和短信登录均支持验证码验证（如 Captcha）
- **Token 提取安全**：Header 和 Parameter 两种方式，自动去除 `Bearer ` 前缀

### 2. 性能考虑

- **getPermissionInfo 是高频接口**：前端每次路由切换、页面刷新都会调用。涉及多次数据库查询（用户、角色、菜单、权限）
- **分层缓存**：角色查询 `RoleService.getRoleList` 有本地缓存，菜单查询 `MenuService.getMenuList` 也走缓存
- **优化建议**：前端可缓存权限信息到 localStorage，减少接口调用

### 3. 数据一致性

- **getPermissionInfo 的数据一致性**：当管理员修改了角色或菜单后，用户下次刷新页面才能看到新权限
- **Token 即时作废**：`logout` 删除 Redis 和 DB 中的 Token，立即生效
- **刷新 Token 安全性**：Refresh Token 使用一次后旧 Access Token 立即删除

### 4. 限流建议

短信登录和发送验证码接口建议开启限流：
```java
@RateLimiter(time = 60, count = 6, keyResolver = ExpressionRateLimiterKeyResolver.class, keyArg = "#reqVO.mobile")
```

### 5. 社交登录注意事项

- SocialClientService 根据 `type` 参数选择不同的社交平台实现（微信、钉钉、支付宝等）
- 需要先在 system-server 中配置对应的 AppId 和 AppSecret
- 未绑定用户的社交账号登录会返回错误，需要先绑定
