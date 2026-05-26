---
name: aggregate-root-ai-skill
description: Use when upgrading, reviewing, or applying the AI model aggregate skill; treat it as draft until current source anchors, contracts, errors, transactions, and tests are verified.
type: ddd-aggregate-skill
status: draft
---

# DDD Skill: AggregateRoot_Ai_Model_Skill

## Production Readiness Boundary

本文件当前不是直接生产级重构指南。必须先按 `DDD_Skill_Production_Readiness_Standard.md` 补齐当前事实源、字段映射、错误码、事务边界、外部契约和验证命令；否则只能用于范围识别和升级 skill。

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

## 1. 技能名称

`AggregateRoot_Ai_Model_Skill` — AI 模型（AiModel）聚合根的领域建模与重构技能

## 2. 适用场景

本技能针对 **AI 模型配置** 的完整生命周期管理，覆盖以下业务操作：

- 创建模型（含平台校验 + API 密钥关联校验）
- 更新模型（含存在性 + 平台 + API 密钥校验）
- 删除模型
- 启用/禁用模型
- 模型查询（按ID、按类型+状态、分页、全量列表）
- 默认模型获取（按类型取 sort 最小的启用模型）
- 模型可用性校验（存在 + 未禁用）
- 模型与 Spring AI 框架集成（ChatModel / ImageModel / VectorStore 等）

## 3. DDD 构造块

### 3.1 聚合根：AiModel

```
com.develop.mvp.pk.module.ai.domain.model.AiModel
```

**角色**：AI 模型配置的领域聚合根，封装模型的完整生命周期和业务规则。

**聚合边界**：
- AiModel（根实体）
- 不包含：AiApiKey、AiChatRole、AiTool — 这些是外部聚合，仅通过 ID 引用

### 3.2 值对象（Value Objects）

| 值对象 | 类名 | 封装字段 | 不可变 | 自校验 |
|--------|------|---------|--------|--------|
| 模型ID | `AiModelId` | `Long value` | ✅ | 非空 |
| 模型状态 | `AiModelStatus` | `Integer code, String name` | ✅ | 只能是 ENABLE(0) / DISABLE(1) |
| 模型类型 | `AiModelType` | `Integer type, String name` | ✅ | CHAT(1)/IMAGE(2)/VOICE(3)/VIDEO(4)/EMBEDDING(5)/RERANK(6) |
| 模型平台 | `AiModelPlatform` | `String platform, String name` | ✅ | 必须是 AiPlatformEnum 有效值 |
| 模型标识 | `AiModelCode` | `String value` | ✅ | 非空（model 字段，如 "gpt-4o"） |
| 模型名称 | `AiModelName` | `String value` | ✅ | 非空 |
| 温度参数 | `AiModelTemperature` | `Double value` | ✅ | 0.0-2.0 范围 |
| Token上限 | `AiModelMaxTokens` | `Integer value` | ✅ | >0 |

### 3.3 仓储接口（Repository，领域层）

```
com.develop.mvp.pk.module.ai.domain.model.repository.AiModelRepository
```

```java
public interface AiModelRepository {
    AiModel save(AiModel model);
    void delete(AiModelId id);
    AiModel findById(AiModelId id);
    List<AiModel> findAll();
    List<AiModel> findByStatusAndType(AiModelStatus status, AiModelType type, AiModelPlatform platform);
    AiModel findFirstByStatus(AiModelType type, AiModelStatus status);
    PageResult<AiModel> findPage(AiModelPageQuery query);
}
```

### 3.4 仓储实现（RepositoryImpl，基础设施层）

```
com.develop.mvp.pk.module.ai.infrastructure.model.AiModelRepositoryImpl
```

委托给 `AiChatMapper`（MyBatis Plus BaseMapperX），负责：
- AiModel 聚合根 ↔ AiModelDO 的映射（通过 AiModelFactory.reconstitute）
- `findFirstByStatus` 的默认模型查询逻辑（按 sort ASC 取第一条）

### 3.5 领域服务（Domain Service）

| 领域服务 | 职责 | 原因 |
|---------|------|------|
| `AiModelPlatformValidator` | 校验平台字符串是否在 AiPlatformEnum 中合法 | 平台枚举是领域概念 |

### 3.6 领域事件（Domain Events）

| 事件 | 触发时机 | 携带数据 | 消费者 |
|------|---------|---------|--------|
| `AiModelCreatedEvent` | 模型创建成功后 | modelId, name, model, platform | 操作日志 |
| `AiModelDeletedEvent` | 模型删除成功后 | modelId, name, model | 清理关联配置 |

### 3.7 工厂（Factory）

```
com.develop.mvp.pk.module.ai.domain.model.AiModelFactory
```

- `create()`：创建新模型，默认状态 ENABLED
- `reconstitute()`：从持久化数据重建聚合根（仓储实现调用）

## 4. 职责边界

### 4.1 AiModel 聚合根必须负责的规则

| 规则编号 | 规则描述 | 对应原代码位置 |
|---------|---------|-------------|
| R01 | 创建时必须校验平台是 AiPlatformEnum 有效值 | `AiPlatformEnum.validatePlatform()` |
| R02 | 创建时必须校验 keyId 引用的 API Key 存在且启用 | `apiKeyService.validateApiKey()` |
| R03 | 更新时必须校验模型存在 | `validateModelExists()` L85-90 |
| R04 | 更新时必须重新校验平台和 API Key | `updateModel()` L67-70 |
| R05 | 删除时只需校验模型存在即可删除 | `deleteModel()` L79-83 |
| R06 | 模型状态只能是 ENABLE(0) 或 DISABLE(1) | AiModelStatus 值对象 |
| R07 | 获取默认模型：同 type 下 status=ENABLE 且 sort ASC 第一条 | `getRequiredDefaultModel()` L99-103 |
| R08 | 模型使用前必须校验：存在 + 状态为 ENABLE（不能 DISABLE） | `validateModel()` L113-117 |
| R09 | 模型按 sort 字段升序排列 | 所有列表查询的 `orderByAsc("sort")` |
| R10 | 名称(name)和模型标志(model)均为必填字段 | DO 字段非空 |
| R11 | 温度参数必须在合理范围内（0.0-2.0） | 隐式约束，前端/应用层校验 |
| R12 | 默认模型不存在时抛出 MODEL_DEFAULT_NOT_EXISTS 异常 | `getRequiredDefaultModel()` L101-103 |

### 4.2 严禁外泄的职责（不可放在 AiModel 聚合内）

| 禁止行为 | 原因 | 应由谁处理 |
|---------|------|----------|
| 直接操作数据库/调用 AiChatMapper | 破坏持久化无关性 | Repository 实现 |
| 校验 API Key 是否存在且启用 | AiApiKey 是独立聚合 | ApplicationService 或 ApiKeyDomainService |
| 创建 ChatModel/ImageModel/EmbeddingModel 等 Spring AI 对象 | 框架适配层职责 | ApplicationService 或独立的 ProviderFactory |
| 处理 UI 层的 VO 转换 | 表示层关注点 | Controller/Convert |
| 调用外部 AI API（如 MidjourneyApi, SunoApi） | 基础设施关注点 | ApplicationService |
| 设置 TinyFlow LLM Provider | 框架集成职责 | ApplicationService 或框架层 |

## 5. 依赖与协作

### 5.1 领域层依赖（向内）

AiModel 聚合根仅依赖：
- 自身值对象（AiModelId, AiModelStatus, AiModelName, AiModelCode, AiModelPlatform, AiModelType, AiModelTemperature, AiModelMaxTokens）
- 仓储接口（AiModelRepository）
- 领域事件发布器（DomainEventPublisher）

### 5.2 跨聚合协作（仅通过 ID 引用）

| 外部聚合 | 引用方式 | 协作场景 |
|---------|---------|---------|
| AiApiKey（API密钥） | `keyId: Long` | 创建/更新时校验存在性（应用层负责） |

### 5.3 基础设施依赖（向外，通过接口倒置）

```
领域层定义接口                   基础设施层实现
─────────────                   ──────────────
AiModelRepository         ←──    AiModelRepositoryImpl (委托 AiChatMapper)
DomainEventPublisher      ←──    SpringDomainEventPublisher
```

## 6. 不变式与约束（Invariants）

| 编号 | 不变式 | 类型 | 验证点 |
|------|--------|------|--------|
| I01 | `model`（模型标志）在全局不可重复（同平台+同类型下） | 跨聚合唯一性 | 创建时（应用层校验） |
| I02 | `status` 只能是 ENABLE 或 DISABLE | 聚合内部（值对象） | 状态变更时 |
| I03 | `keyId` 必须引用一个存在且启用的 API Key | 跨聚合约束 | 创建/修改时（应用层校验） |
| I04 | `platform` 必须是 AiPlatformEnum 中定义的有效平台 | 聚合内部（值对象） | 创建/修改时 |
| I05 | `type` 必须是 AiModelTypeEnum 定义的有效类型 | 聚合内部（值对象） | 创建/修改时 |
| I06 | 同一 type 下，默认模型是 status=ENABLE 且 sort 最小的那个 | 聚合外部（查询规则） | 获取默认模型时 |
| I07 | 禁用的模型不能被用于任何 AI 操作（聊天/图片/音乐等） | 聚合外部（应用层） | 模型使用时 |
| I08 | 删除模型后，该模型的配置数据不再可用 | 聚合内部 | 删除后 |

## 7. 验收标准

| 编号 | 验收标准 | 验证方法 |
|------|---------|---------|
| AC01 | AiModel 类不包含任何 MyBatis/Spring 注解 | 代码审查 |
| AC02 | AiModel 类不直接注入或调用 Mapper/Repository 实现类 | 代码审查 |
| AC03 | AiModelId, AiModelStatus 为不可变值对象（final class, final 字段, 无 setter） | 代码审查 |
| AC04 | AiModelRepository 接口定义在领域层包，不 import MyBatis 类 | 代码审查 |
| AC05 | AiModelRepositoryImpl 在基础设施层（infrastructure.model），负责 DO↔领域模型映射 | 代码审查 |
| AC06 | AiModel 聚合的公共方法名称体现业务语义（enable, disable, updateProfile, markDeleted） | 代码审查 |
| AC07 | AiModelApplicationService 负责编排，聚合根负责业务规则 | 代码审查 |
| AC08 | Controller 注入 AiModelApplicationService 而非旧 Service | 代码审查 |
| AC09 | AiModelController 使用领域对象 AiModel 而非 AiModelDO | 代码审查 |
| AC10 | 编译通过 | 运行 `mvn compile -pl develop-module-ai/develop-module-ai-server -am` |
| AC11 | 创建时校验平台有效性（R01） | 单元测试 |
| AC12 | 禁用模型不能被使用（R08） | 单元测试 |
| AC13 | 获取默认模型返回 sort 最小的启用模型（R07） | 单元测试 |

## 8. 目录结构规划（重构后）

```
develop-module-ai/develop-module-ai-server/src/main/java/com/develop/mvp/pk/module/ai/
├── domain/model/
│   ├── AiModel.java                    # 聚合根
│   ├── AiModelFactory.java             # 工厂
│   ├── valueobject/
│   │   ├── AiModelId.java              # ID 值对象
│   │   ├── AiModelStatus.java          # 状态值对象
│   │   ├── AiModelType.java            # 类型值对象（新增）
│   │   └── AiModelPlatform.java        # 平台值对象（新增）
│   ├── event/
│   │   ├── DomainEvent.java
│   │   ├── DomainEventPublisher.java
│   │   ├── AiModelCreatedEvent.java
│   │   └── AiModelDeletedEvent.java
│   └── repository/
│       ├── AiModelRepository.java      # 仓储接口
│       └── AiModelPageQuery.java       # 分页查询对象
├── application/model/
│   └── AiModelApplicationService.java  # 应用编排服务
├── infrastructure/model/
│   └── AiModelRepositoryImpl.java      # MyBatis 仓储实现
├── controller/admin/model/
│   └── AiModelController.java          # REST 控制器
├── dal/dataobject/model/
│   └── AiModelDO.java                  # 数据对象（保留）
└── dal/mysql/model/
    └── AiChatMapper.java              # MyBatis Mapper（保留）
```

## 9. 回滚条件

如果以下任一情况发生，回滚当前修改：

1. 编译失败
2. AiModel 聚合根内部注入了基础设施依赖
3. 值对象存在 setter 或可变字段
4. 业务规则从聚合根泄漏回 ApplicationService 或旧 Service
5. 现有控制器端点的行为出现回归（CRUD 操作结果不一致）
6. 默认模型查询逻辑丢失或变更

## 10. 分步执行计划

**阶段 1**：补充缺失值对象（AiModelType, AiModelPlatform），确保自校验逻辑
**阶段 2**：验证现有 AiModel 聚合根、工厂、仓储接口是否覆盖所有业务规则（R01-R12）
**阶段 3**：验证 AiModelRepositoryImpl 的 DO↔Domain 映射完整性
**阶段 4**：更新 AiModelApplicationService，补齐缺失方法（validateModel 可用性校验），迁移 Spring AI 集成编排逻辑
**阶段 5**：适配 AiModelController，改为注入 AiModelApplicationService，使用 AiModel 领域对象
**阶段 6**：编译验证
