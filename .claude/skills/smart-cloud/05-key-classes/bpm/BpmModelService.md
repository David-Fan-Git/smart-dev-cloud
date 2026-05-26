---
name: BpmModelService
description: Flowable BPMN model management service for creating, updating, deploying, and versioning workflow models
type: project
---

# BpmModelService

## 功能定位

BpmModelServiceImpl 是流程模型的管理核心，位于 `develop-module-bpm` 的 `service.definition` 包下。它封装了 Flowable `RepositoryService`、`RuntimeService`、`HistoryService`、`TaskService` 等引擎 API，提供 BPMN 模型的全生命周期管理。

核心职责：
- **模型创建**：创建 Flowable Model，支持标准 BPMN 2.0 和仿钉钉"简单模式"
- **模型编辑**：更新模型元信息、BPMN XML、简单模式 JSON
- **模型部署**：将模型部署为可执行的流程定义（ProcessDefinition）
- **版本管理**：部署新版本时挂起旧版本
- **流程清理**：删除模型时清理所有相关的运行中和历史流程数据
- **状态管理**：激活/挂起流程定义

支持的两种设计器模式：
1. **BPMN 模式**: `BpmModelTypeEnum.BPMN`——标准 BPMN 2.0 XML 设计器
2. **SIMPLE 模式**: `BpmModelTypeEnum.SIMPLE`——仿钉钉快捷设计器（JSON 配置）

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Facade** | 封装 Flowable 多个引擎服务的复杂 API | 注入 RepositoryService, RuntimeService, HistoryService, TaskService |
| **State** | 管理流程定义状态 | `updateProcessDefinitionState(ACTIVATE/SUSPEND)` |
| **Template Method** | 保存模型作为模板方法 | `saveModel()` 处理 BPMN 和 SIMPLE 两种模式 |
| **Builder** | 构建 BpmnModel | `SimpleModelUtils.buildBpmnModel()` JSON -> BpmnModel |
| **Manager Validator** | 模型管理员权限校验 | `validateModelManager(id, userId)` |
| **Double Storage** | SIMPLE 模式双存储 | BPMN XML (可执行) + JSON (可编辑) |

## 核心逻辑流程

### 创建模型 (createModel)

```
createModel(createReqVO)
  |
  +-- 1. 校验: key 是否符合 XML NCName 规范
  |    ValidationUtils.isXmlNCName(key)
  |    不能包含空格、特殊符号、不能以数字开头
  |
  +-- 2. 校验: key 是否已存在
  |    getModelByKey(key) -> repositoryService.createModelQuery()
  |    .modelKey(key).singleResult()
  |    如果已存在 -> 抛异常 MODEL_KEY_EXISTS
  |
  +-- 3. 创建 Flowable Model
  |    repositoryService.newModel()
  |    BpmModelConvert.INSTANCE.copyToModel(model, createReqVO)
  |    model.setTenantId(FlowableUtils.getTenantId())  (多租户)
  |
  +-- 4. saveModel(model, createReqVO)
       |
       +-- 4.1 repositoryService.saveModel(model) [基本信息持久化到 ACT_RE_MODEL]
       |
       +-- 4.2 保存流程图:
            |
            +-- [BPMN 模式] && bpmnXml 非空:
            |    updateModelBpmnXml(modelId, bpmnXml)
            |    -> repositoryService.addModelEditorSource(modelId, xmlBytes)
            |    -> 存储到 ACT_RE_MODEL 的 EDITOR_SOURCE_VALUE_ID_ 字段
            |
            +-- [SIMPLE 模式] && simpleModel 非空:
                 +-- SimpleModelUtils.buildBpmnModel(key, name, simpleModel)
                 |    JSON 配置 -> BpmnModel 转换
                 +-- updateModelBpmnXml() 保存转换后的 BPMN XML
                 +-- updateModelSimpleJson() 保存原始 JSON
                      -> repositoryService.addModelEditorSourceExtra(modelId, jsonBytes)
                      -> 存储到 ACT_RE_MODEL 的 EDITOR_SOURCE_EXTRA_VALUE_ID_ 字段
```

### 部署模型 (deployModel)

```
deployModel(userId, id)
  |
  +-- 1. 校验管理员权限
  |    validateModelManager(id, userId)
  |    -> 解析 metaInfo.managerUserIds
  |    -> 校验当前用户是否在管理员列表中
  |
  +-- 2. 校验 BPMN XML 合法性
  |    validateBpmnXml(bpmnBytes, type)
  |    +-- BpmnModel 不为空
  |    +-- 包含 StartEvent
  |    +-- 所有 UserTask 有 name
  |    +-- 第一个 UserTask 候选策略 != "审批人自选"
  |
  +-- 3. 校验表单配置
  |    validateFormConfig(metaInfo)
  |    +-- 普通表单: 校验 formId 对应的 FormDO 存在
  |    +-- 自定义表单: 校验 formCustomCreatePath / formCustomViewPath 非空
  |
  +-- 4. 校验任务分配规则
  |    taskCandidateInvoker.validateBpmnConfig(bpmnBytes)
  |    -> 校验每个 UserTask 的候选人/候选组配置正确
  |
  +-- 5. 创建流程定义
  |    processDefinitionService.createProcessDefinition(model, metaInfo, bpmnBytes, simpleJson, form)
  |    -> repositoryService.createDeployment()
  |       .addBpmnModel(modelKey, bpmnModel)
  |       .deploy()
  |    -> 创建 ProcessDefinition
  |
  +-- 6. 挂起旧版本
  |    updateProcessDefinitionSuspended(model.getDeploymentId())
  |    -> 查找该 deploymentId 关联的旧流程定义
  |    -> 挂起 (SUSPENDED)，新流程才能发起
  |
  +-- 7. 更新 model 的 deploymentId
  |    model.setDeploymentId(newDeployment.getId())
  |    repositoryService.saveModel(model)
```

### 清理模型 (cleanModel)

```
cleanModel(userId, id)
  |
  +-- 1. 校验管理员权限
  |
  +-- 2. 取消所有运行中的流程实例
  |    runtimeService.createProcessInstanceQuery()
  |    .processDefinitionKey(model.getKey()).list()
  |    -> 逐条: runtimeService.deleteProcessInstance(id, reason)
  |    -> 逐条: historyService.deleteHistoricProcessInstance(id)
  |    -> 逐条: processInstanceCopyService.deleteProcessInstanceCopy(id)
  |
  +-- 3. 删除历史流程实例
  |    historyService.createHistoricProcessInstanceQuery()
  |    .processDefinitionKey(model.getKey()).list()
  |    -> 逐条删除历史 + 抄送记录
  |
  +-- 4. 清理任务
  |    taskService.createTaskQuery()
  |    .processDefinitionKey(model.getKey()).list()
  |    -> taskService.deleteTask(id, reason)
```

### 更新模型状态 (updateModelState)

```
updateModelState(userId, id, state)
  |
  +-- 1. 校验管理员权限
  +-- 2. 校验流程定义存在 (根据 model.deploymentId 查)
  +-- 3. processDefinitionService.updateProcessDefinitionState(definitionId, state)
       +-- state = ACTIVE -> repositoryService.activateProcessDefinitionById()
       +-- state = SUSPEND -> repositoryService.suspendProcessDefinitionById()
```

## 关键代码剖析

```java
@Service
@Validated
@Slf4j
public class BpmModelServiceImpl implements BpmModelService {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmFormService bpmFormService;
    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;
    @Resource
    private HistoryService historyService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;
```

### 保存模型详解 (saveModel)

```java
private void saveModel(Model model, BpmModelSaveReqVO saveReqVO) {
    // 1. 保存模型基础信息
    repositoryService.saveModel(model);

    // 2. 保存流程图
    if (ObjUtil.equals(BpmModelTypeEnum.BPMN.getType(), saveReqVO.getType())
            && StrUtil.isNotEmpty(saveReqVO.getBpmnXml())) {
        // BPMN 模式: 直接保存 BPMN XML
        updateModelBpmnXml(model.getId(), saveReqVO.getBpmnXml());
    } else if (ObjUtil.equals(BpmModelTypeEnum.SIMPLE.getType(), saveReqVO.getType())
            && saveReqVO.getSimpleModel() != null) {
        // SIMPLE 模式: JSON -> BPMN XML
        BpmnModel bpmnModel = SimpleModelUtils.buildBpmnModel(model.getKey(),
                model.getName(), saveReqVO.getSimpleModel());
        updateModelBpmnXml(model.getId(), BpmnModelUtils.getBpmnXml(bpmnModel));
        updateModelSimpleJson(model.getId(), saveReqVO.getSimpleModel());
    }
}
```

**SIMPLE 模式的双存储":**

| 存储位置 | 内容 | 用途 |
|----------|------|------|
| `EDITOR_SOURCE_VALUE_ID_` (BLOB) | BPMN XML | Flowable 引擎执行 |
| `EDITOR_SOURCE_EXTRA_VALUE_ID_` (BLOB) | JSON | 前端再次编辑 |

这样既保证了 Flowable 引擎可以执行 BPMN 模型，又保留了前端"仿钉钉"设计器可以继续编辑的原始数据结构。

### 部署校验清单

```java
private void validateBpmnXml(byte[] bpmnBytes, Integer type) {
    BpmnModel bpmnModel = BpmnModelUtils.getBpmnModel(bpmnBytes);
    if (bpmnModel == null) throw exception(MODEL_NOT_EXISTS);

    // 1. 必须有开始事件
    StartEvent startEvent = BpmnModelUtils.getStartEvent(bpmnModel);
    if (startEvent == null) throw exception(MODEL_DEPLOY_FAIL_BPMN_START_EVENT_NOT_EXISTS);

    // 2. 所有 UserTask 必须有名称
    List<UserTask> userTasks = BpmnModelUtils.getBpmnModelElements(bpmnModel, UserTask.class);
    userTasks.forEach(userTask -> {
        if (StrUtil.isEmpty(userTask.getName())) {
            throw exception(MODEL_DEPLOY_FAIL_BPMN_USER_TASK_NAME_NOT_EXISTS, userTask.getId());
        }
    });

    // 3. 第一个 UserTask 不能是"审批人自选"
    UserTask firUserTask = CollUtil.get(userTasks,
            BpmModelTypeEnum.BPMN.getType().equals(type) ? 0 : 1);
    if (firUserTask != null) {
        Integer candidateStrategy = parseCandidateStrategy(firUserTask);
        if (Objects.equals(candidateStrategy, BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT.getStrategy())) {
            throw exception(MODEL_DEPLOY_FAIL_FIRST_USER_TASK_CANDIDATE_STRATEGY_ERROR, firUserTask.getName());
        }
    }
}
```

部署前执行 **4 层校验**，确保部署的流程定义可正常执行。

## 调用链

```
[Upstream]
  BpmModelController (REST)
    -> BpmModelServiceImpl (本类)
       |
       +-- [模型 CRUD] RepositoryService
       |    -> ACT_RE_MODEL (Flowable 模型表)
       |
       +-- [部署] processDefinitionService.createProcessDefinition()
       |    -> repositoryService.createDeployment()
       |    -> ACT_RE_DEPLOYMENT + ACT_RE_PROCDEF
       |
       +-- [校验] taskCandidateInvoker.validateBpmnConfig()
       |    -> 校验每个 UserTask 的候选策略配置
       |
       +-- [校验] bpmFormService.getForm()
       |    -> BpmFormMapper -> MySQL
       |
       +-- [流程清理] RuntimeService
       |    -> ACT_RU_* 运行时表
       |
       +-- [历史清理] HistoryService
       |    -> ACT_HI_* 历史表
       |
       +-- [转换] SimpleModelUtils.buildBpmnModel()
       |    SIMPLE JSON -> BpmnModel 转换器
       |
       +-- [转换] BpmnModelUtils.getBpmnXml()
            BpmnModel -> XML 字符串转换器
```

## 配置与条件

| 配置/条件 | 说明 |
|-----------|------|
| `BpmModelTypeEnum.BPMN` | 标准 BPMN 2.0 XML 设计器模式 |
| `BpmModelTypeEnum.SIMPLE` | 仿钉钉简单模式设计器 |
| `BpmModelFormTypeEnum.NORMAL` | 关联系统表单（使用 formId） |
| `BpmModelFormTypeEnum.CUSTOM` | 自定义表单（前后端分离，使用 URL 路径） |
| `FlowableUtils.getTenantId()` | 多租户场景设置模型的 tenantId |

### 管理员校验

模型的编辑、部署、删除、状态变更都要求用户是模型的管理员：
```java
private Model validateModelManager(String id, Long userId) {
    Model model = validateModelExists(id);
    BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
    if (metaInfo == null || !CollUtil.contains(metaInfo.getManagerUserIds(), userId)) {
        throw exception(MODEL_UPDATE_FAIL_NOT_MANAGER, model.getName());
    }
    return model;
}
```

`managerUserIds` 保存在模型的 `metaInfo` JSON 中，在创建/编辑模型时设置。

## 生产级关注点

### 1. 版本管理策略

部署新版本模型时，框架会：
1. 创建新的 `ProcessDefinition`（新的 deployment）
2. **挂起旧的 ProcessDefinition**（通过 `model.deploymentId` 关联）
3. 新发起的流程实例使用最新版本
4. 正在运行的旧版本流程实例不受影响（继续运行）

这意味着：
- 部署新版本后，老版本不可发起新流程，但正在运行的流程继续
- 如果要强行将所有运行中的流程升级到新版本，需要额外的数据迁移操作

### 2. 管理员校验的必要性

流程模型的编辑和部署是高风险操作，因此设计了管理员校验机制：
- 只有 `metaInfo.managerUserIds` 列表中的用户才能操作
- 创建模型时自动将创建者加入管理员列表
- 可在编辑模型时修改管理员列表

### 3. SIMPLE 模式转换风险

`SimpleModelUtils.buildBpmnModel()` 将 JSON 配置转换为 BPMN 模型：
- 转换逻辑复杂（涉及节点类型、连线、条件表达式等）
- 如果 JSON 结构不正确，可能生成无效的 BPMN 模型
- 部署前的 validateBpmnXml 是最后一道防线

### 4. cleanModel 的破坏性

`cleanModel` 会删除所有与该模型 key 相关的流程数据：
- 正在运行的流程实例（runtime）
- 已完成的历史流程实例（history）
- 所有抄送记录
- 所有 Task

这是一个不可逆的操作，应在界面上给出明确的警告提示。

### 5. 部署原子性

`deployModel` 使用 `@Transactional`，但 Flowable 的部署操作（`repositoryService.createDeployment()`）有自己独立的事务管理，可能不与 Spring 事务同步。因此：
- 如果部署后 Spring 事务回滚，Flowable 的部署可能已经提交
- 这种情况需要手动处理（如删除已部署的流程定义）

### 6. 多租户支持

所有模型操作都设置了 `modelTenantId = FlowableUtils.getTenantId()`：
- 租户间模型隔离
- 查询模型时带 tenantId 过滤
- Flowable 自身支持多租户模型

### 7. 流程定义状态管理

流程定义有两种状态：
- **ACTIVE（激活）**: 可以发起新流程实例
- **SUSPEND（挂起）**: 不能发起新流程实例

状态变更通过 `repositoryService.activateProcessDefinitionById()` / `suspendProcessDefinitionById()` 实现。处于挂起状态的流程定义，尝试发起流程会抛出 `FlowableException`。

### 8. BPMN 校验细节

`validateBpmnXml` 中有一个特殊规则：**第一个用户任务的候选策略不能是"审批人自选"**。因为：
- 如果第一个审批人是自选的，发起人在提交流程前需要手动指定审批人
- 但发起人可能不知道应该选谁，导致流程卡住
- 所以强制第一个节点必须指定候选人/候选组
- BPMN 模式检查第 1 个 UserTask，SIMPLE 模式第 1 个固定为发起人所以检查第 2 个

### 9. 模型排序

`updateModelSortBatch` 使用时间戳作为排序值：
```java
long sort = System.currentTimeMillis();
for (int i = ids.size() - 1; i > 0; i--) {
    BpmModelMetaInfoVO metaInfo = ...setSort(sort);
    sort--;
}
```

从大到小排列，最新的模型排在最前面（sort 值最大）。
