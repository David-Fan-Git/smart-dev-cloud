---
name: module-system-architecture-skill
description: Use when designing, implementing, reviewing, or extending the in-process system module management capability in this repository.
type: ddd-architecture-skill
status: production-ready
---

# Module System Architecture Skill

## Overview

本 skill 用于在当前 Spring Boot/Spring Cloud 仓库中生产级落地进程内系统模块管理能力，不用于实现 OSGi、动态 jar 插件、ClassLoader 沙箱或跨 JVM 模块通信。

## When to Use

- 修改 `develop-module-system` 的 module 子域。
- 新增模块元数据、生命周期、依赖解析、状态查询、启停编排、模块事件或模块指标。
- 审查模块系统是否过度设计或破坏 DDD 分层。

## When Not to Use

- 业务模块普通 CRUD。
- Nacos/Spring Cloud 服务发现。
- 远程插件市场、热卸载、安全沙箱、独立 ClassLoader。

## Reproducibility Contract

1. 先读取当前源码锚点和测试。
2. 当前可编译代码和外部 API 行为优先。
3. 冲突时先修订本 skill，再修改代码。
4. 新能力必须先写失败测试。

## Baseline Failure Findings

| 压力场景 | 必须暴露的问题 | 补救 |
|---|---|---|
| 赶时间直接写实现 | 容易跳过状态机和依赖环测试 | 先写 `SystemModuleTest` 与 `ModuleDependencyResolverTest` |
| 架构完整性压力 | 容易引入 ClassLoader、Disruptor、CDS | 第一阶段明确禁止 |
| Spring 集成压力 | 容易让 domain 依赖 Spring/Micrometer | 通过 outbound port 适配 |
| 管理 API 压力 | 容易让 Controller 操作 `SystemModule` | Controller 只调用 `SystemModuleUseCase` |

## AI Execution Contract

- **Scope:** 每次只处理 module 子域一个闭环。
- **Must Read:** 本 skill、`DDD_Skill_Production_Readiness_Standard.md`、`Module_Structure_Standard.md`、`SystemArchitectureTest.java`、module 子域源码、`ErrorCodeConstants.java`。
- **Must Preserve:** `/system/module` API、权限标识、错误码语义、DDD 依赖方向、Micrometer 指标名称。
- **Allowed Changes:** `domain/module`、`application/module`、`infrastructure/module`、`framework/module`、`controller/admin/module`、`convert/module`、对应测试。
- **Forbidden Changes:** 独立 ClassLoader、动态 jar 加载、跨 JVM 通信、修改无关聚合、把核心逻辑写入旧 `service/dal`。
- **Dependency Rules:** domain 不依赖 Spring、MyBatis、Micrometer、Controller VO；application 依赖端口；infrastructure 实现端口；controller 只调用 inbound port。
- **Verification Gate:** 完成前运行 module 相关单测、`SystemArchitectureTest`、system-server compile。
- **Stop Conditions:** 需要数据库持久化、远程插件、权限菜单 SQL、ClassLoader 隔离或外部契约变更时停止并重新评审。

## Current Source Anchors

- Architecture test: `develop-module-system/develop-module-system-server/src/test/java/com/develop/mvp/pk/module/system/architecture/SystemArchitectureTest.java`
- Error codes: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/enums/ErrorCodeConstants.java`
- Domain: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/module`
- Application: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/application/module`
- Infrastructure: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/infrastructure/module`
- Controller: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/controller/admin/module`
- Convert: `develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/convert/module`

## Standard Skeleton Contract

```text
domain/module/model,valueobject,event,service,repository
application/module/command,query,dto,port/inbound,port/outbound,service
infrastructure/module/persistence,external,rpc,cache,messaging
framework/module/config
controller/admin/module
convert/module
```

## Fixed Data Model

| 模型 | 字段 |
|---|---|
| SystemModule | code, name, version, enabled, dependencies, order, description, state, failureReason |
| ModuleDependency | moduleCode, versionRange, optional |
| ModuleStateChangedEvent | moduleCode, previousState, currentState, reason, occurredAt |

## Method Signatures

- `SystemModuleUseCase.register(RegisterModuleCommand command)`
- `SystemModuleUseCase.listModules()`
- `SystemModuleUseCase.getModule(String code)`
- `SystemModuleUseCase.startModule(String code)`
- `SystemModuleUseCase.stopModule(String code)`
- `ModuleDependencyResolver.resolve(List<SystemModule> modules)`
- `ModuleEventPort.publish(ModuleStateChangedEvent event)`
- `ModuleMetricsPort.recordStateChange(String moduleCode, ModuleRuntimeState state)`

## Business Rules

- R01: 模块编码全局唯一。
- R02: 启用模块初始状态为 NEW，禁用模块初始状态为 DISABLED。
- R03: 启动前必须完成依赖拓扑排序。
- R04: 必选依赖缺失时拒绝启动。
- R05: 循环依赖必须拒绝启动。
- R06: 状态迁移只能由聚合方法完成。
- R07: 事件和指标通过 outbound port 发布。

## Error Code Contract

使用 `SYSTEM_MODULE_*` 错误码段 `1_002_030_000`：模块不存在、编码重复、依赖缺失、循环依赖、非法状态迁移、禁用模块启动、启动失败。

## Transaction / Integration / Mapping Rules

- MVP 使用内存仓储，不创建数据库表。
- Controller VO 不进入 domain。
- Micrometer 和 Spring Event 只在 infrastructure/framework 使用。
- `/system/module` Controller 只调用 `SystemModuleUseCase`。

## Acceptance Criteria

- module 子域通过 DDD 骨架测试。
- 状态机、依赖解析、应用服务测试通过。
- `/system/module` Controller 只依赖 `SystemModuleUseCase`。
- 无 ClassLoader、Disruptor、CDS、动态 jar 加载实现。

## Verification Commands

```bash
mvn test -pl develop-module-system/develop-module-system-server -Dtest=SystemModuleTest,ModuleDependencyResolverTest,SystemModuleApplicationServiceTest,SystemArchitectureTest
mvn compile -pl develop-module-system/develop-module-system-server -am
grep -R "URLClassLoader\|Disruptor\|ClassLoader" develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/domain/module develop-module-system/develop-module-system-server/src/main/java/com/develop/mvp/pk/module/system/application/module
```

## Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 生命周期状态机 | `domain/module/model/SystemModule.java` | Controller、framework 配置 |
| 依赖拓扑解析 | `domain/module/service/ModuleDependencyResolver.java` | Controller、infrastructure |
| 启停编排 | `application/module/service/SystemModuleApplicationService.java` | domain、Controller |
| Spring 事件发布 | `infrastructure/module/messaging` | domain/application 直接依赖 Spring |
| Micrometer 指标 | `infrastructure/module/messaging` | domain/application 直接依赖 MeterRegistry |
| 管理 API | `controller/admin/module` | domain/application |

## Common Mistakes

| 误区 | 后果 | 修正 |
|---|---|---|
| 直接引入 ClassLoader 隔离 | 与 Spring Bean 生命周期冲突，卸载不可控 | 延期到独立评审 |
| Controller 直接操作聚合 | 破坏用例边界和权限/事务编排 | 只调用 inbound port |
| domain 发布 Spring 事件 | 领域层依赖基础设施 | 使用 `ModuleEventPort` |
| 为生产级提前建表 | MVP 验证成本上升 | 第一阶段使用内存仓储 |

## Rationalization Table

| 借口 | 现实约束 |
|---|---|
| “生产级必须支持热加载” | 当前需求是进程内模块管理，热加载需要单独评审 |
| “ClassLoader 才是真模块系统” | Spring Boot 下 ClassLoader 隔离是高风险高级能力 |
| “指标可以后补” | 生产级生命周期系统必须从第一版可观测 |
| “只有一个实现，不需要端口” | 本仓库 DDD 标准要求 inbound/outbound 端口和骨架 |

## Red Flags

- domain 引入 Spring、Micrometer、ApplicationEventPublisher。
- Controller 直接修改 `SystemModule`。
- 为 MVP 创建数据库表、ClassLoader 或插件 jar 扫描。
- 以“生产级”为理由引入未验证优化。

## Rollback Conditions

- system-server 编译失败。
- DDD 架构测试失败。
- 状态机或依赖解析测试失败。
- 新 API 权限或路径需要变更但未评审。

## AI Self-Check

- 已先写失败测试。
- 未实现延期项。
- 每个新增类都在正确层。
- 完成前已运行 Verification Commands。
