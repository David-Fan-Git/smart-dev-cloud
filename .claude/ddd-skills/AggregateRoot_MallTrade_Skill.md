---
name: aggregate-root-mall-trade-skill
description: Use when modifying or reviewing Mall Trade cart, order, after-sale, brokerage, delivery, config, payment, or notification boundaries.
type: ddd-aggregate-skill
status: production-review
---

# AggregateRoot Mall Trade Skill

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

## 0. Overview

Mall Trade 是商城交易核心上下文，覆盖购物车、订单、售后、分销、配送和交易配置。任何重构必须保持现有 Controller/API 契约、订单和售后状态机、支付退款幂等、handler 链、价格计算链、租户 Job、日志和跨模块调用行为不变。

## 1. When to Use / Not Use

Use when:

- 重构或验证 `develop-module-mall/develop-module-trade-*`。
- 拆分 `TradeOrderApi` local/remote 契约。
- 迁移 `cart/order/aftersale/brokerage/delivery/config` 从 legacy `service/dal` 到 `domain/application/infrastructure/convert`。
- 修改交易订单、售后、购物车、分销、配送、价格计算、订单 handler、自动取消/收货/评价 Job、支付/退款回调逻辑。

Do not use when:

- 只修改商品、促销、支付、会员或系统模块内部逻辑，且不改变 trade 调用契约。
- 只调整前端页面、SQL 文案或非交易域配置。
- 准备一次性重构整个 mall；必须按 `cart`、`tradeorder`、`aftersale`、`brokerage`、`delivery/config` 分批。

## 2. Baseline Failure Findings

未升级前的草稿风险：

1. 只列聚合和值对象，没有稳定事实源路径，容易让无上下文 AI 猜字段和方法签名。
2. 把购物车、订单、售后、分销、配送全部混成一个执行计划，容易一次性大改交易链路。
3. 未明确 `TradeOrderUpdateServiceImpl` 仍是生产行为事实源，当前 DDD application 只覆盖少量状态操作。
4. 未记录 `TradeOrderHandler` 生命周期链和 `TradePriceCalculator` 价格链，迁移时会丢库存、优惠券、积分、分销、微信交易组件同步。
5. 未记录支付回调幂等、支付单金额/merchantOrderId 校验、取消前二次查支付单，容易引入并发错单。
6. 未记录售后退款回调对退款单状态、金额、merchantRefundId 的校验和零金额退款直达完成逻辑。
7. 未记录 `@TenantJob`、`@TradeOrderLog`、`@AfterSaleLog`、站内信短路、异步订阅消息等集成行为。
8. 未记录当前 DDD 边界债：infrastructure repository 构造 Controller PageReqVO、`AfterSaleRepositoryImpl` 映射丢 `payChannelCode`、DDD 未接管 Controller/API 入口。

## 3. Reproducibility Contract

执行本 skill 前必须：

1. 先读取本文件和 `.claude/ddd-skills/DDD_Skill_Production_Readiness_Standard.md`、`.claude/ddd-skills/Module_Structure_Standard.md`。
2. 先读取当前事实源文件，不凭本文的抽象描述写代码。
3. 当前可编译代码的外部行为优先于 skill 文档；发现冲突时先修 skill，不先改代码。
4. Controller 路径、HTTP 方法、VO/DTO 字段、权限注解、租户语义、错误码、日志、MQ/Job、API/RPC 契约不得在 DDD 重构中擅改。
5. 每批只处理一个小上下文：`cart`、`tradeorder`、`aftersale`、`brokerage`、`delivery/config` 或 `api local/remote`。
6. legacy `service` 是当前生产行为事实源；迁移到 application/domain 前必须逐条保留业务规则、事务边界和集成点。

## 4. Current Source Anchors

### API module

- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/api/order/TradeOrderApi.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/api/order/dto/TradeOrderRespDTO.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/ApiConstants.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/ErrorCodeConstants.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/DictTypeConstants.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/MessageTemplateConstants.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/order/TradeOrderStatusEnum.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/order/TradeOrderRefundStatusEnum.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/order/TradeOrderItemAfterSaleStatusEnum.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/order/TradeOrderCancelTypeEnum.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/aftersale/AfterSaleStatusEnum.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/aftersale/AfterSaleWayEnum.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/aftersale/AfterSaleTypeEnum.java`
- `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/brokerage/*.java`

### API implementation and RPC configuration

- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/api/order/TradeOrderApiImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

### Controllers and VO contracts

- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/app/cart/AppCartController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/app/order/AppTradeOrderController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/admin/order/TradeOrderController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/app/aftersale/AppAfterSaleController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/admin/aftersale/AfterSaleController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/app/brokerage/AppBrokerageUserController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/app/brokerage/AppBrokerageRecordController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/app/brokerage/AppBrokerageWithdrawController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/admin/brokerage/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/admin/delivery/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/app/delivery/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/admin/config/TradeConfigController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/app/config/AppTradeConfigController.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/controller/**/vo/**/*.java`

### Legacy production behavior source

- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/cart/CartServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/TradeOrderQueryServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/TradeOrderUpdateServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/aftersale/AfterSaleServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/aftersale/AfterSaleLogServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/brokerage/BrokerageUserServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/brokerage/BrokerageRecordServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/brokerage/BrokerageWithdrawServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/delivery/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/config/TradeConfigServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/message/TradeMessageServiceImpl.java`

### Price, handlers, jobs, logs

- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/price/TradePriceServiceImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/price/calculator/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeBargainOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeBrokerageOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeCombinationOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeCouponOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeMemberPointOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradePointOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeProductSkuOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeSeckillOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeStatusSyncToWxaOrderHandler.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/job/order/TradeOrderAutoCancelJob.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/job/order/TradeOrderAutoReceiveJob.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/job/order/TradeOrderAutoCommentJob.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/job/brokerage/BrokerageRecordUnfreezeJob.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/order/core/annotations/TradeOrderLog.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/aftersale/core/annotations/AfterSaleLog.java`

### Data and persistence

- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/dal/dataobject/cart/CartDO.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/dal/dataobject/order/TradeOrderDO.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/dal/dataobject/order/TradeOrderItemDO.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/dal/dataobject/aftersale/AfterSaleDO.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/dal/dataobject/brokerage/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/dal/dataobject/delivery/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/dal/dataobject/config/TradeConfigDO.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/dal/mysql/**/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/dal/redis/no/TradeNoRedisDAO.java`

### Current DDD migration source

- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/cart/Cart.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/cart/CartFactory.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/cart/repository/CartRepository.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/tradeorder/TradeOrder.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/tradeorder/TradeOrderFactory.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/tradeorder/repository/TradeOrderRepository.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/tradeorder/valueobject/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/tradeorder/event/*.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/aftersale/AfterSale.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/aftersale/AfterSaleFactory.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/domain/aftersale/repository/AfterSaleRepository.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/application/cart/CartApplicationService.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/application/tradeorder/TradeOrderApplicationService.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/application/aftersale/AfterSaleApplicationService.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/infrastructure/cart/CartRepositoryImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/infrastructure/tradeorder/TradeOrderRepositoryImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/infrastructure/aftersale/AfterSaleRepositoryImpl.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/infrastructure/SpringDomainEventPublisher.java`

### Convert and tests

- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/convert/cart/TradeCartConvert.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/convert/order/TradeOrderConvert.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/convert/aftersale/AfterSaleConvert.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/convert/brokerage/*.java`
- `develop-module-mall/develop-module-trade-server/src/test/java/com/develop/mvp/pk/module/trade/domain/cart/CartTest.java`
- `develop-module-mall/develop-module-trade-server/src/test/java/com/develop/mvp/pk/module/trade/domain/tradeorder/TradeOrderTest.java`
- `develop-module-mall/develop-module-trade-server/src/test/java/com/develop/mvp/pk/module/trade/domain/tradeorder/OrderItemTest.java`
- `develop-module-mall/develop-module-trade-server/src/test/java/com/develop/mvp/pk/module/trade/domain/aftersale/AfterSaleTest.java`
- `develop-module-mall/develop-module-trade-server/src/test/java/com/develop/mvp/pk/module/trade/application/tradeorder/TradeOrderApplicationServiceTest.java`
- `develop-module-mall/develop-module-trade-server/src/test/java/com/develop/mvp/pk/module/trade/application/aftersale/AfterSaleApplicationServiceTest.java`
- `develop-module-mall/develop-module-trade-server/src/test/resources/application-unit-test.yaml`

## 5. Fixed Data Model

### Cart

| Source | Field | Type | Meaning / Rules |
|---|---|---|---|
| `CartDO` | `id` | `Long` | 主键，插入后必须回填到 domain `CartId` |
| `CartDO` | `userId` | `Long` | 用户编号，购物车查询/修改必须按用户隔离 |
| `CartDO` | `spuId` | `Long` | 商品 SPU 编号 |
| `CartDO` | `skuId` | `Long` | 商品 SKU 编号，同一用户同一 SKU 合并数量 |
| `CartDO` | `count` | `Integer` | 购买数量，添加/更新时必须校验 SKU 库存 |
| `CartDO` | `selected` | `Boolean` | 是否选中，结算仅处理选中项 |

### TradeOrder

| Source | Field | Type | Meaning / Rules |
|---|---|---|---|
| `TradeOrderDO` | `id` | `Long` | 订单主键，支付单 merchantOrderId 使用该值字符串 |
| `TradeOrderDO` | `no` | `String` | `TradeNoRedisDAO.TRADE_ORDER_NO_PREFIX` 生成的订单流水号 |
| `TradeOrderDO` | `type` | `Integer` | `TradeOrderTypeEnum`，普通/拼团/砍价/秒杀/积分等 |
| `TradeOrderDO` | `terminal` | `Integer` | 订单来源终端 |
| `TradeOrderDO` | `userId` | `Long` | 会员用户编号 |
| `TradeOrderDO` | `userIp` | `String` | 创建订单时客户端 IP |
| `TradeOrderDO` | `userRemark` | `String` | 用户备注 |
| `TradeOrderDO` | `status` | `Integer` | `TradeOrderStatusEnum`，核心状态机字段 |
| `TradeOrderDO` | `productCount` | `Integer` | 订单商品总数 |
| `TradeOrderDO` | `finishTime/cancelTime` | `LocalDateTime` | 完成/取消时间 |
| `TradeOrderDO` | `cancelType` | `Integer` | `TradeOrderCancelTypeEnum` |
| `TradeOrderDO` | `remark` | `String` | 商家备注 |
| `TradeOrderDO` | `commentStatus` | `Boolean` | 订单是否已评价 |
| `TradeOrderDO` | `brokerageUserId` | `Long` | 推广人编号 |
| `TradeOrderDO` | `payOrderId` | `Long` | Pay 模块支付单编号 |
| `TradeOrderDO` | `payStatus` | `Boolean` | 支付状态，必须与 `status/payTime/payChannelCode` 协同更新 |
| `TradeOrderDO` | `payTime` | `LocalDateTime` | 支付时间 |
| `TradeOrderDO` | `payChannelCode` | `String` | 支付渠道编码 |
| `TradeOrderDO` | `totalPrice/discountPrice/deliveryPrice/adjustPrice/payPrice` | `Integer` | 金额字段，单位分；调价需分摊到 item 并同步 PayOrder |
| `TradeOrderDO` | `deliveryType` | `Integer` | `DeliveryTypeEnum`，快递/自提等 |
| `TradeOrderDO` | `logisticsId/logisticsNo/deliveryTime/receiveTime` | `Long/String/LocalDateTime` | 发货/收货信息 |
| `TradeOrderDO` | `receiverName/receiverMobile/receiverAreaId/receiverDetailAddress` | `String/Integer` | 收货人信息；仅待发货可修改 |
| `TradeOrderDO` | `pickUpStoreId/pickUpVerifyCode` | `Long/String` | 自提门店和核销码 |
| `TradeOrderDO` | `refundStatus/refundPrice/refundPoint` | `Integer` | 售后退款聚合状态和金额/积分累计 |
| `TradeOrderDO` | `couponId/couponPrice/usePoint/pointPrice/givePoint/vipPrice` | `Long/Integer` | 营销优惠、积分、会员价字段 |
| `TradeOrderDO` | `giveCouponTemplateCounts/giveCouponIds` | `Map/List` | 赠送优惠券模板和发放结果 |
| `TradeOrderDO` | `seckillActivityId/bargainActivityId/bargainRecordId/combinationActivityId/combinationHeadId/combinationRecordId/pointActivityId` | `Long` | 营销活动关联字段 |

### TradeOrderItem

| Source | Field | Type | Meaning / Rules |
|---|---|---|---|
| `TradeOrderItemDO` | `id/userId/orderId/cartId` | `Long` | item 主键、用户、订单、购物车来源 |
| `TradeOrderItemDO` | `spuId/spuName/skuId/properties/picUrl/count` | mixed | 商品快照字段，不能在订单后续读取商品当前值替代 |
| `TradeOrderItemDO` | `commentStatus` | `Boolean` | item 是否评价，一个 item 只能评价一次 |
| `TradeOrderItemDO` | `price/discountPrice/deliveryPrice/adjustPrice/payPrice` | `Integer` | item 金额字段，单位分；订单调价按 item 分摊 |
| `TradeOrderItemDO` | `couponPrice/pointPrice/usePoint/givePoint/vipPrice` | `Integer` | 营销分摊字段 |
| `TradeOrderItemDO` | `afterSaleId/afterSaleStatus` | `Long/Integer` | 售后单关联和 item 售后状态，申请/取消/拒绝/成功时联动 |

### AfterSale

| Source | Field | Type | Meaning / Rules |
|---|---|---|---|
| `AfterSaleDO` | `id/no/status/way/type/userId` | mixed | 售后主键、编号、状态、方式、类型、用户 |
| `AfterSaleDO` | `applyReason/applyDescription/applyPicUrls` | `String/List` | 用户申请原因、描述、凭证 |
| `AfterSaleDO` | `orderId/orderNo/orderItemId` | `Long/String` | 交易订单和订单项关联 |
| `AfterSaleDO` | `spuId/spuName/skuId/properties/picUrl/count` | mixed | 商品快照字段 |
| `AfterSaleDO` | `auditTime/auditUserId/auditReason` | mixed | 审批字段 |
| `AfterSaleDO` | `refundPrice/payRefundId/refundTime` | `Integer/Long/LocalDateTime` | 退款金额、Pay 退款单、退款完成时间 |
| `AfterSaleDO` | `logisticsId/logisticsNo/deliveryTime/receiveTime/receiveReason` | mixed | 退货物流和收货/拒收字段 |

### Brokerage, delivery, config

| Source | Required preservation |
|---|---|
| `BrokerageUserDO` | 推广资格、绑定关系、可用/冻结佣金、绑定模式和层级限制 |
| `BrokerageRecordDO` | 分佣记录、业务类型/业务编号、冻结状态、解冻时间、状态 CAS 更新 |
| `BrokerageWithdrawDO` | 提现方式、金额、状态、支付转账单、审核失败退回佣金 |
| `DeliveryExpressDO` | 快递公司编码、状态、查询客户端配置 |
| `DeliveryExpressTemplateDO` | 运费模板、计费方式、区域规则 |
| `DeliveryPickUpStoreDO` | 自提门店、核销员、状态 |
| `TradeConfigDO` | 自动取消/收货/评价、分销、配送等交易配置 |

## 6. Required Method Signatures and Capabilities

### Stable API contract

Current contract must remain stable until a separate API migration plan exists:

```java
CommonResult<List<TradeOrderRespDTO>> getOrderList(Collection<Long> ids);
CommonResult<TradeOrderRespDTO> getOrder(Long id);
CommonResult<Boolean> cancelPaidOrder(Long userId, Long orderId, Integer cancelType);
```

`TradeOrderApi` currently carries `@FeignClient`; local/remote split must produce a stable API/CommonApi plus `remote/*RemoteClient` without changing method semantics.

### TradeOrder aggregate/application capabilities

Required capabilities before replacing legacy entry paths. Signatures in this section describe behavior parity with legacy entry points; they do not mean Controller `ReqVO` types belong in the final DDD application layer.

```java
TradeOrder createOrder(... calculated price, receiver, items ...);
void updateOrderPaid(Long id, Long payOrderId);
void syncOrderPayStatusQuietly(Long id, Long payOrderId);
void deliveryOrder(TradeOrderDeliveryReqVO reqVO);
void receiveOrderByMember(Long userId, Long id);
int receiveOrderBySystem();
void cancelOrderByMember(Long userId, Long id);
int cancelOrderBySystem();
void cancelOrderByAfterSale(TradeOrderDO order, Integer refundPrice);
void updateOrderPrice(TradeOrderUpdatePriceReqVO reqVO);
void updateOrderAddress(TradeOrderUpdateAddressReqVO reqVO);
void pickUpOrder(Long userId, TradeOrderDO order);
void updateOrderItemWhenAfterSaleCreate(Long orderItemId, Long afterSaleId);
void updateOrderItemWhenAfterSaleCancel(Long orderItemId);
void updateOrderItemWhenAfterSaleSuccess(Long orderItemId, Integer refundPrice);
void createOrderItemCommentByMember(...);
int createOrderItemCommentBySystem();
```

Domain methods may use richer domain parameters, but application services must preserve all behavior above before Controller/API entry points are switched.

### Cart capabilities

```java
void addCart(Long userId, AppCartAddReqVO reqVO);
void updateCartCount(Long userId, Long id, Integer count);
void updateCartSelected(Long userId, AppCartUpdateSelectedReqVO reqVO);
void deleteCart(Long userId, Collection<Long> ids);
List<CartDO> getCartList(Long userId);
```

Must preserve same SKU merge, SKU stock check, selected filtering, and deleted SPU lazy cleanup.

### AfterSale capabilities

```java
Long createAfterSale(Long userId, AppAfterSaleCreateReqVO createReqVO);
void agreeAfterSale(Long userId, Long id);
void disagreeAfterSale(Long userId, AfterSaleDisagreeReqVO reqVO);
void deliveryAfterSale(Long userId, AppAfterSaleDeliveryReqVO reqVO);
void receiveAfterSale(Long userId, Long id);
void refuseAfterSale(Long userId, AfterSaleRefuseReqVO reqVO);
void refundAfterSale(Long userId, String userIp, Long id);
void updateAfterSaleRefunded(Long id, Long orderId, Long payRefundId);
void cancelAfterSale(Long userId, Long id);
```

### Repository capabilities

Domain repository interfaces must stay in `domain/{aggregate}/repository` and infrastructure implementations must not depend on Controller VO.

```java
TradeOrder save(TradeOrder order);
TradeOrder findById(TradeOrderId id);
TradeOrder findByNo(String no);
List<TradeOrder> findByUserId(Long userId);
PageResult<TradeOrder> findPage(... domain/application query object ...);

AfterSale save(AfterSale afterSale);
AfterSale findById(AfterSaleId id);
AfterSale findByUserIdAndId(Long userId, AfterSaleId id);
PageResult<AfterSale> findPage(... domain/application query object ...);

Cart save(Cart cart);
Cart findByUserIdAndSkuId(Long userId, Long skuId);
List<Cart> findByUserId(Long userId);
```

## 7. Business Rules

### Trade order rules

| ID | Rule | Current source | Must preserve |
|---|---|---|---|
| TR-01 | 创建订单先计算价格，再执行 `beforeOrderCreate`，再插入 order/items，最后执行 `afterOrderCreate`、删除购物车、创建 PayOrder、记录日志 | `TradeOrderUpdateServiceImpl#createOrder/afterCreateTradeOrder` | handler 顺序、购物车删除、PayOrder 创建、日志不可丢 |
| TR-02 | 支付金额为 0 的订单不创建 PayOrder | `afterCreateTradeOrder` | 积分兑换等零支付场景保留 |
| TR-03 | 支付回调幂等：订单已支付且 `payOrderId` 相同直接返回 | `updateOrderPaid` | 重复回调不得抛错 |
| TR-04 | 支付回调必须校验支付单存在、支付成功、金额等于订单 `payPrice`、`merchantOrderId == order.id` | `validatePayOrderPaid` | 错误码和参数语义保留 |
| TR-05 | 支付成功用 `updateByIdAndStatus` 从 UNPAID 转 UNDELIVERED，并设置 `payStatus/payTime/payChannelCode` | `updateOrderPaid` | CAS 更新失败抛 `ORDER_UPDATE_PAID_STATUS_NOT_UNPAID` |
| TR-06 | 支付成功后执行所有 `TradeOrderHandler#afterPayOrder` | `updateOrderPaid` | 库存、营销、分销、微信同步等后置动作不可丢 |
| TR-07 | 发货仅允许快递订单且 `refundStatus == NONE`，发货后站内信、异步订阅消息、handler 后置执行 | `deliveryOrder/validateOrderDeliverable` | 不能绕过退款状态和配送类型校验 |
| TR-08 | `TradeMessageServiceImpl#sendMessageWhenDeliveryOrder` 当前存在 `if (true) return` 短路 | `TradeMessageServiceImpl` | 这是现有行为；迁移不得悄悄启用站内信 |
| TR-09 | 会员取消和系统取消都必须二次查询 PayOrder，支付单已成功时不能取消或静默跳过 | `cancelOrderByMember/cancelOrderBySystem` | 防支付回调延迟并发错误 |
| TR-10 | 取消订单用 `updateByIdAndStatus`，成功后执行 `afterCancelOrder` | `cancelOrder0` | CAS 和 handler 不可丢 |
| TR-11 | 自动取消、自动收货、自动评价按过期时间批量查询，逐单调用 self proxy 并捕获单条异常 | `cancelOrderBySystem/receiveOrderBySystem/createOrderItemCommentBySystem` | 一个订单失败不能中断整个 Job |
| TR-12 | 调价仅未支付、仅一次、调后 `payPrice > 0`，分摊到 items，并同步 PayOrder 价格 | `updateOrderPrice` | 金额一致性和 PayOrder 同步不可丢 |
| TR-13 | 收货地址仅 UNDELIVERED 可修改 | `updateOrderAddress` | 错误码为 `ORDER_UPDATE_ADDRESS_FAIL_STATUS_NOT_DELIVERED` |
| TR-14 | 自提核销仅 PICK_UP + UNDELIVERED，且核销员必须有门店权限；拼团单必须成功 | `pickUpOrder` | 权限和拼团校验不可丢 |
| TR-15 | 订单项评价仅 COMPLETED 且 item 未评价；全部 item 评价后订单 `commentStatus=true` | `createOrderItemComment*` | 评价状态一致性不可丢 |

### Cart rules

| ID | Rule | Current source | Must preserve |
|---|---|---|---|
| CART-01 | 同一用户同一 SKU 加购时合并数量，不新建重复项 | `CartServiceImpl#addCart` | 用户+SKU 唯一语义保留 |
| CART-02 | 加购/改数量必须校验 SKU 存在和库存数量 | `CartServiceImpl#checkProductSku` | 不能只更新本地 cart |
| CART-03 | 查询购物车时清理已删除 SPU 对应购物车项 | `CartServiceImpl#deleteCartIfSpuDeleted` | 懒清理行为保留 |
| CART-04 | 结算只允许选中项参与价格计算 | `TradeOrderUpdateServiceImpl#calculatePrice` | 未选中项必须被防御性拒绝 |

### AfterSale rules

| ID | Rule | Current source | Must preserve |
|---|---|---|---|
| AS-01 | 申请售后前校验订单项存在、未申请、退款金额不超过 item `payPrice`、订单存在 | `validateOrderItemApplicable` | 错误码精确保留 |
| AS-02 | 已取消订单不可售后；未支付订单不可售后；退货退款必须已发货 | `validateOrderItemApplicable` | 状态判断保留 |
| AS-03 | 拼团订单进行中不允许售后 | `validateOrderItemApplicable` | `CombinationRecordApi` 校验保留 |
| AS-04 | 创建售后生成售后编号，设置 APPLY，写入订单快照，更新订单项售后状态，记录售后日志 | `createAfterSale` | 快照和 item 联动不可丢 |
| AS-05 | 同意售后：仅 APPLY；退款进入 WAIT_REFUND，退货退款进入 SELLER_AGREE | `agreeAfterSale` | 按 `way` 分支保留 |
| AS-06 | 拒绝售后：仅 APPLY；进入 SELLER_DISAGREE，回滚订单项售后状态 | `disagreeAfterSale` | item 回滚不可丢 |
| AS-07 | 买家退货：仅 SELLER_AGREE；校验快递公司，进入 BUYER_DELIVERY | `deliveryAfterSale` | 物流信息和日志保留 |
| AS-08 | 管理员收货：仅 BUYER_DELIVERY；进入 WAIT_REFUND | `receiveAfterSale` | 状态 CAS 保留 |
| AS-09 | 管理员拒收：仅 BUYER_DELIVERY；进入 SELLER_REFUSE，回滚订单项售后状态 | `refuseAfterSale` | item 回滚不可丢 |
| AS-10 | 退款金额为 0 时直接 COMPLETE，不创建 PayRefund | `refundAfterSale` | 积分兑换特殊场景保留 |
| AS-11 | 非零退款创建 PayRefund，售后状态保持 WAIT_REFUND，等待回调 | `refundAfterSale/createPayRefund` | 不能提前完成 |
| AS-12 | 退款回调校验退款单存在、成功/失败、金额匹配、`merchantRefundId == afterSale.id` | `validatePayRefund` | 幂等/错单保护保留 |
| AS-13 | 退款成功后售后 COMPLETE，订单项售后 SUCCESS，累计订单退款金额；全部退款时订单取消 | `updateAfterSaleRefunded` + `TradeOrderUpdateServiceImpl#updateOrderItemWhenAfterSaleSuccess` | 订单/订单项/售后一致性保留 |
| AS-14 | 用户只能在 APPLY、SELLER_AGREE、BUYER_DELIVERY 取消售后，取消后回滚订单项售后状态 | `cancelAfterSale` | 可取消状态集保留 |

### Brokerage, delivery, price rules

| ID | Rule | Current source | Must preserve |
|---|---|---|---|
| BR-01 | 两级分佣优先固定佣金，无固定值时按配置比例 | `BrokerageRecordServiceImpl#addBrokerage/calculatePrice` | 一级/二级规则保留 |
| BR-02 | 分佣冻结期间不可用，定时解冻使用状态 CAS | `BrokerageRecordServiceImpl#unfreezeRecord` | 冻结/解冻状态一致性保留 |
| BR-03 | 提现创建校验最低金额和可用余额，审核失败退回佣金 | `BrokerageWithdrawServiceImpl` | 金额返还不可丢 |
| BR-04 | 审核通过提现按方式调用 PayTransfer 或 PayWallet | `BrokerageWithdrawServiceImpl` | 远程支付/钱包契约保留 |
| PRICE-01 | 价格计算由 `TradePriceServiceImpl` 和 `List<TradePriceCalculator>` 链完成 | `service/price` | 不要把促销计算塞进 domain entity |
| PRICE-02 | price calculator 依赖商品、促销、会员地址、优惠券等外部 API | calculator classes | 外部 API 必须留在 application/service/adapter 层 |
| DEL-01 | 快递、自提、运费模板、门店状态校验必须保持原错误码 | delivery services | 配送配置不可被订单聚合内部替代 |

## 8. Error Code Contract

Use `develop-module-mall/develop-module-trade-api/src/main/java/com/develop/mvp/pk/module/trade/enums/ErrorCodeConstants.java` as source of truth.

| Scenario | ErrorCodeConstants | Throwing layer today | Preservation rule |
|---|---|---|---|
| 订单不存在 | `ORDER_NOT_FOUND` | service/application | 不得改为通用异常 |
| 订单项不存在 | `ORDER_ITEM_NOT_FOUND` | aftersale/order service | 保持触发条件 |
| item 售后状态更新 CAS 失败 | `ORDER_ITEM_UPDATE_AFTER_SALE_STATUS_FAIL` | order service | 必须使用状态条件更新 |
| 支付回调订单非未支付 | `ORDER_UPDATE_PAID_STATUS_NOT_UNPAID` | order service | CAS 失败同码 |
| 支付单编号不匹配 | `ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR` | order service | 包括已支付但 payOrderId 不同 |
| 支付单未成功 | `ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS` | order service | `PayOrderStatusEnum.isSuccess` 为准 |
| 支付金额不匹配 | `ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH` | order service | `payOrder.price == order.payPrice` |
| 发货非待发货 | `ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED` | order service | CAS 失败同码 |
| 发货退款状态非 NONE | `ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE` | order service | 发货前置校验 |
| 发货类型非快递 | `ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS` | order service | 管理端快递发货必须保留 |
| 收货非待收货 | `ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED` | order service | 会员/系统收货同语义 |
| 取消非未支付 | `ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID` | order service | 含支付单已成功延迟保护 |
| 调价已支付 | `ORDER_UPDATE_PRICE_FAIL_PAID` | order service | 不能调已支付单 |
| 重复调价 | `ORDER_UPDATE_PRICE_FAIL_ALREADY` | order service | 当前判断 `adjustPrice > 0` |
| 调价后金额非法 | `ORDER_UPDATE_PRICE_FAIL_PRICE_ERROR` | order service | `newPayPrice <= 0` |
| 删除非取消订单 | `ORDER_DELETE_FAIL_STATUS_NOT_CANCEL` | order service | 会员删除限制 |
| 自提非自提类型 | `ORDER_RECEIVE_FAIL_DELIVERY_TYPE_NOT_PICK_UP` | order service | 自提核销限制 |
| 修改地址非待发货 | `ORDER_UPDATE_ADDRESS_FAIL_STATUS_NOT_DELIVERED` | order service | 文案虽写 DELIVERED，语义是非 UNDELIVERED |
| 自提无核销权限 | `ORDER_PICK_UP_FAIL_NOT_VERIFY_USER` | order service | 门店核销员校验 |
| 自提拼团未成功 | `ORDER_PICK_UP_FAIL_COMBINATION_NOT_SUCCESS` | order service | 拼团状态校验 |
| 售后不存在 | `AFTER_SALE_NOT_FOUND` | aftersale service | 保持 |
| 售后退款金额非法 | `AFTER_SALE_CREATE_FAIL_REFUND_PRICE_ERROR` | aftersale service | `refundPrice > item.payPrice` |
| 已取消订单申请售后 | `AFTER_SALE_CREATE_FAIL_ORDER_STATUS_CANCELED` | aftersale service | 保持 |
| 未支付订单申请售后 | `AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_PAID` | aftersale service | 保持 |
| 未发货退货退款 | `AFTER_SALE_CREATE_FAIL_ORDER_STATUS_NO_DELIVERED` | aftersale service | 仅 RETURN_AND_REFUND |
| 重复申请售后 | `AFTER_SALE_CREATE_FAIL_ORDER_ITEM_APPLIED` | aftersale service | item afterSaleStatus 非 NONE |
| 审批非 APPLY | `AFTER_SALE_AUDIT_FAIL_STATUS_NOT_APPLY` | aftersale service | 同意/拒绝共用 |
| 售后状态 CAS 失败 | `AFTER_SALE_UPDATE_STATUS_FAIL` | aftersale service | `updateByIdAndStatus` 失败 |
| 买家退货状态错误 | `AFTER_SALE_DELIVERY_FAIL_STATUS_NOT_SELLER_AGREE` | aftersale service | 保持 |
| 管理员确认/拒收状态错误 | `AFTER_SALE_CONFIRM_FAIL_STATUS_NOT_BUYER_DELIVERY` | aftersale service | 保持 |
| 退款非 WAIT_REFUND | `AFTER_SALE_REFUND_FAIL_STATUS_NOT_WAIT_REFUND` | aftersale service | 发起退款和回调共用 |
| 退款单不存在 | `AFTER_SALE_REFUND_FAIL_REFUND_NOT_FOUND` | aftersale service | 保持 |
| 退款单未有结果 | `AFTER_SALE_REFUND_FAIL_REFUND_NOT_SUCCESS_OR_FAILURE` | aftersale service | 成功或失败才处理 |
| 退款金额不匹配 | `AFTER_SALE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH` | aftersale service | 保持 |
| 退款单 merchantRefundId 不匹配 | `AFTER_SALE_REFUND_FAIL_REFUND_ORDER_ID_ERROR` | aftersale service | 保持 |
| 售后取消状态错误 | `AFTER_SALE_CANCEL_FAIL_STATUS_NOT_APPLY_OR_AGREE_OR_BUYER_DELIVERY` | aftersale service | 可取消状态集固定 |
| 拼团进行中申请售后 | `AFTER_SALE_CREATE_FAIL_ORDER_STATUS_COMBINATION_IN_PROGRESS` | aftersale service | 保持 |
| 购物车项不存在 | `CARD_ITEM_NOT_FOUND` | cart service | 注意常量名当前是 `CARD_ITEM_NOT_FOUND` |
| 价格异常 | `PRICE_CALCULATE_*` | price calculators | 保持促销/配送/优惠券错误语义 |
| 快递/模板/自提错误 | `EXPRESS_*`, `EXPRESS_TEMPLATE_*`, `PICK_UP_*` | delivery services | 保持 |
| 分销错误 | `BROKERAGE_*` | brokerage services | 保持 |

## 9. Transaction Contract

| Use case | Current transaction | External calls inside/around transaction | Preservation rule |
|---|---|---|---|
| `createOrder` | `@Transactional(rollbackFor = Exception.class)` | address/product/promotion during price calc; handlers; PayOrder creation after insert | 保持 handler 顺序和 PayOrder 创建语义；不要把外部调用移入 domain |
| `updateOrderPaid` | `@Transactional(rollbackFor = Exception.class)` | PayOrder 查询、handlers afterPayOrder | 保持幂等和 CAS 更新 |
| `deliveryOrder` | `@Transactional(rollbackFor = Exception.class)` | delivery express validation, message, async social API, handlers | 保持消息短路和订阅消息行为 |
| `receiveOrderByMember` / per-order system receive | `@Transactional(rollbackFor = Exception.class)` | handlers afterReceiveOrder | Job 外层逐单捕获异常 |
| `cancelOrderByMember` / per-order system cancel | `@Transactional(rollbackFor = Exception.class)` | PayOrder 查询、handlers afterCancelOrder | 保持支付延迟保护 |
| `updateOrderPrice` | `@Transactional(rollbackFor = Exception.class)` | PayOrder price update | 保持订单/item/PayOrder 一致性 |
| `pickUpOrder` | `@Transactional(rollbackFor = Exception.class)` | pick-up store/combination validation | 保持核销权限和拼团校验 |
| `createAfterSale` | `@Transactional(rollbackFor = Exception.class)` | order query, combination query, order item update | 保持 item 售后状态联动 |
| `agree/disagree/delivery/receive/refuse/cancelAfterSale` | `@Transactional(rollbackFor = Exception.class)` | delivery express validation, order item rollback | 保持 CAS 和日志 |
| `refundAfterSale` | `@Transactional(rollbackFor = Exception.class)` | PayRefund creation | 当前注释称需事务提交后避免重复，但代码在事务内调用；迁移不得改变行为，除非单独设计并验证 |
| `updateAfterSaleRefunded` | `@Transactional(rollbackFor = Exception.class)` | PayRefund query, order item/order refund update | 保持退款结果校验 |
| `BrokerageRecordServiceImpl` writes | `@Transactional(rollbackFor = Exception.class)` | product/user/config, CAS update | 保持冻结/解冻和状态更新 |
| `BrokerageWithdrawServiceImpl` writes | `@Transactional(rollbackFor = Exception.class)` | PayTransfer/PayWallet | 保持审核失败退回佣金 |
| Job entrypoints | `@TenantJob` on job classes | calls service self proxy per item | 保持租户上下文和单条失败隔离 |

## 10. Integration Contract

### External APIs

Do not move these dependencies into domain entities:

- `MemberAddressApi` for address lookup and delivery price context.
- `ProductSkuApi`, `ProductSpuApi`, `ProductCommentApi`, `ProductCategoryApi` for SKU validation, stock, comments, categories.
- `CouponApi`, `DiscountActivityApi`, `RewardActivityApi`, `SeckillActivityApi`, `PointActivityApi`, `CombinationRecordApi`, `BargainRecordApi` for promotions and order lifecycle.
- `PayOrderApi`, `PayRefundApi`, `PayTransferApi`, `PayWalletApi` for payment, refund, withdraw, wallet.
- `SocialClientApi` for WeChat subscription message.

### Handler chain

`TradeOrderHandler` lifecycle hooks are production behavior:

- `beforeOrderCreate`
- `afterOrderCreate`
- `afterPayOrder`
- `afterCancelOrder`
- `beforeDeliveryOrder`
- `afterDeliveryOrder`
- `afterReceiveOrder`

All handlers must remain wired and ordered by Spring list semantics unless a separate migration proves ordering is stable.

### Price calculator chain

`TradePriceServiceImpl` coordinates `List<TradePriceCalculator>`:

- Delivery, coupon, discount, reward, seckill, bargain, combination, point activity, point use, point give.
- Calculators may call remote product/promotion/member APIs.
- Domain aggregate may validate calculated totals but must not directly perform remote price calculation.

### Logs, messages, and events

- `@TradeOrderLog` and `TradeOrderLogUtils` must remain around order mutations.
- `@AfterSaleLog` and `AfterSaleLogUtils` must remain around after-sale mutations.
- `TradeMessageServiceImpl#sendMessageWhenDeliveryOrder` currently returns immediately through `if (true) return`; preserve until a separate behavior change is approved.
- `sendDeliveryOrderMessage` uses `@Async` and `SocialClientApi#sendWxaSubscribeMessage(...).checkError()`.
- Current DDD `TradeOrder` emits `TradeOrderStatusChangedEvent`; `TradeOrderCreatedEvent` class exists but current create flow does not publish it through legacy entry path.
- `SpringDomainEventPublisher` delegates domain events to Spring `ApplicationEventPublisher`.

### Tenant, permission, and jobs

- Trade jobs must preserve `@TenantJob` tenant execution semantics.
- Controller permission annotations and app/admin auth context must not be removed.
- Query paths must preserve user scoping for app endpoints and admin scoping for admin endpoints.

## 11. Mapping Rules

1. Controller VO stays at controller boundary; domain and repository interfaces must not import `controller.*.vo`.
2. API DTO stays at API boundary; stable `TradeOrderRespDTO` fields must not change during DDD refactor.
3. DO stays persistence model; domain cannot import `dal.dataobject` or Mapper.
4. Convert classes may map DO/VO/DTO/domain, but must not contain business decisions, remote calls, or persistence access.
5. Infrastructure repository may use Mapper/DO and map to domain; it must not construct Controller PageReqVO. Current debt:
   - `TradeOrderRepositoryImpl.findPage` constructs `controller.admin.order.vo.TradeOrderPageReqVO`.
   - `AfterSaleRepositoryImpl.findPage` constructs `controller.admin.aftersale.vo.AfterSalePageReqVO`.
6. `AfterSaleRepositoryImpl.toDomain` currently passes `payChannelCode` as `null`; mapping must be corrected before replacing after-sale production paths.
7. Insert-and-return flows must verify MyBatis primary key backfill. If domain ID cannot be null, repository `save(create)` must not return a domain with null ID.
8. `TradeOrderItemDO` product snapshot fields must map from order creation snapshot, not current product state.

## 12. Current Conflict Notes

1. `TradeOrderApi` still has `@FeignClient` on the stable interface. API local/remote split is pending and must not change method semantics.
2. Controller/API entry paths still use legacy services, not DDD application services:
   - `AppTradeOrderController` uses `TradeOrderUpdateService` / `TradeOrderQueryService`.
   - `AppCartController` uses `CartService`.
   - `AppAfterSaleController` uses `AfterSaleService`.
   - `TradeOrderApiImpl` uses legacy order services.
3. Current DDD application services cover only a subset of production behavior and cannot replace legacy service paths yet.
4. Domain layer is mostly clean of Spring/MyBatis/DO/VO/API dependencies, but infrastructure repositories currently leak Controller VO for paging.
5. Current `TradeOrder` domain throws `IllegalStateException`; public behavior must map to `ErrorCodeConstants` at application boundary before switching entry points.
6. Current `TradeOrder.cancel(...)` forbids completed/canceled but legacy member/system cancel allows only UNPAID; do not use current domain method to replace legacy cancel without tightening semantics.
7. Current `TradeOrder.deliver(...)` does not check refundStatus or deliveryType; application must keep legacy checks.
8. Current `TradeOrder.paySuccess(...)` does not perform PayOrder existence/status/amount/merchantOrderId checks; application must keep them.
9. `refundAfterSale` source comment says refund should be initiated after transaction commit, but current code calls PayRefund inside the transaction. Preserve current behavior unless separately redesigned.
10. `TradeMessageServiceImpl` notification short-circuit is existing behavior; do not accidentally enable it.

## 13. Acceptance Criteria

### Architecture AC

- Domain classes have no Spring/MyBatis/Mapper/DO/Controller VO/remote API imports.
- Repository interfaces live in `domain/{aggregate}/repository`.
- Repository implementations live in `infrastructure/{aggregate}` and no longer construct Controller VO.
- Application services own use-case orchestration, transactions, external API ports, handler invocation, logging, tenant/job semantics.
- Controller and API implementations delegate to application/compatibility service without business decisions or Mapper/DO access.

### Behavior AC

- Trade order create/pay/delivery/receive/cancel/update price/update address/pick-up/comment behavior matches legacy service.
- AfterSale create/agree/disagree/delivery/receive/refuse/refund/refund callback/cancel behavior matches legacy service.
- Cart add/update/select/delete/list behavior matches legacy service, including SKU stock check and deleted SPU cleanup.
- Brokerage commission, freeze/unfreeze, withdraw audit/transfer/wallet behavior matches legacy service.
- Handler chain and price calculator chain execute in the same lifecycle points.
- Error codes and trigger conditions match `ErrorCodeConstants`.
- PayOrder/PayRefund idempotency and amount/merchant ID checks remain intact.
- Job tenant semantics and per-item failure isolation remain intact.

### Compile/Test AC

- API module compiles.
- Trade server compiles.
- Existing domain/application tests pass.
- Any migrated use case has regression tests covering success, illegal state, idempotency, and mapping behavior.

## 14. Verification Commands

For this skill document only:

```bash
git diff --check -- .claude/ddd-skills/AggregateRoot_MallTrade_Skill.md
grep -n "^## " .claude/ddd-skills/AggregateRoot_MallTrade_Skill.md
```

For API contract work:

```bash
mvn compile -pl develop-module-mall/develop-module-trade-api -am -DskipTests
mvn compile -pl develop-module-mall/develop-module-trade-server -am -DskipTests
```

For current DDD tests:

```bash
mvn test -pl develop-module-mall/develop-module-trade-server -Dtest=CartTest
mvn test -pl develop-module-mall/develop-module-trade-server -Dtest=TradeOrderTest
mvn test -pl develop-module-mall/develop-module-trade-server -Dtest=OrderItemTest
mvn test -pl develop-module-mall/develop-module-trade-server -Dtest=AfterSaleTest
mvn test -pl develop-module-mall/develop-module-trade-server -Dtest=TradeOrderApplicationServiceTest
mvn test -pl develop-module-mall/develop-module-trade-server -Dtest=AfterSaleApplicationServiceTest
```

For future migrated behavior, add or run tests covering at least:

```bash
mvn test -pl develop-module-mall/develop-module-trade-server -Dtest=*TradeOrder*Test,*AfterSale*Test,*Cart*Test,*Brokerage*Test
```

Required regression names when implementing migration:

- `createOrder_keepsHandlerChainAndCreatesPayOrder`
- `updateOrderPaid_duplicateSamePayOrderId_returnsWithoutError`
- `updateOrderPaid_payPriceMismatch_throwsOrderUpdatePaidFailPayPriceNotMatch`
- `cancelOrderByMember_paidPayOrderDelayed_throwsOrderCancelFailStatusNotUnpaid`
- `deliveryOrder_refundStatusNotNone_throwsOrderDeliveryFailRefundStatusNotNone`
- `updateOrderPrice_distributesAdjustPriceAndUpdatesPayOrder`
- `createAfterSale_combinationInProgress_throwsAfterSaleCreateFailOrderStatusCombinationInProgress`
- `refundAfterSale_zeroRefundPrice_completesWithoutPayRefund`
- `updateAfterSaleRefunded_merchantRefundIdMismatch_throwsAfterSaleRefundFailRefundOrderIdError`
- `repositoryFindPage_doesNotDependOnControllerPageReqVO`

## 15. Quick Reference

| Task | Correct location | Forbidden location |
|---|---|---|
| HTTP params/auth/response | `controller` | domain/repository |
| Stable cross-module contract | `trade-api` | server service/domain |
| Feign client identity | `api/.../remote` after split | stable `TradeOrderApi/CommonApi` |
| Use-case transaction and remote calls | `application` or legacy service during migration | domain entity |
| Order status invariant | domain + application error mapping | controller |
| PayOrder/PayRefund validation | application/service external adapter | domain entity |
| Price calculation chain | application/service/domain service with adapters | entity constructor |
| Mapper/DO persistence | infrastructure/dal | domain/application API contract |
| DO/Domain/VO/DTO mapping | `convert` or infrastructure mapper method | domain business method |
| Job trigger | `job` delegates to application/service | duplicated job business logic |

## 16. Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| Replace legacy order service with current DDD application directly | Loses handler chain, PayOrder checks, logs, price, notifications | First close coverage gaps, then switch entry point |
| Move remote APIs into domain | Domain depends on infrastructure and becomes untestable | Define application ports/adapters |
| Remove `updateByIdAndStatus` CAS updates | Race conditions in payment/refund/order status | Keep status-conditional updates |
| Treat repeated payment callback as error | Payment callback retry breaks | Same `payOrderId` paid order returns silently |
| Ignore PayOrder check during cancel | Paid orders may be canceled during callback delay | Keep second PayOrder query |
| Enable `TradeMessageServiceImpl` station message accidentally | Production behavior changes | Preserve short-circuit unless planned |
| Build page queries with Controller VO in repository | Cross-layer dependency remains | Introduce application query object |
| Forget item after-sale status rollback | User cannot reapply after rejected/canceled after-sale | Keep item rollback calls |
| Change `CARD_ITEM_NOT_FOUND` spelling | Compile/API contract break | Keep existing constant name unless separate cleanup |

## 17. Rationalization Table

| Excuse | Reality |
|---|---|
| “DDD domain already exists, we can wire it in.” | Current DDD covers only a subset and lacks critical legacy checks. |
| “Handler chain is technical, can skip during domain refactor.” | Handlers mutate stock, coupon, points, brokerage and external sync behavior. |
| “Payment callback only needs status update.” | It needs idempotency, PayOrder status, amount and merchant order validation. |
| “Refund just completes after-sale.” | It must validate PayRefund and update order item/order refund state. |
| “Repository can reuse PageReqVO temporarily.” | Production target forbids infrastructure depending on Controller VO; document as debt if not fixed in the batch. |
| “Notifications are non-core.” | Current short-circuit and async subscription behavior are observable integration behavior. |
| “All mall trade subdomains can be migrated together.” | Trade order and after-sale are high-risk money/state flows; migrate one use case at a time. |

## 18. Red Flags

Stop the batch if any of these occur:

- A change modifies Controller path, HTTP method, VO/DTO field, `TradeOrderApi` semantics, permission, or tenant behavior without a separate migration plan.
- Domain imports Spring, MyBatis, Mapper, DO, Controller VO, Feign API, or Redis DAO.
- Order payment/refund/cancel code loses `updateByIdAndStatus` or PayOrder/PayRefund cross-checks.
- Handler chain or price calculator chain is skipped.
- `@TradeOrderLog`, `@AfterSaleLog`, `@TenantJob`, self-proxy per-order Job execution, or async subscription message behavior disappears.
- A repository returns a domain object with null ID after create.
- A migration changes current `TradeMessageServiceImpl` station-message short-circuit as a side effect.
- Tests only compile but do not cover payment callback, cancel delay, after-sale refund, or mapping debt for the migrated path.

## 19. Rollback Conditions

Rollback or stop and repair if:

1. `develop-module-trade-api` or `develop-module-trade-server` fails to compile after the batch.
2. Any existing trade domain/application test fails and the failure is not an intentional test update with matching behavior preservation.
3. Payment callback idempotency or PayOrder validation changes.
4. AfterSale refund callback validation or order item/order refund linkage changes.
5. Cart stock validation or deleted SPU cleanup changes.
6. Handler chain, price calculator chain, order/after-sale logs, or jobs stop executing.
7. API/Controller contracts, error codes, permissions, tenant semantics, or response fields change unexpectedly.

## 20. AI Self-Check

Before claiming a Mall Trade refactor is complete, verify:

- [ ] I read current source anchors for the touched use case.
- [ ] I preserved Controller/API external contracts.
- [ ] I preserved exact `ErrorCodeConstants` trigger conditions for changed flows.
- [ ] I did not move remote API calls into domain.
- [ ] I preserved transaction boundaries and CAS updates.
- [ ] I preserved handler chain and price calculator chain lifecycle points.
- [ ] I preserved `@TradeOrderLog`, `@AfterSaleLog`, `@TenantJob`, and message behavior.
- [ ] I fixed or explicitly documented any repository Controller VO dependency touched in this batch.
- [ ] I ran the relevant Maven compile/test commands fresh.
- [ ] If current code conflicts with this skill, I updated the skill before changing code.
