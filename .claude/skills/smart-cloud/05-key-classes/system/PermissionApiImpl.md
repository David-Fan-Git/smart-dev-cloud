---
name: PermissionApiImpl
description: Adapter that exposes PermissionService as both REST API and Feign RPC endpoint
type: project
---

# PermissionApiImpl

## 功能定位

PermissionApiImpl 是权限服务的 API 适配层，位于 `develop-module-system` 的 `api.permission` 包下。它实现了 `PermissionApi` 接口，承接其他微服务的 Feign 远程调用。

核心职责：
- **暴露 REST API**：通过 `@RestController` 将 PermissionService 的方法暴露为 HTTP 端点
- **服务 Feign 调用**：security starter 中的 `PermissionCommonApi` (Feign 客户端) 通过 HTTP 调用此类
- **结果包装**：将 PermissionService 的返回结果包装为 `CommonResult<T>`，符合框架 RPC 协议

它是"dual REST+RPC"模式的核心体现——**同一个方法既是 HTTP 端点又是 Feign 调用目标**。

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Adapter** | 将 PermissionService 适配为 API 接口 | 实现 `PermissionApi`，委托给 `PermissionService` |
| **Facade** | 封装 PermissionService，提供薄包装 | 4 个方法都是直接委托 + CommonResult 包装 |
| **Primary Bean** | `@Primary` 解决同接口多实现冲突 | `@Primary` + `@RestController` 在同一类上 |
| **Dual Protocol** | 同时支持 HTTP REST + Feign RPC | `@RestController` 暴露端点，Feign 通过相同接口调用 |

## 核心逻辑流程

```
[Feign Client]                         [REST Server]
PermissionCommonApi                    PermissionApiImpl (本类)
  (security starter)                     (system-server)
       |                                      |
       | --- HTTP /rpc-api/system/* ------>   |
       |                                      |
       |                                      +-- permissionService.getUserRoleIdListByRoleId()
       | <-- CommonResult<Set<Long>> -------  +-- CommonResult.success(result)
       |                                      |
       |                                      +-- permissionService.hasAnyPermissions()
       | <-- CommonResult<Boolean> --------   +-- CommonResult.success(result)
       |                                      |
       |                                      +-- permissionService.hasAnyRoles()
       | <-- CommonResult<Boolean> --------   +-- CommonResult.success(result)
       |                                      |
       |                                      +-- permissionService.getDeptDataPermission()
       | <-- CommonResult<DTO> ------------   +-- CommonResult.success(result)
```

## 关键代码剖析

```java
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
@Primary        // 由于 PermissionCommonApi 的存在，必须声明为 @Primary Bean
public class PermissionApiImpl implements PermissionApi {

    @Resource
    private PermissionService permissionService;

    @Override
    public CommonResult<Set<Long>> getUserRoleIdListByRoleIds(Collection<Long> roleIds) {
        return success(permissionService.getUserRoleIdListByRoleId(roleIds));
    }

    @Override
    public CommonResult<Boolean> hasAnyPermissions(Long userId, String... permissions) {
        return success(permissionService.hasAnyPermissions(userId, permissions));
    }

    @Override
    public CommonResult<Boolean> hasAnyRoles(Long userId, String... roles) {
        return success(permissionService.hasAnyRoles(userId, roles));
    }

    @Override
    public CommonResult<DeptDataPermissionRespDTO> getDeptDataPermission(Long userId) {
        return success(permissionService.getDeptDataPermission(userId));
    }
}
```

### 为什么需要 @Primary？

项目中有两个与权限相关的 API 接口：
1. **PermissionApi** (本类所在模块): `PermissionApiImpl implements PermissionApi`
   - 位于 `develop-module-system/develop-module-system-server`
   - 作为 REST 服务端

2. **PermissionCommonApi** (框架定义)
   - 位于 `develop-spring-boot-starter-security`
   - Feign 客户端接口，通过 `@FeignClient` 标记
   - 方法签名与 PermissionApi 兼容

**问题**: 当 Spring 扫描到两个实现了相似接口的 Bean 时，注入会冲突。

**解决**: PermissionApiImpl 加上 `@Primary`，告诉 Spring 在多个候选 Bean 中优先选择此实现。

### 为什么 @RestController 直接在 Impl 上？

典型的 Controller 在 `controller` 包下有单独的类，但 **API 实现类直接标注 `@RestController`** 的原因是：

```
PermissionCommonApi (Feign Client)
  |--- @FeignClient(name = "system-server")
  |--- @RequestMapping("/rpc-api/system/permission") // 路径在 PermissionApi 接口中定义
  |
  v
PermissionApiImpl (REST Server)
  |--- @RestController
  |--- implements PermissionApi
  |--- 路径继承自 PermissionApi 接口的 @RequestMapping
```

路径定义在 `PermissionApi` 接口中，由 `PermissionApiImpl` 继承。所以 `@RestController` 标注在实现类上，Feign 客户端通过相同路径调用。

### RPC 调用链

```java
// SecurityFrameworkServiceImpl (security starter 中)
// 缓存 miss 时调用
return permissionApi.hasAnyPermissions(key.getKey(),
        key.getValue().toArray(new String[0])).getCheckedData();
// getCheckedData() 从 CommonResult 中提取 data
```

```java
// 对应到 PermissionApiImpl
@Override
public CommonResult<Boolean> hasAnyPermissions(Long userId, String... permissions) {
    return success(permissionService.hasAnyPermissions(userId, permissions));
}
```

## 调用链

```
[Upstream - 调用方]
  |-- SecurityFrameworkServiceImpl (通过 Guava LoadingCache CacheLoader)
  |     |-- [缓存 MISS] -> permissionApi.hasAnyPermissions()
  |     |                  (PermissionCommonApi Feign 客户端)
  |     |                  -> HTTP GET /rpc-api/system/permission/has-any-permissions
  |     |                     ?userId=1&permissions=system:user:list
  |     |                     Header[tenant-id]: 1
  |     |
  |     +-- [REST 请求到 system-server]
  |          PermissionApiImpl (本类)
  |            -> PermissionService
  |               -> AdminUserMapper / RoleMapper / MenuMapper / ...
  |                  -> MySQL
  |
  |-- DeptDataPermissionRule (数据权限规则)
  |     -> permissionApi.getDeptDataPermission(userId)
  |     -> PermissionApiImpl.getDeptDataPermission()
  |        -> PermissionService.getDeptDataPermission()
  |           -> 查询用户角色 -> 查询角色数据权限范围
  |           -> 返回 DeptDataPermissionRespDTO

[Downstream - 被调用方]
  PermissionService (业务层)
```

## 配置与条件

| 注解/配置 | 作用 |
|-----------|------|
| `@RestController` | 暴露 REST API，路径继承自 `PermissionApi` 接口的 `@RequestMapping` |
| `@Primary` | 当 `PermissionApi` 和 `PermissionCommonApi` 同时存在时优先注入 |
| `@Validated` | 方法参数校验触发 |
| Gateway 路由 | Gateway 需配置 `/rpc-api/system/**` 路由到 `system-server` 服务 |

## 生产级关注点

### 1. 网络开销

每个 `@PreAuthorize` 注解在缓存 miss 时都会触发一次 HTTP RPC 调用。虽然 Guava 缓存降低了频率（1 分钟 TTL），但在以下场景仍然会有较高频调用：
- 使用 `@PreAuthorize` 注解的 API 首次被某个用户调用
- 用户长时间未操作后首次请求

**优化建议**：对于权限标识固定的场景，可考虑增加 TTL；对于需要即时生效的场景，可使用 `refreshAfterWrite` 异步刷新。

### 2. CommonResult 约定

所有返回值使用 `CommonResult.success()` 包装，Feign 客户端用 `getCheckedData()` 解包：
```java
return success(permissionService.hasAnyPermissions(userId, permissions));
// 上述等价于: CommonResult<Boolean> { code: 0, data: true/false, msg: "success" }
```
`getCheckedData()` 方法会检查 `code`，非 0 时抛出 `ServiceException`。

### 3. 权限模型中的角色

注意方法签名差异：
- `getUserRoleIdListByRoleIds(Collection<Long> roleIds)`：参数是**角色 ID 集合**，返回**用户 ID 集合**
- 语义：查询拥有指定角色的所有用户

### 4. 错误处理

PermissionService 中抛出的 `ServiceException` 会通过 Feign 传输到调用方。调用方通过 `getCheckedData()` 时抛出异常，被 Guava LoadingCache 包装为 `UncheckedExecutionException`，最终由 `GlobalExceptionHandler` 处理。

### 5. 循环依赖风险

SecurityFrameworkServiceImpl -> PermissionCommonApi (Feign) -> HTTP -> PermissionApiImpl -> PermissionService

因为 Feign 调用是 HTTP 远程调用（不是本地 Bean 注入），不会产生 Spring 循环依赖问题。
