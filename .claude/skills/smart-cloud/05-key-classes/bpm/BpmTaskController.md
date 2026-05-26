---
name: BpmTaskController
description: Workflow task controller providing approve, reject, delegate, transfer, and sign operations
type: project
---

# BpmTaskController

## 功能定位

BpmTaskController 是工作流（Flowable）的任务管理入口 Controller，位于 `develop-module-bpm` 的 `controller.admin.task` 包下。它提供**流程任务实例的完整操作入口**，支撑审批流程的全生命周期管理。

核心职责：
- **任务查询**：待办任务（Todo）、已办任务（Done）、全部任务（Manager）的分页查询
- **任务操作**：审批通过、驳回/退回、委派、转派、加签、减签、抄送、撤回
- **数据组装**：将 Flowable 引擎的 `Task`/`HistoricTaskInstance`/`ProcessInstance` 模型转换为业务 VO
- **权限控制**：通过 `@PreAuthorize` 注解进行功能权限控制

它是 BPM 模块中最核心的操作入口，流程审批的每个操作都需要经过此 Controller。

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Facade** | 编排 BpmTaskService、BpmProcessInstanceService、BpmProcessDefinitionService 等多个服务 | 注入 7 个依赖 |
| **Convert (Mapper)** | 将 Flowable 引擎模型转换为业务 VO | `BpmTaskConvert.INSTANCE` MapStruct 转换器 |
| **Command Pattern** | 每个任务操作封装为独立方法 | `approveTask`, `rejectTask`, `delegateTask`, `returnTask` 等 |
| **Data Mapper** | 批量组装关联数据 | `convertSet` + `getUserMap` + `getDeptMap` |

## 核心逻辑流程

### 待办任务分页 (`GET /bpm/task/todo-page`)

```
getTaskTodoPage(pageVO)
  |
  +-- @PreAuthorize("@ss.hasPermission('bpm:task:query')")
  |
  +-- 1. taskService.getTaskTodoPage(getLoginUserId(), pageVO)
  |    调用 Flowable TaskService:
  |      taskService.createTaskQuery()
  |        .taskAssignee(String.valueOf(userId))
  |        .active()
  |        .orderByTaskCreateTime().desc()
  |        .listPage(offset, limit)
  |    返回 PageResult<Task>
  |
  +-- 2. 如果结果为空 -> 返回空分页
  |
  +-- 3. 组装关联数据:
  |    +-- 3.1 查询流程实例 Map
  |    |    processInstanceMap = processInstanceService.getProcessInstanceMap(
  |    |        convertSet(tasks, Task::getProcessInstanceId))
  |    |    批量查询 Flowable RuntimeService
  |    |
  |    +-- 3.2 查询用户 Map (流程发起人)
  |    |    userMap = adminUserApi.getUserMap(
  |    |        convertSet(processInstanceMap, instance -> instance.getStartUserId()))
  |    |    Feign 调用 system-server
  |    |
  |    +-- 3.3 查询流程定义元数据 Map
  |    |    processDefinitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
  |    |        convertSet(tasks, Task::getProcessDefinitionId))
  |    |    从数据库查询 BpmProcessDefinitionInfoDO
  |
  +-- 4. BpmTaskConvert.INSTANCE.buildTodoTaskPage() 组装 VO
  +-- 5. 返回 PageResult<BpmTaskRespVO>
```

### 审批通过 (`PUT /bpm/task/approve`)

```
approveTask(reqVO)
  |
  +-- @PreAuthorize("@ss.hasPermission('bpm:task:update')")
  |
  +-- taskService.approveTask(getLoginUserId(), reqVO)
       |
       +-- 1. 校验: 任务存在, assignee 为当前用户
       +-- 2. 校验: 任务未挂起
       +-- 3. 设置审批意见 (comment)
       +-- 4. taskService.complete(taskId, variables)
       |    执行 Flowable API:
       |      taskService.addComment(taskId, processInstanceId, comment)
       |      runtimeService.setVariables(task.getExecutionId(), variables)
       |      taskService.complete(taskId)
       |    如果后续有节点，Flowable 自动推进
       +-- 5. 记录审批操作日志
```

### 驳回/退回 (`PUT /bpm/task/reject` / `PUT /bpm/task/return`)

- **reject**: 驳回（不通过），流程结束（或到指定的驳回节点）
- **return**: 退回，任务退回到指定节点重新审批

### 任务操作汇总

| 端点 | 方法 | 操作 | 说明 |
|------|------|------|------|
| `/approve` | PUT | 审批通过 | 完成任务，自动推送到下一节点 |
| `/reject` | PUT | 不通过 | 驳回任务，流程结束或退回上一步 |
| `/return` | PUT | 退回 | 退回到指定节点（从可退回节点列表中选择） |
| `/delegate` | PUT | 委派 | 委派给他人处理（委派人可处理，处理完后回到原处理人） |
| `/transfer` | PUT | 转派 | 转派给他人（完全移交处理权） |
| `/create-sign` | PUT | 加签 | 添加会签人（before 前加签/after 后加签） |
| `/delete-sign` | DELETE | 减签 | 删除会签人 |
| `/copy` | PUT | 抄送 | 抄送任务给他人（只读知会） |
| `/withdraw` | PUT | 撤回 | 撤回已提交的任务 |

## 关键代码剖析

```java
@Tag(name = "管理后台 - 流程任务实例")
@RestController
@RequestMapping("/bpm/task")
@Validated
public class BpmTaskController {

    @Resource
    private BpmTaskService taskService;
    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmFormService formService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("todo-page")
    @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<PageResult<BpmTaskRespVO>> getTaskTodoPage(@Valid BpmTaskPageReqVO pageVO) {
        PageResult<Task> pageResult = taskService.getTaskTodoPage(getLoginUserId(), pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 批量组装关联数据 (避免 N+1 查询)
        Map<String, ProcessInstance> processInstanceMap = processInstanceService.getProcessInstanceMap(
                convertSet(pageResult.getList(), Task::getProcessInstanceId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(processInstanceMap.values(), instance -> Long.valueOf(instance.getStartUserId())));
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), Task::getProcessDefinitionId));

        return success(BpmTaskConvert.INSTANCE.buildTodoTaskPage(
                pageResult, processInstanceMap, userMap, processDefinitionInfoMap));
    }

    @PutMapping("/approve")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> approveTask(@Valid @RequestBody BpmTaskApproveReqVO reqVO) {
        taskService.approveTask(getLoginUserId(), reqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> rejectTask(@Valid @RequestBody BpmTaskRejectReqVO reqVO) {
        taskService.rejectTask(getLoginUserId(), reqVO);
        return success(true);
    }
    // ... 其他操作方法
}
```

### 数据组装模式

Controller 中的查询接口使用统一的"先查 Flowable 引擎，再批量组装"的模式：

```
1. 查询 Flowable 引擎数据 (Task / HistoricTaskInstance / ProcessInstance)
   -> 提取 ID 集合 (convertSet)
   
2. 批量查询关联数据
   -> 流程实例: processInstanceService.getProcessInstanceMap()
   -> 用户信息: adminUserApi.getUserMap()   (Feign)
   -> 部门信息: deptApi.getDeptMap()        (Feign)
   -> 流程定义信息: processDefinitionService.getProcessDefinitionInfoMap()
   -> 表单信息: formService.getFormMap()

3. Convert 转换
   -> BpmTaskConvert.INSTANCE.buildXxxPage()
```

这种批量查询模式避免了循环中的 N+1 查询。

### Flowable 引擎中的 Task 状态

每个接口使用的 Flowable 引擎类型：

| 查询接口 | 引擎 Model | 表 | 说明 |
|----------|-----------|-----|------|
| todo-page | `Task` | ACT_RU_TASK | 运行中的任务 (runtime) |
| done-page | `HistoricTaskInstance` | ACT_HI_TASKINST | 已完成的历史任务 (history) |
| manager-page | `HistoricTaskInstance` | ACT_HI_TASKINST | 所有任务 (含运行中) |
| list-by-process-instance-id | `HistoricTaskInstance` | ACT_HI_TASKINST | 指定流程实例的所有任务 |

## 调用链

```
[Upstream]
  前端流程中心页面 / 第三方系统
    |
    +-- Gateway 路由
         |
         +-- BpmTaskController (本类)
              |
              +-- [查询] BpmTaskService
              |    -> Flowable TaskService (引擎 API)
              |    -> ACT_RU_TASK / ACT_HI_TASKINST
              |
              +-- [审批] BpmTaskService
              |    -> Flowable TaskService.complete()
              |    -> Flowable RuntimeService.setVariables()
              |    -> ACT_RU_TASK -> ACT_HI_TASKINST (完成时移动)
              |
              +-- [数据] BpmProcessInstanceService
              |    -> Flowable RuntimeService / HistoryService
              |
              +-- [用户] AdminUserApi (Feign -> system-server)
              +-- [部门] DeptApi (Feign -> system-server)
              +-- [表单] BpmFormService -> BpmFormMapper
              +-- [流程定义] BpmProcessDefinitionService -> BpmProcessDefinitionInfoMapper
```

## 配置与条件

| 端点 | 权限标识 | 说明 |
|------|----------|------|
| `todo-page` | `bpm:task:query` | 查看待办任务 |
| `done-page` | `bpm:task:query` | 查看已办任务 |
| `manager-page` | `bpm:task:manager-query` | 管理员查看全部任务 |
| `list-by-process-instance-id` | `bpm:task:query` | 查看流程实例的任务列表 |
| `approve`, `reject`, `return` | `bpm:task:update` | 审批操作 |
| `delegate`, `transfer` | `bpm:task:update` | 委派/转派 |
| `create-sign`, `delete-sign` | `bpm:task:update` | 加签/减签 |
| `copy` | `bpm:task:update` | 抄送 |
| `withdraw` | `bpm:task:update` | 撤回 |
| `list-by-return` | `bpm:task:update` | 获取可退回节点 |
| `list-by-parent-task-id` | `bpm:task:query` | 获取子任务列表 |

## 生产级关注点

### 1. Flowable 乐观锁

Flowable 引擎在处理并发任务操作时（如多个用户同时审批同一任务），使用**乐观锁**机制：
- `ACT_RU_TASK` 表有 `REV_` 版本号字段
- 每次更新时检查版本号，如果版本号不匹配则抛异常
- 异常信息为 `org.apache.ibatis.exceptions.PersistenceException: optimistic lock`
- 前端需要处理此异常并提示用户"任务已被处理"

### 2. N+1 查询防护

所有查询接口都使用"批量查询"模式，通过 `convertSet` 提取 ID 集合，然后一次性查询。这避免了对每个 Flowable 引擎对象逐个查询用户/部门的 N+1 问题。

### 3. Feign 调用

`AdminUserApi` 和 `DeptApi` 是 Feign 客户端，调用 system-server 获取用户和部门信息：
- 批量查询使用 `getUserMap(Set<Long>)` 一次获取多个用户
- 返回 `Map<Long, AdminUserRespDTO>` 方便按 ID 查找

如果 system-server 不可用，Feign 调用会抛异常。建议配置 Feign 的 fallback 或 circuit breaker（Sentinel）。

### 4. 任务操作的事务

`BpmTaskService` 中的任务操作方法（approve、reject、delegate 等）使用了 `@Transactional`：
- 审批操作涉及多个 Flowable API 调用 + 业务数据更新
- 需要保证原子性，避免部分成功部分失败

但 Flowable 的事务管理与 Spring 事务需要协调（Flowable 使用自己的事务管理器）。

### 5. 前端数据量大优化

对于待办任务较多的用户（如部门主管），`todo-page` 接口通过 Flowable 引擎的分页查询直接支持，不会在 Java 层面全量查询。

### 6. 任务操作的权限校验

所有任务操作（approve、reject 等）在 `BpmTaskService` 中都会再次校验：
1. 任务 assignee 是否为当前登录用户
2. 任务是否处于激活状态（未挂起、未完成）
3. 任务的流程实例是否运行中

@RequestMapping 上的 `@PreAuthorize` 是功能权限，Service 中的校验是数据权限。

### 7. 加签/减签的实现

- **加签**: 创建子任务（设置 `parentTaskId`），子任务审批完成后父任务才继续
- **减签**: 取消子任务（删除未完成的子任务），或从会签集合中移除
- 加签类型：`before`（前加签，先审批）和 `after`（后加签，最后审批）
