---
name: aggregate-root-pay-skill
description: Use when modifying or reviewing Pay app, channel, order, refund, transfer, wallet, recharge, notify, callback, or money-flow DDD boundaries.
type: ddd-aggregate-skill
status: production-review
---

# AggregateRoot Pay Skill

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

This skill is the production refactoring contract for the Pay module. It covers PayApp, PayChannel, PayOrder, PayOrderExtension, PayRefund, PayTransfer, PayWallet, PayWalletRecharge, PayWalletRechargePackage, PayWalletTransaction, and PayNotify.

Pay is a high-risk money-flow module. Existing external behavior, callback idempotency, tenant context, optimistic locks, wallet locks, third-party client behavior, notification tasks, and API contracts must be preserved before any DDD cleanup is considered complete.

## 1. When to Use / Not Use

Use this skill when changing:

- Pay API contracts, DTOs, Feign/local/remote adapters, or RPC configuration.
- Admin/app Pay controllers or VO mappings.
- `service/*/*ServiceImpl.java` money-flow behavior.
- `domain/application/infrastructure/convert` Pay DDD code.
- `dal/mysql`, `dal/redis`, optimistic-lock updates, order number generation, wallet locks.
- Pay jobs, notify tasks, callback endpoints, third-party pay clients, or wallet recharge flows.

Do not use this skill as a license to rewrite all Pay aggregates in one batch. Each implementation batch must target one aggregate or one small flow, such as PayOrder submit/notify, PayRefund notify, PayTransfer notify, PayWallet balance, or Pay API local/remote split.

## 2. Baseline Failure Findings

The previous Pay skill was a broad draft. A clean agent using it could incorrectly:

- Refactor all 9+ Pay concepts at once, mixing API, Controller, Service, Domain, Job, Redis, and third-party client changes.
- Drop the intentionally non-transactional payment submit remote-call behavior.
- Lose `TenantUtils.execute(...)`, `@TenantJob`, or callback `@TenantIgnore` semantics.
- Replace mapper `updateByIdAndStatus(...)` optimistic locks with plain updates.
- Break wallet Redis locking and allow concurrent balance corruption.
- Move third-party pay SDK calls into domain entities.
- Treat PayNotify as a simple event and lose DB-backed retry/lock/log behavior.
- Change Feign/API DTOs, Controller VOs, error codes, or notification callback payloads without migration.
- Ignore current DDD debt: infrastructure repositories construct Controller page VOs.

## 3. Reproducibility Contract

Before writing Pay code:

1. Read this skill and `.claude/ddd-skills/Module_Structure_Standard.md`.
2. Read the current source anchors for the specific flow being changed.
3. If this skill conflicts with current code, current compilable external behavior wins; update this skill before code.
4. Keep each batch limited to one API contract set, one aggregate, or one flow.
5. Do not change Controller/API/DTO/error/tenant/transaction/notify behavior unless the user approves a separate migration.
6. Run fresh compile/tests matching the impact area before claiming completion.

## 4. Current Source Anchors

### 4.1 API contracts and DTOs

- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/order/PayOrderApi.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/order/dto/PayOrderCreateReqDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/order/dto/PayOrderRespDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/refund/PayRefundApi.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/refund/dto/PayRefundCreateReqDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/refund/dto/PayRefundRespDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/transfer/PayTransferApi.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/transfer/dto/PayTransferCreateReqDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/transfer/dto/PayTransferCreateRespDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/transfer/dto/PayTransferRespDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/wallet/PayWalletApi.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/wallet/dto/PayWalletAddBalanceReqDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/wallet/dto/PayWalletRespDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/notify/dto/PayOrderNotifyReqDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/notify/dto/PayRefundNotifyReqDTO.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/api/notify/dto/PayTransferNotifyReqDTO.java`

Current API split debt: Pay API contracts still put `@FeignClient` on stable interfaces. Production API refactor must split into stable contract plus `remote/*RemoteClient` and local adapter without changing method signatures or DTOs.

### 4.2 Error and enum anchors

- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/enums/ErrorCodeConstants.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/enums/PayChannelEnum.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/enums/notify/PayNotifyStatusEnum.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/enums/notify/PayNotifyTypeEnum.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/enums/order/PayOrderStatusEnum.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/enums/refund/PayRefundStatusEnum.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/enums/transfer/PayTransferStatusEnum.java`
- `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/enums/wallet/PayWalletBizTypeEnum.java`

### 4.3 Controllers and VO anchors

- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/app/PayAppController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/channel/PayChannelController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/order/PayOrderController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/refund/PayRefundController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/transfer/PayTransferController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/wallet/PayWalletController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/wallet/PayWalletRechargeController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/wallet/PayWalletRechargePackageController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/wallet/PayWalletTransactionController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/notify/PayNotifyController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/app/order/AppPayOrderController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/app/transfer/AppPayTransferController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/app/wallet/AppPayWalletController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/app/wallet/AppPayWalletRechargeController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/app/wallet/AppPayWalletRechargePackageController.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/app/wallet/AppPayWalletTransactionController.java`

Important VO directories:

- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/order/vo/`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/refund/vo/`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/transfer/vo/`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/admin/wallet/vo/`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/controller/app/wallet/vo/`

Do not change paths, HTTP methods, VO fields, permissions, tenant annotations, export fields, or callback payloads during DDD refactoring.

### 4.4 Legacy behavior source

- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/app/PayAppServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/channel/PayChannelServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/order/PayOrderServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/refund/PayRefundServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/transfer/PayTransferServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/wallet/PayWalletServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/wallet/PayWalletRechargeServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/wallet/PayWalletRechargePackageServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/wallet/PayWalletTransactionServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/notify/PayNotifyServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/demo/PayDemoOrderServiceImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/service/demo/PayDemoWithdrawServiceImpl.java`

These files remain authoritative for behavior until DDD paths prove equivalent.

### 4.5 Current DDD source

- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/domain/app/PayApp.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/domain/channel/PayChannel.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/domain/order/PayOrder.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/domain/refund/PayRefund.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/domain/transfer/PayTransfer.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/domain/wallet/PayWallet.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/domain/wallet/PayWalletRecharge.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/domain/wallet/PayWalletRechargePackage.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/domain/wallet/PayWalletTransaction.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/application/order/PayOrderApplicationService.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/application/refund/PayRefundApplicationService.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/application/transfer/PayTransferApplicationService.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/application/wallet/PayWalletApplicationService.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/infrastructure/order/PayOrderRepositoryImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/infrastructure/refund/PayRefundRepositoryImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/infrastructure/transfer/PayTransferRepositoryImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/infrastructure/wallet/PayWalletRepositoryImpl.java`

Current DDD code is a migration draft when it conflicts with service behavior.

### 4.6 Persistence, Redis, and mapping anchors

DOs:

- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/app/PayAppDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/channel/PayChannelDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/order/PayOrderDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/order/PayOrderExtensionDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/refund/PayRefundDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/transfer/PayTransferDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/wallet/PayWalletDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/wallet/PayWalletRechargeDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/wallet/PayWalletRechargePackageDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/wallet/PayWalletTransactionDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/notify/PayNotifyTaskDO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/dataobject/notify/PayNotifyLogDO.java`

Mappers:

- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/order/PayOrderMapper.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/order/PayOrderExtensionMapper.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/refund/PayRefundMapper.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/transfer/PayTransferMapper.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/wallet/PayWalletMapper.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/wallet/PayWalletRechargeMapper.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/wallet/PayWalletRechargePackageMapper.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/wallet/PayWalletTransactionMapper.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/notify/PayNotifyTaskMapper.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/mysql/notify/PayNotifyLogMapper.java`

Redis:

- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/redis/RedisKeyConstants.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/redis/no/PayNoRedisDAO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/redis/notify/PayNotifyLockRedisDAO.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/dal/redis/wallet/PayWalletLockRedisDAO.java`

Converters:

- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/convert/app/PayAppConvert.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/convert/channel/PayChannelConvert.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/convert/order/PayOrderConvert.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/convert/refund/PayRefundConvert.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/convert/wallet/PayWalletConvert.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/convert/wallet/PayWalletRechargeConvert.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/convert/wallet/PayWalletRechargePackageConvert.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/convert/wallet/PayWalletTransactionConvert.java`

### 4.7 Third-party clients, jobs, and notify anchors

- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/pay/core/client/PayClient.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/pay/core/client/PayClientFactory.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/pay/core/client/impl/PayClientFactoryImpl.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/pay/core/client/impl/alipay/`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/pay/core/client/impl/weixin/`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/pay/core/client/impl/wallet/WalletPayClient.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/pay/core/client/impl/mock/MockPayClient.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/job/order/PayOrderSyncJob.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/job/order/PayOrderExpireJob.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/job/refund/PayRefundSyncJob.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/job/transfer/PayTransferSyncJob.java`
- `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/job/notify/PayNotifyJob.java`

### 4.8 Tests

- `develop-module-pay/develop-module-pay-server/src/test/java/com/develop/mvp/pk/module/pay/service/app/PayAppServiceTest.java`
- `develop-module-pay/develop-module-pay-server/src/test/java/com/develop/mvp/pk/module/pay/service/channel/PayChannelServiceTest.java`
- `develop-module-pay/develop-module-pay-server/src/test/java/com/develop/mvp/pk/module/pay/service/order/PayOrderServiceTest.java`
- `develop-module-pay/develop-module-pay-server/src/test/java/com/develop/mvp/pk/module/pay/service/refund/PayRefundServiceTest.java`
- `develop-module-pay/develop-module-pay-server/src/test/java/com/develop/mvp/pk/module/pay/service/notify/PayNotifyServiceTest.java`
- `develop-module-pay/develop-module-pay-server/src/test/java/com/develop/mvp/pk/module/pay/application/transfer/PayTransferApplicationServiceTest.java`
- `develop-module-pay/develop-module-pay-server/src/test/java/com/develop/mvp/pk/module/pay/application/wallet/PayWalletApplicationServiceTest.java`
- `develop-module-pay/develop-module-pay-server/src/test/java/com/develop/mvp/pk/module/pay/domain/wallet/PayWalletTransactionTest.java`
- `develop-module-pay/develop-module-pay-server/src/test/java/com/develop/mvp/pk/module/pay/framework/pay/core/client/impl/PayClientFactoryImplIntegrationTest.java`

## 5. Fixed Data Model

The DO/API/VO shape is the fixed external and persistence contract. Domain models may be richer, but mapping must preserve these fields.

| Concept | Persistence source | Must preserve |
| --- | --- | --- |
| PayApp | `PayAppDO` | `id`, `name`, `appKey`, `status`, `remark`, audit fields, tenant/delete metadata if present |
| PayChannel | `PayChannelDO` | `id`, `code`, `status`, `remark`, `feeRate`, `appId`, config JSON/object fields, tenant/delete metadata |
| PayOrder | `PayOrderDO` | `id`, `no`, `appId`, `channelId`, `channelCode`, `merchantOrderId`, `subject`, `body`, `notifyUrl`, `price`, `channelFeeRate`, `channelFeePrice`, `status`, `userIp`, `expireTime`, `successTime`, `extensionId`, `channelOrderNo`, `refundPrice`, audit/tenant metadata |
| PayOrderExtension | `PayOrderExtensionDO` | `id`, `no`, `orderId`, `channelId`, `channelCode`, `userIp`, `status`, `channelExtras`, `channelErrorCode`, `channelErrorMsg`, `channelNotifyData`, audit fields |
| PayRefund | `PayRefundDO` | `id`, `no`, `appId`, `channelId`, `channelCode`, `orderId`, `orderNo`, `merchantOrderId`, `merchantRefundId`, `notifyUrl`, `status`, `payPrice`, `refundPrice`, `reason`, `userIp`, `channelOrderNo`, `channelRefundNo`, `successTime`, `channelErrorCode`, `channelErrorMsg`, audit/tenant metadata |
| PayTransfer | `PayTransferDO` | `id`, `no`, `appId`, `channelId`, `channelCode`, `merchantTransferId`, `type`, `price`, `subject`, `userName`, `userAccount`, `status`, `channelTransferNo`, `successTime`, `channelErrorCode`, `channelErrorMsg`, audit/tenant metadata |
| PayWallet | `PayWalletDO` | `id`, `userId`, `userType`, `balance`, `freezePrice`, audit fields |
| PayWalletRecharge | `PayWalletRechargeDO` | `id`, `walletId`, `totalPrice`, `payPrice`, `bonusPrice`, `packageId`, `payOrderId`, `payStatus`, `payTime`, `refundStatus`, `refundPayRefundId`, audit fields |
| PayWalletRechargePackage | `PayWalletRechargePackageDO` | `id`, `name`, `payPrice`, `bonusPrice`, `status`, audit fields |
| PayWalletTransaction | `PayWalletTransactionDO` | `id`, `no`, `walletId`, `bizId`, `bizType`, `title`, `price`, `balance`, audit fields |
| PayNotifyTask | `PayNotifyTaskDO` | type/dataId/notifyUrl/status/retry fields/tenantId required for durable callback retry |
| PayNotifyLog | `PayNotifyLogDO` | taskId/notify result/request/response fields required for audit and retry diagnostics |

Do not drop Excel VO fields, API DTO fields, callback DTO fields, or database metadata during mapping.

## 6. Required Method Signatures and Capabilities

Exact method names may differ, but every capability must remain available.

### 6.1 API capability contract

- Create and query PayOrder through `PayOrderApi` with unchanged DTOs.
- Create and query PayRefund through `PayRefundApi` with unchanged DTOs.
- Create/query PayTransfer through `PayTransferApi` with unchanged DTOs.
- Query/add wallet balance through `PayWalletApi` with unchanged DTOs.

During API local/remote split:

- Stable contract keeps current method signatures.
- Feign annotation moves to `remote/*RemoteClient`.
- Server-local implementation implements the stable contract and delegates to local service/application behavior.

### 6.2 Domain/application capability contract

PayOrder:

- create order with merchant uniqueness validation.
- submit order without wrapping remote channel call in a rollback transaction.
- create PayOrderExtension before remote call.
- handle immediate paid response and async callback idempotently.
- close expired orders and extensions with optimistic status guard.
- update refund price only for SUCCESS/REFUND states and only up to original price.

PayRefund:

- create refund with order/app/channel validation.
- reject over-refund, duplicate merchant refund ID, and concurrent WAITING refund.
- handle SUCCESS/FAILURE callbacks through optimistic status guard.
- create notify task on terminal result.

PayTransfer:

- create transfer with duplicate retry rules.
- allow retry only when previous transfer is CLOSED and price/channel match.
- handle SUCCESS/CLOSED/PROCESSING callbacks through expected-status updates.
- create notify task for terminal/processing behavior matching legacy code.

PayWallet:

- get-or-create wallet under Redis double-check lock semantics.
- add/deduct/freeze/unfreeze balance with sufficient balance checks.
- create immutable transaction record for every balance change.
- keep wallet transaction number prefix and Redis generation behavior.

PayNotify:

- create durable notify task.
- retry according to existing frequency and status rules.
- use Redis notify lock and notify logs.
- preserve tenant header injection.

## 7. Business Rules

### 7.1 PayApp and PayChannel

- `appKey` is globally unique.
- Disabled app/channel cannot be used for order/refund/transfer flows.
- App with existing order/refund cannot be deleted.
- Channel code is unique under the same app.
- Channel config parsing depends on channel code and must call the config object's validation behavior.
- Channel fee rate is used when computing order channel fee price.

### 7.2 PayOrder

- Merchant order ID must be unique within one app.
- Submit rejects missing, already successful, non-WAITING, or expired orders.
- Submit creates an extension order before invoking the third-party pay client.
- Submit is intentionally non-transactional around remote call; do not add rollback semantics that remove the extension after a channel call.
- Existing paid extension prevents another submit.
- Payment success is idempotent: already-successful order returns without corrupting state.
- `PayOrderExtensionDO` and `PayOrderDO` updates use `updateByIdAndStatus(...)` optimistic locks.
- Channel fee price is calculated from order price and channel fee rate using existing utility behavior.
- Sync jobs must not close third-party-closed orders when legacy behavior waits for callback.
- Expire jobs must close only after confirming no extension is paid.

### 7.3 PayRefund

- Refund allowed only when related order is SUCCESS or REFUND.
- Refund amount plus already-refunded amount cannot exceed order price.
- Same order cannot have a concurrent WAITING refund.
- Merchant refund ID must be unique under app/order semantics matching legacy service.
- Refund client exceptions during create are swallowed as legacy behavior because remote refund might have succeeded.
- Refund notify is tenant-aware and transactional.
- Refund success/failure uses optimistic status guard and creates notify tasks.

### 7.4 PayTransfer

- Duplicate transfer is allowed only when previous transfer is CLOSED.
- Retry transfer must keep original price and channel code.
- WAITING can move to PROCESSING.
- WAITING or PROCESSING can move to SUCCESS or CLOSED.
- Transfer status updates use expected-status optimistic locks.
- Notify processing executes under the channel tenant context.

### 7.5 PayWallet and recharge

- Wallet is unique per `userId + userType` under current mapper/database semantics.
- Wallet get-or-create uses Redis double-check lock behavior.
- Balance cannot go negative.
- Freeze amount cannot exceed available balance.
- Unfreeze cannot exceed frozen amount.
- Every balance mutation creates a wallet transaction record.
- Recharge creates a pay order and later validates paid order ID, paid status, amount, and merchant-order relationship.
- Recharge refund must freeze wallet balance before creating refund.
- Recharge refund success deducts balance; refund failure unfreezes.
- Recharge package name is unique and disabled package cannot be used.

### 7.6 PayNotify and jobs

- PayNotify is a durable DB-backed retry pipeline, not a disposable in-memory event.
- Notify task creation, retries, logs, Redis locks, and tenant headers must remain equivalent.
- Jobs use existing XXL-Job names and `@TenantJob` behavior.
- Callback endpoints that currently ignore tenant must continue doing so.

## 8. Error Code Contract

Error constants live in `develop-module-pay/develop-module-pay-api/src/main/java/com/develop/mvp/pk/module/pay/enums/ErrorCodeConstants.java`. Do not replace them with raw Java exceptions at Controller/API boundaries.

| Scenario | Required constant | Parameters |
| --- | --- | --- |
| app missing | `APP_NOT_FOUND` | none |
| app disabled | `APP_IS_DISABLE` | none |
| app with orders cannot delete | `APP_EXIST_ORDER_CANT_DELETE` | none |
| app with refunds cannot delete | `APP_EXIST_REFUND_CANT_DELETE` | none |
| duplicate app key | `APP_KEY_EXISTS` | none |
| channel missing | `CHANNEL_NOT_FOUND` | none |
| channel disabled | `CHANNEL_IS_DISABLE` | none |
| duplicate channel under app | `CHANNEL_EXIST_SAME_CHANNEL_ERROR` | none |
| order missing | `PAY_ORDER_NOT_FOUND` | none |
| order not WAITING | `PAY_ORDER_STATUS_IS_NOT_WAITING` | none |
| order already paid | `PAY_ORDER_STATUS_IS_SUCCESS` | none |
| order expired | `PAY_ORDER_IS_EXPIRED` | none |
| submit channel error | `PAY_ORDER_SUBMIT_CHANNEL_ERROR` | channel error code, channel error message |
| order not refundable | `PAY_ORDER_REFUND_FAIL_STATUS_ERROR` | none |
| extension missing | `PAY_ORDER_EXTENSION_NOT_FOUND` | none |
| extension not WAITING | `PAY_ORDER_EXTENSION_STATUS_IS_NOT_WAITING` | none |
| extension already paid | `PAY_ORDER_EXTENSION_IS_PAID` | none |
| refund price exceed | `REFUND_PRICE_EXCEED` | none |
| refund already processing | `REFUND_HAS_REFUNDING` | none |
| refund duplicate | `REFUND_EXISTS` | none |
| refund missing | `REFUND_NOT_FOUND` | none |
| refund not WAITING | `REFUND_STATUS_IS_NOT_WAITING` | none |
| wallet missing | `WALLET_NOT_FOUND` | none |
| wallet balance insufficient | `WALLET_BALANCE_NOT_ENOUGH` | none |
| wallet transaction missing | `WALLET_TRANSACTION_NOT_FOUND` | none |
| duplicate wallet refund | `WALLET_REFUND_EXIST` | none |
| frozen balance insufficient | `WALLET_FREEZE_PRICE_NOT_ENOUGH` | none |
| recharge missing | `WALLET_RECHARGE_NOT_FOUND` | none |
| recharge paid status invalid | `WALLET_RECHARGE_UPDATE_PAID_STATUS_NOT_UNPAID` | none |
| recharge pay order id mismatch | `WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR` | none |
| recharge pay order not success | `WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_STATUS_NOT_SUCCESS` | none |
| recharge pay price mismatch | `WALLET_RECHARGE_UPDATE_PAID_PAY_PRICE_NOT_MATCH` | none |
| recharge refund not paid | `WALLET_RECHARGE_REFUND_FAIL_NOT_PAID` | none |
| recharge already refunded | `WALLET_RECHARGE_REFUND_FAIL_REFUNDED` | none |
| recharge refund balance insufficient | `WALLET_RECHARGE_REFUND_BALANCE_NOT_ENOUGH` | none |
| recharge refund order id mismatch | `WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR` | none |
| recharge refund not found | `WALLET_RECHARGE_REFUND_FAIL_REFUND_NOT_FOUND` | none |
| recharge refund price mismatch | `WALLET_RECHARGE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH` | none |
| recharge package missing | `WALLET_RECHARGE_PACKAGE_NOT_FOUND` | none |
| recharge package disabled | `WALLET_RECHARGE_PACKAGE_IS_DISABLE` | none |
| recharge package duplicate name | `WALLET_RECHARGE_PACKAGE_NAME_EXISTS` | none |
| transfer missing | `PAY_TRANSFER_NOT_FOUND` | none |
| transfer retry channel mismatch | `PAY_TRANSFER_CREATE_CHANNEL_NOT_MATCH` | none |
| transfer retry price mismatch | `PAY_TRANSFER_CREATE_PRICE_NOT_MATCH` | none |
| duplicate transfer not closed | `PAY_TRANSFER_CREATE_FAIL_STATUS_NOT_CLOSED` | none |
| transfer notify not WAITING | `PAY_TRANSFER_NOTIFY_FAIL_STATUS_IS_NOT_WAITING` | none |
| transfer notify not WAITING/PROCESSING | `PAY_TRANSFER_NOTIFY_FAIL_STATUS_NOT_WAITING_OR_PROCESSING` | none |

Demo order/withdraw constants are part of demo flow compatibility and must be preserved if demo services are touched.

## 9. Transaction Contract

Preserve transaction intent, not just annotations.

- `PayOrderServiceImpl#submitOrder` is intentionally not wrapped in a rollback transaction around third-party submit.
- Pay order notify/update success/close/refund operations that mutate DB state use `@Transactional(rollbackFor = Exception.class)` or equivalent application boundary.
- Pay refund notify success/failure uses transactional status update plus notify task creation.
- Pay transfer notify success/closed/processing uses transactional status update plus notify task creation where legacy code does.
- Wallet get/create and balance mutations are transactional and lock-sensitive.
- Wallet recharge create/paid/refund/refund-result flows are transactional.
- PayNotify task creation and notify execution logging are transactional where legacy code is transactional.
- Do not put transaction annotations in domain classes.
- Do not convert `updateByIdAndStatus` guarded updates into unguarded save/update.

## 10. Integration Contract

Preserve these integrations:

- `PayClientFactory` and `PayClient` for third-party pay/refund/transfer/sync calls.
- Alipay, Weixin, Wallet, and Mock client adapters under `framework/pay/core/client/impl/**`.
- `PayNoRedisDAO` for order/refund/transfer/wallet transaction numbers.
- `PayWalletLockRedisDAO` for wallet concurrency control.
- `PayNotifyLockRedisDAO` for notify retry locking.
- `TenantUtils.execute(...)` for callback processing under channel tenant.
- `TenantUtils.addTenantHeader(...)` for outbound notify requests.
- `@TenantJob` and existing XXL job names for sync/expire/notify jobs.
- Callback `@TenantIgnore` semantics where currently present.
- Notify task and log persistence through `PayNotifyTaskDO` and `PayNotifyLogDO`.

No domain entity may call third-party SDKs, Feign clients, Redis DAOs, Mappers, Controller VOs, or HTTP clients directly.

## 11. Mapping Rules

- Controller VO shape stays unchanged.
- API DTO shape stays unchanged.
- PayNotify callback DTO shape stays unchanged.
- DO table field shape stays unchanged.
- Convert classes preserve existing field mappings and Excel/export response fields.
- Repository implementations may use DO/Mapper but must not depend on Controller page request VOs long-term.
- Current debt to remove in future code migration:
  - `PayOrderRepositoryImpl` constructs `PayOrderPageReqVO`.
  - `PayAppRepositoryImpl` constructs `PayAppPageReqVO`.
  - `PayTransferRepositoryImpl` constructs `PayTransferPageReqVO`.
  - `PayWalletRepositoryImpl` constructs `PayWalletPageReqVO`.
  - `PayRefundRepositoryImpl` constructs `PayRefundPageReqVO`.
- Replace those with application/domain query objects one aggregate at a time, preserving mapper behavior and pagination fields.

## 12. Current Conflict Notes

- Pay API contracts still have `@FeignClient` on stable interfaces; split local/remote before claiming API compliance.
- Current service layer still contains production behavior. Do not delete or bypass it until application/domain paths prove equivalent.
- Current application services are not yet full replacements for legacy services; they are migration scaffolding for several flows.
- PayOrder submit remote-call behavior is intentionally non-transactional and easy to break by over-standardizing.
- PayNotify is not just a domain event; it is a durable retry/log/lock pipeline.
- Wallet balance correctness depends on Redis lock plus DB update constraints; pure in-memory domain checks are insufficient.
- Current infrastructure repository page methods leak Controller VOs and must be fixed in targeted migration batches, not hidden by documentation.

## 13. Acceptance Criteria

A Pay refactor batch is acceptable only when all relevant items are true:

- Domain classes import no Spring, MyBatis, Feign, Redis, Mapper, DO, Controller VO, or HTTP client classes.
- API contract external signatures and DTOs are unchanged unless a separate migration is approved.
- Controller URLs, HTTP methods, VO fields, auth/tenant annotations, callback payloads, and export fields are unchanged.
- Error codes and parameter order match `ErrorCodeConstants`.
- Payment submit still preserves extension creation and remote-call failure behavior.
- Callback notify flows are idempotent and tenant-aware.
- Order/refund/transfer status transitions keep optimistic-lock guards.
- Wallet balance updates keep Redis lock and transaction record behavior.
- PayNotify keeps durable task/log/retry/lock behavior.
- Jobs keep names, tenant semantics, and sync/expire rules.
- Repository implementations no longer introduce new Controller VO dependencies.
- Existing tests pass or are updated with behavior-equivalent DDD tests.

## 14. Verification Commands

Minimum compile checks:

```bash
mvn compile -pl develop-module-pay/develop-module-pay-api -am -DskipTests
mvn compile -pl develop-module-pay/develop-module-pay-server -am -DskipTests
```

Run focused tests by affected area:

```bash
mvn test -pl develop-module-pay/develop-module-pay-server -Dtest=PayAppServiceTest
mvn test -pl develop-module-pay/develop-module-pay-server -Dtest=PayChannelServiceTest
mvn test -pl develop-module-pay/develop-module-pay-server -Dtest=PayOrderServiceTest
mvn test -pl develop-module-pay/develop-module-pay-server -Dtest=PayRefundServiceTest
mvn test -pl develop-module-pay/develop-module-pay-server -Dtest=PayNotifyServiceTest
mvn test -pl develop-module-pay/develop-module-pay-server -Dtest=PayTransferApplicationServiceTest
mvn test -pl develop-module-pay/develop-module-pay-server -Dtest=PayWalletApplicationServiceTest
mvn test -pl develop-module-pay/develop-module-pay-server -Dtest=PayWalletTransactionTest
```

Before migrating Controller/service behavior to DDD paths, regression coverage must exist for these named behaviors. If equivalent tests do not exist, add them first:

- submit order creates extension before remote call and does not roll back extension on channel failure.
- duplicate payment callback is idempotent.
- order/refund/transfer expected-status update failure maps to existing error behavior.
- refund create rejects over-refund and concurrent WAITING refund.
- transfer retry only allowed for CLOSED with same price/channel.
- wallet deduct/freeze/unfreeze rejects insufficient balance/frozen amount and creates transaction rows.
- recharge paid callback validates pay order id/status/price/merchant relation.
- PayNotify retry writes log, respects lock, and sends tenant header.

For documentation-only skill edits, run:

```bash
git diff --check -- .claude/ddd-skills/AggregateRoot_Pay_Skill.md
grep -n "^## " .claude/ddd-skills/AggregateRoot_Pay_Skill.md
```

## 15. Quick Reference

| Task | Correct location | Forbidden location |
| --- | --- | --- |
| status transition rules | domain/value object plus application mapping to errors | Controller, Mapper XML, random service setter chain |
| third-party pay/refund/transfer call | infrastructure adapter around `PayClientFactory` | domain entity |
| transaction boundary | application or legacy service facade | domain entity |
| Redis lock/no generation | infrastructure/redis adapter or legacy DAL | domain entity |
| PayNotify retry | notify service/application + DO/Mapper/Redis lock | in-memory event only |
| DTO/VO/DO mapping | convert/infrastructure | domain entity |
| Feign client identity | `api/**/remote/*RemoteClient` | stable API contract |

## 16. Common Mistakes

- Adding `@Transactional` around submit order and accidentally rolling back extension creation after remote call.
- Replacing `updateByIdAndStatus` with `updateById`.
- Treating callback duplicate as an error instead of idempotent success/no-op where legacy code does.
- Dropping `TenantUtils.execute` or tenant headers in notify flow.
- Moving `PayClientFactory` calls into domain.
- Replacing PayNotify durable retry with immediate HTTP call only.
- Updating wallet balance without Redis lock or without transaction record.
- Assuming current DDD application services are complete replacements for legacy services.

## 17. Rationalization Table

| Excuse | Reality |
| --- | --- |
| "It's just a structure cleanup" | Pay structure carries money, callbacks, retries, and tenant context; behavior must be proven. |
| "Domain should own all logic" | Domain owns rules, not third-party SDKs, Redis DAOs, Mappers, or HTTP callbacks. |
| "A domain event can replace notify task" | PayNotify is durable DB retry with locks/logs; an event alone is not equivalent. |
| "Plain update is simpler" | Expected-status updates prevent duplicate callback corruption. |
| "Submit should be transactional like other writes" | Legacy submit intentionally avoids rollback around remote channel calls. |

## 18. Red Flags

Stop the batch if any occur:

- Controller/API/callback DTO contract changes without explicit migration approval.
- `PayClientFactory`, Redis DAO, Mapper, DO, or Controller VO appears in a domain class.
- `updateByIdAndStatus` behavior disappears from order/refund/transfer transitions.
- Wallet balance mutation has no lock or no transaction record.
- PayNotify task/log/retry/lock behavior is bypassed.
- Tenant handling disappears from callback, notify, or job flow.
- Tests do not cover the money-flow behavior being migrated.

## 19. Rollback Conditions

Rollback the current Pay batch if:

- `develop-module-pay-api` or `develop-module-pay-server` fails to compile.
- Any Pay service test fails for existing behavior.
- Payment submit, notify, refund, transfer, wallet, or notify retry behavior cannot be proven equivalent.
- Public API/Controller/callback payloads change unexpectedly.
- A database schema or seed-data change becomes necessary without explicit approval.

## 20. AI Self-Check

Before reporting completion, answer yes to all relevant questions:

- Did I read the legacy service that owns the current behavior for this flow?
- Did I preserve API/Controller/DTO/VO/error-code contracts?
- Did I preserve transaction intent and non-transactional submit behavior where relevant?
- Did I preserve tenant context and callback tenant-ignore behavior?
- Did I preserve optimistic locks for order/refund/transfer status transitions?
- Did I preserve wallet Redis lock and transaction-row creation?
- Did I preserve PayNotify durable retry/log/lock behavior?
- Did I run fresh verification commands and read their output before claiming completion?
