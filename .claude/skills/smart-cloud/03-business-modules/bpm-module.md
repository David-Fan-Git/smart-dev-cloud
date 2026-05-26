---
name: bpm-module
description: Workflow engine module — Flowable 7.2.0, process definition, dynamic forms, process instance management, task approval, OA leave demo, listeners, user groups
type: project
---

# develop-module-bpm

## 概述

工作流引擎模块。基于 **Flowable 7.2.0** 实现完整的流程审批功能，包括流程定义管理、动态表单、流程实例管理、任务审批、监听器、用户组、OA 审批示例等。

- **包路径**: `com.develop.mvp.pk.module.bpm`
- **服务名**: `bpm-server`（需确认实际配置）
- **错误码区间**: [1-009-000-000 ~ 1-010-000-000)
- **数据库表前缀**: `bpm_` + Flowable 引擎表（`ACT_*`）
- **重要**: 所有 Entity 继承 `BaseDO`（非 `TenantBaseDO`），BPM 默认不隔离租户

## 核心功能与 Controller 清单

### 1. 流程模型管理
| Controller | 路由 | 方法 |
|---|---|---|
| `BpmModelController` | `/bpm/model` | CRUD + `/deploy`(部署)、`/update-state`(挂起/激活)、`/get-bpmn-xml`(导出 BPMN) |
| `BpmSimpleModelController` | `/bpm/simple-model` | 简化模型管理（可视化建模，面向非技术用户） |
| `BpmCategoryController` | `/bpm/category` | 流程分类 CRUD |
| `BpmProcessDefinitionController` | `/bpm/process-definition` | `/page`(列表)、`/get-bpmn-xml`(查看 BPMN)、`/update-state`(挂起/激活) |

**业务流**: 设计 BPMN 模型(在线建模) → 部署流程 → 查看流程定义 → 启动流程实例。

### 2. 动态表单
| Controller | 路由 | 方法 |
|---|---|---|
| `BpmFormController` | `/bpm/form` | CRUD + `/page` |

表单字段支持：文本、数字、日期、下拉框、单选、多选、部门选择、用户选择。流程模型关联表单 ID，审批时动态渲染。

### 3. 流程实例
| Controller | 路由 | 方法 |
|---|---|---|
| `BpmProcessInstanceController` | `/bpm/process-instance` | `/my-page`(我发起的)、`/page`(全部)、`/get`、`/cancel`(取消)、`/update-state`(挂起/激活) |

### 4. 任务审批
| Controller | 路由 | 方法 |
|---|---|---|
| `BpmTaskController` | `/bpm/task` | `/todo-page`(待办)、`/done-page`(已办)、`/approve`(审批通过)、`/reject`(驳回)、`/back`(退回)、`/delegate`(委派)、`/transfer`(转办)、`/add-sign`(加签)、`/urge`(催办)、`/get-return-list`(退回路径) |

### 5. 监听器
| Controller | 路由 | 方法 |
|---|---|---|
| `BpmProcessListenerController` | `/bpm/process-listener` | CRUD + `/page` |

支持执行监听器（ExecutionListener）和任务监听器（TaskListener），支持 Java 类和表达式。

### 6. 用户组
| Controller | 路由 | 方法 |
|---|---|---|
| `BpmUserGroupController` | `/bpm/user-group` | CRUD + `/page`、`/list-all-simple` |

自定义用户组，用于审批节点候选人配置。

### 7. OA 示例
| Controller | 路由 | 方法 |
|---|---|---|
| `BpmOALeaveController` | `/bpm/oa-leave` | CRUD + `/page` 展示完整审批流程 |

## 数据库表

### 业务表
| 表 | 说明 |
|---|---|
| `bpm_form` | 动态表单 |
| `bpm_category` | 流程分类 |
| `bpm_model` | 流程模型（简化模式） |
| `bpm_process_instance_ext` | 流程实例扩展 |
| `bpm_task_ext` | 任务扩展 |
| `bpm_process_listener` | 流程监听器 |
| `bpm_user_group` | 用户组 |
| `bpm_oa_leave` | OA 请假申请 |

### Flowable 引擎表
| 系列 | 说明 |
|---|---|
| `ACT_RE_*` | 部署、模型、流程定义 |
| `ACT_RU_*` | 运行时（执行实例、任务、变量） |
| `ACT_HI_*` | 历史（已完成的任务、流程实例） |
| `ACT_GE_*` | 通用数据（字节数组、属性） |
| `ACT_ID_*` | 身份管理（用户、组） |
| `ACT_EVT_*` | 事件日志 |
| `ACT_CO_*` | 内容存储（Flowable 6+） |

## 关键 Services

| Service | 职责 |
|---|---|
| `BpmModelService` / `BpmModelServiceImpl` | 模型管理、部署、版本控制 |
| `BpmProcessDefinitionService` / `BpmProcessDefinitionServiceImpl` | 流程定义查询、状态管理 |
| `BpmProcessInstanceService` / `BpmProcessInstanceServiceImpl` | 流程实例创建、取消、挂起 |
| `BpmTaskService` / `BpmTaskServiceImpl` | 任务审批、委派、转办、加签核心逻辑 |
| `BpmFormService` / `BpmFormServiceImpl` | 动态表单 CRUD |
| `BpmOALeaveService` / `BpmOALeaveServiceImpl` | OA 请假业务 |
| `BpmProcessInstanceApiImpl` | Feign API 实现 |

## Feign API 清单

| API | 方法 | 用途 |
|---|---|---|
| `BpmProcessInstanceApi` | `createProcessInstance` | 创建流程实例（供其他模块调用） |

**框架级事件**（非 Feign）：
- `BpmProcessInstanceStatusEvent` — 流程实例状态变更事件
- `BpmProcessInstanceStatusEventListener` — 事件监听器接口

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis`
- `flowable-spring-boot-starter-process` 7.2.0 — Flowable 流程引擎

## 关键点

- Entity 继承 `BaseDO` 而非 `TenantBaseDO` — BPM 默认不隔离租户
- 错误码段 [1-009-000-000 ~ 1-010-000-000)
- 通过 `BpmProcessInstanceStatusEvent` 框架级事件解耦流程状态变更
- 依赖 Flowable 引擎自动建表（`ACT_*` 系列）
- 简化模型（`BpmSimpleModelController`）为低代码可视化建模入口
