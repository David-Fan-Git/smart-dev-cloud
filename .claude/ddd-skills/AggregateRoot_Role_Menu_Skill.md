---
name: aggregate-root-role-menu-skill
description: Use when modifying or reviewing system RBAC Role, Menu, Permission, RoleMenu, UserRole, menu tree, permission checks, or data permission boundaries.
type: ddd-aggregate-skill
status: production-review
---

# AggregateRoot Role/Menu Skill

## AI Execution Contract

- **Scope:** 每次只处理本文件声明的一个聚合、一个子域或一个最小闭环；多聚合文件必须先拆分到目标子聚合后再实现。
- **Must Read:** 修改前读取本 skill 的 Current Source Anchors，以及对应 Controller、VO/DTO、DO、Mapper、Convert、Service/Application、Repository、ErrorCode、测试文件。
- **Must Preserve:** Controller 路径、HTTP 方法、VO/DTO 字段、CommonApi/Feign/RPC 契约、权限、租户、数据权限、错误码、分页、Excel、MQ、Job、缓存、第三方回调和 OpenAPI 可见行为。
- **Allowed Changes:** 只在目标聚合的 `domain`、`application`、`infrastructure`、`convert`、入口适配和对应测试内做最小必要修改，并按标准骨架补齐端口或 package 边界。
- **Forbidden Changes:** 禁止批量改无关聚合；禁止把新核心业务写入旧 `service/dal`；禁止让 domain 依赖 Spring、MyBatis、Feign、Mapper、DO、Controller VO、RPC client 或基础设施实现。
- **Dependency Rules:** domain 只依赖领域对象和值对象；application 编排用例、事务和端口；infrastructure 适配 Mapper/DO/外部系统；controller/job/mq 只做入口。
- **Verification Gate:** 完成前运行本 skill 的 Verification Commands；无法运行时写明命令、阻塞原因和未验证风险。
- **Stop Conditions:** 事实源缺失、skill 与当前代码冲突、外部契约可能变化、字段/错误码/事务需要猜测、验证失败时停止并先修订 skill 或缩小范围。

## Standard Skeleton Contract

目标聚合必须固定以下职责边界；Java 空目录用职责明确的接口或 `package-info.java` 固定，禁止 `Temp`/`Placeholder`/`Dummy`：

```text
domain/{aggregate}/model,valueobject,event,service,repository
application/{aggregate}/command,query,dto|result,port/inbound,port/outbound,service
infrastructure/{aggregate}/persistence,external,rpc,cache,messaging
convert/
controller/ job/ mq/ framework/
```

旧 `service/dal` 是迁移源，不是新核心业务最终落位。

## Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 业务不变量 | `domain/{aggregate}` | `controller`、`convert`、`dal` |
| 用例编排和事务 | `application/{aggregate}/service` | `domain` 或 Controller |
| 入站用例契约 | `application/{aggregate}/port/inbound` | Controller 私有方法 |
| 外部能力端口 | `application/{aggregate}/port/outbound` | domain 或 infrastructure 反向定义 |
| 仓储接口 | `domain/{aggregate}/repository` | infrastructure 反向定义业务端口 |
| Mapper/DO 适配 | `infrastructure/{aggregate}/persistence` | domain/application 直接调用 |
| 对象转换 | `convert` | domain 聚合内 |

## AI Self-Check

- 已读取当前事实源，而不是只依据本 skill 猜测。
- 未改变 Controller/API/VO/DTO/权限/租户/数据权限/错误码/分页/Excel/MQ/Job/缓存/回调契约。
- domain 未依赖 Spring、MyBatis、Feign、Mapper、DO、VO、DTO 或基础设施实现。
- 标准目录、入站端口、出站端口、领域仓储和 infrastructure 适配边界没有因“当前为空”被省略。
- 已运行本文件列出的验证命令，或明确记录无法验证的原因。

## 0. Purpose

This skill is the production refactoring contract for the system RBAC boundary: Role, Menu, Permission, RoleMenu, UserRole, menu filtering, permission checking, and department data permission.

Use it before changing any of these areas:

- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/permission/`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/permission/`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/application/permission/`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/permission/`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/dataobject/permission/`
- `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/`

Role/Menu DDD migration is not complete. Treat existing `service/permission/*ServiceImpl.java` classes as the behavior source until the new DDD path proves equivalent through tests and compile checks.

## 1. Baseline Failure Findings

The previous version of this skill was not production-safe. A clean agent using it could incorrectly:

- Replace Controller behavior without preserving URLs, HTTP methods, request/response VO shape, permissions, export behavior, or pagination semantics.
- Move RBAC logic into `domain` while dropping Spring cache eviction, `@DSTransactional`, `@Transactional`, `@LogRecord`, or data permission behavior.
- Treat Menu as tenant-owned data and break `MenuDO`'s tenant-ignore/global-menu semantics plus tenant package filtering.
- Ignore strict permission matching in `PermissionServiceImpl#hasAnyPermissions`: unknown permission strings return `false`, not ignored.
- Drop super-admin fallback in permission checks.
- Drop `processRoleDeleted`, `processMenuDeleted`, or `processUserDeleted` cleanup side effects.
- Use current DDD draft `Role`/`Menu` constructors as-is even though they reject `null` IDs and require data not supplied by create VOs.
- Convert domain exceptions such as `IllegalStateException` / `IllegalArgumentException` directly to API errors, losing existing `ErrorCodeConstants` contracts.
- Omit `application/permission/port/inbound`, `application/permission/port/outbound`, `application/permission/service`, or `infrastructure/permission/persistence|external|rpc|cache|messaging` because the current Role/Menu slice is simple, has one implementation, or has temporarily empty directories.

## 2. Reproducibility Contract

Before changing code, verify this skill against current source. If source behavior differs from this document, update this skill first and do not proceed with code refactoring until the conflict is resolved.

Required production skill sections are:

- Current Source Anchors
- Standard Skeleton Contract
- Fixed Data Model
- Required Method Signatures
- Business Rules
- Error Code Contract
- Transaction Contract
- Integration Contract
- Mapping Rules
- Current Conflict Notes
- Acceptance Criteria
- Verification Commands
- Red Flags
- Rollback Conditions
- AI Self-Check

## 2.1 AI Execution Contract

本 skill 必须在试点过程中持续修订：每当真实代码迁移暴露新的边界、旧行为兼容点、测试装配缺口或 AI 容易误判的地方，先把约束写回本文件，再继续扩大改造范围。

| 项目 | Role/Menu/Permission 执行约束 |
|---|---|
| Scope | 每次只迁移一个最小闭环：Role 写用例、Role 查询、Menu 写用例、Menu 查询、RoleMenu 分配、UserRole 分配、权限判断或部门数据权限之一；禁止一次完成整个 RBAC 子域 |
| Must Read | 先读本 skill、生产标准、模块结构标准、对应 Controller/VO、旧 ServiceImpl、DO/Mapper、现有 domain/application/infrastructure、ErrorCodeConstants 和目标测试类 |
| Must Preserve | Controller URL/HTTP 方法/VO、RoleApi/PermissionApi、权限注解、缓存 key 与 allEntries 范围、LogRecord、事务边界、租户过滤、数据权限绕过、Excel 和分页语义 |
| Allowed Changes | 为当前切片新增/修改标准骨架内的 application、domain、infrastructure、convert 和兼容 service facade；旧 service 可变薄，但外部接口必须保持兼容；迁移任一 Role/Menu/Permission 切片时必须创建标准目录和接口骨架 |
| Forbidden Changes | 禁止把新核心业务继续写进 service/dal；禁止让 domain 依赖 Spring/MyBatis/Controller VO/Mapper/DO；禁止让 repository 为了复用 Mapper 构造 Controller VO；禁止以“当前为空”“只有一个实现”“避免空抽象”“最小切片”为由省略标准骨架 |
| Dependency Rules | 写用例优先：Controller/旧 Service facade → Application inbound port → Application service → Domain Repository port / Application outbound port → Infrastructure adapter → Mapper/DO；RoleMenu/UserRole 分配、查询与清理应通过 `PermissionUseCase` → `PermissionApplicationService` → `RoleMenuRepository`/`UserRoleRepository`，旧 `PermissionServiceImpl` 仅保留事务、缓存和外部兼容注解；关联清理不隐藏在 RoleRepository.delete 中 |
| Verification Gate | 每个切片至少跑对应旧 Service 回归测试、system-server compile、SystemArchitectureTest；修改 skill 时同步跑占位/结构检查 |
| Stop Conditions | 需要改变外部契约、缓存范围、事务类型、错误码、SQL/表结构、跨多个 RBAC 子用例联动，或测试暴露旧行为与 DDD 模型冲突时必须停止扩大范围并先修订 skill |

## 3. Current Source Anchors

### 3.1 External entrypoints

- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/RoleController.java`
  - Base path `/system/role`; preserves create/update/delete/delete-list/get/page/list-all-simple+simple-list/export-excel.
  - Excel export writes `角色数据.xls` sheet `数据` with `RoleRespVO`.
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/MenuController.java`
  - Base path `/system/menu`; preserves create/update/delete/delete-list/list/get and simple menu list aliases `"/list-all-simple"` and `"simple-list"`.
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/PermissionController.java`
  - Base path `/system/permission`; preserves list-role-menus, assign-role-menu, assign-role-data-scope, list-user-roles, and assign-user-role.
  - `assign-role-menu` filters request menu IDs through `TenantService.handleTenantMenu` before assignment.
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/auth/AuthController.java`

Controllers define the stable external API. A DDD refactor must not change:

- URL paths
- HTTP methods
- request VO fields
- response VO fields
- `@PreAuthorize` expressions
- export behavior
- pagination behavior
- tree/list ordering semantics

### 3.2 API contracts and RPC adapters

- `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/RoleApi.java`
- `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/PermissionApi.java`
- `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/remote/RoleRemoteClient.java`
- `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/remote/PermissionRemoteClient.java`

API-contract rule:

- Business callers inject stable `RoleApi` / `PermissionApi`.
- Feign identity stays only on `remote/*RemoteClient`.
- `RoleRemoteClient` uses `contextId = "systemRoleRemoteClient"`; `PermissionRemoteClient` uses `contextId = "systemPermissionRemoteClient"`.
- Server-local implementations implement stable APIs and must not depend on remote clients.
- `PermissionApiImpl` is `@Primary` because `PermissionApi` extends framework `PermissionCommonApi`; do not remove this while both beans/contracts coexist.

### 3.3 Legacy behavior source

- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/permission/RoleServiceImpl.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/permission/MenuServiceImpl.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/permission/PermissionServiceImpl.java`

These files are the current behavior source for rules, errors, cache, transaction, tenant filtering, and side effects.

### 3.4 Current DDD draft source

- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/permission/Role.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/permission/Menu.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/permission/factory/RoleFactory.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/permission/factory/MenuFactory.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/permission/repository/RoleRepository.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/permission/repository/MenuRepository.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/application/permission/RoleApplicationService.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/application/permission/MenuApplicationService.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/application/permission/service/PermissionApplicationService.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/application/permission/port/inbound/PermissionUseCase.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/permission/persistence/RoleRepositoryImpl.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/permission/persistence/MenuRepositoryImpl.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/permission/persistence/RoleMenuRepositoryImpl.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/permission/persistence/UserRoleRepositoryImpl.java`

Current DDD draft is not authoritative when it conflicts with legacy service behavior.

### 3.5 Standard Skeleton Contract

Role/Menu/Permission refactoring uses the repository module-structure standard physically, and hexagonal architecture as dependency direction. The standard skeleton is mandatory and is not considered a meaningless empty abstraction.

Required package skeleton under `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/`:

```text
domain/permission/
  model/
  valueobject/
  event/
  service/
  repository/
application/permission/
  command/
  query/
  dto/ 或 result/
  port/
    inbound/
    outbound/
  service/
infrastructure/permission/
  persistence/
  external/
  rpc/
  cache/
  messaging/
convert/permission/
controller/admin/permission/
job/
mq/
framework/
```

Mandatory interface and package rules:

- `application/permission/port/inbound/` must define use-case entry interfaces for the migrated slice, such as `RoleUseCase`, `MenuUseCase`, `AssignRoleMenuUseCase`, `AssignUserRoleUseCase`, or narrower command/query use cases.
- `application/permission/service/` must contain the inbound use-case implementation, such as `RoleApplicationService`, `MenuApplicationService`, or `PermissionApplicationService`, and implement the relevant inbound port.
- `application/permission/port/outbound/` is the fixed location for application external-capability ports. Keep the package even when the current slice has no external provider.
- `domain/permission/repository/` must contain domain repository ports such as `RoleRepository` and `MenuRepository`.
- `infrastructure/permission/persistence/` must contain repository implementations and Mapper/DO collaboration adapters.
- `infrastructure/permission/external/`, `rpc/`, `cache/`, and `messaging/` are fixed adapter locations. Keep them even when the current Role/Menu slice has no implementation.
- If Java empty directories cannot be tracked by Git, use a clear package boundary file such as `package-info.java` or a real interface required by the slice. Do not create `Temp`, `Placeholder`, or `Dummy` classes.
- Existing draft classes directly under `application/permission` or `infrastructure/permission` must be moved into the standard subpackages when that slice is migrated; until moved, treat them as migration debt, not the target shape.

### 3.6 Persistence source

- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/dataobject/permission/RoleDO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/dataobject/permission/MenuDO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/dataobject/permission/RoleMenuDO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/dataobject/permission/UserRoleDO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/mysql/permission/RoleMapper.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/mysql/permission/MenuMapper.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/mysql/permission/RoleMenuMapper.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/mysql/permission/UserRoleMapper.java`

### 3.7 Error and mapping anchors

- `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/enums/ErrorCodeConstants.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/role/RolePageReqVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/role/RoleRespVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/role/RoleSaveReqVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/role/RoleSimpleRespVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/menu/MenuListReqVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/menu/MenuRespVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/menu/MenuSaveVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/menu/MenuSimpleRespVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/permission/PermissionAssignRoleDataScopeReqVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/permission/PermissionAssignRoleMenuReqVO.java`
- `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/permission/vo/permission/PermissionAssignUserRoleReqVO.java`
- `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/dto/DeptDataPermissionRespDTO.java`

There is no dedicated `convert/permission/*Convert.java` in the current source tree. Legacy Role/Menu/Permission mapping is performed through Controller/Service code, `BeanUtils`, mapper methods, and DO/VO/DTO classes. Do not assume a MapStruct permission convert exists; if one is introduced during refactoring, it must be covered by tests and preserve the VO/DTO fields above.

### 3.8 Regression tests

- `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/permission/RoleServiceImplTest.java`
- `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/permission/MenuServiceImplTest.java`
- `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/permission/PermissionServiceTest.java`
- `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/auth/AdminAuthServiceImplTest.java`

Add or update tests near these existing tests unless the repository already has equivalent DDD application-service tests.

## 4. Fixed Data Model

### 4.1 Role

Source of truth: `RoleDO` and `RoleSaveReqVO`.

Required fields to preserve:

- `id: Long`
- `name: String`
- `code: String`
- `sort: Integer`
- `status: Integer`
- `type: Integer`
- `remark: String`
- `dataScope: Integer`
- `dataScopeDeptIds: Set<Long>` or equivalent persisted representation
- `tenantId: Long` from `TenantBaseDO`, when present on persistence model
- `createTime`, `updateTime`, `creator`, `updater`, `deleted` persistence metadata

Defaults:

- Create role default `type` is `RoleTypeEnum.CUSTOM.getType()` unless an explicit type is supplied by internal service path.
- Create role default `status` is `CommonStatusEnum.ENABLE.getStatus()` when request status is null.
- Create role default `dataScope` is `DataScopeEnum.ALL.getScope()`.

### 4.2 Menu

Source of truth: `MenuDO` and `MenuSaveVO`.

Required fields to preserve:

- `id: Long`
- `name: String`
- `permission: String`
- `type: Integer`
- `sort: Integer`
- `parentId: Long`
- `path: String`
- `icon: String`
- `component: String`
- `componentName: String`
- `status: Integer`
- `visible: Boolean` or current equivalent
- `keepAlive: Boolean` or current equivalent
- `alwaysShow: Boolean` or current equivalent
- persistence metadata

Menu is global system metadata. Do not add tenant ownership to `Menu` or `MenuDO`. Tenant-specific visibility is applied by tenant package/menu filtering, not by changing the menu row's tenant identity.

Defaults and normalization:

- Button menu type clears `component`, `componentName`, `icon`, and `path` to empty string, matching legacy `MenuServiceImpl#initMenuProperty`.
- Parent validation must treat root parent according to existing `MenuId.ROOT` / `MenuSaveVO` behavior.

### 4.3 RoleMenu and UserRole

Role-menu and user-role are association records, not entities embedded by object reference.

Required fields to preserve:

- `RoleMenuDO.roleId`
- `RoleMenuDO.menuId`
- `UserRoleDO.userId`
- `UserRoleDO.roleId`
- persistence metadata if present

The domain must reference cross-aggregate associations by IDs only.

## 5. Required Method Signatures

Do not force these exact signatures if current code already has equivalent names, but every capability must exist with equivalent input/output and behavior.

### 5.1 Role aggregate

```java
public final class Role {
    public RoleId id();
    public RoleName name();
    public RoleCode code();
    public Integer sort();
    public RoleStatus status();
    public RoleType type();
    public String remark();
    public Long tenantId();
    public DataScope dataScope();

    public void rename(RoleName name);
    public void changeCode(RoleCode code);
    public void changeBaseInfo(RoleName name, RoleCode code, Integer sort, String remark);
    public void changeStatus(RoleStatus status);
    public void changeDataScope(DataScope dataScope);
    public void markDeleted();
    public boolean isSystem();
}
```

Creation must support database-generated IDs. Do not require a non-null `RoleId` before insert unless the application layer allocates IDs before construction.

### 5.2 Menu aggregate

```java
public final class Menu {
    public MenuId id();
    public MenuName name();
    public MenuPermission permission();
    public MenuType type();
    public Integer sort();
    public MenuId parentId();
    public String path();
    public String icon();
    public String component();
    public String componentName();
    public Integer status();

    public void changeBaseInfo(...);
    public void validateParentAgainst(Menu parent);
    public boolean isButton();
    public boolean isDirOrMenu();
    public void markDeleted();
}
```

Creation must support database-generated IDs. Do not require a non-null `MenuId` before insert unless the application layer allocates IDs before construction.

### 5.3 Role repository port

```java
public interface RoleRepository {
    Role save(Role role);
    void update(Role role);
    void deleteById(Long id);
    Role findById(Long id);
    Role findByName(String name);
    Role findByCode(String code);
    PageResult<Role> findPage(...);
    List<Role> findList(...);
}
```

### 5.4 Menu repository port

```java
public interface MenuRepository {
    Menu save(Menu menu);
    void update(Menu menu);
    void deleteById(Long id);
    Menu findById(Long id);
    Menu findByParentIdAndName(Long parentId, String name);
    Menu findByComponentName(String componentName);
    List<Menu> findByParentId(Long parentId);
    List<Menu> findList(...);
}
```

### 5.5 Permission application/service capabilities

The DDD application layer must preserve these public use cases:

- Create/update/delete/get/page/list Role.
- Create/update/delete/get/list Menu.
- Assign menus to a role.
- Assign roles to a user.
- Process role deletion cleanup.
- Process menu deletion cleanup.
- Process user deletion cleanup.
- Check user permissions and roles.
- Build department data permission result.

If legacy service interfaces remain public, they may delegate to application services. Do not break existing consumers during migration.

## 6. Business Rules

### 6.1 Role rules

- Role name must be unique under existing repository semantics.
- Role code must be unique under existing repository semantics.
- Creating or updating a role with super-admin code is forbidden for normal custom roles.
- System roles cannot be updated or deleted.
- Role delete must clean both user-role and role-menu associations.
- Role cache eviction must remain equivalent for create/update/delete and assign-menu side effects.
- Role `LogRecord` behavior must remain equivalent if the public service method still carries operation-log responsibility.

### 6.2 Menu rules

- Parent menu cannot be the menu itself.
- Non-root parent menu must exist.
- Parent menu type must be directory or menu.
- Menu name must be unique under the same parent.
- Component name must remain unique according to existing legacy semantics.
- Delete menu must fail when child menus exist.
- Delete menu must clean role-menu associations.
- Button menu must clear `component`, `componentName`, `icon`, and `path`.
- Menu cache eviction must remain equivalent.
- Tenant menu filtering must remain in the application/service/query path, not in menu persistence.
- Disabled menu filtering must recursively remove menus whose own status is disabled or whose parent chain is disabled.

### 6.3 Permission rules

- `hasAnyPermissions(userId, permissions...)` returns true when requested permissions are empty, matching legacy behavior.
- If the user has no roles, permission checks return false.
- Every requested permission string must match an existing menu permission; unknown permission strings return false.
- Permission checks must preserve the super-admin role fallback.
- `assignRoleMenu` must replace role-menu associations by diffing current and requested menu IDs.
- `assignUserRole` must replace user-role associations by diffing current and requested role IDs.
- `processRoleDeleted` must remove both `UserRoleDO` and `RoleMenuDO` records for the role.
- `processMenuDeleted` must remove `RoleMenuDO` records for the menu.
- `processUserDeleted` must remove `UserRoleDO` records for the user.

### 6.4 Department data permission rules

Preserve `PermissionServiceImpl#getDeptDataPermission` semantics:

- Method must ignore data-permission filtering via `@DataPermission(enable = false)` or equivalent boundary behavior.
- A user with no roles gets `self = true`.
- `DataScopeEnum.ALL` sets `all = true`.
- `DataScopeEnum.DEPT_CUSTOM` adds custom department IDs and must include the user's own department.
- `DataScopeEnum.DEPT_ONLY` adds the user's own department.
- `DataScopeEnum.DEPT_AND_CHILD` adds the user's department and child departments.
- `DataScopeEnum.SELF` sets `self = true`.
- Disabled roles must not grant department data permission unless legacy code explicitly includes them.

## 7. Error Code Contract

Do not replace existing business exceptions with raw Java exceptions at Controller/API boundaries. Error constants live in `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/enums/ErrorCodeConstants.java`.

| Business scenario | Required constant | Parameters | Boundary |
| --- | --- | --- | --- |
| create/update role uses super-admin code | `ROLE_ADMIN_CODE_ERROR` | `code` | application/service maps domain validation to `ServiceException` |
| create/update role duplicate name | `ROLE_NAME_DUPLICATE` | `name` | application/service |
| create/update role duplicate code | `ROLE_CODE_DUPLICATE` | `code` | application/service |
| update/delete/get role missing | `ROLE_NOT_EXISTS` | none | application/service |
| update/delete system role | `ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE` | none | application/service |
| role disabled during validation | `ROLE_IS_DISABLE` | `name` | application/service |
| create/update menu parent is itself | `MENU_PARENT_ERROR` | none | application/service |
| create/update menu parent missing | `MENU_PARENT_NOT_EXISTS` | none | application/service |
| create/update menu parent is not dir/menu | `MENU_PARENT_NOT_DIR_OR_MENU` | none | application/service |
| create/update menu duplicate name under parent | `MENU_NAME_DUPLICATE` | none | application/service |
| create/update menu duplicate component name | `MENU_COMPONENT_NAME_DUPLICATE` | none | application/service |
| update/delete/get menu missing | `MENU_NOT_EXISTS` | none | application/service |
| delete menu with child menus | `MENU_EXISTS_CHILDREN` | none | application/service |
| assign roles to missing user | `USER_NOT_EXISTS` | none | application/service |
| assign disabled role where legacy validation rejects it | `ROLE_IS_DISABLE` | `name` | application/service |

Role-menu and user-role assignment validation must continue to use the same role/menu/user validation paths as legacy `PermissionServiceImpl`, `RoleServiceImpl`, and `MenuServiceImpl`; do not invent new error constants.

Domain may use typed results or domain exceptions internally only if the application layer maps them back to existing `ErrorCodeConstants` before crossing the boundary.

## 8. Transaction Contract

Preserve these transaction boundaries unless tests prove an equivalent boundary:

- Role create/update/delete public operations that write DB state must remain transactional when the legacy service is transactional.
- Menu delete must remain transactional because it deletes menu and role-menu associations.
- `PermissionServiceImpl#assignRoleMenu` uses `@DSTransactional` and must keep cross-datasource transaction semantics.
- `PermissionServiceImpl#assignUserRole` uses `@DSTransactional` and must keep cross-datasource transaction semantics.
- `processRoleDeleted` must remain transactional because it deletes user-role and role-menu associations.
- `processMenuDeleted` currently has no explicit transaction and evicts `MENU_ROLE_ID_LIST` for the deleted menu id; preserve its observable association cleanup and cache behavior.
- `processUserDeleted` currently has no explicit transaction and evicts `USER_ROLE_ID_LIST` for the deleted user id; preserve its observable association cleanup and cache behavior.
- Operation-log context updates must stay inside the same logical use case.

Do not move transaction annotations into pure domain classes. Put them on application service, legacy service facade, or infrastructure adapter boundary.

## 9. Cache Contract

Preserve cache keys and eviction breadth from legacy services:

- Role cache by role ID must evict on role update/delete.
- Permission menu-ID list cache must evict when menu permission changes, menu is deleted, or role-menu assignment changes.
- Menu role-ID list cache must evict when role-menu assignment or role/menu deletion changes associations.
- User role-ID list cache must evict when user-role assignment changes or user/role deletion changes associations.
- `assignRoleMenu` currently evicts both `MENU_ROLE_ID_LIST` and `PERMISSION_MENU_ID_LIST` with `allEntries = true`.
- `assignUserRole` currently evicts `USER_ROLE_ID_LIST` by `userId` only.
- `processRoleDeleted` currently evicts both `MENU_ROLE_ID_LIST` and `USER_ROLE_ID_LIST` with `allEntries = true`.
- `processMenuDeleted` currently evicts `MENU_ROLE_ID_LIST` by `menuId` only.
- `processUserDeleted` currently evicts `USER_ROLE_ID_LIST` by `userId` only.
- Any `allEntries = true` eviction in legacy code must not be narrowed unless tests prove no stale cache can remain.
- Self-invocation cache AOP patterns such as `SpringUtil.getBean(getClass())` must be preserved or replaced with an equivalent non-self-invocation boundary.

## 10. Integration Contract

Preserve collaborations with:

- `TenantService` / tenant menu package filtering for menu list visibility and assign-role-menu request filtering.
- `AdminUserService` for user existence, user department, and dept-data-permission input.
- `DeptService` for department child expansion.
- `RoleService` role validation used by permission assignment.
- `MenuService` menu validation used by role-menu assignment.
- `LogRecordContext` and `@LogRecord` for role operation logs.
- `DataPermissionUtils` / `@DataPermission(enable = false)` behavior.
- API contracts `RoleApi` and `PermissionApi`.

Do not introduce remote RPC calls inside domain classes. Remote/local integration belongs in application or infrastructure adapters.

## 11. Mapping Rules

- Controller VO shape stays unchanged.
- API DTO shape stays unchanged.
- DO table field shape stays unchanged.
- Domain-to-DO conversion must preserve all persistence fields needed for update and response mapping.
- Domain-to-VO conversion must preserve fields currently returned by Controller endpoints.
- Role update must actually mutate or replace `name`, `code`, `sort`, `remark`, `status`, and `dataScope` where applicable.
- Role create/update service facade may map domain back to `RoleDO` for existing LogRecord context, but that mapping must remain outside domain.
- Infrastructure repository pagination must not construct Controller `RolePageReqVO`; use persistence query wrappers or an infrastructure-local query object.
- Menu update must actually mutate or replace menu fields used by list/tree/route responses.
- Avoid constructing Controller request VOs in repository implementations. If current draft does this, mark it as boundary debt and do not spread it.

## 12. Current Conflict Notes

Resolve these before production code migration:

- Role create migration must support database-generated IDs: `RoleFactory.create(...)` may receive `null` id before insert, and `RoleRepository.save(...)` must return a reconstituted domain object with generated id.
- Role tenant id can be absent in current unit-test/create flows because `RoleDO` extends `TenantBaseDO`; Role reconstitution must preserve tenant id when present but not reject `null` during create tests.
- Current `Menu` constructor requires non-null `MenuId` and `parentId`; create flows must verify whether generated IDs/root parent are compatible.
- Role update must mutate or replace user-editable fields (`name`, `code`, `sort`, `status`, `remark`) before persistence; a no-op update is a blocker.
- During migration, repository reconstitution from legacy/test `RoleDO` must tolerate unrelated invalid enum values when the legacy use case did not validate that field. Do not let a duplicate-name/code check fail because random test `type` or `status` cannot become a strict value object.
- `RoleRepository.delete(...)` must delete only the role row. User-role and role-menu cleanup plus cache eviction belong to `PermissionService.processRoleDeleted(...)` or an explicit application use case, not a hidden repository side effect.
- Current `Role#markDeleted()` throws `IllegalStateException`; public behavior must map to `ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE`.
- Current `Menu#validateParentAgainst(...)` throws `IllegalArgumentException`; public behavior must map to `MENU_PARENT_*` errors.
- Current `Menu` constructor clears only `component` and `componentName` for button menus; legacy behavior also clears `icon` and `path`.
- Current DDD application services may not yet preserve all cache, transaction, operation-log, tenant filtering, and data-permission behavior from legacy services.
- `PermissionApiImpl` currently requires `@Primary` due to the `PermissionCommonApi` inheritance/bean overlap; dropping it can create ambiguous bean resolution.
- `MenuController#getSimpleMenuList` currently exposes both `/list-all-simple` and `simple-list` aliases; preserve both exact aliases during Controller migration.

## 13. Acceptance Criteria

A Role/Menu/Permission DDD refactor is acceptable only when all items below are true:

- Domain classes contain no Spring, MyBatis, web, Feign, cache, transaction, or persistence annotations.
- Standard Role/Menu/Permission skeleton exists for migrated slices: `application/permission/port/inbound`, `application/permission/port/outbound`, `application/permission/service`, and `infrastructure/permission/persistence|external|rpc|cache|messaging` are present through real interfaces/classes or `package-info.java` package boundaries.
- Domain repository interfaces live in `domain/permission/repository` and do not import infrastructure or DAL classes.
- Application service implementations live in `application/permission/service` and implement inbound use-case ports from `application/permission/port/inbound`.
- Infrastructure repository implementations live in `infrastructure/permission/persistence` and are the only DDD layer that directly uses Mappers/DOs.
- Controller external behavior is unchanged.
- `RoleApi` / `PermissionApi` external contracts are unchanged.
- Existing service interfaces either remain behavior-compatible or become thin facades over application services.
- All role validation errors use existing error codes.
- All menu validation errors use existing error codes.
- Role create/update/delete preserve cache eviction and operation-log behavior.
- Menu create/update/delete preserve cache eviction and child/association cleanup behavior.
- Role-menu and user-role assignment preserve `@DSTransactional` semantics and cache eviction breadth.
- Dept data permission preserves data-permission bypass and data-scope semantics.
- Tenant menu filtering still happens for menu queries and assign-role-menu request filtering where currently applied.
- Button menu normalization clears component, componentName, icon, and path.
- `PermissionApiImpl` remains `@Primary` while `PermissionApi` extends `PermissionCommonApi` and overlapping beans/contracts exist.
- Menu simple-list aliases preserve both `/list-all-simple` and `simple-list`.
- Tests cover id-null creation compatibility or the chosen ID allocation strategy.
- Tests cover effective Role update; no no-op update may pass review.

## 14. Verification Commands

Run targeted checks after each small refactor batch:

```bash
mvn test -pl develop-module-system/develop-module-system-server -am -Dtest=RoleServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl develop-module-system/develop-module-system-server -am -Dtest=MenuServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl develop-module-system/develop-module-system-server -am -Dtest=PermissionServiceTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl develop-module-system/develop-module-system-server -am -Dtest=SystemArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false
mvn compile -pl develop-module-system/develop-module-system-api -am -DskipTests
mvn compile -pl develop-module-system/develop-module-system-server -am -DskipTests
```

If Controller behavior changes or API DTO mapping changes, also run affected controller/API tests or add regression tests before continuing.

Before migrating Controller or legacy service calls to the DDD application path, regression coverage must exist for these named behaviors. If current tests do not contain equivalent methods, add them first and run them with `-Dtest=Class#method`:

- `RoleServiceImplTest#testCreateRole` or equivalent: create role path must not fail because domain `RoleId` is null before insert and must still persist generated ID, default custom type, and ALL data scope.
- `RoleServiceImplTest#testUpdateRole` or equivalent: role update must change persisted/returned fields and cannot be a no-op.
- `RoleServiceImplTest#testValidateRoleDuplicate_nameDuplicate` and `testValidateRoleDuplicate_codeDuplicate` or equivalent: duplicate checks must throw existing `ServiceException` error codes even when unrelated persisted enum fields contain legacy/test values.
- `MenuServiceImplTest#testCreateOrUpdateButtonMenu_clearsDisplayFields` or equivalent: button menu normalization clears `component`, `componentName`, `icon`, and `path`.
- `PermissionServiceTest#testHasAnyPermissions_unknownPermissionReturnsFalse` or equivalent: unknown permission string returns false.
- `PermissionServiceTest#testHasAnyPermissions_superAdminFallback` or equivalent: super-admin role still grants permissions after strict menu matching rules are applied.
- `PermissionServiceTest#testGetDeptDataPermission_ignoresDataPermissionAndPreservesScopes` or equivalent: department data permission preserves all/self/custom/own/child semantics.

For documentation-only skill edits, run at least:

```bash
git diff --check -- .claude/ddd-skills/AggregateRoot_Role_Menu_Skill.md
grep -n "^## " .claude/ddd-skills/AggregateRoot_Role_Menu_Skill.md
```

## 15. Common Mistakes

- Treating Role and Menu as one large aggregate with object references between them. Use IDs for cross-aggregate associations.
- Moving `@CacheEvict` behavior into domain or dropping it entirely.
- Replacing `@DSTransactional` with plain `@Transactional` without proof.
- Changing menu tenant behavior by adding tenant ownership to Menu rows.
- Forgetting recursive disabled-parent menu filtering.
- Allowing unknown permission strings to pass permission checks.
- Forgetting super-admin fallback.
- Keeping current no-op Role update and assuming repository update fixed it.
- Hiding role-menu/user-role cleanup inside `RoleRepository.delete(...)`, which bypasses existing cache eviction contracts.
- Making value-object reconstruction too strict for fields that legacy tests or unchanged use cases do not validate.
- Mapping domain exceptions directly to generic 500/400 responses instead of existing error codes.

## 16. Red Flags

Stop the refactor immediately if any of these appear:

- Controller path, method, VO, permission annotation, export, or pagination changes are required.
- Role/menu/error code behavior differs from legacy service tests.
- Cache eviction breadth is narrowed without a regression test.
- `@DSTransactional` is removed from role-menu or user-role assignment paths.
- Tenant menu filtering disappears from menu query paths.
- Domain classes import Spring, MyBatis, Feign, Mapper, DO, Controller VO, or Cache APIs.
- A create flow passes `null` into a value object that rejects null without an explicit ID allocation strategy.
- Role update still does not modify the fields users can update.
- Role repository imports or constructs Controller VO for persistence queries.
- Role delete removes role-menu/user-role associations without going through the cache-evicting cleanup boundary.
- A migrated slice leaves application classes directly under `application/permission` instead of `application/permission/service` and inbound ports.
- A migrated slice leaves repository implementations directly under `infrastructure/permission` instead of `infrastructure/permission/persistence`.
- `port/outbound`, `external`, `rpc`, `cache`, or `messaging` package boundaries are omitted because they are currently empty.

## 17. Rollback Conditions

Rollback the current batch if:

- `develop-module-system/develop-module-system-server` no longer compiles.
- Existing Role/Menu/Permission service tests fail for behavior unrelated to the intended change.
- Any public API/Controller contract changes without explicit user approval.
- Cache, transaction, tenant, or data-permission behavior cannot be proven equivalent.
- A migration requires changing database schema or seed data outside the approved batch.

## 18. AI Self-Check

Before reporting completion, answer yes to all:

- Did I compare the DDD code against `RoleServiceImpl`, `MenuServiceImpl`, and `PermissionServiceImpl`?
- Did I create or preserve the standard Role/Menu/Permission skeleton for the migrated slice, including inbound ports, outbound port boundary, application service package, persistence adapter package, and fixed external/rpc/cache/messaging package boundaries?
- Did I preserve existing error codes instead of introducing generic exceptions?
- Did I preserve cache eviction keys and `allEntries` breadth?
- Did I preserve `@DSTransactional` where used by assignment operations?
- Did I preserve tenant menu filtering and disabled-parent filtering?
- Did I cover the current Role/Menu id-null creation conflicts?
- Did I prove Role update is not a no-op?
- For code/refactor changes, did I run fresh compile/test commands and read their output before claiming completion?
- For documentation-only skill edits, did I run the section 14 documentation checks and read their output before claiming completion?
