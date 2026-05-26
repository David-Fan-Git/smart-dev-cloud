---
name: aggregate-root-wms-inventory-skill
description: Use when upgrading, reviewing, or applying the WMS inventory aggregate skill; treat it as draft until current source anchors, external contracts, errors, transactions, and tests are verified.
type: ddd-aggregate-skill
status: draft
---

# DDD Skill: AggregateRoot_Wms_Inventory_Skill

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

## 1. 技能名称

`AggregateRoot_Wms_Inventory_Skill` — WMS 库存（WmsInventory）聚合根的领域建模与重构技能

## 2. 适用场景

本技能适用于 WMS 库存余额聚合的 DDD 重构与维护，覆盖当前已落地的库存查询与库存数量变更切片：

- 创建库存余额行
- 按 ID 查询库存余额
- 按 SKU + 仓库查询库存余额
- 按仓库查询库存列表
- 分页查询库存余额
- 增加库存数量
- 扣减库存数量
- 设置库存数量与备注
- 发布库存创建与库存数量变更领域事件

`WmsInventoryHistory`、入库单、出库单、移库单、盘点单、SKU 主数据、仓库主数据仍属于独立模型或旧服务流程，不纳入本聚合根内部。

## 3. DDD 构造块

### 3.1 聚合根：WmsInventory

路径：`com.develop.mvp.pk.module.wms.domain.inventory.WmsInventory`

职责：封装库存余额的标识、SKU、仓库、数量、备注和库存数量变更规则。

必须保持：

- 纯 Java 类，不包含 Spring/MyBatis 注解。
- 不直接依赖 Mapper、DO、Controller VO 或旧 Service。
- 使用 `BigDecimal` 表示库存数量。
- 新建聚合时允许 transient id 为 `null`，持久化后由仓储返回带 ID 的聚合。
- `equals`/`hashCode` 必须对 transient id 为空的对象保持 null-safe。

### 3.2 值对象

路径：`com.develop.mvp.pk.module.wms.domain.inventory.valueobject.WmsInventoryId`

职责：封装已持久化库存 ID。

约束：

- `final class`
- 字段不可变
- 无 setter
- `of(Long value)` 必须拒绝空值

### 3.3 工厂

路径：`com.develop.mvp.pk.module.wms.domain.inventory.WmsInventoryFactory`

职责：

- `create(Long skuId, Long warehouseId)`：创建默认数量为 0 的 transient 库存聚合。
- `create(Long skuId, Long warehouseId, BigDecimal quantity, String remark)`：创建指定数量和备注的 transient 库存聚合。
- `reconstitute(Long id, Long skuId, Long warehouseId, BigDecimal quantity, String remark)`：从持久化数据重建聚合。

### 3.4 仓储接口

路径：`com.develop.mvp.pk.module.wms.domain.inventory.repository.WmsInventoryRepository`

当前接口边界：

```java
public interface WmsInventoryRepository {
    WmsInventory save(WmsInventory inventory);
    WmsInventory findById(WmsInventoryId id);
    Optional<WmsInventory> findBySkuIdAndWarehouseId(Long skuId, Long warehouseId);
    List<WmsInventory> findByWarehouseId(Long warehouseId);
    List<WmsInventory> findByKeys(Collection<WmsInventory> keys);
    List<WmsInventory> findByIdsForUpdate(Collection<Long> ids);
    PageResult<WmsInventory> findPage(WmsInventoryPageQuery query);
    long countBySkuId(Long skuId);
    long countByWarehouseId(Long warehouseId);
}
```

约束：

- 接口定义在 domain 层。
- 不 import MyBatis、DO、Controller VO 或 Spring 类型。
- `save` 在 insert 场景必须返回带持久化 ID 的聚合。

### 3.5 查询对象

路径：`com.develop.mvp.pk.module.wms.domain.inventory.repository.WmsInventoryPageQuery`

职责：承载应用层到仓储层的库存分页查询条件，避免领域仓储接口依赖 Controller VO。

### 3.6 仓储实现

路径：`com.develop.mvp.pk.module.wms.infrastructure.inventory.WmsInventoryRepositoryImpl`

职责：

- 委托 `WmsInventoryMapper` 完成持久化。
- 负责 `WmsInventoryDO` 与 `WmsInventory` 互转。
- 将 `WmsInventoryPageQuery` 转换为现有 `WmsInventoryPageReqVO` 以复用 Mapper 查询。
- insert 后必须将 MyBatis 回填的 ID 转为带 ID 的聚合返回。

### 3.7 领域事件

路径：`com.develop.mvp.pk.module.wms.domain.inventory.event`

当前事件：

- `WmsInventoryCreatedEvent`
- `WmsInventoryStockChangedEvent`

发布接口：`DomainEventPublisher`

Spring 适配器：`com.develop.mvp.pk.module.wms.infrastructure.event.SpringDomainEventPublisher`

约束：

- 领域层只依赖模块内 `DomainEvent` / `DomainEventPublisher` 接口。
- Spring `ApplicationEventPublisher` 只能出现在 infrastructure 层。
- 创建事件必须在聚合持久化并获得 ID 后发布。

### 3.8 应用服务

路径：`com.develop.mvp.pk.module.wms.application.inventory.WmsInventoryApplicationService`

职责：

- 创建库存聚合并返回持久化 ID。
- 调用聚合方法完成增加、扣减、设置数量。
- 调用仓储保存聚合。
- 发布聚合产生的领域事件。
- 将分页查询参数封装为 `WmsInventoryPageQuery`。

旧的 `WmsInventoryService` 仍承担批量库存变更、盘点、流水记录等既有流程；不要在本技能范围内强行整体迁移。

## 4. 核心业务规则

| 编号 | 规则 | 落点 |
|------|------|------|
| R01 | SKU ID 不能为空 | `WmsInventory` 构造 |
| R02 | 仓库 ID 不能为空 | `WmsInventory` 构造 |
| R03 | 库存数量不能为空；空入参按 0 处理 | `WmsInventory` 构造 / `setQuantity` |
| R04 | 库存数量不能小于 0 | `WmsInventory` 构造 / `setQuantity` / `subtractStock` |
| R05 | 增加库存的 amount 必须大于 0 | `addStock` |
| R06 | 扣减库存的 amount 必须大于 0 | `subtractStock` |
| R07 | 扣减后库存不能为负 | `subtractStock` |
| R08 | 数量变更后产生 `WmsInventoryStockChangedEvent` | `addStock` / `subtractStock` / `setQuantity` |
| R09 | 创建库存后产生 `WmsInventoryCreatedEvent` | `markCreated`，由应用服务在保存后调用 |
| R10 | transient 聚合允许 `id == null` | 构造、工厂、equals、hashCode |

## 5. 职责边界

聚合根负责：

- 库存数量不变量。
- 单条库存余额的数量变更。
- 记录自身领域事件。

应用服务负责：

- 查找已有聚合。
- 调用仓储保存聚合。
- 发布领域事件。
- 查询参数封装。

基础设施负责：

- MyBatis Mapper 调用。
- DO 与 Domain 互转。
- Spring 事件发布适配。

不属于本聚合根职责：

- 直接生成库存流水 DO。
- 校验 SKU/仓库是否存在。
- 处理订单类型、价格、总价。
- 批量盘点全流程迁移。
- Controller VO 转换。

## 6. 验收标准

| 编号 | 验收标准 | 验证方法 |
|------|----------|----------|
| AC01 | `WmsInventory` 无 Spring/MyBatis 注解 | 代码审查 |
| AC02 | `WmsInventory` 不依赖 Mapper、DO、VO、Service | 代码审查 |
| AC03 | `WmsInventoryId` 是不可变值对象 | 代码审查 |
| AC04 | `WmsInventoryRepository` 位于 domain 层且不依赖基础设施类型 | 代码审查 |
| AC05 | `WmsInventoryRepositoryImpl` 位于 infrastructure 层并负责 DO↔Domain 映射 | 代码审查 |
| AC06 | insert 保存后返回带持久化 ID 的聚合 | 单元测试 |
| AC07 | 创建库存返回持久化 ID，并发布带持久化 ID 的创建事件 | 单元测试 |
| AC08 | 负库存创建或设置会被拒绝 | 单元测试 |
| AC09 | Spring 事件发布适配器委托 `ApplicationEventPublisher` | 单元测试 |
| AC10 | WMS 模块编译通过 | `mvn compile -pl develop-module-wms/develop-module-wms-server -am` |

## 7. 回滚条件

- WMS 模块编译失败。
- 聚合根直接依赖 Spring/MyBatis/DO/VO。
- 创建库存返回空 ID。
- 创建事件携带空库存 ID。
- 负库存可被创建或设置。
- 仓储 insert 后不返回持久化聚合。
