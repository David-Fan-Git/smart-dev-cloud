---
name: ddd-skill-production-readiness-standard
description: Use when auditing, writing, or upgrading DDD aggregate skills for production use, full project reproduction, or current-code consistency in this repository.
---

# DDD Skill Production Readiness Standard

## 1. Overview

本标准用于判断 `.claude/ddd-skills/AggregateRoot_*_Skill.md` 是否可用于生产环境和项目复现。

**结论规则：** 未满足本标准的聚合 skill 只能作为草稿参考，不能直接指导生产级重构或让无上下文 AI 复现项目。

## 2. Current Audit Result

当前审计结论：

- `AggregateRoot_Tenant_Validation_Skill.md` 接近生产可用，可作为模板。
- 其余大多数 DDD skill 缺少 frontmatter、事实源路径、字段映射、错误码、事务边界、测试路径、冲突处理、红旗清单，不能直接生产使用。
- 极短模板类 skill 或已合并到 `AggregateRoot_Module_Draft_Skill.md` 的模块草稿，必须先升级为本标准格式，否则禁止按其直接实现。

## 3. Mandatory Sections

每个生产级 DDD aggregate skill 必须包含：

| 项目 | 要求 |
|---|---|
| YAML frontmatter | `name` 仅小写字母/数字/连字符；`description` 以 `Use when...` 开头，只描述触发条件 |
| Overview | 一句话说明聚合业务意图和复现目标 |
| When to Use / Not Use | 明确适用与不适用场景 |
| Reproducibility Contract | 明确先读事实源、冲突时外部行为优先、禁止猜测 |
| AI Execution Contract | 明确 AI 执行边界：任务 Scope、Must Read、Must Preserve、Allowed Changes、Forbidden Changes、Dependency Rules、Verification Gate、Stop Conditions |
| Current Source Anchors | Controller、VO/DTO、DO、Mapper、Convert、Service/Application、Repository、ErrorCode、测试路径完整相对路径 |
| Standard Skeleton Contract | 明确本聚合必须创建的标准目录和接口骨架：`domain/{aggregate}/model,valueobject,event,service,repository`、`application/{aggregate}/command,query,dto|result,port/inbound,port/outbound,service`、`infrastructure/{aggregate}/persistence,external,rpc,cache,messaging`；当前为空时用职责明确的接口骨架或 `package-info.java` 固定包边界 |
| Fixed Data Model | DO/DTO/VO/Domain 字段表，包含类型、含义、nullable/default、映射关系 |
| Method Signatures | 聚合、工厂、仓储、应用服务的必须签名 |
| Business Rules | 编号规则，标明所属层和验证方式 |
| Error Code Contract | 场景 → `ErrorCodeConstants` → 参数 → 抛出层级 |
| Transaction Contract | 每个写用例是否 `@Transactional`，事务覆盖哪些外部编排 |
| Integration Contract | 缓存、MQ、Job、操作日志、租户隔离、数据权限、第三方 API 是否必须保留 |
| Mapping Rules | DO ↔ Domain ↔ DTO/VO 转换规则，禁止领域层依赖 Controller VO/Mapper |
| Acceptance Criteria | 至少包含架构 AC、业务行为 AC、编译/测试 AC |
| Verification Commands | Maven compile/test 命令，必要时单测类名或通配符 |
| Quick Reference | “要做什么 → 正确位置 → 禁止位置” |
| Common Mistakes | 常见误改、后果、修正 |
| Rationalization Table | 压力场景中 AI 可能偷懒的借口和现实约束 |
| Red Flags | 出现即停止的误用信号 |
| Rollback Conditions | 编译、行为、安全、外部契约回归时回滚 |
| AI Self-Check | 完成前逐项检查 |

## 4. Production API Contract

生产级 DDD skill 必须明确：

- Controller 路径、HTTP 方法、请求/响应 VO 不得擅改。
- API/CommonApi/Feign/RPC 契约不得擅改。
- 权限注解、租户注解、数据权限注解不得丢失。
- 错误码、异常类型、错误参数顺序必须保持。
- Excel 导入导出、分页字段、OpenAPI/Swagger 可见契约不得因 DDD 重构变化。

如果确实需要改变外部契约，必须单独写迁移计划，不能混入聚合重构。

## 5. Current-Code Conflict Rule

当 skill 与当前代码冲突：

1. 停止实现。
2. 读取当前事实源文件。
3. 记录冲突：字段、方法、错误码、事务、外部 API、测试。
4. 以“当前可编译代码的外部行为”为事实源。
5. 先修订 skill，再按修订后的 skill 实现。

禁止用 skill 文档覆盖现有生产行为。

## 6. AI Execution Contract

每个生产级聚合 skill 必须包含 `AI Execution Contract`，用于约束无上下文 AI 的执行行为。该契约不是业务说明，而是执行边界。

必须包含：

| 项目 | 要求 |
|---|---|
| Scope | 本次 skill 允许处理的模块、聚合、用例或 API 契约集合；禁止跨多个无关聚合批量修改 |
| Must Read | 写代码前必须读取的事实源文件，至少覆盖 Controller、VO/DTO、DO、Mapper、Convert、Service/Application、Repository、ErrorCode、测试 |
| Must Preserve | 不得擅改的外部行为：HTTP API、CommonApi、DTO 字段、权限、租户、数据权限、错误码、MQ、Job、缓存、Excel、OpenAPI |
| Allowed Changes | 允许新增或修改的目录和文件类型，必须与标准骨架职责一致：`domain/{aggregate}`、`application/{aggregate}`、`infrastructure/{aggregate}`、`convert/controller/job/mq/framework`；迁移聚合时必须创建标准目录和接口骨架 |
| Forbidden Changes | 禁止修改的目录、契约、SQL、配置或无关模块；禁止把新核心业务写入 `service/dal`；禁止以“当前为空”“只有一个实现”“避免空抽象”为由省略标准骨架 |
| Dependency Rules | 明确每层允许依赖和禁止依赖的包；领域层不得依赖 Spring、MyBatis、Feign、Controller VO、Mapper、基础设施实现 |
| Verification Gate | 完成前必须执行的 Maven compile/test、ArchUnit、契约检查或无法执行时的说明 |
| Stop Conditions | 出现事实源缺失、skill 与当前代码冲突、外部契约可能变化、验证失败、需要猜字段/错误码/事务时必须停止 |

缺少 `AI Execution Contract` 的聚合 skill 只能作为草稿参考，不能用于生产级 AI coding。

## 7. Minimal Pressure Tests

升级任何 skill 前必须做 RED 阶段压力测试：

| 场景 | 期望暴露的问题 |
|---|---|
| 赶时间直接写代码 | 是否会猜字段、签名、错误码、事务 |
| 无上下文 AI 复现 | 是否缺事实源、标准目录骨架、接口骨架、映射、测试、验收 |
| 简单 CRUD 赶进度 | 是否会以“只有一个实现”“当前为空”“避免空抽象”为由省略 `port/inbound`、`port/outbound`、`infrastructure/*` 固定目录 |
| 生产上线不能破坏 API | 是否缺外部契约、权限、租户、缓存、MQ、Job |
| 代码与 skill 不一致 | 是否知道代码事实优先并先修 skill |

测试失败点必须写入 skill 的 Baseline Failure Findings、Rationalization Table、Red Flags。

## 8. Upgrade Order

优先级：

1. 高风险生产链路：Pay、MallTrade、MallPromotion、BPM。
2. 基础设施链路：Infra、System User/Role/Menu。
3. 已有较多规则的模块：MallProduct、MemberLevel、CRM、WMS、AI。
4. 极短模板草稿或统一草稿覆盖的模块：ERP、IOT、MES、MP、Report、Mall 总览、MemberUser。

每次只升级一个聚合或一个小子域。不要一次批量改所有 skill。

## 9. Quick Reference

| 问题 | 判断 |
|---|---|
| 没有 YAML frontmatter | 非生产级 skill |
| 没有 Current Source Anchors | 不能让无上下文 AI 复现 |
| 没有 Standard Skeleton Contract | 会导致不同聚合目录、端口和适配器骨架不一致 |
| 没有字段映射表 | 会导致 DO/DTO/Domain 偏差 |
| 没有错误码契约 | 会导致前端/调用方错误处理回归 |
| 没有事务边界 | 会导致跨聚合编排不一致 |
| 没有测试路径和命令 | 不能证明生产可用 |
| 没有冲突处理规则 | 容易用文档覆盖现有行为 |
| 没有 AI Execution Contract | AI 不知道执行边界，容易跨范围修改或跳过验证 |
| 没有 Red Flags | 压力下容易偷懒误改 |

## 10. Red Flags

看到以下情况，必须把对应 skill 判为“草稿，不可直接生产使用”：

- 只有“聚合根/值对象/仓储接口”通用描述。
- 没有写清标准目录和接口骨架，或允许因为当前为空而省略 `port/inbound`、`port/outbound`、`infrastructure/{aggregate}/external|rpc|cache|messaging`。
- 只写“编译通过”，没有业务测试和错误码验证。
- 只写包名，不写完整文件路径。
- 只写领域模型，不写 Controller/VO/CommonApi 外部契约。
- 一个 skill 覆盖多个复杂聚合但没有拆分边界。
- 没有说明缓存、MQ、Job、租户隔离、权限链路是否保持。

## 11. Rollback Conditions

升级后的 skill 如果仍出现以下问题，必须回滚或继续补强：

1. 干净 AI 仍需要猜字段、错误码、事务、映射。
2. 干净 AI 不知道哪些文件要读。
3. 干净 AI 不知道哪些外部 API 不能改。
4. 干净 AI 无法列出最小测试命令。
5. 干净 AI 在压力场景下选择“先写代码后补验证”。
