---
name: pay-module
description: Payment system — merchant management, WeChat/Alipay channel config, payment orders, refunds, transfers, wallet, notifications, demo order
type: project
---

# develop-module-pay

## 概述

支付系统模块。提供统一的支付接入能力，支持多渠道支付、退款、转账、钱包、回调通知处理。

- **包路径**: `com.develop.mvp.pk.module.pay`
- **服务名**: `pay-server`
- **错误码区间**: [1-007-000-000 ~ 1-008-000-000)
- **数据库表前缀**: `pay_`
- **多租户支持**: 所有 Entity 继承 `TenantBaseDO`

## 核心功能与 Controller 清单

### 1. 商户与应用管理
| Controller | 路由 | 方法 |
|---|---|---|
| `PayMerchantController` | `/pay/merchant` | CRUD + `/page` |
| `PayAppController` | `/pay/app` | CRUD + `/page`， 每个应用对应一个业务系统 |

### 2. 支付渠道配置
| Controller | 路由 | 方法 |
|---|---|---|
| `PayChannelController` | `/pay/channel` | CRUD + `/page`、`/get-channel-id-by-pay-app-id`、`/get-channel-by-app-id-and-code` |

**渠道实现**:
- **微信支付**: `weixin-java-pay` 4.8.2，支持 JSAPI、H5、Native、App、小程序支付
- **支付宝**: `alipay-sdk-java` 4.40，支持当面付、电脑网站、手机网站、App 支付

### 3. 支付订单
| Controller | 路由 | 方法 |
|---|---|---|
| `PayOrderController` | `/pay/order` | 管理端：CRUD + `/page`、`/get-detail`(详情) |
| `AppPayOrderController` | `/pay/order`(app) | 移动端：`/create`(创建)、`/get`、`/page`、`/submit`(提交支付)、`/get-pay-url`(获取支付链接) |

**业务流**: 创建订单 → 统一下单（调用渠道）→ 返回支付链接 → 用户支付 → 异步通知 → 更新订单状态。

### 4. 退款
| Controller | 路由 | 方法 |
|---|---|---|
| `PayRefundController` | `/pay/refund` | 管理端：CRUD + `/page`、`/get-detail` |
| `AppPayRefundController` | `/pay/refund`(app) | 移动端：`/create`、`/page` |

支持全额/部分退款，结果同步/异步通知。

### 5. 转账
| Controller | 路由 | 方法 |
|---|---|---|
| `PayTransferController` | `/pay/transfer` | 管理端：CRUD + `/page` |
| `AppPayTransferController` | `/pay/transfer`(app) | 移动端：`/create`、`/page` |

### 6. 钱包
| Controller | 路由 | 方法 |
|---|---|---|
| `PayWalletController` | `/pay/wallet` | `/get`、`/get-by-user-id` |
| `PayWalletRechargeController` | `/pay/wallet-recharge` | 充值 CRUD + `/page` |
| `PayWalletRechargePackageController` | `/pay/wallet-recharge-package` | 充值套餐 CRUD |
| `PayWalletTransactionController` | `/pay/wallet-transaction` | 交易记录 `/page` |

### 7. 通知处理
| Controller | 路由 | 方法 |
|---|---|---|
| `PayNotifyController` | `/pay/notify` | 回调通知接收、验签、重试、查询 |

### 8. Demo 示例
| Controller | 路由 |
|---|---|
| `PayDemoOrderController` | `/pay/demo-order` — 支付接入示例 |
| `PayDemoTransferController` | `/pay/demo-transfer` — 转账示例 |

## 数据库表

| 表 | 说明 |
|---|---|
| `pay_merchant` | 商户 |
| `pay_app` | 支付应用 |
| `pay_channel` | 支付渠道配置 |
| `pay_order` | 支付订单 |
| `pay_order_extension` | 支付订单扩展 |
| `pay_refund` | 退款订单 |
| `pay_transfer` | 转账订单 |
| `pay_notify_task` | 通知任务 |
| `pay_notify_log` | 通知日志 |
| `pay_wallet` | 钱包 |
| `pay_wallet_transaction` | 钱包交易 |
| `pay_wallet_recharge` | 钱包充值 |
| `pay_wallet_recharge_package` | 充值套餐 |
| `pay_demo_order` | 示例订单 |

## 关键 Services

| Service | 职责 |
|---|---|
| `PayOrderService` / `PayOrderServiceImpl` | 支付订单核心逻辑，统一下单、回调处理 |
| `PayRefundService` / `PayRefundServiceImpl` | 退款处理 |
| `PayTransferService` / `PayTransferServiceImpl` | 转账处理 |
| `PayChannelService` / `PayChannelServiceImpl` | 渠道配置、渠道客户端创建 |
| `PayNotifyService` / `PayNotifyServiceImpl` | 回调通知（接收、验签、重试） |
| `PayWalletService` / `PayWalletServiceImpl` | 钱包余额管理 |
| `PayWalletRechargeService` / `PayWalletRechargeServiceImpl` | 充值业务 |

## Feign API 清单

| API | 方法 | 用途 |
|---|---|---|
| `PayOrderApi` | `createOrder`, `getOrder`, `updateOrderPaid` | 支付订单操作 |
| `PayRefundApi` | `createRefund`, `getRefund` | 退款操作 |
| `PayTransferApi` | `createTransfer`, `getTransfer` | 转账操作 |
| `PayWalletApi` | `getWallet`, `addBalance`, `reduceBalance` | 钱包操作 |

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis`
- `develop-spring-boot-starter-biz-tenant`
- `weixin-java-pay` 4.8.2 — 微信支付 SDK
- `alipay-sdk-java` 4.40 — 支付宝 SDK

## 关键点

- 所有 Entity 继承 `TenantBaseDO` — 支付数据按租户隔离
- 错误码段 [1-007-000-000 ~ 1-008-000-000)
- 异步通知基于 `PayNotifyTaskDO` 和定时任务实现可靠重试
- 渠道配置通过工厂模式创建不同渠道客户端
- 支付订单状态机：待支付 → 已支付 → 已退款(部分/全额)
