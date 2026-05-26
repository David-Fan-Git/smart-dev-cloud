---
name: aggregate-root-user-validation-skill
description: Use when modifying or reviewing the system AdminUser/User aggregate, user API, auth/profile/import collaboration, tenant quota, permission cleanup, or DDD migration boundaries.
type: ddd-aggregate-skill
status: production-ready
---

# AggregateRoot User Validation Skill

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

## Overview

系统后台用户 `User/AdminUser` 聚合承载后台账号生命周期：创建、注册、更新资料、岗位关联、状态启停、密码变更、登录记录、删除、导入、分页查询和 RPC 校验。当前代码处于旧 `AdminUserServiceImpl` 与 DDD `UserApplicationService` 共存阶段；本 skill 的目标是让后续重构优先保护外部 API、权限、数据权限、租户、错误码、导入导出、操作日志、OAuth2 token 清理和岗位/权限联动。

## When to Use

使用本 skill：
- 修改 `system` 后台用户 Controller/API/DTO/VO/DO/Mapper/Convert。
- 修改 `domain/user` 聚合、值对象、工厂、仓储、事件、基础设施适配器。
- 将 `AdminUserServiceImpl` 行为迁移到 `UserApplicationService` 或领域模型。
- 修复后台用户唯一性、密码编码、状态禁用、OAuth2 token 清理、岗位同步、分页过滤、导入导出问题。

不要使用本 skill：
- 仅修改 Dept、Post、Role、Permission、Tenant、OAuth2、Social、SMS、Notify 独立聚合。
- 需要改变 `/system/user`、`/system/user/profile`、`/system/oauth2/user`、`AdminUserApi` 路径或 DTO 字段；这必须单独写迁移计划。
- 需要一次性删除 `AdminUserServiceImpl`；当前它仍承载注册、导入、LogRecord、租户配额、OAuth2 token 清理和旧测试契约。

## Baseline Failure Findings

RED 基线验证发现旧 skill 会诱导以下失败：
- 无 YAML frontmatter、无生产级事实源锚点、无字段映射、错误码、事务、验证命令、红线和回滚条件。
- 旧 skill 像“新建规划”，但当前已存在 `domain/user`、`application/user/UserApplicationService.java`、`infrastructure/user/UserRepositoryImpl.java`。
- 当前事实冲突：`UserId.of(id)` 与 `User` 构造器要求 `id`、`tenantId` 非空，而 `UserController#createUser` 调用 `userApplicationService.createUser(null, ..., tenantId=null, ...)`。
- 未保护 `AdminUserServiceImpl` 中的注册开关、租户配额、Excel 导入、`@LogRecord`、`PermissionService` 清理、`OAuth2TokenService.removeAccessToken` 等行为。
- 未保护 `/system/user` 权限、`@DataPermission(enable=false)`、分页 `roleId/createTime/deptId` 过滤、Excel 导入导出和 `AdminUserApi` AutoTrans/FeignIgnore 契约。
- 领域层当前部分方法抛 `IllegalArgumentException`，若直接外放会破坏 `USER_PASSWORD_FAILED`、`USER_EMAIL_EXISTS` 等 `ServiceException` 错误码契约。

## Go / No-Go Gate

`status: production-ready` means this skill is ready to guide production refactoring. The DDD create path now resolves `tenantId` from the explicit argument or `TenantContextHolder.getRequiredTenantId()`, uses `UserRepository#create(...)` to insert first when `id == null`, returns the generated user ID, syncs user-post relations, and publishes `UserCreatedEvent`; this is covered by `UserApplicationServiceTest#createUserUsesTenantContextAndReturnsGeneratedId`. Do not remove legacy create/register/import paths until tenant quota, LogRecord, register config, import semantics, OAuth2 and permission cleanup have tested DDD equivalents.

## Reproducibility Contract

1. 先读取 Current Source Anchors，并确认路径与测试现状仍存在，再改代码。
2. skill 与当前代码冲突时，以当前可编译代码的外部行为为准，先更新 skill 或拆出问题批次，再改代码。
3. DDD 重构不得改变 Controller 路径、HTTP 方法、VO/DTO 字段、权限码、数据权限、租户、分页、Excel、OpenAPI 和 RPC 契约。
4. Domain 层不得 import Spring、MyBatis、Controller VO、Mapper、MQ/RPC、Servlet、Tenant/OAuth2/Permission service。
5. 旧 Service 是迁移来源和兼容层，未覆盖注册/导入/操作日志/租户/OAuth2/权限清理前不得删除。
6. 任何 `IllegalArgumentException` 进入外部接口前，必须转换或替换为既有 `ErrorCodeConstants` 的 `ServiceException` 契约。

## Current Source Anchors

### Controllers

- Admin user: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/user/UserController.java`
  - `POST /system/user/create` permission `system:user:create`
  - `PUT /system/user/update` permission `system:user:update`
  - `DELETE /system/user/delete` permission `system:user:delete`
  - `DELETE /system/user/delete-list` permission `system:user:delete`
  - `PUT /system/user/update-password` permission `system:user:update-password`
  - `PUT /system/user/update-status` permission `system:user:update`
  - `GET /system/user/page` permission `system:user:query`
  - `GET /system/user/list` permission `system:user:query`
  - `GET /system/user/list-all-simple` and `/system/user/simple-list`
  - `GET /system/user/get` permission `system:user:query`
  - `GET /system/user/export-excel` permission `system:user:export`
  - `GET /system/user/get-import-template`
  - `POST /system/user/import` permission `system:user:import`
- Profile: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/user/UserProfileController.java`
- OAuth2 user: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/oauth2/OAuth2UserController.java`
- OAuth2 token/open flows: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/oauth2/`

### API, Remote, DTO

- Stable contract: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/AdminUserApi.java`
- Remote adapter: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/remote/AdminUserRemoteClient.java`
- Server implementation: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/api/user/AdminUserApiImpl.java`
- RPC DTO: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/dto/AdminUserRespDTO.java`

### VO

- Admin user VOs: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/user/vo/user/`
  - `UserSaveReqVO.java`, `UserPageReqVO.java`, `UserRespVO.java`, `UserSimpleRespVO.java`, `UserImportExcelVO.java`, `UserImportRespVO.java`, `UserUpdatePasswordReqVO.java`, `UserUpdateStatusReqVO.java`
- Profile VOs: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/user/vo/profile/`
- OAuth2 user VOs: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/oauth2/vo/user/`

### DO, Mapper, Convert

- User DO: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/dataobject/user/AdminUserDO.java`
- User mapper: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/mysql/user/AdminUserMapper.java`
- UserPost DO: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/dataobject/dept/UserPostDO.java`
- UserPost mapper: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/mysql/dept/UserPostMapper.java`
- User convert: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/convert/user/UserConvert.java`
- OAuth2 convert: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/convert/oauth2/OAuth2OpenConvert.java`

### Service, Application, Auth, OAuth2

- DDD application service: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/application/user/service/UserApplicationService.java`
- Legacy service interface: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/user/AdminUserService.java`
- Legacy service implementation: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/user/AdminUserServiceImpl.java`
- Auth service: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/auth/AdminAuthServiceImpl.java`
- OAuth2 token service: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/oauth2/OAuth2TokenServiceImpl.java`

### Domain

- Aggregate: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/user/User.java`
- Factory: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/user/UserFactory.java`
- Repository interface: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/user/repository/UserRepository.java`
- Query object: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/user/repository/UserPageQuery.java`
- Domain services: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/user/service/`
  - `PasswordEncoder.java`, `UserUniquenessChecker.java`
- Value objects: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/user/valueobject/`
  - `UserId.java`, `Username.java`, `RawPassword.java`, `EncodedPassword.java`, `Email.java`, `Mobile.java`, `UserProfile.java`, `UserStatus.java`, `LoginRecord.java`
- Events: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/user/event/`
  - `DomainEvent.java`, `DomainEventPublisher.java`, `UserCreatedEvent.java`, `UserDeletedEvent.java`, `UserDisabledEvent.java`, `UserLoggedInEvent.java`, `UserPasswordChangedEvent.java`

### Infrastructure

- Repository implementation: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/user/persistence/UserRepositoryImpl.java`
- Password adapter: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/user/external/BCryptPasswordEncoderAdapter.java`
- Event publisher: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/user/messaging/SpringDomainEventPublisher.java`
- Uniqueness checker: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/user/persistence/UserUniquenessCheckerImpl.java`
- Subscribers:
  - `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/user/messaging/UserDisabledTokenCleaner.java`
  - `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/user/messaging/UserDeletedPermissionCleaner.java`

### Error Codes and Tests

- Error codes: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/enums/ErrorCodeConstants.java`
- Main tests:
  - `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/user/AdminUserServiceImplTest.java`
  - `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/auth/AdminAuthServiceImplTest.java`
  - `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/controller/admin/oauth2/OAuth2OpenControllerTest.java`
  - `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/oauth2/OAuth2TokenServiceImplTest.java`
  - `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/permission/PermissionServiceTest.java`
  - `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/dept/DeptServiceImplTest.java`
  - `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/dept/PostServiceImplTest.java`

## Fixed Data Model

### `AdminUserDO` table model

| Field | Type | Meaning | Contract |
|---|---|---|---|
| `id` | `Long` | 用户 ID | Primary key; maps to domain `UserId` and all API/VO id fields |
| `username` | `String` | 用户账号 | Unique; `UserSaveReqVO` requires 4-30 alnum |
| `password` | `String` | BCrypt 密码 | Never expose to VO/DTO; maps to `EncodedPassword` |
| `nickname` | `String` | 用户昵称 | Exposed in VO/DTO/profile; max 30 in create/update VO |
| `remark` | `String` | 备注 | Admin/profile field; maps to `UserProfile.remark` |
| `deptId` | `Long` | 部门 ID | External Dept aggregate id; validate through DeptService |
| `postIds` | `Set<Long>` | 岗位 IDs | Stored with Jackson type handler and synchronized with `UserPostDO` relation |
| `email` | `String` | 邮箱 | Optional unique when nonblank；格式由 VO/API 入参校验，领域重建必须兼容历史持久化值；持久化/API 输出必须保留原始大小写，不得在 `Email.value()` 中自动转小写 |
| `mobile` | `String` | 手机号 | Optional unique when nonblank；格式由 VO/API 入参校验，领域重建必须兼容历史持久化值 |
| `sex` | `Integer` | 性别 | `SexEnum`; maps to `UserProfile.sex` |
| `avatar` | `String` | 头像 | Exposed in API/VO/profile |
| `status` | `Integer` | 账号状态 | `CommonStatusEnum`; maps to `UserStatus` |
| `loginIp` | `String` | 最后登录 IP | Updated by login record path |
| `loginDate` | `LocalDateTime` | 最后登录时间 | Updated with current time |
| `tenantId` | inherited | 租户 | From `TenantBaseDO`; current DDD create path has a null conflict that must be resolved before relying on it |
| `createTime` | inherited | 创建时间 | From base DO metadata; exposed in `UserRespVO` and `UserProfileRespVO`; current domain conversion does not set it |

### External DTO/VO field contracts

- `AdminUserRespDTO`: `id`, `nickname`, `status`, `deptId`, `postIds`, `mobile`, `avatar`.
- `UserRespVO`: `id`, `username`, `nickname`, `remark`, `deptId`, `deptName`, `postIds`, `email`, `mobile`, `sex`, `avatar`, `status`, `loginIp`, `loginDate`, `createTime`.
- `UserProfileRespVO`: `id`, `username`, `nickname`, `email`, `mobile`, `sex`, `avatar`, `loginIp`, `loginDate`, `createTime`, `roles`, `dept`, `posts`.
- `UserPageReqVO`: `username`, `mobile`, `status`, `createTime`, `deptId`, `roleId`, `pageNo`, `pageSize`; role and createTime filtering must not be silently dropped.

## Required Method Signatures

Keep these public use cases available while migrating callers; change signatures only after verifying callers and updating this skill first.

### Aggregate and factory

```java
public void disable()
public void enable()
public void changePassword(RawPassword oldPassword, RawPassword newPassword, PasswordEncoder encoder)
public void resetPassword(RawPassword newPassword, PasswordEncoder encoder)
public void updateProfile(UserProfile newProfile)
public void updateContact(Email newEmail, Mobile newMobile, UserUniquenessChecker checker)
public void recordLogin(LoginRecord loginRecord)
public void syncPosts(Set<Long> newPostIds)
public void markDeleted()
public List<DomainEvent> pullEvents()

public static User create(Long id, String username, EncodedPassword encodedPassword,
                          Long tenantId, Long deptId, String email, String mobile,
                          String nickname, String avatar, Integer sex, String remark,
                          Set<Long> postIds)
public static User reconstitute(Long id, String username, String encodedPassword,
                                Long tenantId, Long deptId, String email, String mobile,
                                String nickname, String avatar, Integer sex, String remark,
                                Integer status, Set<Long> postIds,
                                String loginIp, LocalDateTime loginDate)
```

### Repository

```java
User create(String username, EncodedPassword encodedPassword, Long tenantId, Long deptId,
            String email, String mobile, String nickname, String avatar, Integer sex,
            String remark, Set<Long> postIds)
void save(User user)
void delete(UserId id)
User findById(UserId id)
Optional<User> findByUsername(Username username)
Optional<User> findByEmail(Email email)
Optional<User> findByMobile(Mobile mobile)
List<User> findByIds(Collection<UserId> ids)
List<User> findByDeptIds(Collection<Long> deptIds)
List<User> findByPostIds(Collection<Long> postIds)
List<User> findByNickname(String nickname)
List<User> findByStatus(UserStatus status)
PageResult<User> findPage(UserPageQuery query)
boolean existsByUsername(Username username)
boolean existsByEmail(Email email)
boolean existsByMobile(Mobile mobile)
long count()
```

### Application service

```java
@Transactional public Long createUser(Long id, String username, String rawPassword, Long tenantId,
                                      Long deptId, String email, String mobile,
                                      String nickname, String avatar, Integer sex, String remark,
                                      Set<Long> postIds)
@Transactional public void updateUser(Long id, String username, String email, String mobile,
                                      String nickname, String avatar, Integer sex, String remark,
                                      Long deptId, Set<Long> postIds)
@Transactional public void updateUserStatus(Long id, Integer statusCode)
@Transactional public void changePassword(Long id, String oldRawPassword, String newRawPassword)
@Transactional public void resetPassword(Long id, String newRawPassword)
@Transactional public void updateProfile(Long id, String email, String mobile,
                                        String nickname, String avatar, Integer sex, String remark)
@Transactional public void recordLogin(Long id, String loginIp)
@Transactional public void deleteUser(Long id)
@Transactional public void deleteUserList(List<Long> ids)
public User getUser(Long id)
public User getUserByUsername(String username)
public PageResult<User> getUserPage(UserPageQuery query)
public List<User> getUserList(Collection<Long> ids)
public List<User> getUserListByDeptIds(Collection<Long> deptIds)
public List<User> getUserListByPostIds(Collection<Long> postIds)
public List<User> getUserListByStatus(Integer status)
public List<User> getUserListByNickname(String nickname)
public void validateUserList(Collection<Long> ids)
public Set<Long> getDeptCondition(Long deptId)
```

## Business Rules

| ID | Rule | Layer | Verification |
|---|---|---|---|
| BR01 | 创建用户默认启用 | Legacy/Application/Domain | `status = ENABLE` preserved |
| BR02 | 创建/更新时用户名唯一 | Application/legacy service | `USER_USERNAME_EXISTS` |
| BR03 | 创建/更新时邮箱非空唯一 | Application/legacy service | `USER_EMAIL_EXISTS` |
| BR04 | 创建/更新时手机号非空唯一 | Application/legacy service | `USER_MOBILE_EXISTS` |
| BR05 | 创建、注册、重置密码、导入初始密码必须 BCrypt 加密 | Domain adapter/legacy service | Password is encoded before persistence |
| BR06 | 个人修改密码必须校验旧密码；旧密码比对兼容历史短密码，不套用新密码长度校验，新密码仍必须校验长度并加密 | Legacy service/domain migration | `USER_PASSWORD_FAILED` |
| BR07 | 禁用用户必须清理 admin OAuth2 access token | Legacy service or subscriber | `OAuth2TokenService.removeAccessToken(id, ADMIN)` equivalent |
| BR08 | 删除用户必须清理权限和岗位关联 | Legacy service/application/subscriber/repository | `PermissionService.processUserDeleted`, `UserPostMapper.deleteByUserId` |
| BR09 | 创建/注册必须校验租户配额 | Legacy service/application migration | `USER_COUNT_MAX` with account count parameter |
| BR10 | 注册必须读取注册开关配置 | Legacy service/auth migration | `USER_REGISTER_DISABLED` |
| BR11 | 导入空列表、初始密码为空、校验失败、是否更新支持的行为不变 | Legacy service | `UserImportRespVO` create/update/failure lists |
| BR12 | Admin page must preserve dept subtree, roleId, createTime, username/mobile/status filters | Controller/repository | Compare `UserPageReqVO` and mapper behavior |
| BR13 | API validation checks both existence and enabled status | Application/API | `USER_NOT_EXISTS`, `USER_IS_DISABLE(nickname)` |
| BR14 | Operation log context for create/update/delete/password reset must remain | Legacy service or equivalent | `@LogRecord` and `LogRecordContext` preserved |

## Error Code Contract

Use `develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/enums/ErrorCodeConstants.java` exactly.

| Scenario | Error code | Parameter contract | Throwing layer |
|---|---|---|---|
| Username exists | `USER_USERNAME_EXISTS` | none | Application/legacy uniqueness validation |
| Mobile exists | `USER_MOBILE_EXISTS` | none | Application/legacy uniqueness validation |
| Email exists | `USER_EMAIL_EXISTS` | none | Application/legacy uniqueness validation |
| User not found | `USER_NOT_EXISTS` | none | Application/legacy validation |
| Import list empty | `USER_IMPORT_LIST_IS_EMPTY` | none | Import flow |
| Old password mismatch | `USER_PASSWORD_FAILED` | none | Password change flow |
| User disabled | `USER_IS_DISABLE` | `nickname` as first parameter | Validate/auth flows |
| Tenant account quota exceeded | `USER_COUNT_MAX` | `accountCount` as first parameter | Create/register flow |
| Import init password empty | `USER_IMPORT_INIT_PASSWORD` | none | Import flow |
| Mobile not registered | `USER_MOBILE_NOT_EXISTS` | none | Auth/reset mobile flow |
| Register disabled | `USER_REGISTER_DISABLED` | none | Register flow |
| Bad credentials | `AUTH_LOGIN_BAD_CREDENTIALS` | none | Auth service |
| Disabled login | `AUTH_LOGIN_USER_DISABLED` | none | Auth service |

Do not replace these with `IllegalArgumentException` at Controller/API boundaries. Do not rename constants, change numeric codes, change messages, or reorder parameters.

## Transaction Contract

| Use case | Transaction requirement | Notes |
|---|---|---|
| `AdminUserServiceImpl#createUser` | `@Transactional(rollbackFor = Exception.class)` | Tenant quota, validation, user insert, user-post insert, LogRecord context |
| `registerUser` | preserve current behavior | Config switch, quota, validation, insert |
| `updateUser` | `@Transactional(rollbackFor = Exception.class)` | User update and post diff sync atomic |
| `updateUserPassword(profile)` | current: no explicit `@Transactional`; preserve old-password validation then update | Single-row update path; must emit `USER_PASSWORD_FAILED` on mismatch |
| `updateUserPassword(admin reset)` | preserve `@LogRecord` behavior | Password encoded, context variables kept |
| `updateUserStatus` | preserve token cleanup on disable | Domain event subscriber is acceptable only if synchronous behavior is preserved |
| `deleteUser` | `@Transactional(rollbackFor = Exception.class)` | User delete, permission cleanup, post delete, LogRecord context |
| `deleteUserList` | `@Transactional(rollbackFor = Exception.class)` | Batch delete and per-user cleanup |
| `importUserList` | `@Transactional(rollbackFor = Exception.class)` | Per-row success/failure semantics unchanged |
| `UserApplicationService` writes | `@Transactional` | Publish domain events within the same intended transaction boundary |

## Integration Contract

- Tenant: `TenantService.handleTenantInfo` quota check must be preserved before create/register reaches persistence.
- Config: `ConfigApi.getConfigValueByKey(system.user.register-enabled)` and `system.user.init-password` are required in register/import flows.
- Dept/Post: `DeptService.validateDeptList`, `PostService.validatePostList`, dept subtree filtering and post relation sync must remain outside domain aggregate.
- Permission: user delete must call `PermissionService.processUserDeleted` or a tested synchronous equivalent.
- OAuth2: user disable must remove admin access tokens or a tested subscriber equivalent must consume `UserDisabledEvent`.
- Data permission: `DataPermissionUtils.executeIgnore` and `@DataPermission(enable=false)` usage in uniqueness/API reads must not be dropped.
- Operation log: `@LogRecord`, `LogRecordContext`, and diff variables must survive migration.
- Excel: export/import/template endpoint paths, VO classes, failure map semantics and `ExcelUtils` usage are external behavior.
- API local/remote: callers inject `AdminUserApi`; Feign identity stays only on `remote/AdminUserRemoteClient`; keep `@AutoTrans` and `@FeignIgnore` defaults.

## Mapping Rules

- Domain may depend on value objects, domain service interfaces, repository interfaces and domain events only.
- Infrastructure maps `AdminUserDO/UserPostDO ↔ User`; domain must not import DO/Mapper.
- `UserRepositoryImpl.toDataObject()` currently does not set `createTime` and will overwrite through update; avoid losing base metadata and unrelated fields when switching partial updates.
- `UserPageQuery` currently lacks `roleId` and `createTime`; do not claim full `UserPageReqVO` parity until those filters are preserved.
- `UserRepositoryImpl.findPage` currently constructs a Controller-layer `UserPageReqVO`; this is a known boundary debt and must not be copied into domain APIs or used to justify Controller imports in domain.
- `UserConvert.convertUser(...)` currently does not set `remark` or `createTime`; do not switch endpoints to domain conversion unless response fields match old DO conversion.
- Reconstitution must not publish real login events; current `UserFactory.reconstitute` clears login event after restoring last login.

## Current Conflict Notes

- `UserId.of(id)` requires id non-null, and `User` constructor requires `tenantId` non-null. `UserApplicationService#createUser` resolves `tenantId` from the explicit argument or `TenantContextHolder.getRequiredTenantId()` and calls `UserRepository#create(...)` when `id == null`, so DDD create path returns the inserted user id instead of the input null. This only covers the application-level create path; legacy create/register/import behavior remains the baseline until all integration contracts are migrated and tested.
- Legacy `AdminUserServiceImpl#createUser` still enforces tenant quota through `TenantService.handleTenantInfo`, validates Dept/Post, inserts `AdminUserDO`, syncs `UserPostDO`, records `@LogRecord` context, and returns generated `user.getId()`. Any DDD replacement must preserve all of those observable effects.
- `UserRepositoryImpl.findPage` only maps username/mobile/status/deptIds/userIds/pageNo/pageSize; it does not map `UserPageReqVO.createTime` or `roleId` directly. Role filtering should first resolve permission userIds and pass them into `UserPageQuery.userIds`; createTime needs an explicit query field before parity can be claimed.
- `UserRepositoryImpl.findPage` constructs Controller-layer `UserPageReqVO` inside infrastructure. Treat this as boundary debt: it may be tolerated while migrating, but must not leak into domain signatures or be copied to new domain code.
- `UserConvert.convertUser(User, DeptDO)` currently omits `remark` and `createTime`, while old DO conversion can expose them. Do not switch list/export/detail responses to domain conversion until field parity is tested.
- `AdminUserApiImpl` now uses domain `User` and private `toDTO`; verify `@DataPermission(enable=false)`, `DataPermissionUtils.executeIgnore`, `@AutoTrans`, `@FeignIgnore`, and DTO field parity before further API changes.
- Domain methods throw `IllegalArgumentException`; application layer must preserve `ServiceException` error code contract before exposing these paths.
- Import remains legacy-backed: `UserController#importUser` delegates to `AdminUserService.importUserList`, which preserves `system.user.init-password`, row-level validation failures, create/update/failure lists, and `USER_IMPORT_*` errors.

## Quick Reference

| Task | Correct place | Forbidden place |
|---|---|---|
| Validate username/email/mobile uniqueness | Application/domain service port, using repository/mapper in infrastructure | Domain aggregate querying mapper |
| Encode password | Domain `PasswordEncoder` port + infrastructure BCrypt adapter, or legacy Spring encoder during migration | Plaintext in DO or Controller |
| Validate Dept/Post existence | Application/legacy service via DeptService/PostService | Domain aggregate |
| Tenant quota/register config/import password config | Application/legacy orchestration | Domain aggregate |
| Sync user-post relation | Repository infrastructure or legacy service transaction | Controller/domain direct mapper calls |
| Clean OAuth2 token on disable | Legacy service or synchronous domain event subscriber | Domain aggregate calling OAuth2 service |
| Clean permission on delete | Legacy service or synchronous subscriber | Domain aggregate calling PermissionService |
| Convert VO/DTO/Excel | `convert/user` and Controller/API layer | Domain aggregate |

## Acceptance Criteria

- AC01: `User` has no Spring/MyBatis annotations or infrastructure imports.
- AC02: Value objects remain immutable and enforce their own invariants where currently implemented.
- AC03: `UserRepository` remains in domain and imports no MyBatis/DO classes.
- AC04: `UserRepositoryImpl` remains infrastructure and owns DO/UserPost mapping.
- AC05: Controller paths, HTTP methods, permissions, API DTO fields and RPC paths are unchanged.
- AC06: `AdminUserServiceImpl` register/import/LogRecord/tenant/OAuth2/permission cleanup behavior is preserved or replaced with tested equivalent before deletion.
- AC07: Pagination preserves dept subtree, roleId, createTime, username/mobile/status and page semantics before old path is removed.
- AC08: `USER_*` and `AUTH_*` error codes and parameter contracts are unchanged.
- AC09: Disabling a user still invalidates admin OAuth2 tokens.
- AC10: Deleting a user still removes permission associations and user-post associations.
- AC11: `UserApplicationService#createUser` id/tenantId null conflict has a tested resolution: `id == null` inserts first through `UserRepository#create(...)`, tenantId comes from explicit argument or tenant context, and createUser returns the generated user id.
- AC12: Domain-to-VO conversion preserves `remark`, `createTime`, `deptName`, roles/posts/dept profile data and API DTO fields.
- AC13: `AdminUserApiImpl` preserves `@DataPermission(enable=false)`, `@AutoTrans`, `@FeignIgnore`, and `DataPermissionUtils.executeIgnore` read semantics.
- AC14: `UserController#importUser` keeps legacy import behavior until a DDD replacement preserves `system.user.init-password`, row validation, create/update/failure lists, update-support behavior, and `USER_IMPORT_*` errors.
- AC15: System API/server compile and relevant service/auth/OAuth/API/controller tests pass or known pre-existing failures are documented.

## Verification Commands

Run focused checks after modifying this area:

```bash
mvn compile -pl develop-module-system/develop-module-system-api -am -DskipTests
mvn compile -pl develop-module-system/develop-module-system-server -am -DskipTests
```

If tests are added or touched:

```bash
mvn test -pl develop-module-system/develop-module-system-server -Dtest=AdminUserServiceImplTest
mvn test -pl develop-module-system/develop-module-system-server -Dtest=AdminAuthServiceImplTest
mvn test -pl develop-module-system/develop-module-system-server -Dtest=OAuth2TokenServiceImplTest
mvn test -pl develop-module-system/develop-module-system-server -Dtest=OAuth2OpenControllerTest
```

Required regression coverage before removing old paths or claiming behavior parity:

```bash
# add/update focused tests if absent, then run them
mvn test -pl develop-module-system/develop-module-system-server -Dtest='*User*Controller*Test,*AdminUserApi*Test,*UserApplicationService*Test'
```

Those tests must cover create id/tenantId handling, register/import failure maps, LogRecord-sensitive update/delete/password paths, disable-token cleanup, delete-permission/post cleanup, page roleId/createTime/dept filters, `@DataPermission(enable=false)`, and response `remark/createTime` parity.

If API local/remote contract changed or consumers are affected, also compile likely consumers:

```bash
mvn compile -pl develop-module-member/develop-module-member-server,develop-module-mall/develop-module-trade-server,develop-module-mes/develop-module-mes-server -am -DskipTests
```

## Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| Deleting `AdminUserServiceImpl` because `UserApplicationService` exists | Register/import/log/tenant/OAuth2/permission/test behavior breaks | Keep compatibility layer until each behavior is migrated and tested |
| Ignoring tenantId null conflict | Create user path throws before insert | Resolve tenant source or compatibility strategy first |
| Replacing ServiceException with IllegalArgumentException | Frontend/API error handling regresses | Throw existing `ErrorCodeConstants` from application boundary |
| Switching page endpoint to domain query without role/createTime parity | User list filters regress | Extend query/repository and verify mapper behavior |
| Dropping `@DataPermission(enable=false)` or `executeIgnore` | Valid users hidden by data permission | Preserve ignore scopes |
| Publishing events async without transaction semantics | Token/permission cleanup timing changes | Use tested synchronous listener or keep direct service call |
| Domain imports Dept/Post/Tenant/OAuth2/Permission services | DDD boundary regression | Keep cross-aggregate orchestration in application layer |

## Rationalization Table

| Excuse | Reality |
|---|---|
| “DDD 层已经存在，可以直接删旧 Service” | 旧 Service 仍承担注册、导入、操作日志、租户配额、OAuth2 token 和权限清理。 |
| “分页能查出来就行” | `roleId`、`createTime`、部门子树、数据权限和 Excel 导出都是外部契约。 |
| “领域异常更纯粹” | 外部接口依赖现有 `ErrorCodeConstants` 和 `ServiceException`。 |
| “tenantId 之后再补” | 当前 create 路径已传 null，而聚合要求非空，这是启动/运行阻塞风险。 |
| “事件可以替代直接调用” | 只有保持事务与同步语义并有测试时，事件替代才安全。 |

## Red Flags

看到以下情况立即停止当前批次：
- 准备改 `/system/user`、`/system/user/profile`、`/system/oauth2/user` 或 `AdminUserApi` 外部路径/字段/权限。
- 准备删除 `AdminUserServiceImpl`，但注册、导入、LogRecord、租户配额、OAuth2 token、权限清理未迁移验证。
- Domain 层出现 Spring、MyBatis、DO、Mapper、VO、DTO、Tenant、OAuth2、Permission、Dept/Post service import。
- `UserController#createUser` 的 tenantId null 冲突未处理却继续扩展 DDD 创建链路。
- `UserPageReqVO.roleId/createTime` 在新分页路径中丢失。
- `USER_IS_DISABLE` 不再携带 nickname 参数。
- 用户禁用不再清理 admin token，或用户删除不再清理权限/岗位。

## Rollback Conditions

必须回滚或继续补强，如果出现：
1. `develop-module-system-api` 或 `develop-module-system-server` 编译失败。
2. 任一 Controller/API 外部契约发生未批准变化。
3. 注册、导入、分页、Excel、权限、租户、OAuth2 或操作日志行为缺失。
4. 数据权限绕过范围变化导致 API 查询结果变化。
5. 新实现无法说明每个用户错误码场景和事务边界。

## AI Self-Check

完成前逐项确认：
- 我是否先读了 Current Source Anchors 中的相关文件？
- 我是否确认了当前路径和测试状态没有漂移？
- 我是否保留了 Controller/API/VO/DTO/权限/数据权限/错误码外部契约？
- 我是否处理了 `tenantId` null 冲突或明确停止当前批次？
- 我是否没有让 domain 依赖 Spring/MyBatis/VO/DTO/DO/Mapper/Tenant/OAuth2/Permission？
- 我是否保留了注册、导入、操作日志、租户配额、OAuth2 token、权限清理行为？
- 我是否保留了分页 `roleId/createTime/deptId` 和 Excel 导出字段？
- 我是否运行了至少一个 system api/server compile 命令？
