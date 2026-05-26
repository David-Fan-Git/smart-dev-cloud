---
name: aggregate-root-module-draft-skill
description: Use when triaging placeholder DDD aggregate skills for modules that do not yet have production-ready current-code anchors.
type: ddd-aggregate-skill
status: draft
---

# DDD Skill Draft: AggregateRoot_Module_Draft_Skill

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

## Status

这是 ERP、IOT、MES、MP、Report 等模块旧占位 skill 的统一草稿，不是生产级重构指南。

使用它之前必须先阅读并满足 `DDD_Skill_Production_Readiness_Standard.md`。未补齐事实源路径、字段映射、错误码、事务边界、外部契约和验证命令前，禁止按本草稿直接改生产代码。

## Applies To

当前用于收敛 ERP、IOT、MES、MP、Report 等模块已删除的重复占位入口。

这些模块需要按实际业务边界分别创建独立、可复现、可验证的生产级 DDD skill，而不是共享同一份通用模板。

## Shared Draft Checklist

### 聚合根

- 每个真实业务实体或一致性边界对应一个聚合根。
- 聚合根封装业务规则、状态流转和生命周期。
- 聚合根不得依赖 Spring、MyBatis、Mapper、Controller VO 或外部 API client。

### 值对象

- 使用不可变类型表达标识、名称、状态、金额、数量等领域概念。
- 值对象构造时自校验，不暴露 setter。

### 仓储接口

- 每个聚合根只依赖 domain 层仓储接口。
- 仓储接口定义在 domain 层，具体实现放在 infrastructure 层。

### 领域服务

- 跨聚合规则或不属于单个聚合根的领域决策放入领域服务。
- 外部系统调用、事务编排、DTO 转换不放入领域服务。

## Upgrade Requirements

升级任一模块专属 skill 时，必须补齐：

1. 当前代码事实源路径：Controller、VO/DTO、DO、Mapper、Service、Convert、ErrorCode、测试。
2. 固定数据模型：DO/DTO/VO/Domain 字段、类型、nullable/default、映射关系。
3. 方法签名：聚合、工厂、仓储、应用服务的目标签名。
4. 业务规则：编号、来源文件、所属层、验证方式。
5. 错误码契约：场景、错误码、参数顺序、抛出层级。
6. 事务与集成契约：缓存、MQ、Job、租户、权限、第三方 API 是否必须保持。
7. 验收标准和 Maven compile/test 命令。
8. Red Flags、Rollback Conditions、AI Self-Check。

## Acceptance Criteria For Upgrading A Module Skill

- 模块专属 skill 不再引用本草稿作为唯一依据。
- 模块专属 skill 能让无上下文 AI 根据当前代码事实复现重构边界。
- Controller 路径、API/CommonApi/Feign 契约、权限、租户、错误码、DTO 字段不被隐式改变。
- 编译和必要测试命令明确可执行。
