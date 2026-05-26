---
name: erp-module
description: Enterprise Resource Planning — procurement (purchase order/return), sales (order/outbound/return), inventory (stock/move/check), finance (receipt/payment), product/BOM
type: project
---

# develop-module-erp

## 概述

企业资源计划模块。覆盖采购、销售、库存、财务、产品管理的核心业务，支持多仓库、多单位。

- **包路径**: `com.develop.mvp.pk.module.erp`
- **服务名**: `erp-server`
- **数据库表前缀**: `erp_`
- **多租户**: 是（Entity 继承 `TenantBaseDO`）

## 核心功能与 Controller 清单

### 1. 采购管理
| Controller | 路由 | 方法 |
|---|---|---|
| `ErpSupplierController` | `/erp/supplier` | 供应商 CRUD + `/page` |
| `ErpPurchaseOrderController` | `/erp/purchase-order` | 采购订单 CRUD + `/page`、审核、关闭 |
| `ErpPurchaseInController` | `/erp/purchase-in` | 采购入库单 CRUD |
| `ErpPurchaseReturnController` | `/erp/purchase-return` | 采购退货单 CRUD |

**业务流**: 采购下单（审核）→ 采购入库（关联订单）→ 采购退货（关联入库）。

### 2. 销售管理
| Controller | 路由 | 方法 |
|---|---|---|
| `ErpCustomerController` | `/erp/customer` | 客户 CRUD + `/page` |
| `ErpSaleOrderController` | `/erp/sale-order` | 销售订单 CRUD + `/page`、审核、关闭 |
| `ErpSaleOutController` | `/erp/sale-out` | 销售出库单 CRUD |
| `ErpSaleReturnController` | `/erp/sale-return` | 销售退货单 CRUD |

**业务流**: 销售下单（审核）→ 销售出库（关联订单）→ 销售退货（关联出库）。

### 3. 库存管理
| Controller | 路由 | 方法 |
|---|---|---|
| `ErpStockController` | `/erp/stock` | 库存查询（多仓库维度） |
| `ErpStockInController` | `/erp/stock-in` | 入库（盘盈入库、其他入库） |
| `ErpStockOutController` | `/erp/stock-out` | 出库（盘亏出库、其他出库） |
| `ErpStockMoveController` | `/erp/stock-move` | 库存调拨（跨仓库） |
| `ErpStockCheckController` | `/erp/stock-check` | 库存盘点 |
| `ErpStockRecordController` | `/erp/stock-record` | 库存变动记录 |

### 4. 产品管理
| Controller | 路由 | 方法 |
|---|---|---|
| `ErpProductController` | `/erp/product` | 产品 CRUD（物料编码、规格、单位） |
| `ErpProductCategoryController` | `/erp/product-category` | 产品分类 CRUD |
| `ErpProductUnitController` | `/erp/product-unit` | 计量单位 CRUD |

### 5. 财务管理
| Controller | 路由 | 方法 |
|---|---|---|
| `ErpAccountController` | `/erp/account` | 资金账户 CRUD |
| `ErpFinanceReceiptController` | `/erp/finance-receipt` | 收款单 CRUD |
| `ErpFinancePaymentController` | `/erp/finance-payment` | 付款单 CRUD |

### 6. 统计分析
| Controller | 路由 |
|---|---|
| `ErpSaleStatisticsController` | `/erp/statistics-sale` — 销售统计 |
| `ErpPurchaseStatisticsController` | `/erp/statistics-purchase` — 采购统计 |

## 数据库表

### 采购
`erp_supplier`, `erp_purchase_order`, `erp_purchase_order_item`, `erp_purchase_in`, `erp_purchase_in_item`, `erp_purchase_return`, `erp_purchase_return_item`

### 销售
`erp_customer`, `erp_sale_order`, `erp_sale_order_item`, `erp_sale_out`, `erp_sale_out_item`, `erp_sale_return`, `erp_sale_return_item`

### 库存
`erp_stock`, `erp_stock_record`, `erp_stock_check`, `erp_stock_check_item`, `erp_stock_move`, `erp_stock_move_item`

### 产品
`erp_product`, `erp_product_category`, `erp_product_unit`

### 财务
`erp_account`, `erp_finance_receipt`, `erp_finance_receipt_item`, `erp_finance_payment`, `erp_finance_payment_item`

## 关键 Services

| Service | 职责 |
|---|---|
| `ErpPurchaseOrderService` | 采购订单（含审核流程） |
| `ErpSaleOrderService` | 销售订单（含审核流程） |
| `ErpStockService` | 库存核心（扣减、回滚、冻结） |
| `ErpProductService` | 产品管理 |
| `ErpFinanceReceiptService` | 收款处理 |
| `ErpFinancePaymentService` | 付款处理 |

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis`
- `develop-spring-boot-starter-biz-tenant`

## 关键点

- 所有订单类业务有审核流程（状态机：待审核→已审核→已关闭）
- 库存变动通过 `erp_stock_record` 做审计追踪
- 库存调拨/盘点影响库存，需事务保证一致性
- 主子表设计模式适用于订单及明细（`purchase_order` + `purchase_order_item`）
- 无独立的 Feign API 暴露给其他模块（纯管理后台功能）
