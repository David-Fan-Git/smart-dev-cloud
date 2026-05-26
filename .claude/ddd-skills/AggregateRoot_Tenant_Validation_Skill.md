---
name: aggregate-root-tenant-validation-skill
description: Use when modifying or reviewing the system Tenant aggregate, tenant package collaboration, tenant creation, admin bootstrap, permission assignment, or DDD migration boundaries.
type: ddd-aggregate-skill
status: production-ready
---

# Aggregate Root Tenant Validation Skill

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

## 1. Overview

本 skill 是 **Tenant 聚合根 DDD 重构的可复现开发蓝图**。目标不是只描述目标架构，而是让另一个无上下文 AI 能基于本文件，在当前代码库中复现同一套 Tenant 领域模型、应用编排、基础设施映射与验收检查。

**核心原则：** 先验证当前代码，再修改 skill 或代码；当 skill 与代码冲突时，以当前外部行为和可编译代码为事实源，更新 skill 后再实现。

## Go / No-Go Gate

`status: production-ready` 表示本 skill 已可作为生产重构蓝图。当前 DDD Tenant create path 已通过 `TenantApplicationServiceTest#createTenantGeneratesIdAndBackfillsContactUser` 验证：创建请求 `id=null` 时由 `TenantRepository#create(...)` 先插入数据库生成租户 ID，再重建带 `TenantCreatedEvent` 的 Tenant 聚合，随后在租户上下文中创建管理员角色/用户、分配权限、回写 `contactUserId`。继续扩展 Tenant 聚合前仍必须以 legacy `TenantServiceImpl#createTenant` 的外部行为为对照基线，避免遗漏事务、权限、租户上下文和外部协作语义。

## 2. When to Use

使用场景：

- 修改 `develop-module-system` 中 Tenant 租户聚合、值对象、仓储、应用服务或基础设施实现。
- 校验 Tenant 相关 DDD 重构是否仍符合职责边界。
- 让另一个 AI 仅凭 skill 复现 Tenant 聚合重构结果。
- 当前代码、验收标准、业务规则或字段模型与本 skill 不一致。

不使用场景：

- 修改非 Tenant 聚合根，例如 User、Role、Menu、TenantPackage。
- 只改 Controller 文案、Swagger 注解或前端展示。
- 处理数据库脚本但不影响 Tenant 领域行为。

## 3. Reproducibility Contract

任何实现者必须先完成以下步骤：

1. 读取本文件。
2. 读取第 5 节列出的当前事实源文件。
3. 对照第 6-12 节确认字段、方法、异常、映射、边界。
4. 若代码与本 skill 冲突：先记录冲突，优先保持现有外部 API 行为，再修订 skill 或实现。
5. 修改代码前先写失败测试；无法补测试时，至少运行第 16 节命令并说明未覆盖项。

禁止：

- 只按“阶段 1-8”搭 DDD 骨架而不核对当前代码。
- 自行猜测字段名、错误码、事务边界、DTO 映射或分页查询条件。
- 把 User/Role/Permission/TenantPackage 的业务规则塞进 Tenant 聚合根。
- 为了通过编译改 Controller 对外接口语义。

## 4. Baseline Failure Findings

RED 阶段使用旧版 skill 进行压力测试，发现旧 skill 只能指导搭建结构，不能保证复现一致结果。

| 压力场景 | 旧 skill 失败点 | 必须补救 |
|---|---|---|
| 赶时间只按阶段写代码 | 会猜字段、异常、映射和编排细节 | 明确当前事实源、方法签名、字段模型 |
| 无项目上下文复现 | 无法确定 DTO/DO/Domain 映射、事务、错误码 | 补充字段表、错误码、命令、验收测试 |
| 代码与 skill 不一致 | 容易以文档覆盖现有行为 | 明确冲突处理：代码事实优先，skill 随实现更新 |

## 5. Current Source Anchors

路径均相对仓库根目录。

| 类型 | 文件 | 关键位置 |
|---|---|---|
| Controller | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/tenant/TenantController.java` | create/update/delete/page/export 方法约在 39-176 |
| Save VO | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/tenant/vo/tenant/TenantSaveReqVO.java` | 字段约在 21-62 |
| Page VO | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/tenant/vo/tenant/TenantPageReqVO.java` | 字段约在 21-34 |
| Resp VO | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/tenant/vo/tenant/TenantRespVO.java` | 字段约在 20-54 |
| Convert | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/convert/tenant/TenantConvert.java` | `convert02(TenantSaveReqVO)` 约在 18-24 |
| DO | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/dataobject/tenant/TenantDO.java` | 字段约在 30-89 |
| Mapper | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/dal/mysql/tenant/TenantMapper.java` | selectPage/selectByName/selectListByWebsite 等 |
| Legacy Service | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/tenant/TenantService.java` | 对外服务契约约在 28-143 |
| Legacy Impl | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/service/tenant/TenantServiceImpl.java` | 原始校验逻辑约在 83-314 |
| Aggregate | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/tenant/Tenant.java` | 领域行为约在 40-155 |
| Factory | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/tenant/TenantFactory.java` | create/reconstitute 约在 17-52 |
| Repository | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/tenant/repository/TenantRepository.java` | 接口约在 18-31 |
| Page Query | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/tenant/repository/TenantPageQuery.java` | record 字段约在 8-16 |
| Uniqueness | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/tenant/service/TenantUniquenessChecker.java` | 接口约在 11-17 |
| Infrastructure | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/tenant/TenantRepositoryImpl.java` | 映射约在 31-148 |
| Application | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/application/tenant/TenantApplicationService.java` | 编排约在 69-280 |
| Event Publisher Interface | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/user/event/DomainEventPublisher.java` | 领域事件发布接口 |
| Event Publisher Impl | `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/user/SpringDomainEventPublisher.java` | Spring 事件发布适配器 |
| Error Codes | `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/enums/ErrorCodeConstants.java` | Tenant 错误码约在 105-117 |
| Legacy Tests | `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/tenant/TenantServiceImplTest.java` | legacy 租户服务测试，含 create/update/delete/page/handleTenantMenu |
| Package Tests | `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/service/tenant/TenantPackageServiceImplTest.java` | 套餐与租户联动测试 |

## 6. Domain Intent and Boundaries

**聚合根：** `com.develop.mvp.pk.module.system.domain.tenant.Tenant`

**业务责任：** SaaS 租户生命周期中的领域状态与不变式，包括名称、状态、域名、套餐引用、过期时间、账号配额、系统租户保护、启用/禁用/删除标记。

**聚合内部：**

- `Tenant`
- `TenantId`
- `TenantName`
- `TenantStatus`
- `TenantPackageRef`
- `TenantExpireTime`
- Tenant 领域事件

**聚合外部，只能通过 ID 或应用层编排协作：**

- TenantPackage：套餐存在、启用、菜单集合由应用层调用 `TenantPackageService.validTenantPackage(packageId)` 校验。
- User/AdminUser：管理员用户创建由 `TenantApplicationService` 编排。
- Role/Menu/Permission：租户管理员角色创建、菜单权限分配由 `TenantApplicationService` 编排。
- Mapper/DO/PageReqVO：只允许基础设施层或转换层使用。

## 7. Fixed Data Model

### 7.1 TenantDO fields

`TenantDO.PACKAGE_ID_SYSTEM = 0L` 表示系统租户。

| 字段 | 类型 | 说明 | Domain 对应 |
|---|---|---|---|
| `id` | `Long` | 租户 ID | `TenantId.value()` |
| `name` | `String` | 租户名称 | `TenantName.value()` |
| `contactUserId` | `Long` | 管理员用户 ID | `Tenant.contactUserId()` |
| `contactName` | `String` | 联系人 | `Tenant.contactName()` |
| `contactMobile` | `String` | 联系手机号 | `Tenant.contactMobile()` |
| `status` | `Integer` | `0=ENABLE`, `1=DISABLE`；创建请求传入的状态必须保留，未传入时才默认 ENABLE | `TenantStatus.code()` |
| `websites` | `List<String>` | 域名列表 | `Tenant.websites()` |
| `packageId` | `Long` | 套餐 ID | `TenantPackageRef.packageId()` |
| `expireTime` | `LocalDateTime` | 过期时间 | `TenantExpireTime.value()` |
| `accountCount` | `Integer` | 账号配额 | `Tenant.accountCount()` |

### 7.2 TenantSaveReqVO fields

| 字段 | 创建 | 更新 | 说明 |
|---|---:|---:|---|
| `id` | 可空 | 必填 | 更新目标租户 |
| `name` | 必填 | 必填 | 租户名称 |
| `contactName` | 必填 | 必填 | 联系人 |
| `contactMobile` | 可空 | 可空 | 联系手机号 |
| `status` | 必填 | 必填 | 租户状态 |
| `websites` | 可空 | 可空 | 域名列表 |
| `packageId` | 必填 | 必填 | 套餐 ID |
| `expireTime` | 必填 | 必填 | 过期时间 |
| `accountCount` | 必填 | 必填 | 账号配额 |
| `username` | 必填 | 不使用 | 创建管理员用户名 |
| `password` | 必填 | 不使用 | 创建管理员密码 |

当前事实源中 `contactMobile` 在 VO 校验层非必填；若后续要改为必填，需走单独外部契约变更计划。

当前创建请求的 `id` 通常为空；legacy `TenantServiceImpl#createTenant` 依赖数据库自增后返回 `tenant.getId()`。DDD `TenantApplicationService#createTenant` 在 `id == null` 时必须调用 `TenantRepository#create(...)` 先插入数据库生成 ID，再返回由 `TenantFactory.create(...)` 构造的带创建事件聚合；不能在保存前调用 `TenantId.of(null)`。

### 7.3 TenantPageQuery fields

`TenantPageQuery(String name, String contactName, String contactMobile, Integer status, LocalDateTime[] createTime, Integer pageNo, Integer pageSize)` 必须与 `TenantPageReqVO` 保持字段一致。

## 8. Required Building Blocks

| 构造块 | 必须位置 | 说明 |
|---|---|---|
| Aggregate Root | `domain/tenant/Tenant.java` | 纯 Java 类，无 Spring/MyBatis 注解 |
| Factory | `domain/tenant/TenantFactory.java` | `create` 新建，`reconstitute` 从 DO 重建 |
| Value Objects | `domain/tenant/valueobject/*.java` | 不可变，无 setter，自校验 |
| Events | `domain/tenant/event/*.java` | Tenant 事件数据，不编排外部聚合 |
| Repository Interface | `domain/tenant/repository/TenantRepository.java` | 领域层接口，不 import MyBatis |
| Page Query | `domain/tenant/repository/TenantPageQuery.java` | 封装分页查询条件 |
| Domain Service Interface | `domain/tenant/service/TenantUniquenessChecker.java` | 跨聚合唯一性校验接口 |
| Application Service | `application/tenant/TenantApplicationService.java` | 事务、跨聚合编排、错误码转换 |
| Infrastructure | `infrastructure/tenant/*.java` | 调用 Mapper，DO ↔ Domain 映射 |

## 9. Required Method Signatures

### 9.1 TenantFactory

```java
public static Tenant create(Long id, String name, Long contactUserId, String contactName,
                            String contactMobile, List<String> websites,
                            Long packageId, LocalDateTime expireTime, Integer accountCount)

public static Tenant reconstitute(Long id, String name, Long contactUserId,
                                  String contactName, String contactMobile,
                                  Integer statusCode, List<String> websites,
                                  Long packageId, LocalDateTime expireTime,
                                  Integer accountCount)
```

`create` 在未显式传入状态时默认使用 `TenantStatus.ENABLED`；`TenantApplicationService#createTenant` 必须保留 `TenantSaveReqVO.status` 的外部行为，允许创建禁用租户但不得因此记录 `TenantDisabledEvent`。`reconstitute` 必须保留持久化状态。当前 `TenantId.of(id)` 要求非空，因此 create path 不能直接传入 Controller 创建请求中的空 ID，除非先引入与持久化自增兼容的安全生成/回填方案并补测试。

### 9.2 Tenant aggregate

| 方法 | 规则 |
|---|---|
| `disable()` | 状态改为禁用并记录 `TenantDisabledEvent`；重复禁用不应重复变更 |
| `enable()` | 状态改为启用 |
| `updateProfile(TenantName, String, String, List<String>, TenantUniquenessChecker)` | 更新名称、联系人、手机号、域名；名称/域名唯一性通过 checker |
| `changePackage(TenantPackageRef)` | 只更新套餐引用，不校验套餐存在性 |
| `updateExpiration(TenantExpireTime, Integer)` | 更新过期时间和账号配额 |
| `setContactUser(Long)` | 记录创建后的管理员用户 ID |
| `markDeleted()` | 系统租户禁止删除；非系统租户记录 `TenantDeletedEvent` |
| `validateActive()` | 禁用返回禁用错误语义，过期返回过期错误语义，有效返回 `null` |
| `isSystem()` | 基于 `TenantPackageRef.isSystem()` 判断 |
| `pullEvents()` | 返回并清空领域事件 |

### 9.3 TenantRepository

```java
Tenant create(String name, Long contactUserId, String contactName, String contactMobile,
              List<String> websites, Long packageId,
              LocalDateTime expireTime, Integer accountCount);
Tenant save(Tenant tenant);
void delete(TenantId id);
Tenant findById(TenantId id);
Optional<Tenant> findByName(TenantName name);
List<Tenant> findByWebsite(String website);
List<Tenant> findByPackageId(TenantPackageRef packageRef);
List<Tenant> findByStatus(TenantStatus status);
PageResult<Tenant> findPage(TenantPageQuery query);
long countByPackageId(TenantPackageRef packageRef);
List<Tenant> findByIds(Collection<TenantId> ids);
List<Tenant> findAll();
boolean existsByName(TenantName name);
```

不要把 `TenantPageReqVO` 暴露到该接口；它只能出现在基础设施适配中。

### 9.4 TenantUniquenessChecker

```java
boolean isNameUnique(TenantName name, TenantId excludeId);
boolean isWebsiteUnique(String website, TenantId excludeId);
```

`excludeId` 为 `null` 表示创建场景；非空表示更新场景，必须排除当前租户自身。

### 9.5 TenantApplicationService

外部行为必须保持：

- `createTenant(...)` 创建租户，校验名称/域名唯一，校验套餐；当 `id == null` 时通过 `TenantRepository#create(...)` 先插入并取得数据库生成 ID，当 `id != null` 时可用 `TenantFactory.create(...)` 构造后保存；随后创建租户管理员角色与用户，写回 `contactUserId`，发布 `TenantCreatedEvent`。
- `updateTenant(...)` 校验存在，禁止系统租户修改，校验唯一，校验套餐，更新领域对象；当 `packageId` 变化时调用 `updateTenantRoleMenu`。
- `deleteTenant(Long id)` 校验存在，禁止系统租户删除，调用 `markDeleted`，删除持久化数据，发布事件。
- `deleteTenantList(List<Long> ids)` 逐个调用 `deleteTenant`，保持逐项校验和事务语义。
- `getAndValidateTenant(Long id)` 不存在抛 `TENANT_NOT_EXISTS`，禁用抛 `TENANT_DISABLE`，过期抛 `TENANT_EXPIRE`。

## 10. Business Rules and Invariants

| 编号 | 规则 | 所属层 | 验证方式 |
|---|---|---|---|
| R01 | 创建租户默认 `TenantStatus.ENABLED` | Factory/Aggregate | 单元测试 `TenantFactory.create` |
| R02 | 租户名称全局唯一，更新时排除自身 | Application + Domain Service | 单元测试 checker 调用参数 |
| R03 | 每个域名全局唯一，更新时排除自身 | Application + Domain Service | 单元测试 checker 调用参数 |
| R04 | 系统租户 `packageId=0` 不可修改、删除 | Application + Aggregate 防御 | 单元测试 update/delete |
| R05 | 状态只能是 ENABLE/DISABLE | `TenantStatus` | 值对象测试 |
| R06 | 过期时间不能为空 | `TenantExpireTime` | 值对象测试 |
| R07 | 有效租户必须存在、启用、未过期 | Application + Aggregate | `getAndValidateTenant` 测试 |
| R08 | 禁用租户状态变为 DISABLE 并产生事件 | Aggregate | 单元测试 |
| R09 | 套餐变更时同步角色菜单权限 | Application | 应用服务测试或编译审查 |
| R10 | 套餐存在且启用由 `TenantPackageService.validTenantPackage` 校验 | Application | 应用服务测试 |
| R11 | 创建管理员角色、用户、权限分配属于应用编排 | Application | 代码审查 |

## 11. Error Code Contract

必须使用 `ErrorCodeConstants` 中现有 Tenant 错误码：

| 场景 | 错误码 |
|---|---|
| 租户不存在 | `TENANT_NOT_EXISTS` |
| 租户禁用 | `TENANT_DISABLE`，参数为租户名称 |
| 租户过期 | `TENANT_EXPIRE`，参数为租户名称 |
| 系统租户修改或删除 | `TENANT_CAN_NOT_UPDATE_SYSTEM` |
| 租户名称重复 | `TENANT_NAME_DUPLICATE`，参数为租户名称 |
| 租户域名重复 | `TENANT_WEBSITE_DUPLICATE`，参数为域名 |

聚合根内部不要直接依赖 `ErrorCodeConstants`；错误码转换在应用层完成。聚合内部可返回状态语义或抛领域异常，但对外应用服务必须映射为以上错误码。

## 12. Mapping Rules

`TenantRepositoryImpl.toDataObject(Tenant)` 必须逐字段映射：

| Domain | DO |
|---|---|
| `tenant.id().value()` | `id` |
| `tenant.name().value()` | `name` |
| `tenant.contactUserId()` | `contactUserId` |
| `tenant.contactName()` | `contactName` |
| `tenant.contactMobile()` | `contactMobile` |
| `tenant.status().code()` | `status` |
| `tenant.websites()` | `websites` |
| `tenant.packageRef().packageId()` | `packageId` |
| `tenant.expireTime().value()` | `expireTime` |
| `tenant.accountCount()` | `accountCount` |

`TenantRepositoryImpl.toDomain(TenantDO)` 必须调用 `TenantFactory.reconstitute(...)`，不能调用 `create(...)`，否则会丢失禁用状态。

分页查询允许在基础设施层把 `TenantPageQuery` 转成 `TenantPageReqVO`，但不得让领域接口依赖 Controller VO。

## 13. Transaction, Event, and Test Contracts

### 13.1 Transaction boundaries

`TenantApplicationService` 必须承担事务边界：

| 方法 | 事务要求 | 原因 |
|---|---|---|
| `createTenant(...)` | 必须 `@Transactional` | 创建租户、角色、用户、权限和回写 `contactUserId` 必须作为一个用例编排 |
| `updateTenant(...)` | 必须 `@Transactional` | 租户属性更新和套餐菜单同步必须一致 |
| `deleteTenant(Long id)` | 必须 `@Transactional` | 删除与事件发布前的领域状态变更必须一致 |
| `deleteTenantList(List<Long> ids)` | 必须 `@Transactional`，并逐个调用 `deleteTenant` | 保持逐项系统租户校验和删除语义 |

当前 legacy `TenantServiceImpl#createTenant`、`updateTenant`、`updateTenantRoleMenu` 使用 `@DSTransactional`，其中 create 还带 `@DataPermission(enable = false)`；DDD `TenantApplicationService` 当前使用 Spring `@Transactional`。迁移事务边界时必须确认多数据源和租户上下文切换语义没有回归，不能只把注解名视为等价。

查询方法不强制事务注解。

### 13.2 Event publishing contract

- `Tenant` 聚合只收集领域事件，不依赖 Spring 事件发布器。
- `TenantApplicationService.publishEvents(Tenant tenant)` 必须遍历 `tenant.pullEvents()` 并调用 `DomainEventPublisher.publish(event)`。
- `disable()` 产生 `TenantDisabledEvent`；`markDeleted()` 产生 `TenantDeletedEvent`。
- 当前 skill 不强制新增事件订阅者；如果新增订阅者，必须放在应用层或基础设施层，不得放入聚合根。
- 当前系统模块复用 `domain/user/event/DomainEventPublisher` 和 `infrastructure/user/SpringDomainEventPublisher`；若后续拆分 tenant 专属事件发布接口，必须避免重复 Bean 或事件发布路径不一致。

### 13.3 Minimal test path

新增 Tenant 行为测试时，优先放在：

```text
develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/domain/tenant/TenantTest.java
develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/application/tenant/TenantApplicationServiceTest.java
```

当前已有 legacy 测试位于 `TenantServiceImplTest` 和 `TenantPackageServiceImplTest`，覆盖 legacy create/update/delete/page/handleTenantMenu 以及套餐联动；当前没有专属 `domain/tenant/TenantTest.java` 或 `application/tenant/TenantApplicationServiceTest.java`。新增 DDD 行为时，先补 DDD 测试；对照 legacy 测试保护外部行为。

最小测试覆盖：

- DDD create path 处理创建请求 `id=null` 时不会触发 `TenantId` 空值异常，且返回数据库生成后的租户 ID、回写 `contactUserId`、发布 `TenantCreatedEvent`。
- `TenantFactory.create` 默认启用。
- `TenantFactory.reconstitute` 保留禁用状态。
- 系统租户 `markDeleted` 被拒绝。
- 更新唯一性校验传入 `excludeId`。
- `getAndValidateTenant` 对不存在、禁用、过期分别映射指定错误码。

## 14. One Excellent Example

更新租户套餐的最小正确流程：

```java
@Transactional
public void updateTenant(Long id, String name, String contactName, String contactMobile,
                         Integer status, List<String> websites, Long packageId,
                         LocalDateTime expireTime, Integer accountCount) {
    Tenant tenant = findExistingTenant(TenantId.of(id));
    assertNotSystemTenant(tenant);
    assertNameUnique(TenantName.of(name), tenant.id());
    for (String website : websites == null ? List.<String>of() : websites) {
        assertWebsiteUnique(website, tenant.id());
    }
    TenantPackageDO tenantPackage = tenantPackageService.validTenantPackage(packageId);

    tenant.updateProfile(TenantName.of(name), contactName, contactMobile, websites, uniquenessChecker);
    if (status != null && TenantStatus.of(status).isEnabled()) {
        tenant.enable();
    } else if (status != null) {
        tenant.disable();
    }
    tenant.updateExpiration(TenantExpireTime.of(expireTime), accountCount);

    if (!tenant.packageRef().packageId().equals(packageId)) {
        tenant.changePackage(TenantPackageRef.of(packageId));
        updateTenantRoleMenu(tenant.id().value(), tenantPackage.getMenuIds());
    }

    tenantRepository.save(tenant);
    publishEvents(tenant);
}
```

此示例固定了边界：唯一性、系统租户、套餐有效性、角色菜单同步、事件发布都在应用服务；Tenant 聚合只维护自身状态。

## 15. Acceptance Criteria

| 编号 | 验收标准 | 验证方式 |
|---|---|---|
| AC01 | `Tenant` 无 Spring/MyBatis 注解 | grep/代码审查 |
| AC02 | `Tenant` 不注入 Mapper、Repository 实现、Service | grep/代码审查 |
| AC03 | 值对象字段不可变，无 setter | grep/代码审查 |
| AC04 | `TenantFactory.create` 默认 ENABLED | 单元测试或代码审查 |
| AC05 | `TenantFactory.reconstitute` 保留持久化 status | 单元测试或代码审查 |
| AC06 | `TenantRepository` 位于领域层且不 import MyBatis/Controller VO | grep/代码审查 |
| AC07 | `TenantRepositoryImpl` 位于基础设施层并负责 DO ↔ Domain 映射 | 代码审查 |
| AC08 | 更新场景名称/域名唯一性排除当前租户 ID | 单元测试或代码审查 |
| AC09 | 创建租户的角色+用户创建在 `TenantApplicationService` | 代码审查 |
| AC10 | 套餐变更时调用 `updateTenantRoleMenu` | 单元测试或代码审查 |
| AC11 | `getAndValidateTenant` 使用指定错误码 | 单元测试或代码审查 |
| AC12 | 系统租户不可修改/删除 | 单元测试 |
| AC13 | `mvn compile -pl develop-module-system/develop-module-system-server -am` 通过 | Maven 编译 |
| AC14 | 如存在 Tenant 测试，`mvn test -pl develop-module-system/develop-module-system-server -Dtest=*Tenant*` 通过 | Maven 测试 |
| AC15 | DDD create path 解决 `id=null` 与 `TenantId` 非空约束冲突，并保持 legacy create 返回生成 ID、创建租户管理员角色/用户、分配权限、回写 `contactUserId` 的行为 | DDD 应用测试 + legacy 行为对照 |
| AC16 | 事务迁移明确处理 legacy `@DSTransactional` 与 `@DataPermission(enable = false)` 语义，不因切换到普通 `@Transactional` 破坏多数据源/租户上下文行为 | 代码审查 + 集成测试或明确风险记录 |
| AC17 | `TenantRepositoryImpl.findPage` 是迁移期边界债：当前在 infrastructure 内构造 Controller `TenantPageReqVO`，不得进一步泄露到 domain/repository 接口；后续若有机会应改为 Mapper 接收领域查询条件或专用 infra 查询对象 | grep/代码审查 |

## 16. Verification Commands

从仓库根目录运行；第 15 节验收标准中的编译/测试命令以本节为准：

```bash
mvn compile -pl develop-module-system/develop-module-system-server -am
```

如有 Tenant 相关测试，运行：

```bash
mvn test -pl develop-module-system/develop-module-system-server -Dtest=*Tenant*
```

文档-only 修改至少运行：

```bash
grep -E "^(type: ddd-aggregate-skill|module: system-tenant|status: production-ready|last_verified: 2026-05-24|## Go / No-Go Gate|\| AC15 \|)" .claude/ddd-skills/AggregateRoot_Tenant_Validation_Skill.md
grep -n "id=null\|TenantId\|@DSTransactional\|DataPermission\|TenantServiceImplTest\|TenantRepositoryImpl.findPage" .claude/ddd-skills/AggregateRoot_Tenant_Validation_Skill.md
git diff -- .claude/ddd-skills/AggregateRoot_Tenant_Validation_Skill.md
```

若新增或修改测试，必须先看到测试失败，再实现，再看到测试通过。

## 17. Quick Reference

| 想做什么 | 正确位置 | 不要放在 |
|---|---|---|
| 判断系统租户 | `Tenant.isSystem()` / `TenantApplicationService.assertNotSystemTenant` | Controller |
| 校验套餐存在启用 | `TenantApplicationService` 调 `TenantPackageService` | Tenant 聚合根 |
| 唯一性查询 | `TenantUniquenessChecker` 接口和实现 | Tenant 直接查 Mapper |
| DO 转 Domain | `TenantRepositoryImpl.toDomain` | Controller/Service |
| Domain 转 DO | `TenantRepositoryImpl.toDataObject` | Tenant 聚合根 |
| 创建管理员角色用户 | `TenantApplicationService` | Tenant 聚合根 |
| 错误码转换 | `TenantApplicationService` | 值对象/聚合根 |
| 分页 VO 适配 | `TenantRepositoryImpl.findPage` | `TenantRepository` 接口 |

## 18. Common Mistakes

| 错误 | 后果 | 修正 |
|---|---|---|
| 使用 `TenantFactory.create` 重建数据库对象 | 禁用租户被错误变成启用 | 重建必须用 `reconstitute` |
| `TenantRepository` 暴露 `TenantPageReqVO` | 领域层依赖 Controller | 使用 `TenantPageQuery` |
| 聚合根调用 `TenantPackageService` | 跨聚合规则泄漏 | 应用层校验套餐 |
| 更新唯一性不传 `excludeId` | 自己与自己冲突 | 更新时传 `tenant.id()` |
| 在聚合根抛 ServiceException | 领域层依赖框架错误码 | 应用层映射错误码 |
| 只检查 `website` 单值 | 漏掉 `websites` 列表 | 遍历每个域名 |
| 系统租户只禁止删除不禁止更新 | 违反 R04 | update/delete 都调用 `assertNotSystemTenant` |
| 忽略创建请求 `id=null` | DDD create path 在 `TenantId.of(id)` 处失败 | 先设计并测试 ID 生成/回填，再启用 create path |
| 把 `@Transactional` 和 `@DSTransactional` 当作完全等价 | 多数据源和租户上下文编排可能回归 | 对照 legacy 事务注解和测试语义验证 |

## 19. Rationalization Table

| 合理化 | 现实 |
|---|---|
| “先按目录结构把类建出来，字段后面再补。” | 字段和映射是复现核心；先补会导致编译和行为偏差。 |
| “skill 写了 `website`，代码里是 `websites`，我选一个就行。” | 必须以当前代码 `List<String> websites` 为准，并更新 skill。 |
| “应用服务可以先绕过角色用户创建，后面再接。” | 创建租户的对外行为包含管理员角色和用户编排，不能省略。 |
| “聚合根直接抛错误码更方便。” | 领域层不能依赖框架错误码；应用层负责转换。 |
| “当前没有测试，跑编译就够了。” | 新增行为必须先写失败测试；无法测试时需明确说明未覆盖风险。 |

## 20. Red Flags

看到以下情况必须停止并重新核对本 skill：

- 准备新增 Tenant 字段但未更新 DO/VO/Domain/映射表。
- 准备让 `Tenant` import Spring、MyBatis、Mapper、Service、Controller VO。
- 准备让 `TenantRepository` import `TenantPageReqVO` 或 Mapper 类型。
- 准备修改 Controller 对外接口以适配领域模型。
- 准备在未修复 `id=null` 与 `TenantId` 非空冲突前宣称 DDD create path 完成。
- 准备忽略 `TenantPackageService.validTenantPackage(packageId)`。
- 准备在更新唯一性校验中不传当前租户 ID。
- 准备只运行全局编译但不验证 Tenant 行为变化。

## 21. Rollback Conditions

出现以下任一情况，回滚当前 Tenant 修改并重新分析：

1. 系统模块编译失败。
2. Tenant 聚合根引入基础设施依赖。
3. 值对象出现 setter 或可变公开字段。
4. 创建租户时管理员角色/用户编排丢失。
5. 创建租户请求 `id=null` 未被安全处理，或不能返回生成后的租户 ID。
6. 系统租户可被修改或删除。
7. 禁用/过期租户校验错误码回归。
8. DO ↔ Domain 映射丢字段或错误使用 `create` 重建。

## 22. AI Self-Check

完成后逐项确认：

- [ ] 已读取第 5 节所有相关事实源文件。
- [ ] 已确认 `TenantDO`、VO、Domain、Repository 映射字段一致。
- [ ] 已确认 `TenantFactory.create` 与 `reconstitute` 语义不同。
- [ ] 已确认 DDD create path 的 `id=null` 风险已记录；若改 Java，已用测试验证生成 ID 和 contactUserId 回写。
- [ ] 已确认系统租户 update/delete 均被禁止。
- [ ] 已确认创建租户仍创建管理员角色和用户。
- [ ] 已确认套餐变更仍同步角色菜单权限。
- [ ] 已确认名称/域名唯一性更新时排除自身 ID。
- [ ] 代码/refactor 修改已运行第 16 节编译命令；文档-only 修改已运行第 16 节文档检查命令。
- [ ] 新增或修改行为时已遵守测试先行。
