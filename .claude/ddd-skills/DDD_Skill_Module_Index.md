---
name: ddd-skill-module-index
description: Use when locating repository DDD skills by project module before auditing, planning, or executing aggregate refactors.
---

# DDD Skill Module Index

## Overview

本索引按当前项目模块对 `.claude/ddd-skills` 下所有 DDD skill 与标准文档分类，帮助后续 AI Coding 在修改模块前快速定位应读取的 skill。

## Global Standards

| File | Category | Use |
|---|---|---|
| `DDD_Skill_Production_Readiness_Standard.md` | 全局生产级 skill 标准 | 审计、创建或升级聚合 skill 前必须读取。 |
| `Module_Structure_Standard.md` | 全局模块结构标准 | 修改模块结构、API 契约、DDD 层、运行单元前必须读取。 |
| `AggregateRoot_Module_Draft_Skill.md` | 通用草稿入口 | ERP、MES、MP、Report 等尚无生产级聚合 skill 的模块只能用作草稿参考。 |

## develop-module-system

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_Tenant_Validation_Skill.md` | Tenant、租户套餐、租户创建、管理员初始化、权限分配 | production-ready |
| `AggregateRoot_User_Validation_Skill.md` | User、AdminUser、用户校验、部门岗位协作 | production-review |
| `AggregateRoot_Role_Menu_Skill.md` | Role、Menu、Permission、RoleMenu、UserRole、菜单树、权限检查、数据权限 | production-review |

## develop-module-infra

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_Infra_Skill.md` | Infra 配置、文件、代码生成、API 边界与基础设施服务提供者职责 | draft / review-required |

## develop-module-iot

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_IotProduct_Skill.md` | IoT Product、产品密钥、发布状态、动态注册、TDengine 产品属性表同步 | production-ready |
| `AggregateRoot_IotThingModel_Skill.md` | IoT ThingModel、属性/事件/服务定义、TSL、缓存、Modbus 冗余同步 | production-ready |
| `AggregateRoot_IotDevice_Skill.md` | IoT Device、设备实例、认证、动态注册、状态、拓扑、缓存、导入 | production-ready |
| `AggregateRoot_IotCommand_Skill.md` | IoT Command/DeviceMessage、下行命令、requestId、reply/ACK、serverId 路由 | production-ready |

## develop-module-mall

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_Mall_Skill.md` | Mall 总览或跨上下文草稿入口 | draft / split-required |
| `AggregateRoot_MallProduct_Skill.md` | Mall Product 商品上下文 | draft / review-required |
| `AggregateRoot_MallPromotion_Skill.md` | Mall Promotion 营销上下文 | draft / review-required |
| `AggregateRoot_MallTrade_Skill.md` | Mall Trade 交易上下文 | draft / review-required |
| `AggregateRoot_MallStatistics_Skill.md` | Mall Statistics 统计上下文 | draft / review-required |

## develop-module-member

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_MemberUser_Skill.md` | Member User、会员用户 | draft / review-required |
| `AggregateRoot_MemberLevel_Skill.md` | Member Level、会员等级 | draft / review-required |

## develop-module-pay

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_Pay_Skill.md` | Pay 支付、退款、渠道、订单相关聚合入口 | draft / review-required |

## develop-module-bpm

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_Bpm_Skill.md` | BPM 流程、任务、模型、审批相关聚合入口 | draft / review-required |

## develop-module-crm

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_Crm_Skill.md` | CRM 客户、商机、合同、回款等聚合入口 | draft / review-required |

## develop-module-wms

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_Wms_Skill.md` | WMS 仓储、库存、出入库等聚合入口 | draft / review-required |

## develop-module-ai

| File | Aggregate / Scope | Status |
|---|---|---|
| `AggregateRoot_Ai_Skill.md` | AI 模块聚合入口 | draft / review-required |

## Modules Covered Only by Draft Skill

| Module | Skill Coverage | Required Action Before Production Refactor |
|---|---|---|
| `develop-module-erp` | `AggregateRoot_Module_Draft_Skill.md` | 按真实聚合创建生产级 skill。 |
| `develop-module-mes` | `AggregateRoot_Module_Draft_Skill.md` | 按真实聚合创建生产级 skill。 |
| `develop-module-mp` | `AggregateRoot_Module_Draft_Skill.md` | 按真实聚合创建生产级 skill。 |
| `develop-module-report` | `AggregateRoot_Module_Draft_Skill.md` | 按真实聚合创建生产级 skill。 |

## Quick Selection Rules

| Work Item | Read First |
|---|---|
| 修改模块结构或 API 契约 | `Module_Structure_Standard.md` |
| 创建或升级聚合 skill | `DDD_Skill_Production_Readiness_Standard.md` |
| 修改 system 租户 | `AggregateRoot_Tenant_Validation_Skill.md` |
| 修改 system 用户 | `AggregateRoot_User_Validation_Skill.md` |
| 修改 system 权限、角色、菜单 | `AggregateRoot_Role_Menu_Skill.md` |
| 修改 IoT P0 生产闭环 | 对应 `AggregateRoot_Iot*_Skill.md` |
| 修改只有草稿覆盖的模块 | 先按生产标准创建目标聚合 skill |

## Red Flags

- 直接按 `AggregateRoot_Module_Draft_Skill.md` 修改生产代码。
- 一个任务同时使用多个无关模块 skill 并批量改代码。
- 聚合 skill 状态不是 production-ready，却直接作为生产级实现依据。
- 修改 API 契约或 DDD 目录前没有读取 `Module_Structure_Standard.md`。
- 修改聚合前没有读取对应模块 skill 和生产级标准。
