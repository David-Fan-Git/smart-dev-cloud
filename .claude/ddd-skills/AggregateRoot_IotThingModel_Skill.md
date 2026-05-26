---
name: aggregate-root-iot-thingmodel-skill
description: Use when creating, auditing, or refactoring the IoT thing model aggregate in develop-module-iot-server.
---

# IoT ThingModel Aggregate Skill

## Overview

IoT ThingModel 聚合负责产品物模型功能定义，包括属性、事件、服务三类功能的标识、名称、JSON 结构、缓存、发布态保护和 Modbus 点位冗余字段同步，复现目标是在保持现有 TSL 聚合响应和错误码契约不变的前提下迁移到标准 DDD/六边形结构。

## When to Use

- 修改物模型创建、更新、删除、列表、分页、TSL 查询或缓存逻辑时使用。
- 将 `IotThingModelServiceImpl` 迁移到 `domain/thingmodel`、`application/thingmodel`、`infrastructure/thingmodel` 时使用。
- Product 发布态对物模型操作限制、系统保留 identifier、Modbus 冗余字段同步受影响时使用。
- 创建 IoT ThingModel `CommonApi`、DTO、local/remote 适配时使用。

## When Not to Use

- 只处理 Product 生命周期、Device 实例或 Device Message 时不用本 skill。
- 只修改 Modbus 点位业务且不改变物模型字段同步契约时不用本 skill。
- 需要新增物模型版本表或改变 TSL 外部响应格式时，先写单独设计和迁移计划。

## Reproducibility Contract

1. 先读取 Current Source Anchors，不得凭 IoT 平台通用概念猜测当前物模型行为。
2. 当前代码以 `productId` 为主要关联，`productKey` 是冗余字段；不得反向改成只按 `productKey` 关联。
3. 当前可编译代码的 Controller、VO、缓存、错误码和 TSL 响应优先。
4. 如果本文档与当前代码冲突，停止实现，先修本文档。
5. 每次只迁移 ThingModel 一个聚合；Product 和 Modbus 作为协作边界处理。

## AI Execution Contract

| Item | Contract |
|---|---|
| Scope | 仅处理 IoT ThingModel 聚合：功能创建、更新、删除、查询、TSL 聚合、缓存、发布态保护、Modbus 冗余字段同步。 |
| Must Read | `IotThingModelController.java`、ThingModel VO、`IotThingModelDO.java`、`ThingModelProperty/Event/Service`、`IotThingModelMapper.java`、`IotThingModelConvert.java`、`IotThingModelService.java`、`IotThingModelServiceImpl.java`、`IotProductService.java`、`IotDeviceModbusPointService.java`、`ErrorCodeConstants.java`。 |
| Must Preserve | `/admin-api/iot/thing-model/**` URL、HTTP 方法、权限、VO 字段、TSL `properties/services/events` 响应结构、`THING_MODEL_*` 与 `PRODUCT_STATUS_NOT_ALLOW_THING_MODEL` 错误码、`THING_MODEL_LIST` 缓存语义。 |
| Allowed Changes | `domain/thingmodel/**`、`application/thingmodel/**`、`infrastructure/thingmodel/**`、ThingModel convert、ThingModel Controller 注入应用端口、ThingModel API 契约与测试。 |
| Forbidden Changes | 禁止修改 Product 状态枚举；禁止删除系统保留 identifier 校验；禁止把 TSL 改为单表原样返回；禁止让 Domain 依赖 `ThingModelProperty/ThingModelEvent/ThingModelService` 等 DO-side JSON 类型或其他技术实现；禁止扩大到设备属性存储或规则引擎。 |
| Dependency Rules | Domain 可表达物模型类型和功能不变量，但不得依赖 Spring、Mapper、Controller VO、DO、缓存注解；Application 协调 Product 状态、Modbus 同步、缓存失效；Infrastructure 适配 Mapper、DO、缓存。 |
| Verification Gate | 至少运行 ThingModel 测试、Controller 契约测试、domain 纯净 grep、IoT server compile。 |
| Stop Conditions | TSL 字段含义不清、Product 发布态语义冲突、缓存租户语义不清、错误码重复编号被误认为可修、Modbus 同步行为无法确认、验证失败时停止。 |

## Current Source Anchors

| Layer | Current Path |
|---|---|
| Controller | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/thingmodel/IotThingModelController.java` |
| Controller HTTP | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/thingmodel/IotThingModelController.http` |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/thingmodel/vo/IotThingModelSaveReqVO.java` |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/thingmodel/vo/IotThingModelRespVO.java` |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/thingmodel/vo/IotThingModelTSLRespVO.java` |
| DO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/dataobject/thingmodel/IotThingModelDO.java` |
| Model Value | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/dataobject/thingmodel/model/ThingModelProperty.java` |
| Model Value | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/dataobject/thingmodel/model/ThingModelEvent.java` |
| Model Value | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/dataobject/thingmodel/model/ThingModelService.java` |
| Mapper | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/mysql/thingmodel/IotThingModelMapper.java` |
| Convert | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/convert/thingmodel/IotThingModelConvert.java` |
| Service | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/thingmodel/IotThingModelService.java` |
| Service Impl | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/thingmodel/IotThingModelServiceImpl.java` |
| Collaborator | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/product/IotProductService.java` |
| Collaborator | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/device/IotDeviceModbusPointService.java` |
| ErrorCode | `develop-module-iot/develop-module-iot-api/src/main/java/com/develop/mvp/pk/module/iot/enums/ErrorCodeConstants.java` |
| Tests | `develop-module-iot/develop-module-iot-server/src/test`，若无 ThingModel 专用测试，迁移前创建最小测试。 |

## Standard Skeleton Contract

必须创建或收口到以下结构：

```text
domain/thingmodel/model/IotThingModel.java
domain/thingmodel/model/IotThingModelFunction.java
domain/thingmodel/valueobject/IotThingModelId.java
domain/thingmodel/valueobject/IotThingModelIdentifier.java
domain/thingmodel/valueobject/IotThingModelName.java
domain/thingmodel/valueobject/IotThingModelType.java
domain/thingmodel/valueobject/IotThingModelPropertyDefinition.java
domain/thingmodel/valueobject/IotThingModelEventDefinition.java
domain/thingmodel/valueobject/IotThingModelServiceDefinition.java
domain/thingmodel/event/IotThingModelCreatedEvent.java
domain/thingmodel/event/IotThingModelUpdatedEvent.java
domain/thingmodel/service/IotThingModelPolicy.java
domain/thingmodel/repository/IotThingModelRepository.java
application/thingmodel/command/CreateIotThingModelCommand.java
application/thingmodel/command/UpdateIotThingModelCommand.java
application/thingmodel/query/IotThingModelPageQuery.java
application/thingmodel/query/IotThingModelListQuery.java
application/thingmodel/result/IotThingModelResult.java
application/thingmodel/result/IotThingModelTslResult.java
application/thingmodel/port/inbound/IotThingModelUseCase.java
application/thingmodel/port/outbound/IotThingModelModbusPointPort.java
application/thingmodel/service/IotThingModelApplicationService.java
infrastructure/thingmodel/persistence/IotThingModelRepositoryImpl.java
infrastructure/thingmodel/external/package-info.java
infrastructure/thingmodel/rpc/package-info.java
infrastructure/thingmodel/cache/package-info.java
infrastructure/thingmodel/messaging/package-info.java
```

## Fixed Data Model

| Field | Current Type | Meaning | Nullable / Default | Mapping |
|---|---|---|---|---|
| `id` | `Long` | 物模型功能编号 | DB generated | `IotThingModelId` |
| `identifier` | `String` | 功能标识 | 创建/更新 VO 校验为准；创建时不得为保留字 | `IotThingModelIdentifier` |
| `name` | `String` | 功能名称 | VO 校验为准；同产品唯一 | `IotThingModelName` |
| `description` | `String` | 功能描述 | 可空 | Domain field |
| `productId` | `Long` | 产品编号，主关联 | 必填 | Product reference id |
| `productKey` | `String` | 产品标识冗余字段 | 当前 DO 保留 | Redundant field |
| `type` | `Integer` | 功能类型：属性/事件/服务 | 枚举 `IotThingModelTypeEnum` | `IotThingModelType` |
| `property` | `ThingModelProperty` | 属性定义 JSON | type 为 PROPERTY 时使用；仅作为当前 DO-side 事实源 | `IotThingModelPropertyDefinition` |
| `event` | `ThingModelEvent` | 事件定义 JSON | type 为 EVENT 时使用；仅作为当前 DO-side 事实源 | `IotThingModelEventDefinition` |
| `service` | `ThingModelService` | 服务定义 JSON | type 为 SERVICE 时使用；仅作为当前 DO-side 事实源 | `IotThingModelServiceDefinition` |
| base fields | inherited | 创建/更新时间等 | `BaseDO` | Infrastructure only |

## Method Signatures

### Domain

```java
public final class IotThingModel {
    public static IotThingModel create(Long productId, String productKey, IotThingModelIdentifier identifier,
                                       IotThingModelName name, String description, IotThingModelType type,
                                       IotThingModelPropertyDefinition property,
                                       IotThingModelEventDefinition event,
                                       IotThingModelServiceDefinition service);
    public void update(IotThingModelIdentifier identifier, IotThingModelName name, String description,
                       IotThingModelType type, IotThingModelPropertyDefinition property,
                       IotThingModelEventDefinition event,
                       IotThingModelServiceDefinition service);
    public boolean isProperty();
    public boolean isEvent();
    public boolean isService();
}
```

### Repository

```java
public interface IotThingModelRepository {
    IotThingModel findById(IotThingModelId id);
    IotThingModel findByProductIdAndIdentifier(Long productId, String identifier);
    IotThingModel findByProductIdAndName(Long productId, String name);
    List<IotThingModel> findByProductId(Long productId);
    List<IotThingModel> findByProductIdAndIdentifiers(Long productId, Collection<String> identifiers);
    List<IotThingModel> findByProductIdAndType(Long productId, Integer type);
    PageResult<IotThingModel> findPage(IotThingModelPageQuery query);
    List<IotThingModel> findList(IotThingModelListQuery query);
    void save(IotThingModel thingModel);
    void delete(IotThingModelId id);
    void evictProductThingModelCache(Long productId);
}
```

### Application / Inbound Port

```java
public interface IotThingModelUseCase {
    Long createThingModel(CreateIotThingModelCommand command);
    void updateThingModel(UpdateIotThingModelCommand command);
    void deleteThingModel(Long id);
    IotThingModelResult getThingModel(Long id);
    IotThingModelTslResult getTsl(Long productId);
    List<IotThingModelResult> getThingModelList(IotThingModelListQuery query);
    PageResult<IotThingModelResult> getThingModelPage(IotThingModelPageQuery query);
    void validateThingModelListExists(Long productId, Set<String> identifiers);
}
```

## Business Rules

| ID | Rule | Layer | Verification |
|---|---|---|---|
| TM-BR-001 | 创建时同一产品下 `identifier` 唯一。 | Application + Repository | Duplicate identifier test expects `THING_MODEL_IDENTIFIER_EXISTS` |
| TM-BR-002 | 创建时同一产品下 `name` 唯一。 | Application + Repository | Duplicate name test expects `THING_MODEL_NAME_EXISTS` |
| TM-BR-003 | 创建时 `identifier` 不得为 `set/get/post/property/event/time/value`。 | Domain Policy/Application | Reserved identifier test expects `THING_MODEL_IDENTIFIER_INVALID` |
| TM-BR-004 | 创建、更新、删除前产品必须存在且状态不是 published。 | Application | Published product operation test expects `PRODUCT_STATUS_NOT_ALLOW_THING_MODEL` |
| TM-BR-005 | 更新前物模型必须存在，否则 `THING_MODEL_NOT_EXISTS`。 | Application | Missing update test |
| TM-BR-006 | 更新时 identifier 可保持自身，不能与同产品其他功能重复。 | Application + Repository | Update conflict test |
| TM-BR-007 | 更新后必须同步 Modbus 点位冗余字段 identifier/name。 | Application + Outbound Port | Update test verifies port call |
| TM-BR-008 | 创建、更新、删除后必须删除 `THING_MODEL_LIST` 缓存。 | Application/Infrastructure | Cache eviction test |
| TM-BR-009 | `get-tsl` 产品不存在时当前 Controller 返回 `success(null)`。 | Controller/Application | Controller contract test |
| TM-BR-010 | `get-tsl` 按 `type` 聚合为 `properties`、`services`、`events` 三组 JSON 结构。 | Application/Controller | TSL response test |

## Error Code Contract

| Scenario | ErrorCodeConstants | Parameters | Throwing Layer |
|---|---|---|---|
| Thing model missing | `THING_MODEL_NOT_EXISTS` | none | Application |
| ProductKey duplicate legacy scenario | `THING_MODEL_EXISTS_BY_PRODUCT_KEY` | none | Application if current code path requires it |
| Duplicate identifier | `THING_MODEL_IDENTIFIER_EXISTS` | none | Application |
| Duplicate name | `THING_MODEL_NAME_EXISTS` | none | Application |
| Reserved or invalid identifier | `THING_MODEL_IDENTIFIER_INVALID` | none | Domain Policy/Application |
| Product published blocks thing model operation | `PRODUCT_STATUS_NOT_ALLOW_THING_MODEL` | none | Application |
| Product missing | `PRODUCT_NOT_EXISTS` | none | Product collaborator |

当前 `THING_MODEL_NAME_EXISTS` 与 `THING_MODEL_IDENTIFIER_INVALID` 数字编号相同，这是事实源；DDD 迁移不得擅自修正编号。

## Transaction Contract

| Use Case | Current Transaction | Required Contract |
|---|---|---|
| createThingModel | `@Transactional(rollbackFor = Exception.class)` | 唯一性校验、产品状态校验、插入、缓存删除同一事务边界。 |
| updateThingModel | `@Transactional(rollbackFor = Exception.class)` | 存在校验、唯一性校验、产品状态校验、更新、Modbus 同步、缓存删除同一事务边界。 |
| deleteThingModel | `@Transactional(rollbackFor = Exception.class)` | 存在校验、产品状态校验、删除、缓存删除同一事务边界。 |
| read/list/page/cache | no transaction | 保持只读语义。 |

## Integration Contract

- Product：通过 `IotProductService.validateProductExists(productId)` 校验产品存在和读取状态；禁止 Domain 直接依赖 Product service。
- Modbus：更新物模型后调用 `IotDeviceModbusPointService.updateDeviceModbusPointByThingModel(id, identifier, name)`。
- 缓存：`RedisKeyConstants.THING_MODEL_LIST`，`getThingModelListByProductIdFromCache` 和 `deleteThingModelListCache0` 当前均 `@TenantIgnore`。
- TSL：`get-tsl` 由 Product 信息和物模型列表组合，返回 productId、productKey、properties、services、events。
- JSON 类型：`property/event/service` 通过 `JacksonTypeHandler` 存储，迁移不能改变字段结构。

## Mapping Rules

| Mapping | Rule |
|---|---|
| `IotThingModelSaveReqVO -> Command` | Controller/Convert 层完成，Domain 不依赖 VO。 |
| `IotThingModelDO -> IotThingModel` | Infrastructure persistence adapter 完成。 |
| `ThingModelProperty/Event/Service` | 当前为 DO-side model 包，迁移前若提纯为 domain value object，必须保持 JSON 字段兼容。 |
| `IotThingModel -> IotThingModelDO` | RepositoryImpl 保存前转换，保留 `productId/productKey/type/property/event/service`。 |
| list -> TSL result | Application 或 Controller 组合，按 type 分组，不返回未分组原始列表。 |
| result -> VO | Convert 层完成；禁止 Domain 依赖 `IotThingModelTSLRespVO`。 |

## Acceptance Criteria

- 架构 AC：ThingModel 核心规则进入 `domain/thingmodel` 与 `application/thingmodel`；Domain 不依赖 Spring、Mapper、VO、DO、缓存注解。
- 业务 AC：identifier/name 唯一、保留字校验、发布态阻断、Modbus 同步、缓存删除、TSL 响应均与迁移前一致。
- 契约 AC：Controller URL、HTTP 方法、权限、VO 字段、错误码、TSL 字段不变。
- 编译 AC：ThingModel 测试和 IoT server compile 成功。

## Verification Commands

```bash
grep -RInE "org\.springframework|Mapper|RedisTemplate|RabbitTemplate|RocketMQ|KafkaTemplate|HttpServlet|controller\.admin|dal\.dataobject" develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/domain/thingmodel
mvn -f develop-module-iot/pom.xml -pl develop-module-iot-server test -Dtest=*ThingModel*Test,*ThingModel*Controller*Test
mvn -f develop-module-iot/pom.xml -pl develop-module-iot-server compile -DskipTests
```

## Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| identifier/name 不变量 | `domain/thingmodel/service` 或 Application policy | Controller、Mapper |
| Product 发布态校验编排 | `application/thingmodel/service` | Domain 直接调 ProductService |
| Modbus 冗余同步端口 | `application/thingmodel/port/outbound` | Domain、Controller |
| MyBatis 持久化 | `infrastructure/thingmodel/persistence` | Domain |
| TSL 结果组合 | Application result 或 Controller adapter | Mapper XML 拼外部 VO |
| 缓存适配 | `infrastructure/thingmodel/cache` 或 RepositoryImpl 明确封装 | Domain |

## Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| 只按 productKey 关联物模型 | 当前 productId 主关联被破坏 | 保持 productId 主关联，productKey 冗余 |
| 漏掉系统保留 identifier | 与协议方法冲突 | 保留 `set/get/post/property/event/time/value` 黑名单 |
| 更新不调用 Modbus 同步 | Modbus 点位展示旧 identifier/name | 通过 outbound port 调用同步 |
| TSL 返回原始列表 | 前端契约回归 | 按 properties/services/events 分组 |
| 修正重复错误码编号 | 外部错误处理回归 | 保持当前编号，另行迁移才可改 |
| 删除 `@TenantIgnore` | 缓存 key 和跨租户行为变化 | 先测试和迁移计划，不在 DDD 重构中改 |

## Rationalization Table

| Pressure Scenario | Likely Bad Shortcut | Required Response |
|---|---|---|
| 赶时间迁移 CRUD | 漏掉 TSL、Modbus、缓存、Product 状态 | 先写冻结行为测试再迁移 |
| 看到没有版本表 | 直接新增 version 聚合并改 API | P0 保持当前单表行为，版本化另行设计 |
| 认为错误码重复是 bug | 顺手改编号 | 错误码是外部契约，禁止混改 |
| 认为缓存忽略租户不安全 | 直接去掉 `@TenantIgnore` | 保留现有行为并标注风险 |
| 只有一个 Mapper 实现 | 省略 Repository 和 infrastructure | 标准骨架必须存在 |

## Red Flags

- ThingModel 迁移未读取 `IotThingModelServiceImpl.java`。
- Domain 中出现 Spring、Mapper、DO、VO、缓存注解。
- `get-tsl` 不再返回 `properties/services/events`。
- 创建时不再校验 identifier 保留字。
- 更新物模型不再同步 Modbus 点位冗余字段。
- 发布态产品仍允许新增、更新或删除物模型。
- 修改了 `THING_MODEL_NAME_EXISTS` 或 `THING_MODEL_IDENTIFIER_INVALID` 编号。

## Rollback Conditions

1. IoT server 编译失败。
2. ThingModel HTTP/TSL 外部响应变化。
3. 错误码编号、异常类型或参数顺序变化。
4. Product 发布态保护失效。
5. Modbus 冗余字段同步丢失。
6. 缓存租户语义无意变化。
7. Domain 依赖技术框架或持久化实现。

## AI Self-Check

- [ ] 已读取 ThingModel Controller、VO、DO、Mapper、Convert、Service、ErrorCode。
- [ ] 已保留 productId 主关联和 productKey 冗余字段。
- [ ] 已保留 identifier/name 唯一和保留字校验。
- [ ] 已保留 Product 发布态阻断。
- [ ] 已保留 Modbus 冗余字段同步。
- [ ] 已保留 TSL `properties/services/events` 响应结构。
- [ ] 已执行 domain 纯净 grep、ThingModel 测试和 IoT server compile。
