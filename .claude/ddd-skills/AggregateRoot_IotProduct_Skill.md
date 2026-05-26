---
name: aggregate-root-iot-product-skill
description: Use when creating, auditing, or refactoring the IoT product aggregate in develop-module-iot-server.
---

# IoT Product Aggregate Skill

## Overview

IoT Product 聚合负责产品基础资料、产品密钥、设备类型、协议类型、发布状态、动态注册开关和产品级 TDengine 属性表同步，复现目标是在不改变现有 HTTP、Excel、缓存、租户和错误码契约的前提下，把旧 `service/product` 逻辑迁移到标准 DDD/六边形骨架。

## When to Use

- 修改 `iot_product` 创建、更新、删除、发布状态、查询、导出或属性表同步逻辑时使用。
- 将 `IotProductServiceImpl` 迁移到 `domain/product`、`application/product`、`infrastructure/product` 时使用。
- 创建 `develop-module-iot-api` 中 product `CommonApi`、DTO、local/remote 适配时使用。
- 审计 Product 聚合是否保留现有缓存、租户、TDengine 和设备引用约束时使用。

## When Not to Use

- 只修改 Product Category 聚合时不用本 skill，除非同时影响 `IotProductDO.categoryId` 映射。
- 只修改设备实例、物模型、设备消息或网关协议时不用本 skill。
- 需要改变 Controller URL、VO 字段、Excel 导出契约或错误码编号时，先写单独迁移计划，不得混入 Product 聚合迁移。

## Reproducibility Contract

1. 先读 Current Source Anchors 中全部事实源，再写代码。
2. 当前可编译代码的外部行为优先于本文档描述；若冲突，先修订本 skill，再实现。
3. 不猜字段、错误码、事务边界、缓存 key、权限注解或 TDengine 行为。
4. Product DDD 重构只迁移边界和职责，不改变业务逻辑、HTTP 契约、Excel 契约、缓存语义和租户语义。
5. 每次只处理 Product 一个聚合；Product Category、Device、ThingModel、Property 只可作为外部协作事实源。

## AI Execution Contract

| Item | Contract |
|---|---|
| Scope | 仅处理 IoT Product 聚合：产品创建、更新、删除、发布状态、查询、缓存、TDengine 产品属性表同步和 API 契约。 |
| Must Read | `IotProductController.java`、product VO、`IotProductDO.java`、`IotProductMapper.java`、`IotProductService.java`、`IotProductServiceImpl.java`、`IotProductCategoryService.java`、`IotDeviceService.java`、`IotDevicePropertyService.java`、`ErrorCodeConstants.java`、Product 相关测试或 HTTP 文件。 |
| Must Preserve | `/admin-api/iot/product/**` 路径、HTTP 方法、权限注解、Excel 文件名 `产品.xls`、分页字段、`PRODUCT_*` 错误码、`@TenantIgnore` 缓存语义、发布时 TDengine 表结构同步、产品下有设备不可删除。 |
| Allowed Changes | `domain/product/**`、`application/product/**`、`infrastructure/product/**`、`convert/**` 中 Product 映射、Product Controller 注入应用端口、Product API 契约与测试。 |
| Forbidden Changes | 禁止把新核心业务继续写入 `service/product` 或 `dal/mysql/product`；禁止删除 `productSecret` 动态注册语义；禁止改变 `productKey` 更新时被置空的行为；禁止修改无关 Device/ThingModel 实现。 |
| Dependency Rules | Domain 只依赖 Java 和 product valueobject/event/repository/service；Application 可依赖 domain repository、outbound port、convert；Infrastructure 才能依赖 Mapper/DO/TDengine/缓存；Controller 只能依赖 inbound port 和 VO convert。 |
| Verification Gate | 至少运行 Product 测试、Product Controller 测试、IoT server compile；无测试时先补最小测试或说明当前测试缺口。 |
| Stop Conditions | 找不到事实源、需要改变 Product HTTP/Excel/RPC 契约、Product 状态语义与代码冲突、TDengine 同步语义不清、缓存租户边界不清、验证失败且无法定位原因时停止。 |

## Current Source Anchors

| Layer | Current Path |
|---|---|
| Controller | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/product/IotProductController.java` |
| Controller HTTP | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/product/IotProductController.http` |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/product/vo/product/IotProductSaveReqVO.java` |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/product/vo/product/IotProductPageReqVO.java` |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/product/vo/product/IotProductRespVO.java` |
| DO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/dataobject/product/IotProductDO.java` |
| Mapper | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/mysql/product/IotProductMapper.java` |
| Service | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/product/IotProductService.java` |
| Service Impl | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/product/IotProductServiceImpl.java` |
| Collaborator | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/device/IotDeviceService.java` |
| Collaborator | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/device/property/IotDevicePropertyService.java` |
| ErrorCode | `develop-module-iot/develop-module-iot-api/src/main/java/com/develop/mvp/pk/module/iot/enums/ErrorCodeConstants.java` |
| Enum | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/enums/product/IotProductStatusEnum.java` |
| Enum | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/enums/product/IotProductDeviceTypeEnum.java` |
| Tests | `develop-module-iot/develop-module-iot-server/src/test`，若无 Product 专用测试，迁移前创建最小 Product 测试。 |

## Standard Skeleton Contract

必须创建或收口到以下结构：

```text
domain/product/model/IotProduct.java
domain/product/valueobject/IotProductId.java
domain/product/valueobject/IotProductKey.java
domain/product/valueobject/IotProductSecret.java
domain/product/valueobject/IotProductStatus.java
domain/product/event/IotProductCreatedEvent.java
domain/product/event/IotProductPublishedEvent.java
domain/product/service/IotProductPolicy.java
domain/product/repository/IotProductRepository.java
application/product/command/CreateIotProductCommand.java
application/product/command/UpdateIotProductCommand.java
application/product/command/UpdateIotProductStatusCommand.java
application/product/query/IotProductPageQuery.java
application/product/result/IotProductResult.java
application/product/port/inbound/IotProductUseCase.java
application/product/port/outbound/IotProductPropertyTablePort.java
application/product/service/IotProductApplicationService.java
infrastructure/product/persistence/IotProductRepositoryImpl.java
infrastructure/product/external/package-info.java
infrastructure/product/rpc/package-info.java
infrastructure/product/cache/package-info.java
infrastructure/product/messaging/package-info.java
```

Java 空目录用职责明确的接口或 `package-info.java` 固定边界，不使用 `Temp`、`Dummy`、无业务语义类。

## Fixed Data Model

| Field | Current Type | Meaning | Nullable / Default | Mapping |
|---|---|---|---|---|
| `id` | `Long` | 产品 ID | DB generated | `IotProductId` |
| `name` | `String` | 产品名称 | VO 校验为准 | Domain name |
| `productKey` | `String` | 产品标识 | 创建必填；更新时置空不更新 | `IotProductKey` |
| `productSecret` | `String` | 一型一密动态注册密钥 | 创建时 `IdUtil.fastSimpleUUID()` | `IotProductSecret` |
| `registerEnabled` | `Boolean` | 是否开启动态注册 | 当前 VO/DB 为准 | Domain flag |
| `categoryId` | `Long` | 产品分类编号 | 可空性以 VO 为准 | Domain field |
| `icon` | `String` | 产品图标 | 可空 | Domain field |
| `picUrl` | `String` | 产品图片 | 可空 | Domain field |
| `description` | `String` | 产品描述 | 可空 | Domain field |
| `status` | `Integer` | 产品状态 | 创建默认为 `UNPUBLISHED` | `IotProductStatus` |
| `deviceType` | `Integer` | 设备类型 | VO/枚举为准 | Domain field |
| `netType` | `Integer` | 联网方式 | VO/枚举为准 | Domain field |
| `protocolType` | `String` | 协议类型 | VO/枚举为准 | Domain field |
| `serializeType` | `String` | 序列化类型 | VO/枚举为准 | Domain field |
| tenant fields | inherited | 租户隔离 | `TenantBaseDO` | Infrastructure only |

## Method Signatures

### Domain

```java
public final class IotProduct {
    public static IotProduct create(String name, IotProductKey productKey, IotProductSecret productSecret,
                                    Boolean registerEnabled, Long categoryId, String icon, String picUrl,
                                    String description, Integer deviceType, Integer netType,
                                    String protocolType, String serializeType);
    public void updateProfile(String name, Boolean registerEnabled, Long categoryId, String icon,
                              String picUrl, String description, Integer deviceType,
                              Integer netType, String protocolType, String serializeType);
    public void changeStatus(IotProductStatus status);
    public boolean isPublished();
}
```

### Repository

```java
public interface IotProductRepository {
    IotProduct findById(IotProductId id);
    IotProduct findByProductKey(IotProductKey productKey);
    PageResult<IotProduct> findPage(IotProductPageQuery query);
    List<IotProduct> findAll();
    List<IotProduct> findByIds(Collection<Long> ids);
    List<IotProduct> findByDeviceType(Integer deviceType);
    long countByCreateTime(LocalDateTime createTime);
    void save(IotProduct product);
    void delete(IotProductId id);
}
```

### Application / Inbound Port

```java
public interface IotProductUseCase {
    Long createProduct(CreateIotProductCommand command);
    void updateProduct(UpdateIotProductCommand command);
    void deleteProduct(Long id);
    void updateProductStatus(UpdateIotProductStatusCommand command);
    IotProductResult getProduct(Long id);
    IotProductResult getProductByProductKey(String productKey);
    PageResult<IotProductResult> getProductPage(IotProductPageQuery query);
    List<IotProductResult> getProductList(Integer deviceType);
    void syncProductPropertyTable();
}
```

## Business Rules

| ID | Rule | Layer | Verification |
|---|---|---|---|
| P-BR-001 | 创建产品时 `productKey` 必须唯一；当前实现通过 `productMapper.selectByProductKey` 校验。 | Application + Repository | Duplicate key test expects `PRODUCT_KEY_EXISTS` |
| P-BR-002 | 创建产品默认状态为 `IotProductStatusEnum.UNPUBLISHED`。 | Domain/Application | Create product test asserts status |
| P-BR-003 | 创建产品生成 `productSecret`，当前算法为 `IdUtil.fastSimpleUUID()`。 | Application/Domain Factory | Create test asserts nonblank secret |
| P-BR-004 | 更新产品不得更新 `productKey`，当前实现 `updateReqVO.setProductKey(null)`。 | Application | Update test asserts key unchanged |
| P-BR-005 | 删除产品前必须存在，否则 `PRODUCT_NOT_EXISTS`。 | Application | Delete missing test |
| P-BR-006 | 已发布产品不可删除，抛 `PRODUCT_STATUS_NOT_DELETE`。 | Domain/Application | Published delete test |
| P-BR-007 | 产品下存在设备不可删除，抛 `PRODUCT_DELETE_FAIL_HAS_DEVICE`。 | Application | Device count collaborator test |
| P-BR-008 | 发布产品时必须调用 `IotDevicePropertyService.defineDevicePropertyData(id)` 同步 TDengine 产品属性表。 | Application + Outbound Port | Status publish test verifies port call |
| P-BR-009 | `getProductFromCache` 忽略租户，调用方必须确认不会跨租户泄漏。 | Infrastructure/Cache | Cache boundary review |
| P-BR-010 | Excel 导出保持 `产品.xls` 和 sheet `数据`，并保留 `iot:product:export` 权限与 `ApiAccessLog(EXPORT)`。 | Controller | Controller contract test |

## Error Code Contract

| Scenario | ErrorCodeConstants | Parameters | Throwing Layer |
|---|---|---|---|
| Product missing by id/key | `PRODUCT_NOT_EXISTS` | none | Application |
| Duplicate product key | `PRODUCT_KEY_EXISTS` | none | Application |
| Published product delete | `PRODUCT_STATUS_NOT_DELETE` | none | Domain/Application |
| Published product modifying thing model | `PRODUCT_STATUS_NOT_ALLOW_THING_MODEL` | none | ThingModel collaborator |
| Product has devices on delete | `PRODUCT_DELETE_FAIL_HAS_DEVICE` | none | Application |

禁止重编号、改消息、调换参数顺序或用其他异常替代 `ServiceExceptionUtil.exception(...)` 的外部语义。

## Transaction Contract

| Use Case | Current Transaction | Required Contract |
|---|---|---|
| createProduct | no explicit transaction | 单表写，迁移时可保持无事务；若聚合事件/外部端口加入，同一应用服务方法加事务。 |
| updateProduct | no explicit transaction + cache evict | 更新和缓存失效保持同一用例边界。 |
| deleteProduct | no explicit transaction + cache evict | 设备数量校验、删除、缓存失效必须保持顺序。 |
| updateProductStatus | `@DSTransactional(rollbackFor = Exception.class)` + cache evict | 发布状态与 TDengine 表结构同步保持当前事务语义，不得降级为普通局部事务。 |
| syncProductPropertyTable | no transaction; per product catch/log | 保持逐产品同步失败不阻断后续产品的行为。 |

## Integration Contract

- 缓存：`RedisKeyConstants.PRODUCT`，`getProductFromCache` 为 `@Cacheable` + `@TenantIgnore`，更新/删除/状态变更为 `@CacheEvict`。
- TDengine：发布产品和手工同步必须通过 `IotDevicePropertyService.defineDevicePropertyData(productId)`。
- Device：删除产品前通过 `IotDeviceService.getDeviceCountByProductId(id)` 检查设备引用。
- Product Category：Controller 返回详情时补 `categoryName`，不得在 Domain 中依赖分类服务。
- Excel：导出保留 `ExcelUtils.write(response, "产品.xls", "数据", IotProductRespVO.class, data)`。
- Tenant：产品 DO 继承 `TenantBaseDO`；缓存忽略租户是现有外部行为，迁移时必须明确保留并审查调用边界。

## Mapping Rules

| Mapping | Rule |
|---|---|
| `IotProductSaveReqVO -> CreateIotProductCommand` | Controller/Convert 层完成，Domain 不依赖 VO。 |
| `IotProductSaveReqVO -> UpdateIotProductCommand` | 更新命令不携带可变更 productKey；即使 VO 传入也不更新。 |
| `IotProductDO -> IotProduct` | Infrastructure persistence adapter 完成，Domain 不依赖 DO。 |
| `IotProduct -> IotProductDO` | RepositoryImpl 保存前转换。 |
| `IotProduct -> IotProductRespVO` | Convert 层完成；categoryName 在 Controller/Application 组合结果中补齐。 |
| `IotProduct -> API DTO` | API convert 与 controller VO 分离，禁止跨用。 |

## Acceptance Criteria

- 架构 AC：Product 核心规则进入 `domain/product` 与 `application/product`；`domain/product` 不出现 Spring、MyBatis、Redis、Controller VO、DO、Mapper。
- 业务 AC：创建、更新、删除、状态变更、缓存、TDengine 同步、设备引用检查、Excel 导出外部行为与迁移前一致。
- 契约 AC：Controller URL、HTTP 方法、权限、VO 字段、错误码、Excel 文件名和 sheet 名不变。
- 编译 AC：IoT server compile 成功；Product 相关测试通过。

## Verification Commands

```bash
grep -RInE "org\.springframework|Mapper|RedisTemplate|RabbitTemplate|RocketMQ|KafkaTemplate|HttpServlet|controller\.admin|dal\.dataobject" develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/domain/product
mvn -f develop-module-iot/pom.xml -pl develop-module-iot-server test -Dtest=*Product*Test,*Product*Controller*Test
mvn -f develop-module-iot/pom.xml -pl develop-module-iot-server compile -DskipTests
```

## Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 产品业务不变量 | `domain/product/model`、`domain/product/service` | Controller、Mapper、Convert |
| 产品用例编排和事务 | `application/product/service` | Domain、Controller |
| 产品入站端口 | `application/product/port/inbound` | Controller 私有方法 |
| TDengine 产品属性表同步端口 | `application/product/port/outbound/IotProductPropertyTablePort.java` | Domain、Controller |
| MyBatis 产品持久化 | `infrastructure/product/persistence` | Domain、Application port inbound |
| Product VO 映射 | `convert` | Domain |
| 缓存适配 | `infrastructure/product/cache` 或 persistence adapter 明确封装 | Domain |

## Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| 更新产品时允许修改 `productKey` | 设备、消息、动态注册关联断裂 | 保持更新命令不更新 productKey |
| 发布状态只改 DB，不同步 TDengine | 属性上报存储缺表 | 发布用例调用属性表端口 |
| 删除产品不检查设备数 | 设备引用悬挂 | 保留 `getDeviceCountByProductId` 检查 |
| 将 `@TenantIgnore` 缓存去掉或隐藏 | 跨租户缓存行为变化 | 保留并显式标注风险 |
| Domain 直接依赖 `IotProductDO` | 依赖方向反转 | 通过 RepositoryImpl 映射 |
| 把 Product Category 合并进 Product 聚合 | 范围扩大，风险增加 | 分类作为外部协作读取 |

## Rationalization Table

| Pressure Scenario | Likely Bad Shortcut | Required Response |
|---|---|---|
| 赶时间直接写 Product CRUD | 跳过发布、TDengine、设备引用、缓存 | 先冻结现有业务规则和测试，再迁移 |
| 认为 Product 很简单 | 不建 outbound port 和标准目录 | 标准骨架是验收项，必须创建 |
| 想顺手清理 productKey 行为 | 允许更新 productKey | 当前行为是外部契约，禁止改变 |
| 看到 `@TenantIgnore` 感觉危险 | 直接删除 | 这是现有缓存契约，先记录风险并用测试保护 |
| TDengine 本地跑不起来 | 删除同步调用 | 保留端口，测试可用 fake adapter 验证调用 |

## Red Flags

- Product skill 或实现没有读取 `IotProductServiceImpl.java`。
- Product DDD 迁移改变了 Controller path、权限、VO 字段、Excel 名称。
- Product 发布状态不再调用 TDengine 属性表同步。
- Domain 中出现 Spring、Mapper、DO、Controller VO 或 Redis 注解。
- 以“只有一个实现”为理由省略 `IotProductRepository`、inbound port、outbound port 或 infrastructure 子目录。
- 删除或弱化产品下存在设备不可删除规则。

## Rollback Conditions

1. IoT server 编译失败。
2. Product HTTP/Excel/API 外部契约变化。
3. Product 错误码、异常类型或参数顺序变化。
4. 发布产品不再同步 TDengine 产品属性表。
5. 缓存租户语义无意变化。
6. Domain 依赖技术框架或持久化实现。

## AI Self-Check

- [ ] 已读取 Product Controller、VO、DO、Mapper、Service、ErrorCode 和测试。
- [ ] 已保留 productKey 创建唯一、更新不可变规则。
- [ ] 已保留 productSecret 创建生成规则。
- [ ] 已保留发布状态删除限制和设备引用删除限制。
- [ ] 已保留发布时 TDengine 属性表同步。
- [ ] 已保留 Product cache 与 `@TenantIgnore` 语义。
- [ ] 已执行 domain 纯净 grep、Product 测试和 IoT server compile。
