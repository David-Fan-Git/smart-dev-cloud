---
name: aggregate-root-mall-statistics-skill
description: Use when modifying or reviewing Mall Statistics product/trade statistics read models, jobs, dashboard queries, or API enrichment boundaries.
type: ddd-aggregate-skill
status: production-review
---

# DDD Skill: Mall Statistics Aggregate Roots

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

## Overview

Mall Statistics is a read/aggregation context for admin dashboards and scheduled daily snapshots. The production behavior source is still the legacy Controller + Service + Mapper path; current DDD code is partial and must not replace legacy behavior until every external contract in this skill is preserved.

The context currently has two DDD aggregate candidates:

- `ProductStatistics`: daily product statistics at `LocalDate + spuId` granularity.
- `TradeStatistics`: daily trade statistics at `LocalDateTime` day granularity.

Member and pay dashboards are part of the same statistics runtime module, but they are legacy query/read-model services today. Do not force them into the two aggregates unless a scoped production skill is created for those read models.

## When to Use

Use this skill when changing any of these areas:

- `develop-module-mall/develop-module-statistics-*` Maven modules.
- Product statistics admin APIs, exports, rank pages, jobs, or daily snapshot generation.
- Trade statistics admin APIs, exports, summary cards, order count dashboards, or trend comparisons.
- Statistics DDD classes under `domain/productstatistics`, `domain/tradestatistics`, `application/*`, or `infrastructure/*`.
- Statistics mapper SQL, BO/VO conversions, time-range comparison rules, or `TimeRangeTypeEnum`.
- `ProductSpuApi` enrichment for product statistics rank pages.

Do not use this skill for product catalog business rules, trade order lifecycle rules, pay channel/pay order rules, member profile rules, or promotion activity rules. Those belong to their own aggregate skills.

## Reproducibility Contract

A clean agent must be able to reproduce the same refactor from only this skill plus the current codebase. Before changing Java code:

1. Read `DDD_Skill_Production_Readiness_Standard.md` and `Module_Structure_Standard.md`.
2. Read every source anchor listed below.
3. Treat legacy Controller/Service/Mapper behavior as the external contract.
4. Compare current DDD code to the fixed data model and conflict notes.
5. Stop if a proposed change would alter routes, permissions, VO fields, Excel output, idempotence messages, time-window semantics, or mapper aggregate formulas.
6. Compile the target module and run the domain tests listed in Verification Commands.

## Baseline Failure Findings

The previous draft was not production-ready because it lacked:

- YAML frontmatter and trigger metadata.
- Exact source anchors for Controller, VO, DO, Mapper, legacy Service, DDD classes, Job, RPC config, and tests.
- Fixed data model for `ProductStatisticsDO`, `TradeStatisticsDO`, BOs, VOs, and current DDD models.
- Exact controller route, permission, export file, time comparison, and idempotence contracts.
- Current conflict notes showing that the DDD model is smaller than the persistence model.
- Verification commands and rollback conditions tied to current module behavior.

## Current Source Anchors

### API module

- `develop-module-mall/develop-module-statistics-api/src/main/java/com/develop/mvp/pk/module/statistics/enums/TimeRangeTypeEnum.java`
- `develop-module-mall/develop-module-statistics-api/src/main/java/com/develop/mvp/pk/module/statistics/package-info.java`

The statistics API module currently exposes no cross-module business API contract. It only exposes `TimeRangeTypeEnum` and package metadata.

### Admin controllers

- `develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics/controller/admin/product/ProductStatisticsController.java`
- `develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics/controller/admin/trade/TradeStatisticsController.java`
- `develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics/controller/admin/member/MemberStatisticsController.java`
- `develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics/controller/admin/pay/PayStatisticsController.java`

### Product statistics VO

- `controller/admin/common/vo/DataComparisonRespVO.java`
- `controller/admin/product/vo/ProductStatisticsReqVO.java`
- `controller/admin/product/vo/ProductStatisticsRespVO.java`

### Trade statistics VO

- `controller/admin/trade/vo/TradeSummaryRespVO.java`
- `controller/admin/trade/vo/TradeTrendReqVO.java`
- `controller/admin/trade/vo/TradeTrendSummaryRespVO.java`
- `controller/admin/trade/vo/TradeTrendSummaryExcelVO.java`
- `controller/admin/trade/vo/TradeOrderCountRespVO.java`
- `controller/admin/trade/vo/TradeOrderSummaryRespVO.java`
- `controller/admin/trade/vo/TradeOrderTrendReqVO.java`
- `controller/admin/trade/vo/TradeOrderTrendRespVO.java`

### Member and pay statistics VO

- `controller/admin/member/vo/MemberAnalyseDataRespVO.java`
- `controller/admin/member/vo/MemberAnalyseReqVO.java`
- `controller/admin/member/vo/MemberAnalyseRespVO.java`
- `controller/admin/member/vo/MemberAreaStatisticsRespVO.java`
- `controller/admin/member/vo/MemberCountRespVO.java`
- `controller/admin/member/vo/MemberRegisterCountRespVO.java`
- `controller/admin/member/vo/MemberSexStatisticsRespVO.java`
- `controller/admin/member/vo/MemberSummaryRespVO.java`
- `controller/admin/member/vo/MemberTerminalStatisticsRespVO.java`
- `controller/admin/pay/vo/PaySummaryRespVO.java`

### Persistence model

- `dal/dataobject/product/ProductStatisticsDO.java`
- `dal/dataobject/trade/TradeStatisticsDO.java`

### Mappers

- `dal/mysql/product/ProductStatisticsMapper.java`
- `dal/mysql/trade/TradeStatisticsMapper.java`
- `dal/mysql/trade/TradeOrderStatisticsMapper.java`
- `dal/mysql/trade/AfterSaleStatisticsMapper.java`
- `dal/mysql/trade/BrokerageStatisticsMapper.java`
- `dal/mysql/pay/PayWalletStatisticsMapper.java`
- `dal/mysql/member/MemberStatisticsMapper.java`
- `dal/mysql/infra/ApiAccessLogStatisticsMapper.java`

### Legacy services

- `service/product/ProductStatisticsService.java`
- `service/product/ProductStatisticsServiceImpl.java`
- `service/trade/TradeStatisticsService.java`
- `service/trade/TradeStatisticsServiceImpl.java`
- `service/trade/TradeOrderStatisticsService.java`
- `service/trade/TradeOrderStatisticsServiceImpl.java`
- `service/trade/AfterSaleStatisticsService.java`
- `service/trade/AfterSaleStatisticsServiceImpl.java`
- `service/trade/BrokerageStatisticsService.java`
- `service/trade/BrokerageStatisticsServiceImpl.java`
- `service/pay/PayWalletStatisticsService.java`
- `service/pay/PayWalletStatisticsServiceImpl.java`
- `service/member/MemberStatisticsService.java`
- `service/member/MemberStatisticsServiceImpl.java`
- `service/infra/ApiAccessLogStatisticsService.java`
- `service/infra/ApiAccessLogStatisticsServiceImpl.java`

### Service BO

- `service/trade/bo/TradeSummaryRespBO.java`
- `service/trade/bo/TradeOrderSummaryRespBO.java`
- `service/trade/bo/AfterSaleSummaryRespBO.java`
- `service/trade/bo/WalletSummaryRespBO.java`
- `service/trade/bo/MemberAreaStatisticsRespBO.java`
- `service/pay/bo/RechargeSummaryRespBO.java`
- `service/member/bo/MemberAreaStatisticsRespBO.java`

### Convert

- `convert/trade/TradeStatisticsConvert.java`
- `convert/member/MemberStatisticsConvert.java`
- `convert/pay/PayStatisticsConvert.java`

### Current DDD code

- `domain/productstatistics/ProductStatistics.java`
- `domain/productstatistics/ProductStatisticsFactory.java`
- `domain/productstatistics/valueobject/ProductStatisticsId.java`
- `domain/productstatistics/ProductStatisticsRepository.java`
- `domain/tradestatistics/TradeStatistics.java`
- `domain/tradestatistics/TradeStatisticsFactory.java`
- `domain/tradestatistics/valueobject/TradeStatisticsId.java`
- `domain/tradestatistics/TradeStatisticsRepository.java`
- `application/productstatistics/ProductStatisticsApplicationService.java`
- `application/tradestatistics/TradeStatisticsApplicationService.java`
- `infrastructure/productstatistics/ProductStatisticsRepositoryImpl.java`
- `infrastructure/tradestatistics/TradeStatisticsRepositoryImpl.java`

### Jobs and integration

- `job/product/ProductStatisticsJob.java`
- `job/trade/TradeStatisticsJob.java`
- `framework/rpc/config/RpcConfiguration.java`

### Tests

- `develop-module-mall/develop-module-statistics-server/src/test/java/com/develop/mvp/pk/module/statistics/domain/productstatistics/ProductStatisticsTest.java`
- `develop-module-mall/develop-module-statistics-server/src/test/java/com/develop/mvp/pk/module/statistics/domain/tradestatistics/TradeStatisticsTest.java`

Current tests only verify that factories allow transient ids. They do not verify daily aggregation, conversion percentage, idempotence, export contracts, or trade multi-source aggregation.

## Fixed API Contract

### Statistics API module

`develop-module-statistics-api` currently has no stable `XxxApi` contract and no Feign client. Do not invent a statistics RPC API during DDD refactoring.

### TimeRangeTypeEnum

`TimeRangeTypeEnum` exposes these integer values:

| Enum | type | Meaning |
| --- | ---: | --- |
| `DAY` | 1 | Day range |
| `WEEK` | 7 | Week range |
| `MONTH` | 30 | Month range |
| `YEAR` | 365 | Year range |

`ARRAYS` must remain derived from `values()` and used by `@InEnum` validation.

### Product statistics routes

Base route: `/statistics/product`

| Method | Path | Permission | Response |
| --- | --- | --- | --- |
| GET | `/analyse` | `statistics:product:query` | `CommonResult<DataComparisonRespVO<ProductStatisticsRespVO>>` |
| GET | `/list` | `statistics:product:query` | `CommonResult<List<ProductStatisticsRespVO>>` |
| GET | `/export-excel` | `statistics:product:export` | Excel response |
| GET | `/rank-page` | `statistics:product:query` | `CommonResult<PageResult<ProductStatisticsRespVO>>` |

Product Excel output must keep file name `商品状况.xls`, sheet name `数据`, and row class `ProductStatisticsRespVO`.

### Trade statistics routes

Base route: `/statistics/trade`

| Method | Path | Permission | Response |
| --- | --- | --- | --- |
| GET | `/summary` | `statistics:trade:query` | `CommonResult<DataComparisonRespVO<TradeSummaryRespVO>>` |
| GET | `/analyse` | `statistics:trade:query` | `CommonResult<DataComparisonRespVO<TradeTrendSummaryRespVO>>` |
| GET | `/list` | `statistics:trade:query` | `CommonResult<List<TradeTrendSummaryRespVO>>` |
| GET | `/export-excel` | `statistics:trade:export` | Excel response |
| GET | `/order-count` | `statistics:trade:query` | `CommonResult<TradeOrderCountRespVO>` |
| GET | `/order-comparison` | `statistics:trade:query` | `CommonResult<DataComparisonRespVO<TradeOrderSummaryRespVO>>` |
| GET | `/order-count-trend` | `statistics:trade:query` | `CommonResult<List<DataComparisonRespVO<TradeOrderTrendRespVO>>>` |

Trade Excel output must keep file name `交易状况.xls`, sheet name `数据`, and row class `TradeTrendSummaryExcelVO`.

## Fixed Data Model

### ProductStatisticsDO

Table: `product_statistics`

| Field | Type | Contract |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `time` | `LocalDate` | Statistics date |
| `spuId` | `Long` | Product SPU id |
| `browseCount` | `Integer` | Browse count |
| `browseUserCount` | `Integer` | Browse user count |
| `favoriteCount` | `Integer` | Favorite count |
| `cartCount` | `Integer` | Cart count |
| `orderCount` | `Integer` | Ordered item count |
| `orderPayCount` | `Integer` | Paid item count |
| `orderPayPrice` | `Integer` | Paid amount in cents |
| `afterSaleCount` | `Integer` | Refund item count |
| `afterSaleRefundPrice` | `Integer` | Refund amount in cents |
| `browseConvertPercent` | `Integer` | Visitor payment conversion percent |

### TradeStatisticsDO

Table: `trade_statistics`

| Field | Type | Contract |
| --- | --- | --- |
| `id` | `Long` | Primary key |
| `time` | `LocalDateTime` | Statistics day timestamp |
| `orderCreateCount` | `Integer` | Created order count |
| `orderPayCount` | `Integer` | Paid order item count |
| `orderPayPrice` | `Integer` | Order paid amount in cents |
| `afterSaleCount` | `Integer` | Refund order count |
| `afterSaleRefundPrice` | `Integer` | Refund amount in cents |
| `brokerageSettlementPrice` | `Integer` | Settled brokerage in cents |
| `walletPayPrice` | `Integer` | Wallet payment amount in cents |
| `rechargePayCount` | `Integer` | Recharge paid order count |
| `rechargePayPrice` | `Integer` | Recharge paid amount in cents |
| `rechargeRefundCount` | `Integer` | Recharge refund count |
| `rechargeRefundPrice` | `Integer` | Recharge refund amount in cents |

### ProductStatisticsRespVO

| Field | Type | Contract |
| --- | --- | --- |
| `id` | `Long` | Statistics row id |
| `time` | `LocalDate` | JSON format `yyyy-MM-dd`, Excel column `统计日期` |
| `spuId` | `Long` | Excel column `商品SPU编号` |
| `name` | `String` | Product name, enriched from `ProductSpuApi` on rank page |
| `picUrl` | `String` | Product cover image, enriched from `ProductSpuApi` on rank page |
| `browseCount` | `Integer` | Excel column `浏览量` |
| `browseUserCount` | `Integer` | Excel column `访客量` |
| `favoriteCount` | `Integer` | Excel column `收藏数量` |
| `cartCount` | `Integer` | Excel column `加购数量` |
| `orderCount` | `Integer` | Excel column `下单件数` |
| `orderPayCount` | `Integer` | Excel column `支付件数` |
| `orderPayPrice` | `Integer` | Excel column `支付金额，单位：分` |
| `afterSaleCount` | `Integer` | Excel column `退款件数` |
| `afterSaleRefundPrice` | `Integer` | Excel column `退款金额，单位：分` |
| `browseConvertPercent` | `Integer` | Visitor payment conversion percent |

### TradeTrendSummaryRespVO and Excel VO

| Field | RespVO Type | Excel Column | Contract |
| --- | --- | --- | --- |
| `date` | `LocalDate` | `日期` | Date derived from `TradeStatisticsDO.time.toLocalDate()` |
| `turnoverPrice` | `Integer` | `营业额` | `orderPayPrice + rechargePayPrice` |
| `orderPayPrice` | `Integer` | `商品支付金额` | Order paid amount |
| `walletPayPrice` | `Integer` | `余额支付金额` | Wallet payment amount |
| `afterSaleRefundPrice` | `Integer` | `商品退款金额` | Refund amount |
| `brokerageSettlementPrice` | `Integer` | `支付佣金金额` | Settled brokerage amount |
| `rechargePrice` | `Integer` | `充值金额` | Recharge amount field exposed by mapper/converter |
| `expensePrice` | `Integer` | `支出金额` | `walletPayPrice + brokerageSettlementPrice + afterSaleRefundPrice` |

### Trade dashboard VO and BO

| Class | Fields |
| --- | --- |
| `TradeSummaryRespVO` | `yesterdayOrderCount`, `yesterdayPayPrice`, `monthOrderCount`, `monthPayPrice` |
| `TradeOrderSummaryRespVO` | `orderPayCount`, `orderPayPrice` |
| `TradeOrderCountRespVO` | `undelivered`, `pickUp`, `afterSaleApply`, `auditingWithdraw` |
| `TradeOrderTrendReqVO` | `type`, `beginTime`, `endTime`; `type` is required and validated by `TimeRangeTypeEnum` |
| `TradeOrderTrendRespVO` | `date`, `orderPayCount`, `orderPayPrice` |
| `TradeSummaryRespBO` | `count`, `summary` |
| `TradeOrderSummaryRespBO` | `orderCreateCount`, `orderPayCount`, `orderPayPrice` |
| `AfterSaleSummaryRespBO` | `afterSaleCount`, `afterSaleRefundPrice` |
| `WalletSummaryRespBO` | `walletPayPrice`, `rechargePayCount`, `rechargePayPrice`, `rechargeRefundCount`, `rechargeRefundPrice` |

### DataComparisonRespVO

`DataComparisonRespVO<T>` contains exactly:

- `value`: current data.
- `reference`: comparison data.

Do not rename fields or invert semantics.

## Current DDD Model

### ProductStatistics current fields

Current domain fields are:

- `ProductStatisticsId id`
- `Long spuId`
- `LocalDate date`
- `Integer browseCount`
- `Integer favoriteCount`
- `Integer cartCount`
- `Integer orderCount`
- `Integer orderPayCount`
- `Integer orderPayPrice`

Missing compared to `ProductStatisticsDO` and external VO:

- `browseUserCount`
- `afterSaleCount`
- `afterSaleRefundPrice`
- `browseConvertPercent`

### TradeStatistics current fields

Current domain fields are:

- `TradeStatisticsId id`
- `LocalDate date`
- `Integer orderCount`
- `Integer orderPayCount`
- `Integer orderPayPrice`
- `Integer refundCount`
- `Integer refundPrice`
- `Integer brokerageSettlementPrice`

Missing compared to `TradeStatisticsDO` and external VO:

- `walletPayPrice`
- `rechargePayCount`
- `rechargePayPrice`
- `rechargeRefundCount`
- `rechargeRefundPrice`

Naming differences that must be mapped explicitly:

| Domain | DO |
| --- | --- |
| `orderCount` | `orderCreateCount` |
| `refundCount` | `afterSaleCount` |
| `refundPrice` | `afterSaleRefundPrice` |
| `date` | `time.toLocalDate()` or `date.atStartOfDay()` |

## Required Method Signatures and Capabilities

### Legacy product service capabilities to preserve

`ProductStatisticsService` behavior must remain equivalent to these capabilities:

```java
ProductStatisticsRespVO getProductStatisticsAnalyse(ProductStatisticsReqVO reqVO);
List<ProductStatisticsDO> getProductStatisticsList(ProductStatisticsReqVO reqVO);
PageResult<ProductStatisticsDO> getProductStatisticsRankPage(ProductStatisticsReqVO reqVO, SortablePageParam pageParam);
String statisticsProduct(Integer days);
```

Implementation requirements:

- `getProductStatisticsAnalyse` builds a previous reference range with the same duration as the requested range.
- `getProductStatisticsRankPage` applies default sorting by `ProductStatisticsDO::getBrowseCount`.
- `statisticsProduct(Integer days)` runs one day at a time for `today.minusDays(1..days)`, sorts daily result messages, and joins them with `\n`.
- `statisticsProduct(LocalDateTime date)` is idempotent and returns the existing-data message when data already exists.

### Legacy trade service capabilities to preserve

`TradeStatisticsService` behavior must remain equivalent to these capabilities:

```java
DataComparisonRespVO<TradeSummaryRespVO> getTradeSummaryComparison();
DataComparisonRespVO<TradeTrendSummaryRespVO> getTradeStatisticsAnalyse(TradeTrendReqVO reqVO);
List<TradeTrendSummaryRespVO> getTradeStatisticsList(TradeTrendReqVO reqVO);
String statisticsTrade(Integer days);
```

Implementation requirements:

- Summary comparison compares yesterday vs before yesterday and this month vs last month.
- Trend analysis compares the requested range against an immediately preceding range of the same duration.
- Daily statistics are built from order, after-sale, brokerage, and wallet summaries.
- Existing daily data returns the existing-data message instead of inserting another row.

### Legacy trade order service capabilities to preserve

```java
TradeOrderCountRespVO getOrderCount();
DataComparisonRespVO<TradeOrderSummaryRespVO> getOrderComparison();
List<DataComparisonRespVO<TradeOrderTrendRespVO>> getOrderCountTrendComparison(TradeOrderTrendReqVO reqVO);
```

Implementation requirements:

- Order count dashboard includes undelivered, pick-up, after-sale applying, and auditing withdraw counts.
- Order comparison compares today with yesterday.
- Trend comparison compares requested range with immediately preceding range.
- `TimeRangeTypeEnum.YEAR` groups by month; day/week/month group by day.

### Target ProductStatistics aggregate signatures

When upgrading DDD code, the product aggregate must be capable of representing all persisted fields:

```java
public final class ProductStatistics {
    public ProductStatisticsId id();
    public Long spuId();
    public LocalDate date();
    public Integer browseCount();
    public Integer browseUserCount();
    public Integer favoriteCount();
    public Integer cartCount();
    public Integer orderCount();
    public Integer orderPayCount();
    public Integer orderPayPrice();
    public Integer afterSaleCount();
    public Integer afterSaleRefundPrice();
    public Integer browseConvertPercent();
    public void calculateBrowseConvertPercent();
}
```

The factory must support transient creation and full reconstitution:

```java
public static ProductStatistics create(Long id, Long spuId, LocalDate date);
public static ProductStatistics reconstitute(Long id, Long spuId, LocalDate date,
        Integer browseCount, Integer browseUserCount, Integer favoriteCount, Integer cartCount,
        Integer orderCount, Integer orderPayCount, Integer orderPayPrice,
        Integer afterSaleCount, Integer afterSaleRefundPrice, Integer browseConvertPercent);
```

The repository must not lose fields during round trips:

```java
ProductStatistics save(ProductStatistics statistics);
ProductStatistics findById(ProductStatisticsId id);
ProductStatistics findBySpuIdAndDate(Long spuId, LocalDate date);
List<ProductStatistics> findByDateBetween(LocalDate start, LocalDate end);
List<ProductStatistics> findBySpuId(Long spuId);
```

If batch daily snapshot generation is migrated, add repository methods only in the same scoped change that replaces the legacy mapper path.

### Target TradeStatistics aggregate signatures

When upgrading DDD code, the trade aggregate must be capable of representing all persisted fields:

```java
public final class TradeStatistics {
    public TradeStatisticsId id();
    public LocalDate date();
    public Integer orderCreateCount();
    public Integer orderPayCount();
    public Integer orderPayPrice();
    public Integer afterSaleCount();
    public Integer afterSaleRefundPrice();
    public Integer brokerageSettlementPrice();
    public Integer walletPayPrice();
    public Integer rechargePayCount();
    public Integer rechargePayPrice();
    public Integer rechargeRefundCount();
    public Integer rechargeRefundPrice();
    public Integer turnoverPrice();
    public Integer expensePrice();
}
```

The factory must support transient creation and full reconstitution:

```java
public static TradeStatistics create(Long id, LocalDate date);
public static TradeStatistics reconstitute(Long id, LocalDate date,
        Integer orderCreateCount, Integer orderPayCount, Integer orderPayPrice,
        Integer afterSaleCount, Integer afterSaleRefundPrice, Integer brokerageSettlementPrice,
        Integer walletPayPrice, Integer rechargePayCount, Integer rechargePayPrice,
        Integer rechargeRefundCount, Integer rechargeRefundPrice);
```

The repository must not lose fields during round trips:

```java
TradeStatistics save(TradeStatistics statistics);
TradeStatistics findById(TradeStatisticsId id);
TradeStatistics findByDate(LocalDate date);
List<TradeStatistics> findByDateBetween(LocalDate start, LocalDate end);
```

## Business Rules

### Product statistics rules

1. Daily product statistics are idempotent by date. If any product statistics rows already exist for the day range, the job returns `yyyy-MM-dd 数据已存在，如果需要重新统计，请先删除对应的数据` and does not insert.
2. Daily product snapshot generation pages source aggregation with page size `100`.
3. Snapshot row `time` is set to the target `LocalDate` before insert.
4. `browseConvertPercent = 100 * orderPayCount / browseUserCount` only when `browseUserCount` is not null and not zero.
5. Product statistics list groups by `time` and aggregates numeric fields.
6. Product statistics rank page groups by `spuId` and defaults sorting to `browseCount`.
7. Rank page enriches product `name` and `picUrl` through `ProductSpuApi#getSpuList(spuIds).getCheckedData()`.
8. Product analysis compares the requested period with the immediately preceding period of the same duration.

### Trade statistics rules

1. Daily trade statistics are idempotent by date. If a row exists for the day range, the job returns `yyyy-MM-dd 数据已存在，如果需要重新统计，请先删除对应的数据` and does not insert.
2. Daily trade statistics compose four sources:
   - order summary: `orderCreateCount`, `orderPayCount`, `orderPayPrice`;
   - after-sale summary: `afterSaleCount`, `afterSaleRefundPrice`;
   - brokerage settlement: `brokerageSettlementPrice`;
   - wallet summary: `walletPayPrice`, `rechargePayCount`, `rechargePayPrice`, `rechargeRefundCount`, `rechargeRefundPrice`.
3. `turnoverPrice = orderPayPrice + rechargePayPrice`.
4. `expensePrice = walletPayPrice + brokerageSettlementPrice + afterSaleRefundPrice`.
5. Trade analysis compares the requested period with the immediately preceding period of the same duration.
6. Trade summary comparison uses yesterday vs before yesterday and this month vs last month.
7. Order comparison uses today vs yesterday.
8. Order trend comparison uses current requested range and an immediately preceding reference range.
9. Year trend groups by month; other trend types group by day.

### Member and pay statistics rules inside this module

1. Member analysis comparison uses the requested range and an immediately preceding range.
2. Member count comparison uses today vs yesterday and includes visit IP count from access logs.
3. Member area statistics adds an unknown area row with `id = null` and `name = "未知"`.
4. Pay wallet summary combines recharge paid data, recharge refund data, and wallet payment amount.
5. These rules are read-model behavior and should remain in their legacy services unless a scoped skill promotes them.

## Mapper and Conversion Contracts

### ProductStatisticsMapper

Required capabilities:

- `selectPageGroupBySpuId(ProductStatisticsReqVO reqVO, SortablePageParam pageParam)` groups by `spuId` and selects `spuId` plus aggregate expressions.
- `selectListByTimeBetween(ProductStatisticsReqVO reqVO)` groups by `time` and selects `time` plus aggregate expressions.
- Aggregate wrapper must keep sums for browse, visitor, favorite, cart, order, paid count, paid amount, after-sale count, and refund amount.
- Aggregate wrapper must keep average for `browseConvertPercent`.
- `selectStatisticsResultPageByTimeBetween(IPage<ProductStatisticsDO> page, LocalDateTime beginTime, LocalDateTime endTime)` is the source for daily product snapshots.

### TradeStatisticsMapper

Required capabilities:

- `selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(beginTime, endTime)` returns `TradeSummaryRespBO`.
- `selectVoByTimeBetween(beginTime, endTime)` returns aggregated `TradeTrendSummaryRespVO`.
- `selectExpensePriceByTimeBetween(beginTime, endTime)` returns total expense.
- `selectByTimeBetween(beginTime, endTime)` finds the existing daily row.

### TradeStatisticsConvert

Required conversions:

- `convert(TradeSummaryRespBO yesterdayData, TradeSummaryRespBO beforeYesterdayData, TradeSummaryRespBO monthData, TradeSummaryRespBO lastMonthData)` produces `DataComparisonRespVO<TradeSummaryRespVO>`.
- `convert(TradeSummaryRespBO yesterdayData, TradeSummaryRespBO monthData)` maps `count/summary` to yesterday/month order count and pay price.
- `convert(LocalDateTime time, TradeOrderSummaryRespBO orderSummary, AfterSaleSummaryRespBO afterSaleSummary, Integer brokerageSettlementPrice, WalletSummaryRespBO walletSummary)` creates `TradeStatisticsDO`.
- `convert(TradeStatisticsDO)` sets `date`, `turnoverPrice`, and `expensePrice` after MapStruct mapping.
- `convert(Long undelivered, Long pickUp, Long afterSaleApply, Long auditingWithdraw)` creates `TradeOrderCountRespVO`.

## Transaction Contract

- Daily snapshot insert operations must remain atomic per invocation path. If refactored into application services, use `@Transactional(rollbackFor = Exception.class)` on write orchestration.
- Read-only dashboard aggregation methods may remain non-transactional unless existing code already declares otherwise.
- Do not move transaction annotations into domain objects.
- Domain objects must remain pure Java and must not import Spring transaction APIs.

## Job Contract

### ProductStatisticsJob

- Annotation: `@XxlJob("productStatisticsJob")`.
- Annotation: `@TenantJob`.
- Method signature: `public String execute(String param)`.
- Blank param defaults to `"1"`.
- Param must parse as a positive integer.
- Invalid param throws `RuntimeException("商品统计任务的参数只能为是正整数")`.
- Success response wraps service result as `商品统计:\n{result}`.

### TradeStatisticsJob

- Annotation: `@XxlJob("tradeStatisticsJob")`.
- Annotation: `@TenantJob`.
- Method signature: `public String execute(String param)`.
- Blank param defaults to `"1"`.
- Param must parse as a positive integer.
- Invalid param throws `RuntimeException("交易统计任务的参数只能为是正整数")`.
- Success response wraps service result as `交易统计:\n{result}`.

## Integration Contract

### RPC scanning

`framework/rpc/config/RpcConfiguration.java` must keep:

```java
@Configuration(value = "statisticsRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {ProductSpuApi.class})
public class RpcConfiguration {
}
```

Only product SPU enrichment is currently scanned. Do not add Feign clients unless current code introduces a real cross-module API dependency.

### ProductSpuApi enrichment

Product rank page must:

1. Collect SPU ids from the ranked page.
2. Call `productSpuApi.getSpuList(spuIds).getCheckedData()`.
3. Fill `name` and `picUrl` into `ProductStatisticsRespVO`.
4. Preserve page metadata.

## Current Conflict Notes

### C01 — ProductStatistics domain loses persisted fields

Current `ProductStatistics` and its repository implementation only map a subset of `ProductStatisticsDO`. Any save/reconstitute round trip can lose `browseUserCount`, `afterSaleCount`, `afterSaleRefundPrice`, and `browseConvertPercent`. Do not route production daily snapshots through current DDD code until these fields are added and tested.

### C02 — TradeStatistics domain loses wallet and recharge fields

Current `TradeStatistics` and its repository implementation only map a subset of `TradeStatisticsDO`. Any save/reconstitute round trip can lose wallet and recharge fields required by trade dashboard VO. Do not route production daily snapshots through current DDD code until these fields are added and tested.

### C03 — ProductStatisticsApplicationService saves before incrementing

Current `ProductStatisticsApplicationService#recordMetric` saves a newly loaded/created aggregate before increment methods are called. This likely prevents increments from being persisted. Treat current application service record methods as non-production until persistence-after-mutation tests exist.

### C04 — DDD repositories use different day boundary style than legacy code

Legacy daily jobs use `beginOfDay` and `endOfDay`; current `TradeStatisticsRepositoryImpl#findByDate` uses `[date.atStartOfDay(), date.plusDays(1).atStartOfDay()]`. Do not change this behavior casually. If unifying boundaries, add tests against the mapper behavior and confirm no duplicate or missed daily rows.

### C05 — Existing tests are insufficient

Current domain tests only assert transient id support. They do not guard the production contracts in this skill.

## Acceptance Criteria

A Mall Statistics DDD refactor is acceptable only when all relevant criteria pass:

1. Product and trade admin routes, HTTP methods, permissions, request VO fields, response VO fields, and Excel file names are unchanged.
2. `TimeRangeTypeEnum` integer values remain unchanged.
3. Product daily statistics remain idempotent and preserve the existing duplicate-data message.
4. Trade daily statistics remain idempotent and preserve the existing duplicate-data message.
5. Product conversion percent calculation preserves integer percentage semantics and null/zero guard.
6. Product rank page still defaults to browse count sorting and enriches `name`/`picUrl` through `ProductSpuApi`.
7. Trade daily snapshot still aggregates order, after-sale, brokerage, and wallet sources.
8. `turnoverPrice` and `expensePrice` formulas are unchanged.
9. Trend comparison periods and grouping rules are unchanged.
10. Domain objects remain free of Spring, MyBatis, mapper, controller VO, Feign, Redis, and HTTP dependencies.
11. Repository interfaces remain in the domain package and infrastructure implementations remain under infrastructure.
12. Full DO ↔ domain round trips preserve every field listed in Fixed Data Model.
13. Jobs keep `@XxlJob`, `@TenantJob`, default param behavior, invalid param messages, and result prefixes.
14. Maven compile for the statistics server succeeds.
15. Domain tests cover more than transient id when production code is changed.

## Verification Commands

Run after changing this skill document:

```bash
git diff --check -- .claude/ddd-skills/AggregateRoot_MallStatistics_Skill.md
grep -n "^## " .claude/ddd-skills/AggregateRoot_MallStatistics_Skill.md
```

Run after changing Statistics Java code:

```bash
mvn test -pl develop-module-mall/develop-module-statistics-server -Dtest=ProductStatisticsTest,TradeStatisticsTest
mvn compile -pl develop-module-mall/develop-module-statistics-server -am -DskipTests
```

Run targeted grep checks after DDD migration:

```bash
grep -R "org.springframework\|com.baomidou\|Mapper\|DO\|FeignClient\|RestTemplate" develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics/domain
```

Expected: no forbidden domain imports. Investigate any match before claiming success.

## Common Mistakes

| Mistake | Why it breaks production behavior |
| --- | --- |
| Treating statistics as a command-heavy transactional domain | Most behavior is read-model aggregation and scheduled snapshots; forcing all dashboards into aggregates adds risk without preserving contracts. |
| Reusing current DDD repositories for production snapshots without adding missing fields | Current mappings lose persisted and VO-required fields. |
| Changing `TradeTrendSummaryRespVO.rechargePrice` mapping name casually | Excel and mapper/converter behavior rely on the current exposed field. |
| Moving ProductSpu enrichment into the domain | Domain must not call RPC APIs or depend on DTOs. |
| Replacing legacy idempotence with upsert | Current jobs return existing-data messages and do not rewrite rows. |
| Changing trend date range math | Dashboard comparisons depend on immediately preceding same-duration ranges. |
| Removing `@TenantJob` | Statistics jobs are tenant-aware. |
| Assuming tests prove business correctness | Current tests only cover transient ids. |

## Rationalization Table

| Rationalization | Required response |
| --- | --- |
| "This is just statistics, exact fields are not domain-critical." | Stop. Statistics VO and Excel fields are the product contract. Preserve every field. |
| "The current DDD model already compiles, so it is safe to use." | Stop. It compiles while missing persisted fields. Add field-preservation tests first. |
| "We can calculate turnover and expense in the frontend." | Stop. Current backend VO already calculates them. Preserve backend behavior. |
| "Upsert is better than idempotent skip." | Stop. Existing job semantics are skip-with-message; changing it is a product behavior change. |
| "Member/pay stats are in the same module, so include them in Product/Trade aggregates." | Stop. They are legacy read models unless separately scoped. |
| "Feign enrichment can live in the aggregate for convenience." | Stop. Domain cannot depend on RPC. Use application/infrastructure orchestration. |

## Red Flags

Stop the refactor immediately if any of these happens:

- A controller route, permission string, request field, response field, or Excel file name changes.
- A daily statistics job inserts duplicate rows on repeated execution.
- Product statistics rank page loses product name or cover image enrichment.
- A DDD save/reconstitute path drops any DO field listed in this skill.
- Domain code imports Spring, MyBatis, Mapper, DO, Controller VO, Feign, Redis, or RPC client classes.
- `TimeRangeTypeEnum` values change.
- Trend comparison range or year/month grouping behavior changes without explicit user approval.
- Compilation errors spread outside `develop-module-statistics` and direct dependencies.

## Rollback Conditions

Rollback the current Statistics refactor batch if:

1. `mvn compile -pl develop-module-mall/develop-module-statistics-server -am -DskipTests` fails because of this batch.
2. Existing product/trade admin API contracts change.
3. Daily job idempotence or messages change unintentionally.
4. Mapper aggregate formulas diverge from current behavior.
5. DDD repositories lose fields during persistence round trip.
6. A required dependency would force domain code to import infrastructure or RPC classes.

Rollback means revert only this batch's Statistics changes. Do not reset unrelated user changes.

## AI Self-Check

Before reporting completion, answer these checks from current code and command output:

- Did I read the current source anchors, not rely on memory?
- Did I preserve product/trade routes, permissions, VO fields, and Excel names?
- Did I preserve `TimeRangeTypeEnum` values and validation usage?
- Did I preserve product daily idempotence and conversion percent semantics?
- Did I preserve trade multi-source aggregation and derived formulas?
- Did I keep `ProductSpuApi` outside the domain?
- Did I add or update tests for any production behavior I changed?
- Did I run the verification commands in this session and read the output?
- Did I avoid changing Java business code while only upgrading this skill?
