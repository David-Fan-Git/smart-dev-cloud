---
name: mes-wms-module
description: Manufacturing Execution System (MES) and Warehouse Management System (WMS) -- production orders, work orders, process routes, quality control (IQC/IPQC/OQC/RQC), Andon, device management, inventory, inbound/outbound, transfers, stock counting, barcode/SN
type: project
---

# develop-module-mes + develop-module-wms

## 概述

制造执行系统 (MES) 和仓库管理系统 (WMS) 两个独立模块。

- **MES**: 生产过程管理，涵盖基础数据、日历排班、设备管理、工具管理、工艺路线、工单、生产任务、报工、流转卡、质量管理（IQC/IPQC/OQC/RQC）、安灯、仓库管理等制造核心业务。
- **WMS**: 仓库管理，涵盖仓库/库位、物料、库存、入库/出库/移库/盘点等仓储核心业务。

### 模块信息对比

| 属性 | MES | WMS |
|---|---|---|
| 包路径 | `com.develop.mvp.pk.module.mes` | `com.develop.mvp.pk.module.wms` |
| 服务名 | `mes-server` | `wms-server` |
| 错误码区间 | [1-040-000-000 ~ 1-041-000-000) | [1-060-000-000 ~ 1-061-000-000) |
| 多租户 | 是（Entity 继承 `TenantBaseDO`） | 是（Entity 继承 `TenantBaseDO`） |

## 模块结构

```
develop-module-mes/
  pom.xml                                    # 聚合 pom
  develop-module-mes-api/                    # Feign 接口、DTO、枚举
  develop-module-mes-server/                 # 实现层：Controller + Service + DAL

develop-module-wms/
  pom.xml                                    # 聚合 pom
  develop-module-wms-api/                    # Feign 接口、DTO、枚举
  develop-module-wms-server/                 # 实现层：Controller + Service + DAL
```

---

# MES 模块 — develop-module-mes

## MES 核心功能与 Controller 清单

### 1. 基础数据 (md) — 物料/产品/客户/供应商/车间/工作站

**物料管理**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesMdItemTypeController` | `/mes/md/item-type` | 物料分类 CRUD（树形结构） |
| `MesMdItemController` | `/mes/md/item` | 物料定义 CRUD + `/page`、导入/导出 |
| `MesMdItemBatchConfigController` | `/mes/md/item-batch-config` | 物料批次属性配置 |
| `MesMdUnitMeasureController` | `/mes/md/unit-measure` | 计量单位 CRUD（主单位 + 辅单位） |
| `MesMdProductBomController` | `/mes/md/product-bom` | 产品 BOM 组成（防闭环校验） |
| `MesMdProductSopController` | `/mes/md/product-sop` | 产品 SOP（标准作业指导书） |
| `MesMdProductSipController` | `/mes/md/product-sip` | 产品 SIP（标准检验指导书） |

**客户/供应商**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesMdClientController` | `/mes/md/client` | 客户 CRUD + `/page`、导入/导出 |
| `MesMdVendorController` | `/mes/md/vendor` | 供应商 CRUD + `/page`、导入/导出 |

**车间/工作站**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesMdWorkshopController` | `/mes/md/workshop` | 车间 CRUD |
| `MesMdWorkstationController` | `/mes/md/workstation` | 工作站 CRUD + `/page` |
| `MesMdWorkstationMachineController` | `/mes/md/workstation-machine` | 工作站设备资源分配 |
| `MesMdWorkstationToolController` | `/mes/md/workstation-tool` | 工作站工装夹具资源分配 |
| `MesMdWorkstationWorkerController` | `/mes/md/workstation-worker` | 工作站人力资源分配 |

**编码规则**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesMdAutoCodeRuleController` | `/mes/md/auto-code-rule` | 自动编码规则 CRUD（编码规则 + 组成段） |
| `MesMdAutoCodePartController` | `/mes/md/auto-code-part` | 编码规则组成段管理 |
| `MesMdAutoCodeRecordController` | `/mes/md/auto-code-record` | 编码生成记录 |

### 2. 日历排班 (cal)

| Controller | 路由 | 方法 |
|---|---|---|
| `MesCalCalendarController` | `/mes/cal/calendar` | 生产日历管理 |
| `MesCalHolidayController` | `/mes/cal/holiday` | 假期设置 CRUD |
| `MesCalPlanController` | `/mes/cal/plan` | 排班计划 CRUD（确认/取消确认） |
| `MesCalPlanShiftController` | `/mes/cal/plan-shift` | 排班计划班次配置 |
| `MesCalPlanTeamController` | `/mes/cal/plan-team` | 排班计划班组关联 |
| `MesCalTeamController` | `/mes/cal/team` | 班组 CRUD |
| `MesCalTeamMemberController` | `/mes/cal/team-member` | 班组成员管理 |
| `MesCalTeamShiftController` | `/mes/cal/team-shift` | 班组排班记录（按轮班方式自动生成） |

### 3. 设备管理 (dv)

| Controller | 路由 | 方法 |
|---|---|---|
| `MesDvMachineryTypeController` | `/mes/dv/machinery-type` | 设备类型 CRUD（树形结构） |
| `MesDvMachineryController` | `/mes/dv/machinery` | 设备台账 CRUD + `/page`、导入/导出 |
| `MesDvSubjectController` | `/mes/dv/subject` | 点检保养项目 CRUD |
| `MesDvCheckPlanController` | `/mes/dv/check-plan` | 点检保养方案（启用/停用） |
| `MesDvCheckPlanMachineryController` | `/mes/dv/check-plan-machinery` | 方案关联设备 |
| `MesDvCheckPlanSubjectController` | `/mes/dv/check-plan-subject` | 方案关联项目 |
| `MesDvCheckRecordController` | `/mes/dv/check-record` | 点检记录（提交/完成） |
| `MesDvCheckRecordLineController` | `/mes/dv/check-record-line` | 点检记录明细 |
| `MesDvMaintenRecordController` | `/mes/dv/mainten-record` | 保养记录（提交/完成） |
| `MesDvMaintenRecordLineController` | `/mes/dv/mainten-record-line` | 保养记录明细 |
| `MesDvRepairController` | `/mes/dv/repair` | 维修工单（提交/派工/完成/验收） |
| `MesDvRepairLineController` | `/mes/dv/repair-line` | 维修工单行 |

### 4. 工具管理 (tm)

| Controller | 路由 | 方法 |
|---|---|---|
| `MesTmToolTypeController` | `/mes/tm/tool-type` | 工具类型 CRUD |
| `MesTmToolController` | `/mes/tm/tool` | 工具台账 CRUD + `/page` |

### 5. 生产管理 (pro)

**工序/工艺路线**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesProProcessController` | `/mes/pro-process` | 工序定义 CRUD |
| `MesProProcessContentController` | `/mes/pro-process-content` | 工序内容/作业指导书 CRUD |
| `MesProRouteController` | `/mes/pro-route` | 工艺路线 CRUD（启用/停用） |
| `MesProRouteProcessController` | `/mes/pro-route-process` | 工艺路线工序（排序、关键工序标记） |
| `MesProRouteProductController` | `/mes/pro-route-product` | 工艺路线产品关联 |
| `MesProRouteProductBomController` | `/mes/pro-route-product-bom` | 工艺路线产品 BOM 消耗配置 |

**工单/任务**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesProWorkOrderController` | `/mes/pro-work-order` | 生产工单 CRUD + `/page`（下达/派工/完工/拆分工单） |
| `MesProWorkOrderBomController` | `/mes/pro-work-order-bom` | 工单 BOM 分配 |
| `MesProTaskController` | `/mes/pro-task` | 生产任务 CRUD + `/page`（开始/暂停/继续/完成/取消） |
| `MesProTaskIssueController` | `/mes/pro-task-issue` | 生产任务投料记录 |

**流转卡/报工**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesProCardController` | `/mes/pro-card` | 生产流转卡 CRUD + `/page`（打印/取消） |
| `MesProCardProcessController` | `/mes/pro-card-process` | 流转卡工序记录 |
| `MesProFeedbackController` | `/mes/pro-feedback` | 生产报工 CRUD + `/page`（提交/审批/检验/驳回） |
| `MesProWorkRecordController` | `/mes/pro-work-record` | 工作记录（上工/下工） |

**安灯 (Andon)**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesProAndonConfigController` | `/mes/pro-andon-config` | 安灯呼叫配置 |
| `MesProAndonRecordController` | `/mes/pro-andon-record` | 安灯呼叫记录（处置） |

### 6. 质量管理 (qc)

**质检基础**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesQcTemplateController` | `/mes/qc/template` | 质检方案 CRUD |
| `MesQcTemplateIndicatorController` | `/mes/qc/template-indicator` | 质检方案检测指标项 |
| `MesQcTemplateItemController` | `/mes/qc/template-item` | 质检方案产品关联 |
| `MesQcIndicatorController` | `/mes/qc/indicator` | 质检指标 CRUD |
| `MesQcDefectController` | `/mes/qc/defect` | 缺陷类型 CRUD |
| `MesQcDefectRecordController` | `/mes/qc/defect-record` | 质检缺陷记录（通用，支持 IQC/IPQC/OQC/RQC） |
| `MesQcIndicatorResultController` | `/mes/qc/indicator-result` | 检验结果录入 |
| `MesQcPendingInspectController` | `/mes/qc/pending-inspect` | 待检验列表 |

**来料检验 (IQC)**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesQcIqcController` | `/mes/qc/iqc` | 来料检验单 CRUD（提交/完成/驳回） |
| `MesQcIqcLineController` | `/mes/qc/iqc-line` | 来料检验行 |

**过程检验 (IPQC)**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesQcIpqcController` | `/mes/qc/ipqc` | 过程检验单 CRUD（提交/完成/驳回） |
| `MesQcIpqcLineController` | `/mes/qc/ipqc-line` | 过程检验行 |

**出货检验 (OQC)**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesQcOqcController` | `/mes/qc/oqc` | 出货检验单 CRUD（提交/完成/驳回） |
| `MesQcOqcLineController` | `/mes/qc/oqc-line` | 出货检验行 |

**退货检验 (RQC)**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesQcRqcController` | `/mes/qc/rqc` | 退货检验单 CRUD（提交/完成/驳回） |
| `MesQcRqcLineController` | `/mes/qc/rqc-line` | 退货检验行 |

### 7. MES 仓库管理 (wm)

**仓库基础**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmWarehouseController` | `/mes/wm/warehouse` | 仓库 CRUD |
| `MesWmWarehouseLocationController` | `/mes/wm/warehouse-location` | 库区 CRUD |
| `MesWmWarehouseAreaController` | `/mes/wm/warehouse-area` | 库位 CRUD |
| `MesWmBatchController` | `/mes/wm/batch` | 批次管理 |

**库存管理**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmMaterialStockController` | `/mes/wm/material-stock` | 实时库存（按仓库/库位/物料查询，库存冻结/解冻） |

**入库管理**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmArrivalNoticeController` | `/mes/wm/arrival-notice` | 到货通知单（提交/审批/完成/取消） |
| `MesWmArrivalNoticeLineController` | `/mes/wm/arrival-notice-line` | 到货通知单行 |
| `MesWmItemReceiptController` | `/mes/wm/item-receipt` | 采购入库单（提交/上架/完成/取消） |
| `MesWmItemReceiptLineController` | `/mes/wm/item-receipt-line` | 采购入库单行 |
| `MesWmItemReceiptDetailController` | `/mes/wm/item-receipt-detail` | 采购入库明细/上架记录 |
| `MesWmProductReceiptController` | `/mes/wm/product-receipt` | 产品收货单（提交/上架/完成/取消） |
| `MesWmProductReceiptLineController` | `/mes/wm/product-receipt-line` | 产品收货单行 |
| `MesWmProductReceiptDetailController` | `/mes/wm/product-receipt-detail` | 产品收货明细 |
| `MesWmOutsourceReceiptController` | `/mes/wm/outsource-receipt` | 外协入库单（提交/上架/完成/取消） |
| `MesWmOutsourceReceiptLineController` | `/mes/wm/outsource-receipt-line` | 外协入库单行 |
| `MesWmOutsourceReceiptDetailController` | `/mes/wm/outsource-receipt-detail` | 外协入库明细 |
| `MesWmMiscReceiptController` | `/mes/wm/misc-receipt` | 杂项入库单（提交/入库/完成/取消） |
| `MesWmMiscReceiptLineController` | `/mes/wm/misc-receipt-line` | 杂项入库单行 |
| `MesWmProductProduceLineController` | `/mes/wm/product-produce` | 生产入库（产成品入库） |

**出库管理**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmProductIssueController` | `/mes/wm/product-issue` | 生产领料出库单（提交/拣货/领出/取消） |
| `MesWmProductIssueLineController` | `/mes/wm/product-issue-line` | 领料出库单行 |
| `MesWmProductIssueDetailController` | `/mes/wm/product-issue-detail` | 领料出库明细/拣货记录 |
| `MesWmProductSalesController` | `/mes/wm/product-sales` | 销售出库单（提交/拣货/出库/完成/取消） |
| `MesWmProductSalesLineController` | `/mes/wm/product-sales-line` | 销售出库单行 |
| `MesWmProductSalesDetailController` | `/mes/wm/product-sales-detail` | 销售出库明细/拣货记录 |
| `MesWmOutsourceIssueController` | `/mes/wm/outsource-issue` | 外协发料单（提交/拣货/出库/完成/取消） |
| `MesWmOutsourceIssueLineController` | `/mes/wm/outsource-issue-line` | 外协发料单行 |
| `MesWmOutsourceIssueDetailController` | `/mes/wm/outsource-issue-detail` | 外协发料明细 |
| `MesWmMiscIssueController` | `/mes/wm/misc-issue` | 杂项出库单（提交/出库/完成/取消） |
| `MesWmMiscIssueLineController` | `/mes/wm/misc-issue-line` | 杂项出库单行 |

**调拨/转移**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmTransferController` | `/mes/wm/transfer` | 转移调拨单（提交/确认/上架/完成/取消） |
| `MesWmTransferLineController` | `/mes/wm/transfer-line` | 调拨单行 |
| `MesWmTransferDetailController` | `/mes/wm/transfer-detail` | 调拨明细 |

**退货管理**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmReturnIssueController` | `/mes/wm/return-issue` | 生产退料单（提交/检验/上架/完成/取消） |
| `MesWmReturnIssueLineController` | `/mes/wm/return-issue-line` | 生产退料单行 |
| `MesWmReturnIssueDetailController` | `/mes/wm/return-issue-detail` | 生产退料明细 |
| `MesWmReturnVendorController` | `/mes/wm/return-vendor` | 供应商退货单（提交/退货/完成/取消） |
| `MesWmReturnVendorLineController` | `/mes/wm/return-vendor-line` | 供应商退货单行 |
| `MesWmReturnVendorDetailController` | `/mes/wm/return-vendor-detail` | 供应商退货明细 |
| `MesWmReturnSalesController` | `/mes/wm/return-sales` | 销售退货单（提交/退货/上架/完成/取消） |
| `MesWmReturnSalesLineController` | `/mes/wm/return-sales-line` | 销售退货单行 |
| `MesWmReturnSalesDetailController` | `/mes/wm/return-sales-detail` | 销售退货明细 |

**发货通知**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmSalesNoticeController` | `/mes/wm/sales-notice` | 发货通知单 CRUD（提交/完成/取消） |
| `MesWmSalesNoticeLineController` | `/mes/wm/sales-notice-line` | 发货通知单行 |

**盘点管理**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmStockTakingPlanController` | `/mes/wm/stock-taking-plan` | 盘点方案 CRUD（启用/停用） |
| `MesWmStockTakingPlanParamController` | `/mes/wm/stock-taking-plan-param` | 盘点方案参数配置 |
| `MesWmStockTakingTaskController` | `/mes/wm/stock-taking-task` | 盘点任务 CRUD（生成/开始/完成/取消） |
| `MesWmStockTakingTaskLineController` | `/mes/wm/stock-taking-task-line` | 盘点任务行 |
| `MesWmStockTakingTaskResultController` | `/mes/wm/stock-taking-task-result` | 盘点结果录入 |

**条码/序列号**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmBarcodeConfigController` | `/mes/wm/barcode-config` | 条码配置 |
| `MesWmBarcodeController` | `/mes/wm/barcode` | 条码清单 |
| `MesWmSnController` | `/mes/wm/sn` | 序列号管理 |

**装箱**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmPackageController` | `/mes/wm/package` | 装箱单 CRUD（装箱/拆箱） |
| `MesWmPackageLineController` | `/mes/wm/package-line` | 装箱明细 |

**其他**
| Controller | 路由 | 方法 |
|---|---|---|
| `MesWmItemConsumeLineController` | `/mes/wm/item-consume-line` | 物料消耗记录 |

### 8. 首页统计

| Controller | 路由 | 方法 |
|---|---|---|
| `MesHomeStatisticsController` | `/mes/home-statistics` | MES 首页看板统计 |

## MES 错误码

MES 模块使用 `1-040-000-000` 段，按子域分段：

```
ErrorCodeConstants.java:
  1-040-100-000 ~ 1-040-109-999  基础数据（物料/分类/单位/客户/供应商/车间/工作站/BOM/SOP/SIP）
  1-040-110-000 ~ 1-040-199-999  自动编码规则
  1-040-200-000 ~ 1-040-209-999  日历排班（班次/班组/排班计划/假期）
  1-040-300-000 ~ 1-040-399-999  设备管理（设备类型/台账/点检/保养/维修）
  1-040-400-000 ~ 1-040-409-999  工具管理
  1-040-500-000 ~ 1-040-599-999  生产管理（工序/工艺路线/工单/任务/安灯/报工/流转卡/工作记录）
  1-040-600-000 ~ 1-040-609-999  质量管理（质检方案/指标/IQC/IPQC/OQC/RQC/缺陷）
  1-040-700-000 ~ 1-040-749-999  仓库管理（仓库/库区/库位/入库/出库/调拨/盘点/退货/条码/SN/装箱）
```

## MES 关键 Services

| Service | 职责 |
|---|---|
| `MesMdItemService` | 物料管理（含导入导出、编码生成） |
| `MesMdProductBomService` | 产品 BOM（防闭环校验、层级展开） |
| `MesProWorkOrderService` | 生产工单（状态机：草稿→已确认→已完成/已取消，支持拆分工单） |
| `MesProTaskService` | 生产任务（派工到人/工作站，状态机：待开始→进行中→已完成/已取消） |
| `MesProRouteService` | 工艺路线（启用校验：必须有工序、关键工序、BOM 消耗） |
| `MesProCardService` | 流转卡（按工序流转） |
| `MesProFeedbackService` | 生产报工（状态机：草稿→审批中→已通过/已驳回/已检验，数量校验） |
| `MesProAndonRecordService` | 安灯呼叫（处置闭环） |
| `MesQcIqcService` / `MesQcIpqcService` / `MesQcOqcService` / `MesQcRqcService` | 各类质检（来料/过程/出货/退货检验） |
| `MesWmMaterialStockService` | 库存核心（扣减、回滚、冻结、批次管理） |
| `MesWmItemReceiptService` | 采购入库（支持到货通知单关联、上架流程） |
| `MesWmTransferService` | 转移调拨（跨仓库/库位移动） |
| `MesWmStockTakingService` | 盘点（方案→任务→结果闭环） |
| `MesWmBarcodeService` | 条码生成与解析 |

## MES 关键 Entities

所有 Entity 继承 `TenantBaseDO`。

**基础数据**: `MesMdItemDO`(物料)、`MesMdItemTypeDO`(物料分类)、`MesMdUnitMeasureDO`(计量单位)、`MesMdClientDO`(客户)、`MesMdVendorDO`(供应商)、`MesMdWorkshopDO`(车间)、`MesMdWorkstationDO`(工作站)、`MesMdProductBomDO`(产品 BOM)

**日历排班**: `MesCalPlanDO`(排班计划)、`MesCalTeamDO`(班组)、`MesCalHolidayDO`(假期)

**设备**: `MesDvMachineryDO`(设备)、`MesDvCheckPlanDO`(点检方案)、`MesDvCheckRecordDO`(点检记录)、`MesDvMaintenRecordDO`(保养记录)、`MesDvRepairDO`(维修工单)

**生产**: `MesProWorkOrderDO`(工单)、`MesProTaskDO`(任务)、`MesProRouteDO`(工艺路线)、`MesProCardDO`(流转卡)、`MesProFeedbackDO`(报工单)、`MesProAndonRecordDO`(安灯记录)

**质量**: `MesQcTemplateDO`(质检方案)、`MesQcIqcDO`(来料检验)、`MesQcIpqcDO`(过程检验)、`MesQcOqcDO`(出货检验)、`MesQcRqcDO`(退货检验)

**仓库**: `MesWmWarehouseDO`(仓库)、`MesWmWarehouseLocationDO`(库区)、`MesWmWarehouseAreaDO`(库位)、`MesWmMaterialStockDO`(库存)、`MesWmBatchDO`(批次)

**单据**: `MesWmItemReceiptDO`(采购入库)、`MesWmProductIssueDO`(领料出库)、`MesWmTransferDO`(调拨)、`MesWmStockTakingPlanDO`(盘点方案)

## MES 数据库表 (部分)

| 表 | 说明 |
|---|---|
| `mes_md_item` | 物料定义 |
| `mes_md_item_type` | 物料分类（树形） |
| `mes_md_product_bom` | 产品 BOM |
| `mes_md_client` / `mes_md_vendor` | 客户 / 供应商 |
| `mes_md_workshop` / `mes_md_workstation` | 车间 / 工作站 |
| `mes_cal_plan` / `mes_cal_team` | 排班计划 / 班组 |
| `mes_dv_machinery` / `mes_dv_check_plan` | 设备 / 点检方案 |
| `mes_pro_work_order` / `mes_pro_task` | 工单 / 任务 |
| `mes_pro_route` / `mes_pro_route_process` | 工艺路线 / 工序 |
| `mes_pro_card` / `mes_pro_feedback` | 流转卡 / 报工 |
| `mes_pro_andon_record` | 安灯记录 |
| `mes_qc_template` / `mes_qc_iqc` / `mes_qc_ipqc` / `mes_qc_oqc` | 质检方案 / 各类检验单 |
| `mes_wm_warehouse` / `mes_wm_warehouse_location` / `mes_wm_warehouse_area` | 仓库 / 库区 / 库位 |
| `mes_wm_material_stock` | 库存 |
| `mes_wm_item_receipt` / `mes_wm_product_issue` / `mes_wm_transfer` | 采购入库 / 领料出库 / 调拨 |
| `mes_wm_stock_taking_plan` / `mes_wm_stock_taking_task` | 盘点方案 / 任务 |
| `mes_wm_barcode` | 条码 |

---

# WMS 模块 — develop-module-wms

## WMS 核心功能与 Controller 清单

### 1. 基础数据 (md)

| Controller | 路由 | 方法 |
|---|---|---|
| `WmsWarehouseController` | `/wms/warehouse` | 仓库 CRUD |
| `WmsItemCategoryController` | `/wms/item-category` | 商品分类 CRUD（树形结构） |
| `WmsItemBrandController` | `/wms/item-brand` | 商品品牌 CRUD |
| `WmsItemController` | `/wms/item` | 商品定义 CRUD + `/page`（商品编码、规格、单位） |
| `WmsItemSkuController` | `/wms/item-sku` | 商品 SKU CRUD |
| `WmsMerchantController` | `/wms/merchant` | 往来企业（供应商/客户）CRUD + `/page` |

### 2. 库存管理

| Controller | 路由 | 方法 |
|---|---|---|
| `WmsInventoryController` | `/wms/inventory` | 实时库存（按仓库/库位/商品查询） |
| `WmsInventoryHistoryController` | `/wms/inventory-history` | 库存变动历史 |

### 3. 入库管理

| Controller | 路由 | 方法 |
|---|---|---|
| `WmsReceiptOrderController` | `/wms/receipt-order` | 入库单 CRUD + `/page`（采购入库、生产入库、退货入库等） |
| `WmsReceiptOrderDetailController` | `/wms/receipt-order-detail` | 入库单明细 |

### 4. 出库管理

| Controller | 路由 | 方法 |
|---|---|---|
| `WmsShipmentOrderController` | `/wms/shipment-order` | 出库单 CRUD + `/page`（销售出库、生产领料、退货出库等） |
| `WmsShipmentOrderDetailController` | `/wms/shipment-order-detail` | 出库单明细 |

### 5. 移库管理

| Controller | 路由 | 方法 |
|---|---|---|
| `WmsMovementOrderController` | `/wms/movement-order` | 移库单 CRUD + `/page`（库间调拨、库内移位） |
| `WmsMovementOrderDetailController` | `/wms/movement-order-detail` | 移库单明细 |

### 6. 盘点管理

| Controller | 路由 | 方法 |
|---|---|---|
| `WmsCheckOrderController` | `/wms/check-order` | 盘库单 CRUD + `/page` |
| `WmsCheckOrderDetailController` | `/wms/check-order-detail` | 盘库单明细 |

### 7. 首页统计

| Controller | 路由 | 方法 |
|---|---|---|
| `WmsHomeStatisticsController` | `/wms/home-statistics` | WMS 首页看板统计 |

## WMS 错误码

WMS 模块使用 `1-060-000-000` 段，按子域分段：

```
ErrorCodeConstants.java:
  1-060-100-000 ~ 1-060-109-999  基础数据（仓库/商品分类/品牌/商品/SKU/往来企业）
  1-060-200-000 ~ 1-060-209-999  订单（入库单/出库单/移库单/盘库单）
  1-060-300-000 ~ 1-060-309-999  库存
```

## WMS 关键 Services

| Service | 职责 |
|---|---|
| `WmsWarehouseService` | 仓库管理 |
| `WmsItemService` / `WmsItemSkuService` | 商品/SKU 管理 |
| `WmsInventoryService` / `WmsInventoryServiceImpl` | 库存核心（数量变更、库存校验，含分布式锁） |
| `WmsReceiptOrderService` / `WmsReceiptOrderServiceImpl` | 入库（状态机：草稿→已提交→已完成/已作废） |
| `WmsShipmentOrderService` / `WmsShipmentOrderServiceImpl` | 出库（状态机：草稿→已提交→已完成/已作废） |
| `WmsMovementOrderService` / `WmsMovementOrderServiceImpl` | 移库（状态机：草稿→已提交→已完成/已作废） |
| `WmsCheckOrderService` / `WmsCheckOrderServiceImpl` | 盘点（状态机：草稿→已提交→已完成/已作废，库存变化校验） |

## WMS 关键 Entities

所有 Entity 继承 `TenantBaseDO`。

| Entity | 说明 |
|---|---|
| `WmsWarehouseDO` | 仓库 |
| `WmsItemCategoryDO` | 商品分类（树形结构） |
| `WmsItemBrandDO` | 商品品牌 |
| `WmsItemDO` | 商品定义 |
| `WmsItemSkuDO` | 商品 SKU |
| `WmsMerchantDO` | 往来企业（供应商/客户） |
| `WmsInventoryDO` | 实时库存 |
| `WmsInventoryHistoryDO` | 库存变动历史 |
| `WmsReceiptOrderDO` | 入库单（主子表：明细 `WmsReceiptOrderDetailDO`） |
| `WmsShipmentOrderDO` | 出库单（主子表：明细 `WmsShipmentOrderDetailDO`） |
| `WmsMovementOrderDO` | 移库单（主子表：明细 `WmsMovementOrderDetailDO`） |
| `WmsCheckOrderDO` | 盘库单（主子表：明细 `WmsCheckOrderDetailDO`） |

## WMS 数据库表

| 表 | 说明 |
|---|---|
| `wms_warehouse` | 仓库 |
| `wms_item_category` | 商品分类 |
| `wms_item_brand` | 商品品牌 |
| `wms_item` | 商品 |
| `wms_item_sku` | 商品 SKU |
| `wms_merchant` | 往来企业 |
| `wms_inventory` | 实时库存 |
| `wms_inventory_history` | 库存历史 |
| `wms_receipt_order` | 入库单 |
| `wms_receipt_order_detail` | 入库单明细 |
| `wms_shipment_order` | 出库单 |
| `wms_shipment_order_detail` | 出库单明细 |
| `wms_movement_order` | 移库单 |
| `wms_movement_order_detail` | 移库单明细 |
| `wms_check_order` | 盘库单 |
| `wms_check_order_detail` | 盘库单明细 |

## 依赖的 Starter

**MES 和 WMS 共用**:
- `develop-spring-boot-starter-web` — REST API
- `develop-spring-boot-starter-security` — 认证授权
- `develop-spring-boot-starter-mybatis` — 数据访问
- `develop-spring-boot-starter-redis` — 缓存（库存扣减分布式锁）
- `develop-spring-boot-starter-biz-tenant` — 多租户

## 对外 API

MES 和 WMS 模块主要面向管理后台，**无独立的 Feign API 暴露给其他业务模块**。

## 关键点

### MES

- 所有单据类业务有严格的状态机控制（草稿→提交/审批→完成/取消），状态变更由 Service 层校验
- 生产报工与质量检验联动：报工完成后自动生成 IPQC 待检任务
- 工艺路线启用前校验：必须包含工序、关键工序、BOM 消耗配置
- 库存事务支持：到货通知单→采购入库单→上架→库存更新，完整的采购入库闭环
- 盘点流程：盘点方案（参数配置）→ 盘点任务（自动生成清单）→ 盘点结果录入 → 差异处理
- 条码/SN 支持：条码配置 → 条码生成 → 条码扫描（入库/出库/盘点）
- 自动编码规则：支持自定义编码规则（固定段+日期段+流水号段），基于 Redis 生成
- 设备管理完整闭环：点检计划 → 点检记录 / 保养记录 / 维修工单
- 安灯闭环：呼叫 → 响应 → 处置 → 关闭
- MES 与 ERP 交叉：MES 的工单/任务/报工与 ERP 的采购/销售/库存有数据交互需求

### WMS

- 所有订单状态机：草稿 → 已提交 → 已完成 / 已作废
- 库存管理支持实时库存查询、库存变动历史追溯
- 入库/出库/移库/盘点均使用主子表设计（Order + OrderDetail）
- 库存扣减使用 Redisson 分布式锁，保证并发安全
- 盘库单在完成时校验库存是否已变化（乐观锁机制）
- WMS 是轻量级仓库管理，不做批次/库位精细化管理（由 MES 的 wm 子域覆盖）

### MES 与 WMS 的选择

- 如果只需要基础的仓库管理（入库/出库/移库/盘点），使用 `develop-module-wms`
- 如果需要完整的制造执行 + 精细化工序级仓库管理（批次/库位/SN/BOM 消耗），使用 `develop-module-mes`（其内部 wm 子域覆盖更完整的仓库功能）
- 两个模块可同时启用，MES 的 wm 子域包含 WMS 的超集功能
