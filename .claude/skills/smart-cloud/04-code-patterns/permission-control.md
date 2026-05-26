---
name: permission-control
description: Permission control — @PreAuthorize, SecurityFrameworkService programmatic checks, @DataPermission, SSO, multi-terminal, mock mode
type: project
---

# 权限控制模式

## 概述

权限控制使用 Spring Security + Token + Redis 方案。提供三层控制：**Controller 注解**（`@PreAuthorize`）、**Service 编程式**（`SecurityFrameworkService`）、**数据行级**（`@DataPermission`）。支持多终端（管理后台、用户 APP）和 SSO。

## 1. Controller 层：注解声明式

```java
@Tag(name = "管理后台 - 用户")
@RestController
@RequestMapping("/system/user")
@Validated
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/create")
    @Operation(summary = "创建用户")
    @PreAuthorize("@ss.hasPermission('system:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody UserSaveReqVO createReqVO) { ... }

    @PutMapping("/update")
    @Operation(summary = "更新用户")
    @PreAuthorize("@ss.hasPermission('system:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody UserSaveReqVO updateReqVO) { ... }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户")
    @PreAuthorize("@ss.hasPermission('system:user:delete')")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) { ... }

    @GetMapping("/get")
    @Operation(summary = "获得用户")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<UserRespVO> getUser(@RequestParam("id") Long id) { ... }

    @GetMapping("/page")
    @Operation(summary = "获得用户分页")
    @PreAuthorize("@ss.hasPermission('system:user:query')")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO pageReqVO) { ... }
}
```

## 2. 权限表达式速查

```java
// ===== 权限点 =====
// 格式：{module}:{domain}:{action}
@PreAuthorize("@ss.hasPermission('system:user:create')")
@PreAuthorize("@ss.hasPermission('system:user:query')")
@PreAuthorize("@ss.hasPermission('system:user:export')")

// ===== 角色 =====
@PreAuthorize("@ss.hasRole('admin')")
@PreAuthorize("@ss.hasAnyRoles('admin', 'super_admin')")

// ===== 多条件组合 =====
@PreAuthorize("@ss.hasPermission('system:user:delete') && @ss.hasRole('admin')")

// ===== 无需鉴权（白名单 URL） =====
// 不加 @PreAuthorize，在配置文件中注册：
// develop.security.permit-all-urls:
//   - /system/health/**
```

## 3. Service 层：编程式权限校验

```java
@Service
@Validated
public class UserServiceImpl implements UserService {

    @Resource
    private SecurityFrameworkService securityFrameworkService;

    @Override
    public void sensitiveOperation(Long userId) {
        // 权限点校验
        if (!securityFrameworkService.hasPermission("system:user:sensitive-op")) {
            throw new AccessDeniedException("无权限");
        }

        // 角色校验
        if (!securityFrameworkService.hasRole("admin")) {
            throw new AccessDeniedException("仅管理员可操作");
        }

        // 数据归属校验（只能操作自己的数据）
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!currentUserId.equals(userId) && !securityFrameworkService.hasRole("admin")) {
            throw new AccessDeniedException("只能操作自己的数据");
        }
    }
}
```

## 4. 数据权限（行级）

通过 `develop-spring-boot-starter-biz-data-permission` 实现行级数据过滤。

```java
// Mapper 方法上加 @DataPermission 注解
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {

    @DataPermission(
        includeRules = @DataPermissionColumn(alias = "u", name = "dept_id"), // 部门数据权限
        excludeRules = @DataPermissionColumn(alias = "u", name = "dept_id")  // 排除某些规则
    )
    default PageResult<UserDO> selectPage(UserPageReqVO reqVO) { ... }
}
```

数据权限通过 MyBatis 拦截器自动拼接 SQL 条件（如 `AND u.dept_id IN (...)`），支持：
- **全部数据**：不拼接过滤条件
- **自定义部门数据**：按指定部门 ID 过滤
- **本部门数据**：`dept_id = 当前用户部门`
- **本部门及子部门**：`dept_id IN (部门及所有子部门)`
- **仅本人数据**：`creator = 当前用户 ID`

## 5. 多租户隔离

通过 `develop-spring-boot-starter-biz-tenant` 实现。`TenantBaseDO` 子类自动在 SQL 末尾追加 `AND tenant_id = ?`。

```java
// 忽略租户隔离：在 Service 方法上标注
@TenantIgnore // 此方法不拼接 tenant_id 条件
public void systemWideOperation() { ... }

// 忽略租户隔离：在实体类上标注（适用于菜单等全局配置表）
@TenantIgnore
public class MenuDO extends BaseDO { ... }
```

## 6. 白名单配置

```yaml
# application.yaml
develop:
  security:
    permit-all-urls:
      - /login
      - /captcha
      - /swagger-ui/**
      - /v3/api-docs/**
      - /actuator/**
      - /callback/**
      - /webhook/**
      - /health/**
```

## 7. Mock 模式（本地开发）

```yaml
# application-local.yaml
develop:
  security:
    mock-enable: true   # Mock 模式开关
    mock-secret: any    # Mock 密钥
```

Mock Token 格式：`test{userId}`，如 `test1` = userId=1 的超级管理员。

## 权限点命名规范

```
{module}:{domain}:{action}
```

| 部分 | 说明 | 示例 |
|---|---|---|
| module | 模块名 | system, infra, bpm, pay, crm |
| domain | 业务实体 | user, role, menu, dept, order |
| action | 操作 | create, update, delete, query, export, import |

## 关键点

1. **`@ss.hasPermission()`** — `ss` 是 `SecurityFrameworkService` 的 Spring Bean 名称
2. **权限格式严格：全小写** — `system:user:create` 而非 `system:user:Create`
3. **GET 方法用 `query`**，写操作用 `create`/`update`/`delete`
4. **不直接使用 `@PermitAll`** — 统一通过 `develop.security.permit-all-urls` 配置白名单
5. **Mock 模式严禁在生产环境开启**
6. **行级数据权限**使用 `@DataPermission` 注解在 Mapper 方法上
7. **租户隔离**通过 `TenantBaseDO` 自动实现，`@TenantIgnore` 可忽略
8. **`@TenantIgnore` 可以在实体类上标注** — 适用于菜单等全局配置表

## 常见错误

- GET 方法使用了 `update`/`delete` 动作而非 `query`
- 未在配置文件中注册白名单就未加 `@PreAuthorize` — 返回 401
- 权限字符串大小写不统一
- 在 Service 方法上使用 `@PreAuthorize` — 注解只在 Controller Bean 上生效
- 生产环境开启了 `develop.security.mock-enable=true`
- 多租户表未继承 `TenantBaseDO` — 租户数据泄露
