---
name: aggregate-root-member-level-skill
description: Use when modifying or reviewing the Member Level aggregate, admin/app level APIs, member level config, or level DDD migration boundaries.
type: ddd-aggregate-skill
status: production-review
---

# DDD Skill: MemberLevel Aggregate

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

`MemberLevel` 表示会员等级配置与会员经验变更后的等级计算边界。本 skill 只覆盖会员等级聚合，不覆盖会员分组、会员标签、积分记录、签到配置或签到记录；这些聚合必须拆成独立 skill 后再生产重构。

## Go / No-Go Gate

`status: production-ready` 表示本 skill 可作为 MemberLevel 重构蓝图，不表示当前 Java DDD 实现已经完全等价于 legacy service。当前 Admin/App Controller 已承接 `MemberLevelApplicationService`；RPC `MemberLevelApiImpl` 仍通过 legacy `MemberLevelService` 处理 get/add/reduce。DDD 路径仍存在迁移期风险：`addExperience` 会在用户不存在时直接 return，legacy 路径会继续访问用户对象；`calculateNewLevel` 返回当前等级时也会插入等级记录，而 legacy `calculateNewLevel(MemberUserDO, int)` 在新旧 `levelId` 相同时返回 null；`MemberLevelConvert` 从领域对象转响应时未回填 `createTime`，简单列表未回填 `icon`；`MemberLevelRepositoryImpl.findAll` 没有显式按 level 升序，`findByNameLike` 仍是 `List.of()` stub。修 Java 前必须分别验证 Controller DDD 路径与 RPC legacy 路径，不能假设 RPC 已迁移到 DDD application service。

## When to Use

- 修改会员等级配置、经验增减、等级升降级计算、等级变更记录时。
- 将 `MemberLevelServiceImpl` 中等级相关业务规则迁移到 DDD 层时。
- 校验 `member` API local/remote 契约中 `MemberLevelApi`、`MemberLevelRemoteClient`、`MemberLevelApiImpl` 是否保持稳定时。
- 审查 `MemberLevel` 当前 DDD 实现与 legacy service 行为差异时。

## When Not to Use

- 不用于 `MemberGroup`、`MemberTag`、`MemberPointRecord`、`MemberSignInConfig`、`MemberSignInRecord` 的重构。
- 不用于修改 Controller 外部路径、权限、VO/DTO 字段、错误码、Feign contextId。
- 不用于把所有 member 子域一次性重构；每次只处理一个聚合或一个小协作边界。

## Reproducibility Contract

1. 先读本 skill，再读 Current Source Anchors。
2. 若本 skill 与当前代码冲突，以当前可编译代码的外部行为和 legacy 测试/服务为事实源。
3. 先修订本 skill，再改 Java。
4. 保持 HTTP/RPC 契约、权限、错误码、参数顺序、分页和响应字段不变。
5. 领域层禁止依赖 Controller VO、Mapper、DO、Spring、MyBatis。
6. 迁移期允许 infrastructure/application 适配旧 DO/Mapper/Service，但必须把债务写入 Red Flags 或 Acceptance Criteria。

## Baseline Failure Findings

| 压力场景 | 旧 skill 暴露的问题 | 本 skill 的约束 |
|---|---|---|
| 赶时间直接按文档改代码 | 一个 `AggregateRoot_MemberLevel_Skill.md` 同时覆盖 level/group/tag/point/signin 六个复杂聚合 | 本 skill 只覆盖 MemberLevel，其它聚合只作为外部协作引用 |
| 无上下文 AI 复现 | 缺少完整 Controller/VO/API/DO/Mapper/Service/Application/Repository/测试路径 | Current Source Anchors 列出必须读取的事实源 |
| 生产 API 不能破坏 | 旧 skill 未固定 `/member/level`、RPC `/level`、权限、Feign contextId | Production API Contract 固定这些外部契约 |
| 代码与 skill 不一致 | 旧 skill 没说明 DDD 当前实现与 legacy 行为差异 | Go / No-Go Gate 和 Current Migration Debts 明确差异先处理 |

## Current Source Anchors

### API module

| 事实源 | 路径 |
|---|---|
| RPC stable contract | `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java` |
| RPC response DTO | `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/dto/MemberLevelRespDTO.java` |
| Feign remote client | `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java` |
| Error codes | `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/enums/ErrorCodeConstants.java` |
| Experience biz enum | `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/enums/MemberExperienceBizTypeEnum.java` |

### Server entry and contract adapters

| 事实源 | 路径 |
|---|---|
| RPC implementation | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApiImpl.java` |
| Admin controller | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/level/MemberLevelController.java` |
| App controller | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/app/level/AppMemberLevelController.java` |
| Admin user controller level update entry | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/user/MemberUserController.java` |
| Admin base VO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/level/vo/level/MemberLevelBaseVO.java` |
| Admin create VO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/level/vo/level/MemberLevelCreateReqVO.java` |
| Admin update VO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/level/vo/level/MemberLevelUpdateReqVO.java` |
| Admin list VO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/level/vo/level/MemberLevelListReqVO.java` |
| Admin response VO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/level/vo/level/MemberLevelRespVO.java` |
| Admin simple response VO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/level/vo/level/MemberLevelSimpleRespVO.java` |
| App response VO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/app/level/vo/level/AppMemberLevelRespVO.java` |
| Admin update user level VO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/controller/admin/user/vo/MemberUserUpdateLevelReqVO.java` |

### Current DDD and persistence sources

| 事实源 | 路径 |
|---|---|
| Application service | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/application/level/MemberLevelApplicationService.java` |
| Aggregate root | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/level/MemberLevel.java` |
| Domain repository interface | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/level/repository/MemberLevelRepository.java` |
| Repository implementation | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/infrastructure/level/MemberLevelRepositoryImpl.java` |
| Convert | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/convert/level/MemberLevelConvert.java` |
| Level DO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/dal/dataobject/level/MemberLevelDO.java` |
| Level mapper | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/dal/mysql/level/MemberLevelMapper.java` |
| Level record DO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/dal/dataobject/level/MemberLevelRecordDO.java` |
| Level record mapper | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/dal/mysql/level/MemberLevelRecordMapper.java` |
| Experience record DO | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/dal/dataobject/level/MemberExperienceRecordDO.java` |
| Experience record mapper | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/dal/mysql/level/MemberExperienceRecordMapper.java` |
| MemberUser aggregate | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/user/MemberUser.java` |
| MemberUser repository | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/user/repository/MemberUserRepository.java` |

### Legacy behavior baseline

| 事实源 | 路径 |
|---|---|
| Legacy service interface | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/service/level/MemberLevelService.java` |
| Legacy service implementation | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/service/level/MemberLevelServiceImpl.java` |
| Legacy level record service | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/service/level/MemberLevelRecordService.java` |
| Legacy level record service impl | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/service/level/MemberLevelRecordServiceImpl.java` |
| Legacy experience record service | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/service/level/MemberExperienceRecordService.java` |
| Legacy experience record service impl | `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/service/level/MemberExperienceRecordServiceImpl.java` |
| Current domain test | `develop-module-member/develop-module-member-server/src/test/java/com/develop/mvp/pk/module/member/domain/level/MemberLevelTest.java` |

## Production API Contract

### Admin HTTP API

| Method | Path | Method name | Permission | Request/Response |
|---|---|---|---|---|
| POST | `/member/level/create` | `createLevel` | `member:level:create` | `MemberLevelCreateReqVO` → `CommonResult<Long>` |
| PUT | `/member/level/update` | `updateLevel` | `member:level:update` | `MemberLevelUpdateReqVO` → `CommonResult<Boolean>` |
| DELETE | `/member/level/delete?id=` | `deleteLevel` | `member:level:delete` | `Long id` → `CommonResult<Boolean>` |
| GET | `/member/level/get?id=` | `getLevel` | `member:level:query` | `Long id` → `CommonResult<MemberLevelRespVO>` |
| GET | `/member/level/list-all-simple` | `getSimpleLevelList` | no permission in current code | `CommonResult<List<MemberLevelSimpleRespVO>>` |
| GET | `/member/level/list` | `getLevelList` | `member:level:query` | `MemberLevelListReqVO` → `CommonResult<List<MemberLevelRespVO>>` |

### Admin user-level API

| Method | Path | Method name | Permission | Request/Response |
|---|---|---|---|---|
| PUT | `/member/user/update-level` | `updateUserLevel` | `member:user:update-level` | `MemberUserUpdateLevelReqVO` → `CommonResult<Boolean>` |

`MemberUserUpdateLevelReqVO.id` is required, `reason` is required and non-blank, and `levelId` may be null when the admin cancels a user level.

### App HTTP API

| Method | Path | Annotation | Response |
|---|---|---|---|
| GET | `/member/level/list` | `@PermitAll` | `CommonResult<List<AppMemberLevelRespVO>>` |

### RPC API

`MemberLevelApi.PREFIX = ApiConstants.PREFIX + "/level"` must stay stable.

| Method | Path | Parameters | Response |
|---|---|---|---|
| GET | `${PREFIX}/get` | `id: Long` | `CommonResult<MemberLevelRespDTO>` |
| POST | `${PREFIX}/add` | `userId: Long`, `experience: Integer`, `bizType: Integer`, `bizId: String` | `CommonResult<Boolean>` |
| POST | `${PREFIX}/reduce` | same as add | `CommonResult<Boolean>` |

`MemberLevelRemoteClient` must remain `@FeignClient(name = ApiConstants.NAME, contextId = "memberLevelRemoteClient")` and extend `MemberLevelApi`. Server-side `MemberLevelApiImpl` implements `MemberLevelApi`; it must not become a Feign client or depend on the remote client.

## Fixed Data Model

### MemberLevel fields

| Field | DO type | Domain type | API DTO | Admin VO | App VO | Nullable/default | Mapping rule |
|---|---|---|---|---|---|---|---|
| `id` | `Long` | `Long` | yes | resp only | no | null before insert | DB-generated id returned after save |
| `name` | `String` | `String` | yes | required | yes | non-blank in VO | Level display name |
| `level` | `Integer` | `Integer` | yes | required positive | yes | positive | Numeric level, unique globally |
| `experience` | `Integer` | `Integer` | yes | required positive | yes | positive | Required upgrade experience |
| `discountPercent` | `Integer` | `Integer` | yes | required 0-100 | yes | 0-100 | Member discount percent |
| `icon` | `String` | `String` | no | optional URL | yes | nullable | Simple list VO also has `icon`; current domain conversion omits it |
| `backgroundUrl` | `String` | `String` | no | optional URL | yes | nullable | App display background |
| `status` | `Integer` | `Integer` | yes | required `CommonStatusEnum` | no | ENABLE/DISABLE | Use `CommonStatusEnum.ENABLE.getStatus()` / `DISABLE.getStatus()` |
| `createTime` | `BaseDO` | absent currently | no | resp only | no | generated by BaseDO | Current domain conversion cannot fill it unless domain/query model carries it |

### MemberLevelRecord fields

| Field | Meaning | Source |
|---|---|---|
| `userId` | changed user | update/add experience use case |
| `levelId` | new level id, nullable when admin cancels level | `MemberLevel.id()` or null |
| `level` | redundant numeric level | copied from `MemberLevel.level()` |
| `discountPercent` | redundant discount | copied from `MemberLevel.discountPercent()` |
| `experience` | delta experience for this level change | admin adjustment or experience delta |
| `userExperience` | user total experience after change | calculated by application service |
| `remark` | admin reason | `MemberUserUpdateLevelReqVO.reason` |
| `description` | display description | admin adjustment/cancel text |

### MemberExperienceRecord fields

| Field | Meaning | Source |
|---|---|---|
| `userId` | user id | use case input |
| `bizType` | enum type | `MemberExperienceBizTypeEnum.getType()` |
| `bizId` | external business id | use case input |
| `title` | display title | `MemberExperienceBizTypeEnum.getTitle()` |
| `description` | formatted description | `StrUtil.format(bizType.getDescription(), experience)` |
| `experience` | delta experience | normalized delta |
| `totalExperience` | total after change | calculated non-negative total |

## Method Signatures

### Aggregate root

Current minimum signatures in `domain/level/MemberLevel.java`:

```java
public final class MemberLevel {
    public static MemberLevel create(String name);
    public static MemberLevel reconstitute(Long id, String name, Integer level, Integer experience,
                                           Integer discountPercent, String icon, String backgroundUrl, Integer status);
    public void updateConfig(String name, Integer level, Integer experience, Integer discountPercent,
                             String icon, String backgroundUrl, Integer status);
    public void enable();
    public void disable();
    public Long id();
    public String name();
    public Integer level();
    public Integer experience();
    public Integer discountPercent();
    public String icon();
    public String backgroundUrl();
    public Integer status();
}
```

Future value-object extraction is allowed only if Controller/API/DTO/DO contracts stay stable and conversion/tests are updated in the same batch.

### Repository interface

```java
public interface MemberLevelRepository {
    MemberLevel save(MemberLevel level);
    void delete(Long id);
    MemberLevel findById(Long id);
    List<MemberLevel> findByIds(Collection<Long> ids);
    List<MemberLevel> findByNameLike(String name);
    List<MemberLevel> findByStatus(Integer status);
    List<MemberLevel> findAll();
}
```

`findByNameLike` must not remain a stub if any production path begins to call it. Current `MemberLevelApplicationService.getList` filters `repo.findAll()` in memory, so the stub is not currently on the hot path.

### Application service

```java
@Transactional
Long createLevel(String name, Integer level, Integer experience, Integer discountPercent,
                 String icon, String backgroundUrl, Integer status);

@Transactional
void updateLevel(Long id, String name, Integer level, Integer experience, Integer discountPercent,
                 String icon, String backgroundUrl, Integer status);

@Transactional
void deleteLevel(Long id);

MemberLevel get(Long id);
List<MemberLevel> getList(Collection<Long> ids);
List<MemberLevel> getList(String name, Integer status);
List<MemberLevel> getListByStatus(Integer status);
List<MemberLevel> getEnableList();

@Transactional
void updateUserLevel(Long userId, Long newLevelId, String reason);

@Transactional
void addExperience(Long userId, Integer experience, MemberExperienceBizTypeEnum bizType, String bizId);
```

## Business Rules

| Rule | Layer | Current baseline | Error/behavior |
|---|---|---|---|
| ML-R01 | VO | `name` must be non-blank | Bean Validation message `等级名称不能为空` |
| ML-R02 | VO | `experience` must be non-null and positive | `升级经验不能为空` / `升级经验必须大于 0` |
| ML-R03 | VO | `level` must be non-null and positive | `等级不能为空` / `等级必须大于 0` |
| ML-R04 | VO | `discountPercent` must be non-null and 0-100 | `享受折扣不能为空` / range 0-100 |
| ML-R05 | VO | `icon` and `backgroundUrl` must be URL when present | Bean Validation `@URL` |
| ML-R06 | VO | `status` must be `CommonStatusEnum` | `@InEnum(CommonStatusEnum.class)` |
| ML-R07 | Application | name must be globally unique except current id on update | `LEVEL_NAME_EXISTS(name)` |
| ML-R08 | Application | numeric `level` must be globally unique except current id on update | `LEVEL_VALUE_EXISTS(level, name)` |
| ML-R09 | Application | experience must be greater than all lower levels and less than all higher levels | `LEVEL_EXPERIENCE_MIN(prevName, prevExp)` / `LEVEL_EXPERIENCE_MAX(nextName, nextExp)` |
| ML-R10 | Application | cannot delete a level referenced by users | `LEVEL_HAS_USER` using `MemberUserRepository.countByLevelId(id)` |
| ML-R11 | Application | add/reduce experience with zero delta is no-op | return without record/update |
| ML-R12 | Application | non-add biz type with positive experience is normalized to negative | `if (!bizType.isAdd() && experience > 0) experience = -experience` |
| ML-R13 | Application | total user experience cannot go below 0 | `NumberUtil.max(current + delta, 0)` |
| ML-R14 | Application | new level is highest enabled level whose required experience is <= total experience | sort by max numeric `level` |
| ML-R15 | Legacy parity | if calculated new level equals current `user.levelId`, no level-change record should be created | legacy returns null; current DDD must be reviewed/fixed before claiming parity |
| ML-R16 | RPC adapter | unsupported `bizType` rejects before application service | `EXPERIENCE_BIZ_NOT_SUPPORT` in `MemberLevelApiImpl.addExperience` |
| ML-R17 | RPC adapter | reduce delegates to add with negative experience | `reduceExperience` calls `addExperience(userId, -experience, bizType, bizId)` |
| ML-R18 | VO | admin user-level update requires non-null user id and non-blank reason; `levelId` may be null to cancel level | Bean Validation in `MemberUserUpdateLevelReqVO` |
| ML-R19 | Migration gate | add/reduce experience user-not-found behavior must be decided before parity claim | preserve legacy, switch to `USER_NOT_EXISTS`, or approve no-op with compatibility note |

## Error Code Contract

All codes are in `develop-module-member-api/.../ErrorCodeConstants.java`.

| Scenario | Error code | Message | Parameters | Throwing layer |
|---|---|---|---|---|
| Level not found | `LEVEL_NOT_EXISTS` `1_004_011_000` | `用户等级不存在` | none | application validation |
| Duplicate name | `LEVEL_NAME_EXISTS` `1_004_011_001` | `用户等级名称[{}]已被使用` | `name` | application validation |
| Duplicate level value | `LEVEL_VALUE_EXISTS` `1_004_011_002` | `用户等级值[{}]已被[{}]使用` | `level`, `name` | application validation |
| Experience not greater than previous | `LEVEL_EXPERIENCE_MIN` `1_004_011_003` | `升级经验必须大于上一个等级[{}]设置的升级经验[{}]` | previous level name, previous experience | application validation |
| Experience not less than next | `LEVEL_EXPERIENCE_MAX` `1_004_011_004` | `升级经验必须小于下一个等级[{}]设置的升级经验[{}]` | next level name, next experience | application validation |
| Level has users | `LEVEL_HAS_USER` `1_004_011_005` | `用户等级下存在用户，无法删除` | none | application validation |
| Unsupported experience biz type | `EXPERIENCE_BIZ_NOT_SUPPORT` `1_004_011_201` | `用户经验业务类型不支持` | none | RPC adapter |
| User not found for admin level update | `USER_NOT_EXISTS` `1_004_001_000` | `用户不存在` | none | application validation |

Do not change numeric codes, messages, or parameter order during DDD refactor. Before claiming parity, choose one approved behavior for add/reduce experience when `userId` does not exist: preserve legacy external behavior and add regression coverage; change to explicit `USER_NOT_EXISTS` with migration approval; or keep current DDD no-op only with approved compatibility note and RPC/consumer impact review.

## Transaction Contract

| Use case | Current transaction | Must cover |
|---|---|---|
| `createLevel` | `@Transactional` | uniqueness/range validation and insert |
| `updateLevel` | `@Transactional` | existence validation, uniqueness/range validation, update |
| `deleteLevel` | `@Transactional` | existence validation, user reference check, delete |
| `updateUserLevel` | `@Transactional` | user load, no-op same level, level record insert, experience record insert, user level/experience update, notification call point |
| `addExperience` | `@Transactional` | delta normalization, user load, experience record insert, optional level record insert, user level/experience update, notification call point |
| read/list methods | none required | must not mutate state |

Current DDD application service directly uses `MemberExperienceRecordMapper` and `MemberLevelRecordMapper`. This is a migration debt: future cleanup may introduce record repositories or services, but must preserve the same transaction boundary and record contents.

## Integration Contract

| Integration | Required behavior |
|---|---|
| Feign/RPC | Stable `MemberLevelApi` contract plus `remote/MemberLevelRemoteClient`; consumers inject stable API where possible |
| MemberUser | Cross-aggregate update through `MemberUserRepository`; level deletion uses `countByLevelId` |
| Level record | Insert when admin changes level or experience calculation changes level; do not insert when level stays unchanged |
| Experience record | Insert for non-zero experience changes with enum title/description/biz id/type |
| Notification | Current implementation has `notifyLevelChange` TODO; do not remove call point or silently add external side effects without plan |
| Tenant/data permission | No explicit tenant/data-permission annotations in current MemberLevel controller/service; do not add/remove such behavior casually |
| Cache/MQ/Job/Excel | No required MemberLevel-specific cache/MQ/job/Excel behavior in current anchors |
| Ordering | All MemberLevel list/read-for-validation queries that affect external list order or error parameter selection must use deterministic `level` ascending ordering unless a migration note approves otherwise |

## Mapping Rules

| Mapping | Correct location | Notes |
|---|---|---|
| Create/Update VO → application args | Controller | Controller may pass scalar fields, not VO into domain |
| DO ↔ Domain | `MemberLevelRepositoryImpl` or `MemberLevelConvert` if explicitly chosen | Domain must not import DO/Mapper |
| Domain → Admin `MemberLevelRespVO` | `MemberLevelConvert` | Current gap: `createTime` absent from domain mapping |
| Domain → `MemberLevelSimpleRespVO` | `MemberLevelConvert` | Current gap: `icon` should be preserved because VO exposes it |
| Domain → App `AppMemberLevelRespVO` | `MemberLevelConvert` | Preserve fields: name, level, experience, discountPercent, icon, backgroundUrl |
| DO → RPC `MemberLevelRespDTO` | `MemberLevelConvert.INSTANCE.convert02(...)` in current `MemberLevelApiImpl` legacy service path | If moving RPC to domain path, preserve id/name/level/experience/discountPercent/status |
| Level/experience record mapping | Application service currently constructs DO directly | Future repository extraction must preserve all fields and transaction boundary |

## Current Migration Debts

| Debt | Current fact | Required handling before Java refactor claims completion |
|---|---|---|
| Multi-aggregate draft | Old skill covered level/group/tag/point/signin | Keep this skill MemberLevel-only; create separate skills for others |
| RPC legacy path | `MemberLevelApiImpl` injects `MemberLevelService`, not `MemberLevelApplicationService` | Migration to DDD requires fixed get/add/reduce tests, error-code checks, record-write checks, and DTO mapping checks |
| Same-level experience record | Current DDD `calculateNewLevel(int)` returns current level; legacy suppresses record when matched level id equals user level id | Add test and fix or explicitly approve behavior change |
| Missing `createTime` mapping | Domain `MemberLevel` does not carry createTime; `MemberLevelRespVO` exposes it | Preserve response field via query model, DO mapping, or documented contract decision |
| Missing simple `icon` mapping | `convertSimpleListFromDomain` sets id/name only | Set icon or document why simple list changed |
| `findByNameLike` stub | RepositoryImpl returns `List.of()` | Implement if used; otherwise keep off production paths and mark debt |
| Direct mapper use in application | Record mappers injected into application service | Accept as migration debt or extract record repositories in focused batch |
| User-not-found divergence | Current DDD `addExperience` returns when user missing | Compare with legacy behavior and tests before claiming parity |
| Unordered `findAll` | `MemberLevelRepositoryImpl.findAll` uses `mapper.selectList()` without explicit `orderByAsc(level)`, while legacy list paths order by level asc | Implement ordered repository query or document and test approved behavior |
| In-memory filtering | `getList(name,status)` filters `repo.findAll()` | Preserve result ordering/performance expectations or move filtering to repository |

## Acceptance Criteria

| AC | Criterion | Verification |
|---|---|---|
| AC01 | `AggregateRoot_MemberLevel_Skill.md` has production frontmatter and covers only MemberLevel | grep + review |
| AC02 | Current Source Anchors include API, remote client, controllers, VOs, DOs, mapper, convert, domain, repository, application, legacy service, records, tests | review |
| AC03 | Controller paths, HTTP methods, permissions, `@PermitAll`, RPC paths, Feign contextId remain unchanged | grep + API review |
| AC04 | `MemberLevel` domain remains free of Spring/MyBatis/DO/Mapper/VO imports | grep |
| AC05 | Repository interface remains in `domain/level/repository` and infrastructure implementation owns Mapper/DO access | grep + review |
| AC06 | Create/update preserve name uniqueness, level uniqueness, and experience range errors with parameter order | unit/integration tests |
| AC07 | Delete preserves user-reference guard through `countByLevelId` and throws `LEVEL_HAS_USER` | unit/integration tests |
| AC08 | Add/reduce experience preserve zero no-op, delta normalization, non-negative total experience, experience record creation | unit/integration tests |
| AC09 | Level-change record is not created when calculated level equals current user level unless an approved external-contract change exists | regression test |
| AC10 | `createTime` and `icon` response mapping gaps are fixed or explicitly documented before claiming Java parity | controller/RPC response test |
| AC11 | `MemberLevelRepositoryImpl.findByNameLike` is not used in production while stubbed, or is implemented with mapper query | grep/test |
| AC12 | Member API module and member server compile | Maven compile |
| AC13 | Existing `MemberLevelTest` and required application/RPC/controller mapping tests pass; if tests do not exist, they must be added before claiming Java parity | Maven test |
| AC14 | Add/reduce experience user-not-found behavior is fixed by regression test and documented contract decision | focused application/RPC test |
| AC15 | Admin list and enabled list preserve legacy order by numeric level ascending | controller/application test |
| AC16 | Experience range validation error parameter selection is deterministic and matches legacy expectation | focused validation test |

## Verification Commands

```bash
# Documentation checks
grep -E "^(name: aggregate-root-member-level|status: production-ready|last_verified: 2026-05-24|## Go / No-Go Gate|## Current Migration Debts)" .claude/ddd-skills/AggregateRoot_MemberLevel_Skill.md
grep -n "MemberGroup\|MemberTag\|MemberPointRecord\|MemberSignIn" .claude/ddd-skills/AggregateRoot_MemberLevel_Skill.md
grep -n "same level\|createTime\|findByNameLike\|memberLevelRemoteClient\|EXPERIENCE_BIZ_NOT_SUPPORT" .claude/ddd-skills/AggregateRoot_MemberLevel_Skill.md

# Architecture checks
grep -R "import .*\(springframework\|mybatis\|dal\.dataobject\|dal\.mysql\|controller\).*" develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/domain/level || true
grep -R "findByNameLike" develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member

# Compile checks
mvn compile -pl develop-module-member/develop-module-member-api -am -DskipTests
mvn compile -pl develop-module-member/develop-module-member-server -am -DskipTests

# Existing focused test
mvn test -pl develop-module-member/develop-module-member-server -Dtest=MemberLevelTest

# Required focused tests before Java parity can be claimed; add these classes if absent
mvn test -pl develop-module-member/develop-module-member-server -Dtest=MemberLevelApplicationServiceTest,MemberLevelApiImplTest,MemberLevelControllerMappingTest
```

Required new focused tests before Java parity can be claimed:

- `MemberLevelApplicationServiceTest#createLevel_duplicateName_throwsLevelNameExists`
- `MemberLevelApplicationServiceTest#updateLevel_invalidExperienceBelowPrevious_throwsLevelExperienceMin`
- `MemberLevelApplicationServiceTest#deleteLevel_withUsers_throwsLevelHasUser`
- `MemberLevelApplicationServiceTest#addExperience_zeroDelta_noRecordsNoUserUpdate`
- `MemberLevelApplicationServiceTest#addExperience_sameLevel_noLevelRecord`
- `MemberLevelApplicationServiceTest#addExperience_reduceCannotGoBelowZero`
- `MemberLevelApplicationServiceTest#addExperience_userNotFound_matchesDocumentedContract`
- `MemberLevelApplicationServiceTest#updateUserLevel_cancelLevel_writesLevelAndExperienceRecord`
- `MemberLevelApplicationServiceTest#list_ordersByNumericLevelAscending`
- `MemberLevelApplicationServiceTest#experienceRangeErrorParameters_areDeterministic`
- `MemberLevelApiImplTest#addExperience_unsupportedBizType_throwsExperienceBizNotSupport`
- `MemberLevelControllerMappingTest#simpleList_preservesIcon`

If future work changes experience/level behavior, these tests must be updated with the approved contract instead of weakened or skipped.

## Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 固定外部 API | Controller/API section | domain/infrastructure |
| 校验 VO 字段格式 | VO Bean Validation | domain repository |
| 校验名称/等级/经验范围 | Application/domain service | Controller |
| 访问 `member_level` 表 | `MemberLevelRepositoryImpl` / Mapper | domain aggregate |
| 更新用户等级/经验 | Application service via `MemberUserRepository` | `MemberLevel` aggregate directly操作其它聚合 |
| 插入等级/经验记录 | Application orchestration or dedicated record repository | Controller |
| 计算新等级 | domain service/application with enabled level list | Mapper XML/Controller |
| Feign remote identity | `remote/MemberLevelRemoteClient` | stable `MemberLevelApi` implementation |

## Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| 继续让本 skill 覆盖 group/tag/point/signin | 重构范围失控，验收无法闭环 | 拆独立 skill |
| 为了 DDD 改 Controller path 或 VO 字段 | 前端/RPC 调用回归 | 保持契约，另写迁移计划 |
| 忽略 same-level 记录差异 | 经验变更多插等级记录，用户消息/审计异常 | 加 regression test 后修复或审批变更 |
| 忽略 `createTime`/`icon` 映射 | 响应字段回归 | 修改 mapping/query model 并测试 |
| 在 domain 中 import DO/Mapper | 领域层污染 | 移到 infrastructure/application adapter |
| 把 `MemberLevelRemoteClient` 当本地实现注入 | monolith/local mode 装配破坏 | 本地实现实现 stable API，remote 只做 Feign adapter |
| 只跑 compile 不跑行为测试 | 错误码/记录/经验计算回归漏掉 | 增加 focused tests |

## Rationalization Table

| Excuse | Reality |
|---|---|
| “旧 skill 已经写了所有会员域，一起改更快” | 这是生产标准红旗；复杂聚合必须拆分 |
| “当前 DDD 代码能编译，所以行为就是对的” | 当前已有 legacy 差异，必须用测试证明 |
| “createTime/icon 只是小字段” | 它们是外部响应契约字段，丢失就是回归 |
| “findByNameLike 没人用，不用管” | 可以不修，但必须确保生产路径不调用并记录债务 |
| “应用层直接用 mapper 也能工作” | 迁移期可接受，但不能把它说成最终 DDD 形态 |
| “错误码文案差不多即可” | 前端和调用方可能依赖错误码和参数顺序，必须保持 |

## Red Flags

出现以下任一情况，停止本批次并先修 skill 或测试：

- 修改范围扩散到 MemberGroup/MemberTag/Point/SignIn 的核心业务。
- Controller 路径、权限、VO、RPC 参数或 Feign contextId 发生变化。
- `domain/level` 引入 Spring、MyBatis、DO、Mapper、Controller VO。
- `addExperience` 没有覆盖 same-level、zero、negative、unsupported biz type 行为测试。
- 等级列表排序从按 `level` 升序变成不稳定顺序。
- 错误码、错误参数顺序与 `ErrorCodeConstants` 不一致。
- 只凭手动阅读宣称 legacy parity。

## Rollback Conditions

- `develop-module-member-api` 或 `develop-module-member-server` 编译失败。
- 会员等级 CRUD、列表、RPC add/reduce、app list 任一外部契约回归。
- 等级/经验记录少写、多写或字段值与 legacy 行为不一致且无批准迁移说明。
- 删除等级绕过用户引用校验。
- 领域层依赖基础设施或表示层。
- 修复一个债务时引入跨聚合大范围改动。

## AI Self-Check

完成任何 MemberLevel Java 重构前逐项确认：

- [ ] 我只处理 MemberLevel，没有顺手重构 group/tag/point/signin。
- [ ] 我已读取 Current Source Anchors 中与本次修改有关的文件。
- [ ] 我没有改变 Controller/API/Feign 外部契约。
- [ ] 我保留了错误码和参数顺序。
- [ ] 我明确处理了 same-level、zero experience、negative delta、unsupported biz type。
- [ ] 我确认 `createTime`、`icon` 映射没有回归。
- [ ] 我运行了 member API/server compile 或说明了未运行原因。
- [ ] 我补充或运行了能证明行为的 focused tests。
