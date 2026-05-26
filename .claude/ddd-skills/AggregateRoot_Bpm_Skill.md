---
name: aggregate-root-bpm-skill
description: Use when modifying or reviewing BPM Flowable model, process, task, OA leave, form, user group, listener, expression, or category DDD migration boundaries.
type: ddd-aggregate-skill
status: production-review
---

# AggregateRoot BPM Skill

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

BPM 是工作流核心上下文，既包含已部分 DDD 化的分类、表单、用户组、表达式、监听器、OA 请假和抄送，也包含仍由 legacy service 承载的 Flowable 模型、流程定义、流程实例和任务审批。任何重构必须保持 Flowable 行为、API/事件契约、审批状态机、候选人策略、消息、权限、错误码和现有 Controller 外部行为不变。

## 1. When to Use / Not Use

Use when:

- 重构或验证 `develop-module-bpm/develop-module-bpm-*`。
- 拆分 `BpmProcessInstanceApi` local/remote 契约。
- 迁移 `definition/model/process/task/oa/copy/form/usergroup/listener/expression/category` 到 DDD 分层。
- 修改 Flowable `RepositoryService`、`RuntimeService`、`TaskService`、`HistoryService`、`ManagementService`、BPMN 模型解析、任务候选人策略、流程事件监听或审批按钮行为。

Do not use when:

- 只修改 Flowable XML 设计器静态资源或非 BPM 业务模块。
- 只调整 UI 文案、SQL 数据或普通配置。
- 准备一次性重构全部 BPM；必须按 `definition`、`model`、`process-instance`、`task`、`oa-leave`、`copy`、`candidate` 等小批次执行。

## 2. Baseline Failure Findings

未升级前的草稿风险：

1. 只有聚合和值对象列表，缺少生产级事实源路径、错误码、事务、Flowable API 和测试命令。
2. 将“已 DDD”与“可替换 legacy”混为一谈。当前 DDD application 多数只是迁移脚手架，Flowable 核心仍在 legacy service。
3. 未记录 `create(null, ...)` 与 ID 值对象非空约束的冲突，实际迁移可能直接 NPE。
4. 未记录 infrastructure repository 依赖 Controller PageReqVO 和 legacy service 的边界债。
5. 未记录 OA 请假 `updateStatus` 通过占位字段重建聚合再全量保存的字段污染风险。
6. 未记录 Flowable 任务审批、退回、撤回、加签、转办、委派、候选人策略和 BPMN 模型解析的高风险规则。
7. 未记录 `BpmProcessInstanceStatusEvent`、Flowable event listener、candidate strategy 单测路径和错误码重复码现状。
8. 未明确 Controller/API/VO/DTO/权限/错误码不得随 DDD 重构改变。

## 3. Reproducibility Contract

执行本 skill 前必须：

1. 先读取本文件和 `.claude/ddd-skills/DDD_Skill_Production_Readiness_Standard.md`、`.claude/ddd-skills/Module_Structure_Standard.md`。
2. 当前可编译代码的外部行为优先。发现 skill 与代码冲突时，先修 skill，不先改代码。
3. 每批只处理一个小上下文：`category/form/usergroup/expression/listener/oa-leave/copy/model/process-definition/process-instance/task/candidate`。
4. Flowable API 调用必须留在 application/service/infrastructure adapter，不进入 domain entity。
5. Controller 路径、HTTP 方法、VO/DTO 字段、权限注解、错误码、事件契约、消息语义和 Flowable 流程行为不得在聚合重构中擅改。
6. legacy `service` 是 Flowable 核心行为事实源；只有当 DDD application 覆盖完整规则、事务、错误码和测试后，才可切换入口。

## 4. Current Source Anchors

### API module and events

- `develop-module-bpm/develop-module-bpm-api/src/main/java/com/develop/mvp/pk/module/bpm/api/task/BpmProcessInstanceApi.java`
- `develop-module-bpm/develop-module-bpm-api/src/main/java/com/develop/mvp/pk/module/bpm/api/task/dto/BpmProcessInstanceCreateReqDTO.java`
- `develop-module-bpm/develop-module-bpm-api/src/main/java/com/develop/mvp/pk/module/bpm/api/event/BpmProcessInstanceStatusEvent.java`
- `develop-module-bpm/develop-module-bpm-api/src/main/java/com/develop/mvp/pk/module/bpm/api/event/BpmProcessInstanceStatusEventListener.java`
- `develop-module-bpm/develop-module-bpm-api/src/main/java/com/develop/mvp/pk/module/bpm/enums/ApiConstants.java`
- `develop-module-bpm/develop-module-bpm-api/src/main/java/com/develop/mvp/pk/module/bpm/enums/ErrorCodeConstants.java`
- `develop-module-bpm/develop-module-bpm-api/src/main/java/com/develop/mvp/pk/module/bpm/enums/definition/*.java`
- `develop-module-bpm/develop-module-bpm-api/src/main/java/com/develop/mvp/pk/module/bpm/enums/task/*.java`

### API implementations and event listeners

- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/api/task/BpmProcessInstanceApiImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/api/event/CrmContractStatusListener.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/api/event/CrmReceivableStatusListener.java`

### Controllers and VO contracts

- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/definition/BpmCategoryController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/definition/BpmFormController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/definition/BpmUserGroupController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/definition/BpmProcessExpressionController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/definition/BpmProcessListenerController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/definition/BpmModelController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/definition/BpmProcessDefinitionController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/task/BpmProcessInstanceController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/task/BpmTaskController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/task/BpmProcessInstanceCopyController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/oa/BpmOALeaveController.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/controller/admin/**/vo/**/*.java`

### Legacy production behavior source

- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/definition/BpmCategoryServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/definition/BpmFormServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/definition/BpmUserGroupServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/definition/BpmProcessExpressionServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/definition/BpmProcessListenerServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/definition/BpmModelServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/definition/BpmProcessDefinitionServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/task/BpmProcessInstanceServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/task/BpmTaskServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/task/BpmProcessInstanceCopyServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/oa/BpmOALeaveServiceImpl.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/oa/listener/BpmOALeaveStatusListener.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/message/BpmMessageServiceImpl.java`

### Flowable framework and listeners

- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/flowable/config/BpmFlowableConfiguration.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/flowable/core/listener/BpmTaskEventListener.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/flowable/core/listener/BpmProcessInstanceEventListener.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/flowable/core/enums/BpmnVariableConstants.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/flowable/core/util/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/flowable/core/candidate/**/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/task/listener/BpmCallActivityListener.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/task/listener/BpmUserTaskListener.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/service/task/trigger/BpmTrigger.java`

### Current DDD migration source

- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/domain/definition/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/domain/form/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/domain/usergroup/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/domain/expression/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/domain/listener/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/domain/leave/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/domain/copy/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/application/definition/BpmCategoryApplicationService.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/application/form/BpmFormApplicationService.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/application/usergroup/BpmUserGroupApplicationService.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/application/expression/BpmProcessExpressionApplicationService.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/application/listener/BpmProcessListenerApplicationService.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/application/leave/BpmOALeaveApplicationService.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/application/copy/BpmProcessInstanceCopyApplicationService.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/infrastructure/**/*.java`

### Data, mappers, converts, tests

- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/dal/dataobject/definition/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/dal/dataobject/oa/BpmOALeaveDO.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/dal/dataobject/task/BpmProcessInstanceCopyDO.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/dal/mysql/**/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/convert/definition/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/convert/task/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/convert/message/BpmMessageConvert.java`
- `develop-module-bpm/develop-module-bpm-server/src/test/java/com/develop/mvp/pk/module/bpm/framework/flowable/core/candidate/**/*.java`
- `develop-module-bpm/develop-module-bpm-server/src/test/java/com/develop/mvp/pk/module/bpm/service/category/BpmCategoryServiceImplTest.java`
- `develop-module-bpm/develop-module-bpm-server/src/test/java/com/develop/mvp/pk/module/bpm/service/definition/BpmFormServiceTest.java`
- `develop-module-bpm/develop-module-bpm-server/src/test/java/com/develop/mvp/pk/module/bpm/service/definition/BpmUserGroupServiceTest.java`
- `develop-module-bpm/develop-module-bpm-server/src/test/resources/application-unit-test.yaml`

## 5. Fixed Data Model

### Stable API and event DTOs

| Source | Field | Type | Meaning / Rules |
|---|---|---|---|
| `BpmProcessInstanceCreateReqDTO` | `processDefinitionKey` | `String` | 发起流程定义 key，内部 API 必填语义保持 |
| `BpmProcessInstanceCreateReqDTO` | `variables` | `Map<String, Object>` | 发起变量，必须透传给 Flowable |
| `BpmProcessInstanceCreateReqDTO` | `startUserSelectAssignees` | map-like | 发起人自选审批人，必须覆盖预测节点且用户存在 |
| `BpmProcessInstanceStatusEvent` | `id` | `String` | 流程实例 ID |
| `BpmProcessInstanceStatusEvent` | `processDefinitionKey` | `String` | 流程定义 key，业务监听器按 key 过滤 |
| `BpmProcessInstanceStatusEvent` | `businessKey` | `String` | 业务主键，如 OA 请假 id |
| `BpmProcessInstanceStatusEvent` | `status` | `Integer` | BPM 流程实例状态 |

### Definition aggregates

| Source | Field | Type | Meaning / Rules |
|---|---|---|---|
| `BpmCategoryDO` | `id` | `Long` | 分类主键；当前 DDD create(null) 与 `CategoryId` 非空冲突 |
| `BpmCategoryDO` | `name/code/description/status/sort` | `String/Integer` | 名称、编码唯一；删除前校验模型使用数 |
| `BpmFormDO` | `id/name/status/conf/fields/remark` | mixed | 动态表单配置；`fields` JSON 数组；必须校验 vModel 重复 |
| `BpmUserGroupDO` | `id/name/description/status/userIds` | mixed | 用户组；状态禁用时任务候选人校验失败 |
| `BpmProcessExpressionDO` | `id/name/status/expression` | mixed | 流程表达式；Spring EL 表达式管理 |
| `BpmProcessListenerDO` | `id/name/type/event/valueType/value/status` | mixed | 监听器；CLASS 类型校验类存在且实现接口，EXPRESSION 校验 `${}` 格式 |

### OA leave and copy

| Source | Field | Type | Meaning / Rules |
|---|---|---|---|
| `BpmOALeaveDO` | `id` | `Long` | 请假单主键；创建后用于 BPM businessKey |
| `BpmOALeaveDO` | `userId/type/reason/startTime/endTime/day` | mixed | 请假申请业务字段，更新状态时不得被占位值污染 |
| `BpmOALeaveDO` | `status` | `Integer` | 复用 BPM 流程状态，随流程实例事件联动 |
| `BpmOALeaveDO` | `processInstanceId` | `String` | Flowable 流程实例 ID；创建流程后回写 |
| `BpmProcessInstanceCopyDO` | `id/userId/processInstanceId/processDefinitionId/taskId/taskName/startUserId` | mixed | 抄送记录；与任务、流程实例、流程定义关联 |

### Flowable runtime concepts

| Concept | Source | Preservation rule |
|---|---|---|
| Model | `BpmModelServiceImpl`, Flowable `RepositoryService` | 模型 key/name/form/manager/deploy 校验保持 |
| ProcessDefinition | `BpmProcessDefinitionServiceImpl`, Flowable deployment | 定义部署、挂起、权限、分类、用户视图保持 |
| ProcessInstance | `BpmProcessInstanceServiceImpl`, `RuntimeService`, `HistoryService` | 发起、取消、状态事件、变量、业务 key 保持 |
| Task | `BpmTaskServiceImpl`, `TaskService`, `HistoryService`, `ManagementService` | 审批、拒绝、退回、委派、转办、加签、减签、撤回、评论、按钮权限保持 |
| Candidate strategy | `framework/flowable/core/candidate` | 部门/角色/岗位/用户/用户组/发起人/表达式/自选审批人策略保持 |

## 6. Required Method Signatures and Capabilities

Signatures here describe behavior parity with existing entry points; they do not mean Controller VO or Flowable classes belong in final domain entities.

### Stable API contract

Current contract must remain stable until a separate API migration plan exists:

```java
CommonResult<String> createProcessInstance(Long userId, BpmProcessInstanceCreateReqDTO reqDTO);
```

`BpmProcessInstanceApi` currently carries `@FeignClient`; local/remote split must produce a stable API/CommonApi plus `remote/*RemoteClient` without changing method semantics.

### Definition capabilities

```java
Long createCategory(...);
void updateCategory(...);
void deleteCategory(Long id);
void updateCategorySortBatch(List<Long> ids);
Long createForm(...);
void updateForm(...);
void deleteForm(Long id);
void validateFormFields(...);
Long createUserGroup(...);
void updateUserGroup(...);
void deleteUserGroup(Long id);
void validateUserGroups(Collection<Long> ids);
Long createProcessExpression(...);
void updateProcessExpression(...);
void deleteProcessExpression(Long id);
Long createProcessListener(...);
void updateProcessListener(...);
void deleteProcessListener(Long id);
void validateListenerValue(Integer type, Integer valueType, String value);
```

### Flowable model/definition capabilities

```java
String createModel(...);
void updateModel(...);
void updateModelState(String id, Integer state);
void updateModelBpmn(String id, String bpmnXml);
void updateModelSortBatch(List<String> ids);
String deployModel(String id);
void deleteModel(String id);
void validateBpmnModel(...);
void updateProcessDefinitionState(String id, Integer state);
List<?> getProcessDefinitionList(...);
```

### Process instance and task capabilities

```java
String createProcessInstance(Long userId, BpmProcessInstanceCreateReqDTO reqDTO);
void cancelProcessInstance(Long userId, String id, String reason);
void cancelProcessInstanceByAdmin(...);
void updateProcessInstanceExtStatus(...);
void approveTask(Long userId, BpmTaskApproveReqVO reqVO);
void rejectTask(Long userId, BpmTaskRejectReqVO reqVO);
void returnTask(Long userId, BpmTaskReturnReqVO reqVO);
void delegateTask(Long userId, BpmTaskDelegateReqVO reqVO);
void transferTask(Long userId, BpmTaskTransferReqVO reqVO);
void createSignTask(Long userId, BpmTaskSignCreateReqVO reqVO);
void deleteSignTask(Long userId, BpmTaskSignDeleteReqVO reqVO);
void withdrawTask(Long userId, BpmTaskWithdrawReqVO reqVO);
void createCopy(...);
```

### OA leave capabilities

```java
Long createLeave(Long userId, BpmOALeaveCreateReqVO reqVO);
void updateLeaveStatus(Long id, Integer status);
PageResult<BpmOALeaveDO> getLeavePage(...);
```

## 7. Business Rules

### Definition and model rules

| ID | Rule | Current source | Must preserve |
|---|---|---|---|
| BPM-DEF-01 | 分类名称和编码唯一，删除前校验没有模型使用该分类 | `BpmCategoryServiceImpl`, `BpmCategoryApplicationService` | `CATEGORY_*` 错误码保持 |
| BPM-DEF-02 | 表单字段 vModel 不得重复 | `BpmFormServiceImpl` | `FORM_FIELD_REPEAT` 参数保持 |
| BPM-DEF-03 | 用户组存在且启用才可作为候选人 | `BpmUserGroupServiceImpl`, candidate strategies | 禁用抛 `USER_GROUP_IS_DISABLE` |
| BPM-DEF-04 | 监听器 CLASS 类型必须类存在且实现对应接口，EXPRESSION 必须是合法 `${}` | `BpmProcessListenerServiceImpl/ApplicationService` | `PROCESS_LISTENER_*` 错误码保持 |
| BPM-MODEL-01 | 模型 key 格式合法且唯一 | `BpmModelServiceImpl` | `MODEL_KEY_VALID`, `MODEL_KEY_EXISTS` 保持 |
| BPM-MODEL-02 | 部署前 BPMN 必须有 StartEvent、UserTask 名称、表单配置、任务候选人配置 | `BpmModelServiceImpl#deployModel/validateBpmnModel` | 部署失败错误码保持 |
| BPM-MODEL-03 | 首个用户任务不能使用“审批人自选”策略 | `BpmModelServiceImpl` | `MODEL_DEPLOY_FAIL_FIRST_USER_TASK_CANDIDATE_STRATEGY_ERROR` 保持 |
| BPM-MODEL-04 | 当前 `updateModelSortBatch` 可能漏更新 index=0，这是现有代码风险，不得在无测试时顺手修复 | `BpmModelServiceImpl` | 需单独测试/计划 |

### Process instance rules

| ID | Rule | Current source | Must preserve |
|---|---|---|---|
| BPM-PI-01 | 发起流程前校验流程定义存在、未挂起、用户有发起权限 | `BpmProcessInstanceServiceImpl` | `PROCESS_DEFINITION_*`, `PROCESS_INSTANCE_START_USER_CAN_START` 保持 |
| BPM-PI-02 | 发起人自选审批人必须覆盖预测节点且用户存在 | `BpmProcessInstanceServiceImpl` | `PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_*` 保持 |
| BPM-PI-03 | 流程变量必须合并业务变量、发起人、候选人、表单变量，不能丢历史变量 | `BpmnVariableConstants`, process/task service | 变量 key 保持 |
| BPM-PI-04 | 取消流程必须校验流程运行中、发起人本人或管理员权限、流程允许取消 | `BpmProcessInstanceServiceImpl` | `PROCESS_INSTANCE_CANCEL_*` 保持 |
| BPM-PI-05 | 流程状态变化必须发布 `BpmProcessInstanceStatusEvent`，业务监听器按 processDefinitionKey/businessKey 联动 | `BpmProcessInstanceEventPublisher`, API event listeners | 事件字段保持 |

### Task rules

| ID | Rule | Current source | Must preserve |
|---|---|---|---|
| BPM-TASK-01 | 审批/拒绝/退回/转办/委派等操作必须校验当前用户是任务处理人且任务未挂起 | `BpmTaskServiceImpl` | `TASK_OPERATE_FAIL_ASSIGN_NOT_SELF`, `TASK_IS_PENDING` 保持 |
| BPM-TASK-02 | 审批签名必填，审批意见必填 | `BpmTaskServiceImpl` | `TASK_SIGNATURE_NOT_EXISTS`, `TASK_REASON_REQUIRE` 保持 |
| BPM-TASK-03 | 审批时必须保留/合并历史变量，处理发起人自选下一个节点审批人 | `BpmTaskServiceImpl` | 避免变量丢失 |
| BPM-TASK-04 | 退回只能退到串行可达且历史已走过节点，不能退到并行网关或非同一路线 | `BpmTaskServiceImpl` + `BpmnModelUtils` | `TASK_RETURN_FAIL_SOURCE_TARGET_ERROR` 保持 |
| BPM-TASK-05 | 委派/转办目标用户必须存在且不能与当前审批人相同 | `BpmTaskServiceImpl` | `TASK_DELEGATE_*`, `TASK_TRANSFER_*` 保持 |
| BPM-TASK-06 | 加签用户必须存在且不能重复；减签任务必须来自加签父任务 | `BpmTaskServiceImpl`, `ManagementService` | `TASK_SIGN_*` 保持 |
| BPM-TASK-07 | 撤回要求流程仍运行、已办任务存在、下一节点未办且满足撤回条件 | `BpmTaskServiceImpl` | `TASK_WITHDRAW_*` 保持 |
| BPM-TASK-08 | 任务 created/assigned/completed/cancelled/timer Flowable 事件必须触发消息、候选人、状态扩展逻辑 | `BpmTaskEventListener` | listener 行为保持 |

### OA leave and copy rules

| ID | Rule | Current source | Must preserve |
|---|---|---|---|
| BPM-OA-01 | OA 请假先落库，再通过内部 BPM API 发起流程，最后回写 `processInstanceId` | `BpmOALeaveServiceImpl` | 顺序和 process key `oa_leave` 保持 |
| BPM-OA-02 | 流程实例状态事件按 process key 过滤后更新请假状态 | `BpmOALeaveStatusListener` | 事件联动保持 |
| BPM-OA-03 | 当前 DDD `updateStatus` 有占位字段污染风险，不能直接替换 legacy | `BpmOALeaveApplicationService`, repository | 需专用 updateStatus 仓储方法 |
| BPM-COPY-01 | 抄送记录关联任务、流程实例、流程定义和发起人 | `BpmProcessInstanceCopyServiceImpl/ApplicationService` | 分页和用户维度查询保持 |

## 8. Error Code Contract

Use `develop-module-bpm/develop-module-bpm-api/src/main/java/com/develop/mvp/pk/module/bpm/enums/ErrorCodeConstants.java` as source of truth.

| Scenario | ErrorCodeConstants | Preservation rule |
|---|---|---|
| 请假单不存在 | `OA_LEAVE_NOT_EXISTS` | OA 查询/状态更新保持 |
| 模型 key 重复/非法 | `MODEL_KEY_EXISTS`, `MODEL_KEY_VALID` | 参数和格式校验保持 |
| 模型不存在 | `MODEL_NOT_EXISTS` | Flowable model 查询/更新/部署保持 |
| 部署表单未配置 | `MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG` | 保持部署前置校验 |
| 用户任务候选人未配置 | `MODEL_DEPLOY_FAIL_TASK_CANDIDATE_NOT_CONFIG` | 参数为任务名 |
| BPMN 无开始事件 | `MODEL_DEPLOY_FAIL_BPMN_START_EVENT_NOT_EXISTS` | 保持 |
| 用户任务名称缺失 | `MODEL_DEPLOY_FAIL_BPMN_USER_TASK_NAME_NOT_EXISTS` | 参数为任务 id/标识 |
| 非管理员操作模型 | `MODEL_UPDATE_FAIL_NOT_MANAGER` | 保持管理员校验 |
| 首节点审批人自选错误 | `MODEL_DEPLOY_FAIL_FIRST_USER_TASK_CANDIDATE_STRATEGY_ERROR` | 保持 |
| 流程定义 key/name 不匹配 | `PROCESS_DEFINITION_KEY_NOT_MATCH`, `PROCESS_DEFINITION_NAME_NOT_MATCH` | BPMN 与模型校验保持 |
| 流程定义不存在/挂起 | `PROCESS_DEFINITION_NOT_EXISTS`, `PROCESS_DEFINITION_IS_SUSPENDED` | 发起流程前校验保持 |
| 流程实例不存在 | `PROCESS_INSTANCE_NOT_EXISTS` | 查询/取消/任务联动保持 |
| 取消流程不存在/非本人/不允许 | `PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS`, `PROCESS_INSTANCE_CANCEL_FAIL_NOT_SELF`, `PROCESS_INSTANCE_CANCEL_FAIL_NOT_ALLOW` | 注意 `PROCESS_INSTANCE_CANCEL_FAIL_NOT_ALLOW` 与 `PROCESS_INSTANCE_START_USER_CAN_START` 当前码值重复，保留现状除非单独迁移 |
| 发起权限不足 | `PROCESS_INSTANCE_START_USER_CAN_START` | 当前码值 `1_009_004_005` 保持 |
| 发起人自选审批人缺失/不存在 | `PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_CONFIG`, `PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_EXISTS` | 保持 task name/user 参数 |
| HTTP 调用失败 | `PROCESS_INSTANCE_HTTP_CALL_ERROR` | HTTP task/trigger 保持 |
| 下个任务自选审批人缺失 | `PROCESS_INSTANCE_APPROVE_USER_SELECT_ASSIGNEES_NOT_CONFIG` | 审批时保持 |
| 任务非本人/不存在/挂起 | `TASK_OPERATE_FAIL_ASSIGN_NOT_SELF`, `TASK_NOT_EXISTS`, `TASK_IS_PENDING` | 所有任务操作保持 |
| 退回目标错误 | `TASK_TARGET_NODE_NOT_EXISTS`, `TASK_RETURN_FAIL_SOURCE_TARGET_ERROR` | 退回逻辑保持 |
| 委派/转办/加减签错误 | `TASK_DELEGATE_*`, `TASK_TRANSFER_*`, `TASK_SIGN_*` | 保持用户存在/重复/父任务校验 |
| 签名/意见缺失 | `TASK_SIGNATURE_NOT_EXISTS`, `TASK_REASON_REQUIRE` | 审批/拒绝保持 |
| 撤回失败 | `TASK_WITHDRAW_*` | 保持运行中、已办、下一任务条件 |
| 表单不存在/字段重复 | `FORM_NOT_EXISTS`, `FORM_FIELD_REPEAT` | 保持 vModel 校验 |
| 用户组不存在/禁用 | `USER_GROUP_NOT_EXISTS`, `USER_GROUP_IS_DISABLE` | 候选人策略保持 |
| 分类不存在/重复/被模型使用 | `CATEGORY_*` | 保持 |
| 监听器不存在/类错误/表达式错误 | `PROCESS_LISTENER_*` | 保持 class/expression 校验 |
| 表达式不存在 | `PROCESS_EXPRESSION_NOT_EXISTS` | 保持 |

## 9. Transaction Contract

| Use case | Current transaction | Flowable/external scope | Preservation rule |
|---|---|---|---|
| Category/Form/UserGroup/Expression/Listener create/update/delete | legacy/application methods often transactional or mapper writes | Mapper + event publisher | 保持唯一性、删除校验、事件发布 |
| Model deploy/update/delete/sort | `@Transactional` in legacy service paths | Flowable `RepositoryService`, BPMN validation | 不能半部署半失败 |
| Process definition state update | legacy service | Flowable repository/deployment | 保持挂起/激活语义 |
| Process instance create/cancel | `@Transactional` | `RuntimeService`, process variables, event publisher, user/dept APIs | 保持 Flowable 与扩展数据一致 |
| Task approve/reject/return/delegate/transfer/sign/withdraw | `@Transactional` | `TaskService`, `RuntimeService`, `HistoryService`, `ManagementService`, comments, variables, messages | 保持变量、评论、任务状态和消息一致 |
| OA leave create | `@Transactional` in legacy service | insert leave -> internal BPM API -> update processInstanceId | 顺序不可改 |
| OA leave status update | event listener calls service | process status event -> leave status | 避免 DDD 占位字段全量覆盖 |
| Copy create | service/application | task/process/definition lookup + mapper insert | 保持用户维度和任务维度关系 |

## 10. Flowable and Integration Contract

### Flowable APIs

- `RepositoryService` owns model, BPMN model, deployment and process definition operations.
- `RuntimeService` owns process instance start/cancel/state change and runtime variables.
- `TaskService` owns active task query, complete, delegate, transfer, comments and task variables.
- `HistoryService` owns historical task/process/activity data and approval trace.
- `ManagementService` is used for native SQL or parent task lookup in sign/delete-sign flows.
- `BpmnModel` and `BpmnModelUtils` are required for validation, returnable node calculation, candidate prediction, button permissions and task node metadata.

Flowable classes must not be imported by domain aggregate classes.

### Events and listeners

- `BpmFlowableConfiguration` wires Flowable event listeners.
- `BpmTaskEventListener` must preserve created/assigned/completed/cancelled/timer task behavior.
- `BpmProcessInstanceEventListener` must preserve process created/completed/cancelled behavior.
- `BpmProcessInstanceEventPublisher` publishes `BpmProcessInstanceStatusEvent` for business modules.
- `BpmProcessInstanceStatusEventListener` filters events and delegates to business `onEvent`.
- `BpmOALeaveStatusListener` currently uses legacy `BpmOALeaveService` and `BpmOALeaveServiceImpl.PROCESS_KEY`; migration must keep process key filtering.

### External APIs and security

- User/dept/post/role APIs are used by candidate strategies and approval display.
- Security user context and admin permissions in controllers must remain intact.
- Message service sends process approval/reject/task-created/task-timeout notifications.
- BPM API consumers such as CRM status listeners depend on `BpmProcessInstanceStatusEvent` field semantics.

## 11. Mapping Rules

1. Controller VO stays at controller boundary; domain and repository interfaces must not import `controller.admin.*.vo`.
2. API DTO and event DTO stay in `bpm-api`; stable fields must not change during DDD refactor.
3. Flowable objects (`Task`, `ProcessInstance`, `HistoricTaskInstance`, `BpmnModel`) stay in application/service/infrastructure/convert, not domain entities.
4. DO stays persistence model; domain cannot import `dal.dataobject` or Mapper.
5. Convert classes may map Flowable/DO/VO/DTO/domain, but must not contain domain decisions that belong in domain/application services.
6. Current infrastructure debt that must be fixed before claiming target architecture:
   - `BpmCategoryRepositoryImpl` uses `BpmCategoryPageReqVO` and `BpmModelService`.
   - `BpmFormRepositoryImpl` uses `BpmFormPageReqVO`.
   - `BpmUserGroupRepositoryImpl` uses `BpmUserGroupPageReqVO`.
   - `BpmProcessExpressionRepositoryImpl` uses `BpmProcessExpressionPageReqVO`.
   - `BpmProcessListenerRepositoryImpl` uses `BpmProcessListenerPageReqVO`.
   - `BpmOALeaveRepositoryImpl` uses `BpmOALeavePageReqVO`.
   - `BpmProcessInstanceCopyRepositoryImpl` uses `BpmProcessInstanceCopyPageReqVO`.
7. Insert-and-return flows must define ID strategy. Current `Factory.create(null, ...)` conflicts with non-null ID value objects.
8. Partial updates must not use placeholder domain objects plus full `save`; use dedicated repository update methods.

## 12. Current Conflict Notes

1. `BpmProcessInstanceApi` still has `@FeignClient` on the stable interface. API local/remote split is pending.
2. Seven DDD aggregates exist, but many `create(null, ...)` calls conflict with ID value objects that require non-null IDs.
3. DDD applications use `ApplicationEventPublisher` directly; there is no BPM `DomainEventPublisher` abstraction like other modules.
4. Infrastructure repositories depend on Controller PageReqVO; `BpmCategoryRepositoryImpl` also depends on legacy `BpmModelService`.
5. Flowable core (`BpmModelServiceImpl`, `BpmProcessDefinitionServiceImpl`, `BpmProcessInstanceServiceImpl`, `BpmTaskServiceImpl`) remains legacy and is the production behavior source.
6. `BpmProcessInstanceCopyApplicationService` directly depends on legacy `BpmTaskService`, `BpmProcessInstanceService`, `BpmProcessDefinitionService`.
7. `BpmOALeaveApplicationService#updateStatus` can overwrite fields with placeholders; do not use it to replace legacy listener until fixed.
8. `ErrorCodeConstants` has duplicate numeric code `1_009_004_005` for `PROCESS_INSTANCE_START_USER_CAN_START` and `PROCESS_INSTANCE_CANCEL_FAIL_NOT_ALLOW`; preserve current external behavior unless separately migrated.
9. `BpmModelServiceImpl#updateModelSortBatch` may skip index 0; do not silently fix without regression test and approval.

## 13. Acceptance Criteria

### Architecture AC

- Domain classes have no Spring/MyBatis/Mapper/DO/Controller VO/Flowable/remote API imports.
- Repository interfaces live in `domain/{aggregate}/repository`.
- Repository implementations live in `infrastructure/{aggregate}` and no longer construct Controller PageReqVO in target code.
- Application services own transactions, Flowable adapters, external API ports, events and messages.
- Controller/API implementations delegate to application/compatibility service without direct Mapper/Flowable orchestration.

### Behavior AC

- Model create/update/deploy/delete/sort behavior matches legacy service.
- Process definition query/state behavior matches legacy service.
- Process instance create/cancel/status event behavior matches legacy service.
- Task approve/reject/return/delegate/transfer/sign/delete-sign/withdraw behavior matches legacy service.
- OA leave create/status listener behavior matches legacy service.
- Copy records preserve task/process/definition/user relationships.
- Candidate strategies and BPMN model utilities keep existing user/dept/role/post/user-group/expression behavior.
- Error codes and trigger conditions match `ErrorCodeConstants`.

### Compile/Test AC

- BPM API module compiles.
- BPM server compiles.
- Existing candidate and legacy service tests pass.
- Any migrated use case has regression tests for success, illegal state, error code, Flowable call boundary, event publication and mapping behavior.

## 14. Verification Commands

For this skill document only:

```bash
git diff --check -- .claude/ddd-skills/AggregateRoot_Bpm_Skill.md
grep -n "^## " .claude/ddd-skills/AggregateRoot_Bpm_Skill.md
```

For API/compile work:

```bash
mvn compile -pl develop-module-bpm/develop-module-bpm-api -am -DskipTests
mvn compile -pl develop-module-bpm/develop-module-bpm-server -am -DskipTests
```

For current tests:

```bash
mvn test -pl develop-module-bpm/develop-module-bpm-server -Dtest=BpmCategoryServiceImplTest
mvn test -pl develop-module-bpm/develop-module-bpm-server -Dtest=BpmFormServiceTest
mvn test -pl develop-module-bpm/develop-module-bpm-server -Dtest=BpmUserGroupServiceTest
mvn test -pl develop-module-bpm/develop-module-bpm-server -Dtest=BpmTaskCandidateInvokerTest
mvn test -pl develop-module-bpm/develop-module-bpm-server -Dtest=BpmTaskCandidate*Test
```

Required regression names when implementing migration:

- `createCategory_generatesNonNullIdAndRejectsDuplicateCode`
- `repositoryFindPage_doesNotDependOnControllerPageReqVO`
- `oaLeaveUpdateStatus_updatesOnlyStatusAndDoesNotOverwriteBusinessFields`
- `createProcessInstance_startUserSelectAssigneesMissing_throwsConfiguredError`
- `deployModel_withoutStartEvent_throwsModelDeployFailBpmnStartEventNotExists`
- `approveTask_missingSignature_throwsTaskSignatureNotExists`
- `returnTask_parallelOrUnreachableTarget_throwsTaskReturnFailSourceTargetError`
- `withdrawTask_nextTaskCompleted_throwsTaskWithdrawFailNextTaskNotAllow`
- `processInstanceStatusEvent_preservesBusinessKeyAndDefinitionKey`

## 15. Quick Reference

| Task | Correct location | Forbidden location |
|---|---|---|
| HTTP params/auth/response | `controller` | domain/repository |
| Stable cross-module contract | `bpm-api` | server service/domain |
| Feign client identity | `api/.../remote` after split | stable API/CommonApi |
| Flowable calls | application/service/infrastructure adapter | domain entity |
| Task approval rules | application/domain service with Flowable adapter | controller |
| Candidate strategy | `framework/flowable/core/candidate` or application port | entity constructor |
| Process status business events | API event + publisher/listener | Controller side effects |
| Mapper/DO persistence | infrastructure/dal | domain/application API contract |
| Page query VO | controller boundary only | repository implementation |
| Partial status update | dedicated repository update method | placeholder domain + full save |

## 16. Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| Treat current DDD app as production replacement | Loses Flowable behavior and may NPE on null ID | Close gaps first, then switch entry point |
| Move Flowable `TaskService` into domain | Domain becomes technical and untestable | Use application ports/adapters |
| Keep Controller PageReqVO in repository | Cross-layer dependency remains | Introduce application query object |
| Use `save` for status-only updates | Overwrites fields with placeholders | Add dedicated update method |
| “Fix” duplicate error code while refactoring | Breaks external assumptions | Separate migration only |
| Skip BPMN model validation tests | Deploy accepts invalid models | Preserve validation and tests |
| Ignore history variables on approve | Subsequent nodes lose context | Merge variables as legacy does |
| Rewrite task return/withdraw by simple status checks | Allows illegal jumps or invalid withdraw | Preserve BpmnModel/history logic |

## 17. Rationalization Table

| Excuse | Reality |
|---|---|
| “BPM already has DDD folders, so it is production-ready.” | Current DDD has null-ID conflicts, repository VO leaks and incomplete Flowable coverage. |
| “Flowable is infrastructure; domain can call it directly for convenience.” | Flowable is a technical engine and must stay outside domain entities. |
| “Approval is just completing a task.” | Approval includes signatures, opinions, variables, next-node candidates, comments, messages and listeners. |
| “Repository PageReqVO reuse is harmless.” | It locks infrastructure to controller contracts and blocks clean DDD layering. |
| “OA status update only changes status.” | Current DDD implementation may overwrite business fields; it needs dedicated update. |
| “Duplicate error code is a bug, fix it now.” | It is an external contract change; fix only in a separate migration. |

## 18. Red Flags

Stop the batch if any of these occur:

- A change modifies Controller path, HTTP method, VO/DTO field, `BpmProcessInstanceApi` semantics, permission, or event payload without a migration plan.
- Domain imports Spring, MyBatis, Mapper, DO, Controller VO, Flowable API, or remote API.
- Flowable task/process behavior is replaced by simple status fields without preserving `TaskService`/`RuntimeService`/`HistoryService`/`BpmnModel` logic.
- `BpmProcessInstanceStatusEvent` field semantics or listener filtering changes.
- Candidate strategy tests are skipped after candidate or task changes.
- `create(null, ...)` remains in a path that constructs non-null ID value objects.
- Repository still constructs Controller PageReqVO after claiming DDD target architecture.
- Partial update uses placeholder aggregate plus full save.

## 19. Rollback Conditions

Rollback or stop and repair if:

1. `develop-module-bpm-api` or `develop-module-bpm-server` fails to compile after the batch.
2. Existing BPM candidate or service tests fail unexpectedly.
3. Flowable model deployment validation changes.
4. Process instance event publication or business listener behavior changes.
5. Task approve/reject/return/delegate/transfer/sign/withdraw behavior changes without explicit migration.
6. API/Controller contracts, error codes, permissions, or event fields change unexpectedly.
7. OA leave or copy data is overwritten or loses process/task links.

## 20. AI Self-Check

Before claiming a BPM refactor is complete, verify:

- [ ] I read current source anchors for the touched use case.
- [ ] I preserved Controller/API/event external contracts.
- [ ] I preserved exact `ErrorCodeConstants` trigger conditions for changed flows.
- [ ] I did not move Flowable APIs into domain.
- [ ] I preserved transaction boundaries, process variables, comments, messages and listeners.
- [ ] I preserved candidate strategies and BPMN model utilities.
- [ ] I fixed or explicitly documented null-ID and repository PageReqVO debt touched in this batch.
- [ ] I avoided placeholder aggregate full-save for partial updates.
- [ ] I ran the relevant Maven compile/test commands fresh.
- [ ] If current code conflicts with this skill, I updated the skill before changing code.
