---
name: crm-module
description: Customer Relationship Management — customer/clue management, contacts, business opportunities, contracts, receivables, invoices, BI reports, follow-up records, data permissions
type: project
---

# develop-module-crm

## 概述

客户关系管理模块。覆盖售前线索、客户管理、商机跟进、合同签约、回款管理、发票管理的全流程，提供 BI 报表分析。

- **包路径**: `com.develop.mvp.pk.module.crm`
- **服务名**: `crm-server`
- **错误码区间**: [1-020-000-000 ~ 1-021-000-000)
- **数据库表前缀**: `crm_`
- **多租户**: 是（Entity 继承 `TenantBaseDO`）
- **数据权限**: 涉及团队成员数据隔离（`@DataPermission`）

## 核心功能与 Controller 清单

### 1. 客户管理
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmCustomerController` | `/crm/customer` | CRUD + `/page`、`/transfer`(转移)、`/lock`(锁定)、`/put-in-pool`(放入公海) |
| `CrmCustomerPoolConfigController` | `/crm/customer-pool-config` | 公海规则配置（自动回收、认领限制） |
| `CrmCustomerLimitConfigController` | `/crm/customer-limit-config` | 客户拥有数量限制配置 |

**业务流**: 线索→转化客户→分配负责人→客户跟进（私海）→自动回收（公海规则）→退回公海。

### 2. 线索管理
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmClueController` | `/crm/clue` | CRUD + `/page`、`/translate-customer`(转化为客户) |

### 3. 联系人
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmContactController` | `/crm/contact` | CRUD + `/page`，关联客户和商机 |

### 4. 商机管理
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmBusinessController` | `/crm/business` | CRUD + `/page`、商机跟进 |
| `CrmBusinessStatusController` | `/crm/business-status` | 商机阶段配置（自定义销售漏斗） |

### 5. 合同管理
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmContractController` | `/crm/contract` | CRUD + `/page` |
| `CrmContractConfigController` | `/crm/contract-config` | 合同审批配置 |

### 6. 回款管理
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmReceivableController` | `/crm/receivable` | 回款单 CRUD + `/page` |
| `CrmReceivablePlanController` | `/crm/receivable-plan` | 回款计划（分期） |

### 7. 发票管理
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmInvoiceController` | `/crm/invoice` | 开票 CRUD + `/page` |
| `CrmInvoiceConfigController` | `/crm/invoice-config` | 发票配置 |

### 8. 产品管理
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmProductController` | `/crm/product` | CRM 产品 CRUD |
| `CrmProductCategoryController` | `/crm/product-category` | 产品分类 |

### 9. 跟进记录
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmFollowUpRecordController` | `/crm/follow-up-record` | 跟进记录 CRUD + `/page` |

### 10. 数据权限
| Controller | 路由 | 方法 |
|---|---|---|
| `CrmPermissionController` | `/crm/permission` | 团队成员权限配置 |

### 11. BI 报表
| Controller | 路由 |
|---|---|
| `CrmStatisticsCustomerController` | `/crm/statistics-customer` — 客户分析 |
| `CrmStatisticsPerformanceController` | `/crm/statistics-performance` — 业绩分析 |
| `CrmStatisticsFunnelController` | `/crm/statistics-funnel` — 销售漏斗 |
| `CrmStatisticsRankController` | `/crm/statistics-rank` — 排行榜 |
| `CrmStatisticsPortraitController` | `/crm/statistics-portrait` — 客户画像 |

### 12. 操作日志
| Controller | 路由 |
|---|---|
| `CrmOperateLogController` | `/crm/operate-log` — CRM 专属操作日志 |

## 数据库表

| 表 | 说明 | 关键字段 |
|---|---|---|
| `crm_customer` | 客户 | owner_id(负责人), lock_status, pool_status |
| `crm_customer_pool_config` | 公海规则 | enabled, max_follow_counts, auto_recycle_days |
| `crm_customer_limit_config` | 数量限制 | type, max_count |
| `crm_clue` | 线索 | customer_id(转化后关联) |
| `crm_contact` | 联系人 | customer_id, business_ids |
| `crm_business` | 商机 | customer_id, status_id, deal_amount |
| `crm_business_status` | 商机阶段 | type, name, sort |
| `crm_contract` | 合同 | customer_id, business_id, total_amount, discount_percent |
| `crm_receivable` | 回款 | contract_id, plan_id, price |
| `crm_invoice` | 发票 | contract_id, receivable_id |
| `crm_follow_up_record` | 跟进记录 | customer_id, contact_id, business_id, type(content) |
| `crm_permission` | 数据权限 | type, biz_id, user_id |

## 关键 Services

| Service | 职责 |
|---|---|
| `CrmCustomerService` | 客户核心业务（公海/私海转换、分配/转移、自动回收） |
| `CrmClueService` | 线索管理、线索转化客户 |
| `CrmContactService` | 联系人管理 |
| `CrmBusinessService` | 商机阶段推进 |
| `CrmContractService` | 合同（关联回款） |
| `CrmPermissionService` | CRM 数据权限（团队成员隔离） |
| `CrmFollowUpRecordService` | 跟进记录 |

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis`
- `develop-spring-boot-starter-biz-tenant`
- `develop-spring-boot-starter-biz-data-permission` — CRM 数据权限

## 关键点

- CRM 模块与其他模块的交互：查询系统用户（`AdminUserApi`）、部门（`DeptApi`）、字典（`DictDataApi`）
- 数据权限精细化控制：团队成员可见特定客户数据
- 公海回收机制：超时未跟进的客户自动回收
- 无独立的 Feign API 暴露给其他模块（纯管理后台功能）
- 错误码段 [1-020-000-000 ~ 1-021-000-000)
