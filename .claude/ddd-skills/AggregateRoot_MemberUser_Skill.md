---
name: aggregate-root-member-user-skill
description: Use when modifying or reviewing the Member User aggregate, member user API, auth collaboration, points, profile, tags, groups, or DDD migration boundaries.
type: ddd-aggregate-skill
status: production-review
---

# AggregateRoot MemberUser Skill

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

会员用户 `MemberUser` 聚合承载会员账号基础生命周期：创建/自动注册、资料更新、手机与密码变更、登录记录、状态、等级、经验、积分、分组与标签查询映射。当前代码处于旧 Service 与 DDD 层共存阶段；本 skill 的目标是让重构在不改变外部 API、权限、错误码、事务和消息行为的前提下逐步收敛。

## When to Use

使用本 skill：
- 修改会员用户聚合、值对象、工厂、仓储、应用服务或基础设施适配器。
- 将 `service/user` 中的会员用户规则迁移到 `domain/application/infrastructure/convert`。
- 调整 `MemberUserApi`、`MemberUserApiImpl`、`MemberUserRemoteClient` 或会员用户 DTO 映射。
- 修复会员用户启动装配、密码编码、手机号唯一性、登录记录、积分/等级相关问题。

不要使用本 skill：
- 仅修改 member level、point、tag、group、address、signin 的独立聚合；应使用对应 skill。
- 需要改变 Controller 路径、HTTP 方法、VO/DTO 字段、权限码、错误码或 RPC 路径；这必须单独写迁移计划。
- 需要一次性删除旧 `MemberUserService`；当前它仍承载 app/auth/SMS/微信/MQ 行为。

## Baseline Failure Findings

RED 基线验证发现旧 skill 会诱导以下失败：
- 只列聚合/值对象，未锚定 Controller、VO、API、DO、Mapper、旧 Service、错误码和测试路径。
- 未说明 `MemberUserServiceImpl` 仍保留 `SmsCodeApi`、`SocialClientApi`、Spring Security `PasswordEncoder`、`MemberUserProducer.afterCommit` 行为。
- 未说明领域 `PasswordEncoder` 与 Spring Security `PasswordEncoder` 是两个不同接口，容易误删或重复装配。
- 未列出 `MemberUserDO` 的 `name/sex/birthday/areaId/mark/createTime/tenantId` 等外部字段，容易在 DDD 映射中丢失。
- 未定义事务、MQ afterCommit、手机号唯一性、错误码参数和分页返显契约。

## Reproducibility Contract

1. 先读取本 skill 的 Current Source Anchors，并确认路径和测试现状仍存在，再改代码。
2. skill 与当前代码冲突时，以当前可编译代码的外部行为为准，先修 skill，再改代码。
3. DDD 重构不得改变 Controller/API/CommonApi/Feign/RPC 契约、VO/DTO 字段、权限码、错误码、租户、分页语义。
4. 领域层不得 import Spring、MyBatis、Controller VO、Mapper、MQ、RPC、Servlet 工具。
5. 旧 Service 层是迁移来源和兼容层，不得在未覆盖 app/auth/SMS/微信/MQ 行为前删除。

## Current Source Anchors

### Controllers

- Admin user: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/user/MemberUserController.java`
  - `PUT /member/user/update` permission `member:user:update`
  - `PUT /member/user/update-level` permission `member:user:update-level`
  - `PUT /member/user/update-point` permission `member:user:update-point`
  - `GET /member/user/get` permission `member:user:query`
  - `GET /member/user/page` permission `member:user:query`
- App user: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/app/user/AppMemberUserController.java`
- App auth: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/app/auth/AppAuthController.java`

### API, Remote, DTO, Message

- Stable contract: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/MemberUserApi.java`
  - `GET ${ApiConstants.PREFIX}/user/get` → `CommonResult<MemberUserRespDTO>`
  - `GET ${ApiConstants.PREFIX}/user/list` → `CommonResult<List<MemberUserRespDTO>>`
  - `GET ${ApiConstants.PREFIX}/user/list-by-nickname` → `CommonResult<List<MemberUserRespDTO>>`
  - `GET ${ApiConstants.PREFIX}/user/get-by-mobile` → `CommonResult<MemberUserRespDTO>`
  - `GET ${ApiConstants.PREFIX}/user/valid` → `CommonResult<Boolean>`
  - default `getUserMap(Collection<Long> ids)` maps `getUserList(ids).getCheckedData()` by id.
- Remote adapter: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/remote/MemberUserRemoteClient.java`
  - Feign identity is only here: `@FeignClient(name = ApiConstants.NAME, contextId = "memberUserRemoteClient")`.
- Server implementation: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/user/MemberUserApiImpl.java`
  - currently delegates to legacy `MemberUserService` and converts DO → DTO via `MemberUserConvert.convert2`.
  - `validateUser(Long id)` currently throws `USER_MOBILE_NOT_EXISTS` when id is absent; document this current behavior even though the name is surprising.
- RPC DTO: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/dto/MemberUserRespDTO.java`
- Create message: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/message/user/MemberUserCreateMessage.java`

### VO

- Admin VOs: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/user/vo/`
  - `MemberUserBaseVO.java`, `MemberUserPageReqVO.java`, `MemberUserRespVO.java`, `MemberUserUpdateReqVO.java`, `MemberUserUpdateLevelReqVO.java`, `MemberUserUpdatePointReqVO.java`
- App user VOs: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/app/user/vo/`
- App auth VOs: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/app/auth/`

### DO, Mapper, Convert

- DO: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/dal/dataobject/user/MemberUserDO.java`
- Mapper: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/dal/mysql/user/MemberUserMapper.java`
- Convert: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/convert/user/MemberUserConvert.java`
- Auth convert: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/convert/auth/AuthConvert.java`

### Service and Application

- Legacy service interface: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/service/user/MemberUserService.java`
- Legacy service implementation: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/service/user/MemberUserServiceImpl.java`
- Auth service: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/service/auth/MemberAuthServiceImpl.java`
- DDD application service: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/application/user/MemberUserApplicationService.java`

### Domain

- Aggregate: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/user/MemberUser.java`
- Factory: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/user/MemberUserFactory.java`
- Repository interface: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/user/repository/MemberUserRepository.java`
- Domain service interface: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/user/service/PasswordEncoder.java`
- Value objects: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/user/valueobject/`
  - `Mobile.java`, `Nickname.java`, `RawPassword.java`, `EncodedPassword.java`, `UserStatus.java`, `LoginRecord.java`
- Events: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/user/event/`
  - `MemberUserCreatedEvent.java`, `MemberUserDeletedEvent.java`, `MemberUserPasswordChangedEvent.java`

### Infrastructure

- Repository implementation: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/infrastructure/user/MemberUserRepositoryImpl.java`
- Password adapter: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/infrastructure/user/BCryptPasswordEncoderAdapter.java`
- Factory configuration: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/infrastructure/user/MemberUserFactoryConfiguration.java`
- MQ producer: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/mq/producer/user/MemberUserProducer.java`

### Error Codes and Tests

- Error codes: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/enums/ErrorCodeConstants.java`
- Current tests found: `develop-module-member/develop-module-member-server/src/test/java/com/develop/mvp/pk/module/member/`
- No dedicated `MemberUser*Test` currently found; add tests before high-risk behavior migration.

## Fixed Data Model

### `MemberUserDO` table model

| Field | Type | Meaning | Contract |
|---|---|---|---|
| `id` | `Long` | 用户 ID | Primary key; maps to domain `id` and all API/VO id fields |
| `mobile` | `String` | 手机号 | Unique by `uk_mobile`; can be null for non-mobile create paths |
| `password` | `String` | BCrypt 密码 | Never expose to VO/DTO; maps to `EncodedPassword` |
| `status` | `Integer` | 账号状态 | `CommonStatusEnum`; maps to `UserStatus` |
| `registerIp` | `String` | 注册 IP | Preserve for admin/app display and audit |
| `registerTerminal` | `Integer` | 注册终端 | `TerminalEnum`; preserve on create |
| `loginIp` | `String` | 最后登录 IP | Updated by login record flow |
| `loginDate` | `LocalDateTime` | 最后登录时间 | Updated with current time |
| `nickname` | `String` | 昵称 | Blank create path defaults to `用户` + 6 digits |
| `avatar` | `String` | 头像 | Exposed in admin/app/API DTOs |
| `name` | `String` | 真实姓名 | Existing external field; do not drop in admin update/page behavior |
| `sex` | `Integer` | 性别 | Exposed in app info/admin base VO |
| `birthday` | `LocalDateTime` | 出生日期 | Existing external field; preserve mapping if touched |
| `areaId` | `Integer` | 地区 | Admin VO maps area name via `AddressConvert` |
| `mark` | `String` | 备注 | Existing admin field; preserve if touched |
| `point` | `Integer` | 积分 | Negative update must not overdraw |
| `tagIds` | `List<Long>` | 标签 ID | `LongListTypeHandler`; admin page returns tag names |
| `levelId` | `Long` | 等级 ID | `0L` represents no level in old update path |
| `experience` | `Integer` | 经验 | Preserved with level update |
| `groupId` | `Long` | 分组 ID | Admin page returns group name |
| `tenantId` | inherited | 租户 | From `TenantBaseDO`; do not remove tenant isolation |
| `createTime` | inherited | 创建时间 | From base DO metadata, not declared directly in `MemberUserDO`; exposed in `MemberUserRespDTO` and admin response |

### API/VO field contracts

- `MemberUserRespDTO`: `id`, `nickname`, `status`, `avatar`, `mobile`, `createTime`, `levelId`, `point`.
- `MemberUserBaseVO`: `mobile`, `status`, `nickname`, `avatar`, `name`, `sex`, `areaId`, `areaName`, `birthday`, `mark`, `tagIds`, `levelId`, `groupId`.
- `MemberUserPageReqVO`: `mobile`, `nickname`, `loginDate`, `createTime`, `tagIds`, `levelId`, `groupId`, plus inherited `pageNo/pageSize`.
- `MemberUserRespVO`: includes base VO fields plus `id`, `registerIp`, `loginIp`, `loginDate`, `createTime`, `point`, `totalPoint`, `tagNames`, `levelName`, `groupName`, `experience`.
- `AppMemberUserInfoRespVO`: `id`, `nickname`, `avatar`, `mobile`, `sex`, `point`, `experience`, `level`, `brokerageEnabled`.

### Controller behavior contracts

- Admin `PUT /member/user/update` currently calls `MemberUserApplicationService.updateProfile(id, nickname, avatar)` only. This means current DDD-backed admin update does not preserve all `MemberUserUpdateReqVO` base fields; treat this as a current conflict before further migration.
- Admin `GET /member/user/get` currently calls `MemberUserApplicationService.get(id)` and throws `USER_NOT_EXISTS` through application service when missing, despite the controller containing a null-success branch. Document and test this before changing not-found semantics.
- Admin `GET /member/user/page` currently passes `null` for status/loginDate/createTime to application service, so current DDD-backed paging does not apply login/create date filters even though VO exposes them.
- App `/member/user/*` endpoints still use legacy `MemberUserService`; do not migrate them without preserving SMS, Weixin, password and DO-to-VO behavior.
- App `/member/auth/*` endpoints use `MemberAuthService` and external `SocialClientApi`; MemberUser migration must not break these auth flows.

## Required Method Signatures

### Aggregate

```java
static MemberUser create(Nickname nickname, Mobile mobile, EncodedPassword password)
public static MemberUser reconstitute(Long id, Nickname nickname, Mobile mobile, EncodedPassword password,
                                      UserStatus status, String email, String avatar, Long tenantId,
                                      String loginIp, LocalDateTime loginDate, String registerIp, Integer registerTerminal,
                                      Long levelId, Integer experience, Integer point, Long groupId, List<Long> tagIds)
public MemberUser recordRegisterInfo(String registerIp, Integer terminal)
public void recordLogin(String loginIp)
public void changePassword(EncodedPassword newPassword)
public void updateProfile(Nickname nickname, String avatar)
public void updateMobile(Mobile mobile)
public void updateLevel(Long levelId, Integer experience)
public boolean addPoint(Integer delta)
```

### Factory and password ports

```java
public MemberUserFactory(PasswordEncoder passwordEncoder)
public MemberUser create(Nickname nickname, Mobile mobile, RawPassword rawPassword)
public MemberUser createQuick(String mobile, String registerIp, Integer terminal)
```

- `domain.user.service.PasswordEncoder` is a domain port.
- `infrastructure.user.BCryptPasswordEncoderAdapter` adapts it using Spring Security BCrypt.
- `MemberUserFactoryConfiguration` wires `MemberUserFactory` as a Spring bean; do not annotate `MemberUserFactory` with Spring stereotypes.

### Repository

```java
MemberUser save(MemberUser u)
void delete(Long id)
MemberUser findById(Long id)
Optional<MemberUser> findByMobile(Mobile mobile)
List<MemberUser> findByIds(Collection<Long> ids)
List<MemberUser> findByNicknameLike(String keyword)
PageResult<MemberUser> findPage(String nickname, Mobile mobile, Integer status, Long levelId, Long groupId,
                                List<Long> tagIds, String loginDateStart, String loginDateEnd,
                                String createTimeStart, String createTimeEnd, Integer pageNo, Integer pageSize)
int updatePointIncr(Long id, Integer incrCount)
int updatePointDecr(Long id, Integer decrCount)
long count()
long countByGroupId(Long groupId)
long countByLevelId(Long levelId)
long countByTagId(Long tagId)
```

### Application service

Keep these public use cases available while migrating callers; change signatures only after verifying current callers and updating this skill first:

```java
@Transactional public MemberUser createIfAbsent(String mobile, String registerIp, Integer terminal)
@Transactional public MemberUser createUser(String nickname, String avatar, String registerIp, Integer terminal)
public MemberUser get(Long id)
public MemberUser getByMobile(String mobile)
public List<MemberUser> getList(Collection<Long> ids)
public List<MemberUser> getListByNickname(String nickname)
public PageResult<MemberUser> getPage(...)
@Transactional public void updateProfile(Long userId, String nickname, String avatar)
@Transactional public void updateMobile(Long userId, String newMobile)
@Transactional public void updatePassword(Long userId, String encodedPassword)
@Transactional public void updateStatus(Long userId, Integer status)
@Transactional public void updateLevel(Long id, Long levelId, Integer experience)
@Transactional public boolean updatePoint(Long userId, Integer point)
@Transactional public void recordLogin(Long id, String loginIp)
@Transactional public void delete(Long id)
```

## Business Rules

| ID | Rule | Layer | Verification |
|---|---|---|---|
| BR01 | 创建用户默认启用，积分和经验默认为 0 | Domain/Factory | Create test or mapper assertion |
| BR02 | 昵称为空时生成 `用户` + 6 位数字 | Factory/legacy service | Existing create behavior preserved |
| BR03 | 手机号变更前必须校验唯一性 | Application/legacy service | `USER_MOBILE_USED` with mobile parameter |
| BR04 | `createUserIfAbsent` 手机已存在时返回已有用户，不重复创建 | Application/legacy service | Repository/mapper count unchanged |
| BR05 | 密码存储必须是 BCrypt 后的密文，不暴露明文 | Factory/infrastructure/legacy service | Password encoder adapter/service behavior |
| BR06 | 更新密码、重置密码、更新手机的短信验证码场景不能丢 | Legacy service/auth migration | `SmsCodeApi.useSmsCode(...).checkError()` preserved |
| BR07 | 微信手机号更新必须保留 `SocialClientApi.getWxMaPhoneNumberInfo` | Legacy service migration | External social call preserved |
| BR08 | 创建用户 MQ 消息必须 afterCommit 发送 | Legacy service/application migration | `TransactionSynchronization.afterCommit` or equivalent |
| BR09 | 扣减积分目标语义是不允许扣成负数，但当前 mapper 缺少余额 guard | Repository/application | 先修 mapper 条件并测试，再用 `USER_POINT_NOT_ENOUGH` 表达失败 |
| BR10 | 等级清空的旧语义使用 `0L` 防止 updateById 过滤 | Legacy compatibility | Preserve before behavior migration |
| BR11 | Admin page must return tag/level/group names | Controller/convert | `convertPageFromDomain` equivalent preserved |
| BR12 | Login record updates `loginIp` and current `loginDate` | Domain/application | Login flow assertion |

## Error Code Contract

Use `develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/enums/ErrorCodeConstants.java` exactly.

| Scenario | Error code | Parameter contract | Throwing layer |
|---|---|---|---|
| User id not found | `USER_NOT_EXISTS` | none | Application or legacy service validation |
| Mobile not registered | `USER_MOBILE_NOT_EXISTS` | none | Legacy reset/find-by-mobile flows |
| Mobile already used | `USER_MOBILE_USED` | `mobile` as first parameter | Application/legacy uniqueness validation |
| Point not enough | `USER_POINT_NOT_ENOUGH` | none | Point flow if converting boolean failure to exception |
| Bad login credentials | `AUTH_LOGIN_BAD_CREDENTIALS` | none | Auth service |
| Disabled user login | `AUTH_LOGIN_USER_DISABLED` | none | Auth service |
| Social user not found | `AUTH_SOCIAL_USER_NOT_FOUND` | none | Auth/social flow |
| Auth mobile used | `AUTH_MOBILE_USED` | none | Auth/social/mobile flow |

Do not rename constants, change numeric codes, change messages, or reorder error parameters.

## Current Conflict Notes

### C01 — Admin update currently narrows legacy update semantics

Legacy `MemberUserServiceImpl#updateUser(MemberUserUpdateReqVO)` validates existence, validates mobile uniqueness, and maps the full admin update VO to `MemberUserDO`. Current `MemberUserController#updateUser` calls `MemberUserApplicationService.updateProfile(id, nickname, avatar)` only, so `mobile/status/name/sex/areaId/birthday/mark/tagIds/levelId/groupId` are not updated through the current DDD-backed path. Do not treat this as completed migration.

### C02 — Application createUser uses UUID as mobile placeholder

Legacy `MemberUserServiceImpl#createUser(String nickname, String avatar, String registerIp, Integer terminal)` passes `mobile = null` for third-party create paths. Current `MemberUserApplicationService#createUser` creates a UUID and stores it as mobile. This changes the meaning of `mobile`, can affect unique indexes and get-by-mobile behavior, and must be fixed or explicitly accepted before using the DDD path for production auth/social creation.

### C03 — Current DDD create flows do not publish create MQ after commit

Legacy create flows register `TransactionSynchronization.afterCommit()` and call `MemberUserProducer.sendUserCreateMessage(user.getId())`. Current `MemberUserApplicationService#createIfAbsent` and `createUser` save users but do not publish the create message. Do not switch create callers to DDD application service until this is restored with after-commit semantics.

### C04 — Repository mapping loses persisted/admin/app fields

Current `MemberUserRepositoryImpl.toDO()` and `fromDO()` do not map `name`, `sex`, `birthday`, `areaId`, `mark`, `createTime`, and pass `email = null`. Saving a partially reconstituted domain can overwrite or drop fields not modeled in the aggregate. Use partial updates or full field reconstitution before routing update/save flows through this repository.

### C05 — DDD paging ignores date filters

`MemberUserRepositoryImpl.findPage(...)` accepts login/create date strings but does not apply them to the wrapper. The current admin controller also passes null for those filters. Legacy mapper `selectPage(MemberUserPageReqVO)` applies `loginDate` and `createTime` ranges. Preserve legacy date filtering before claiming DDD page parity.

### C06 — Point decrement behavior differs from stated invariant

`MemberUserMapper.updatePointDecr` currently increments by a negative number without a `point >= abs(delta)` guard. The skill must not claim current code prevents overdraw unless the repository/application adds the guard and tests it. If converting boolean failure to `USER_POINT_NOT_ENOUGH`, first align mapper behavior.

### C07 — API validateUser error code is surprising but current behavior

`MemberUserApiImpl#validateUser(Long id)` throws `USER_MOBILE_NOT_EXISTS` when `id` does not exist. This should not be silently changed during DDD/API cleanup because callers may rely on current error shape.

## Transaction Contract

| Use case | Transaction requirement | Notes |
|---|---|---|
| `createUserIfAbsent` | `@Transactional(rollbackFor = Exception.class)` or equivalent | Existing user returns without insert |
| `createUser` | transactional | Create message must publish after commit |
| `updateUserMobile` | transactional | SMS verification and DB update stay atomic enough for current behavior |
| `updateUserPassword` / `resetUserPassword` | preserve current behavior | SMS use and password update must not be split unsafely |
| `updateUser` admin | transactional in legacy service | Preserve validation before update |
| `updatePoint` | transactional where orchestrating records + balance | Negative balance result must be handled |
| `recordLogin` | write operation | Must update IP and time together |
| Repository `save/delete` | repository currently annotated transactional | Do not rely on domain object mutation alone |

## Integration Contract

- MQ: `MemberUserProducer.sendUserCreateMessage(user.getId())` must be emitted after DB commit for create flows.
- SMS: `SmsCodeApi` verification must remain for update mobile, update password, reset password, send/validate flows.
- Social/Weixin: `SocialClientApi` phone lookup and auth paths must remain intact.
- API implementation: `MemberUserApiImpl` currently delegates to old `MemberUserService` and `MemberUserConvert.convert2`; do not switch it to domain objects until `MemberUserRespDTO.createTime` and null/error behavior are preserved.
- API validation: `MemberUserApiImpl#validateUser` currently throws `USER_MOBILE_NOT_EXISTS` for missing id. Preserve or migrate deliberately with caller review.
- Auth: `MemberAuthServiceImpl` callers may still depend on old `MemberUserService` behavior; migrate only with full coverage.
- Create semantics: legacy third-party create uses `mobile = null`; current application create uses UUID-as-mobile. Do not switch auth/social create callers to the application path until this conflict is resolved.
- Tenant: `MemberUserDO` extends `TenantBaseDO`; repository and mapper changes must preserve tenant plugin behavior.
- Security: admin Controller `@PreAuthorize` values must not change.
- API local/remote: business consumers inject `MemberUserApi`; Feign identity stays only on `remote/MemberUserRemoteClient`.
- Pagination: admin page must preserve filtering and enrichment semantics; if adding date filters, verify they are actually applied.

## Mapping Rules

- Domain may depend on value objects only, never VO/DTO/DO/Mapper.
- Infrastructure maps `MemberUserDO ↔ MemberUser` and must not silently discard persisted fields when saving.
- Current `MemberUserRepositoryImpl.toDO()` maps only DDD-owned fields; if a use case updates partial profile fields, avoid overwriting unrelated DO fields such as `name`, `sex`, `birthday`, `areaId`, `mark`, `tenantId`, `createTime`.
- Convert maps external response fields. Do not replace `MemberUserConvert.convertPage(...)` with a domain-only mapping unless tag/level/group enrichment and admin/app fields are preserved.
- `MemberUserRespDTO.createTime` comes from DO/base metadata; domain currently lacks `createTime`, so API DTO conversion must preserve it through DO or an explicit domain field before switching.

## Quick Reference

| Task | Correct place | Forbidden place |
|---|---|---|
| Enforce mobile format | `Mobile` value object | Controller string checks repeated everywhere |
| Encode raw password | `MemberUserFactory` via domain `PasswordEncoder` port | Domain using Spring Security directly |
| Wire factory bean | `infrastructure/user/MemberUserFactoryConfiguration.java` | `@Component` on domain factory |
| Use MyBatis mapper | `infrastructure/user` or legacy `service/user` during migration | Domain aggregate/repository interface |
| Call SMS/Social APIs | Application/legacy service orchestration | Domain aggregate |
| Publish user create MQ | Application/legacy orchestration after commit | Domain aggregate directly calling producer |
| Convert VO/DTO | `convert/user/MemberUserConvert.java` | Domain aggregate |

## Acceptance Criteria

- AC01: `MemberUser` has no Spring/MyBatis annotations or infrastructure imports.
- AC02: Value objects validate their own invariants and remain immutable.
- AC03: `MemberUserRepository` remains in domain and imports no MyBatis/DO classes.
- AC04: `MemberUserRepositoryImpl` remains in infrastructure and is the only DDD repository implementation using `MemberUserMapper`.
- AC05: `MemberUserFactory` stays a plain domain class; Spring bean wiring stays in infrastructure configuration.
- AC06: Existing Controller paths, HTTP methods, permissions, VO fields, API DTO fields and RPC paths are unchanged.
- AC07: `MemberUserServiceImpl` app/auth/SMS/微信/MQ behavior is either preserved or replaced with tested equivalent before deletion.
- AC08: `USER_NOT_EXISTS`, `USER_MOBILE_NOT_EXISTS`, `USER_MOBILE_USED`, auth error codes and parameter contracts are unchanged.
- AC09: User create MQ publication remains after commit.
- AC10: Admin update and page behavior match legacy fields and filters before DDD paths replace legacy paths.
- AC11: Third-party create semantics do not introduce UUID-as-mobile unless explicitly approved and tested.
- AC12: Member module compiles and any added MemberUser tests pass.

## Verification Commands

Run focused checks after modifying this area:

```bash
mvn compile -pl develop-module-member/develop-module-member-api -am -DskipTests
mvn compile -pl develop-module-member/develop-module-member-server -am -DskipTests
```

If tests are added or touched:

```bash
mvn test -pl develop-module-member/develop-module-member-server -Dtest='*MemberUser*Test'
mvn test -pl develop-module-member/develop-module-member-server
```

If API local/remote contract changed or consumers are affected, also compile likely consumers:

```bash
mvn compile -pl develop-module-mall/develop-module-trade-server,develop-module-mall/develop-module-product-server -am -DskipTests
```

## Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| Deleting old `MemberUserServiceImpl` too early | App/auth/SMS/微信/MQ flows break | Keep as compatibility layer until each behavior is migrated and tested |
| Treating the two `PasswordEncoder` interfaces as one | Bean collision or raw password persistence | Keep domain port and Spring Security adapter separate |
| Domain directly imports `MemberUserDO` or VO | DDD boundary regression | Move mapping to infrastructure/convert |
| Replacing admin page mapping with simple domain mapping | Tag/level/group names disappear | Preserve enrichment logic |
| Saving partial domain object over full DO | `name/sex/birthday/areaId/mark/createTime/tenantId` lost | Use partial update or full reconstitution before save |
| Publishing MQ before commit | Consumers see missing/rolled-back user | Use `afterCommit` equivalent |
| Changing `MemberUserApi` annotations | Local/remote contract regression | Keep Feign identity in remote adapter only |

## Rationalization Table

| Excuse | Reality |
|---|---|
| “旧 Service 已经有 DDD Application，可直接删” | 旧 Service 仍有 SMS、Social、MQ afterCommit 和 auth 兼容行为。 |
| “字段不在领域对象里就不重要” | VO/DTO/DO 外部契约仍要求 `sex/createTime/areaId/name/mark` 等字段。 |
| “编译通过就说明重构完成” | 编译不能证明错误码、权限、MQ afterCommit、短信/微信行为没变。 |
| “密码编码只是技术细节” | 这是安全边界；domain port 与 Spring adapter 必须清晰隔离。 |
| “分页先能查出来就行” | Admin page 还必须返回标签、等级、分组名称。 |

## Red Flags

看到以下情况立即停止当前批次：
- 准备改 Controller 路径、HTTP 方法、VO/DTO 字段、权限码或 API/RPC 路径。
- 准备删除 `MemberUserServiceImpl`、`MemberAuthServiceImpl` 或 `MemberUserProducer` 调用，但没有等价测试。
- Domain 层出现 Spring、MyBatis、Mapper、DO、VO、DTO、MQ、RPC、Servlet import。
- `MemberUserRepositoryImpl.save()` 可能用不完整 domain 覆盖 DO 中未建模字段。
- `MemberUserRespDTO.createTime` 或 admin/app VO 既有字段无法说明来源。
- `USER_MOBILE_USED` 不再携带 mobile 参数。
- 用户创建消息不再 afterCommit。

## Rollback Conditions

必须回滚或继续补强，如果出现：
1. `develop-module-member-api` 或 `develop-module-member-server` 编译失败。
2. 任一 Controller/API 外部契约发生未批准变化。
3. 登录、短信改绑、微信改绑、密码更新、重置密码、创建用户 MQ 行为缺失。
4. 租户字段、创建时间、admin/app 展示字段在映射中丢失。
5. 新实现无法解释每个错误码场景和事务边界。

## AI Self-Check

完成前逐项确认：
- 我是否先读了 Current Source Anchors 中的相关文件？
- 我是否保留了 Controller/API/VO/DTO/权限/错误码外部契约？
- 我是否没有让 domain 依赖 Spring/MyBatis/VO/DTO/DO/Mapper/MQ/RPC？
- 我是否处理了旧 Service 中 SMS、Social、MQ afterCommit 行为？
- 我是否说明或保留了 `MemberUserDO` 中未进入当前 domain 的字段？
- 我是否运行了至少一个 member api/server compile 命令？
- 如果修改了行为，我是否新增或更新了对应 MemberUser 测试？
