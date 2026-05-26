---
name: module-structure-standard
description: Use when auditing, creating, or refactoring module structure, API contracts, DDD layers, or runtime units in this repository.
---

# Module Structure Standard

## 1. Overview

本标准定义本仓库模块结构、API 契约、DDD 分层和运行单元的统一目标。它用于让未来修改模块结构、`CommonApi` 契约、`local/remote` 适配、`server/gateway` 运行单元或 DDD 层时，先有同一套可复现、可验证、可维护的判断标准。

核心原则：统一不是机械地让所有目录完全同名，而是让每个 Maven 运行单元、每个 API 契约、每个业务聚合都有一致职责边界、稳定依赖方向和明确验收方式。

## 2. When to Use

在以下场景必须先读取并遵守本标准：

- 审计、创建或重构任一 `develop-module-*` 的 Maven 结构。
- 调整 API 模块、`CommonApi`、DTO、枚举、Feign、RPC、本地调用或远程调用适配。
- 新增、迁移或重构 `domain/application/infrastructure/convert` DDD 分层。
- 判断 `controller/job/mq/framework/service/dal` 中的代码应保留、迁移、拆分还是删除。
- 处理 `develop-module-iot`、`develop-module-mall` 等特殊模块结构收口。
- 编写或升级聚合 skill、模块迁移计划、结构验收清单。
- 发现模块职责混乱、依赖方向反转、核心业务散落在旧 `service/dal`、API 模块只有枚举常量而缺契约时。

## 3. When Not to Use

以下场景不应把本标准作为主要执行依据：

- 只修复单个业务 bug，且不改变模块结构、API 契约、DDD 层或依赖方向。
- 只修改配置值、SQL 数据、文案、测试断言或非结构性脚本。
- 只执行构建、测试、代码格式检查或只读排查。
- 第三方依赖升级、Spring Boot 配置升级、Maven 版本治理等纯技术升级；这些任务只在影响模块边界时引用本标准。
- 外部 API 兼容性迁移尚未被确认时，不能用本标准直接推动破坏性契约变更。

## 4. Reproducibility Contract

未来任何代理或开发者按本标准执行时，必须满足以下复现契约：

1. 先识别修改对象属于 Maven 运行单元、API 契约、运行单元内部 DDD 分层，还是特殊模块收口。
2. 修改领域聚合前，先读取 `.claude/ddd-skills/DDD_Skill_Production_Readiness_Standard.md` 和对应聚合 skill；聚合 skill 不达标时先升级 skill。
3. 每次只处理一个模块、一个运行单元、一个 API 契约集合或一个聚合根，不把多个结构问题混在一次无边界改动中。
4. 当前可编译代码的外部行为优先：Controller 路径、HTTP 方法、权限、租户、数据权限、错误码、DTO 字段、分页、Excel、MQ、Job、缓存和 RPC 契约不得被文档假设覆盖。
5. 发现标准、skill、spec 与当前代码冲突时，停止实现，读取事实源，记录冲突，先修订标准或 skill，再继续迁移。
6. 不为“看起来统一”移动代码；每次移动必须对应明确职责边界、依赖方向、复用性、扩展性或可维护性收益。
7. 完成前必须执行与影响范围匹配的 Maven compile/test 或明确说明无法执行的原因。

## 5. Unified Target

统一结构分为三层：Maven 运行单元、API 契约、运行单元内部分层。

- Maven 层按 runtime unit 统一，允许 `server`、`gateway` 等运行单元存在，但每个运行单元内部结构必须一致。
- API 层采用一套 `CommonApi` 契约 + `local/remote` 双适配：同一个契约同时支持本地模块调用和远程 Feign/RPC 调用。
- `server/gateway` 内部保留 `controller/job/mq/framework` 作为入口和技术配置，核心业务进入 `domain/application/infrastructure/convert`。
- `service/dal` 是迁移源，不是核心业务最终承载层；迁移完成后不能继续沉淀核心业务流程、领域规则或跨层决策。
- `iot/mall` 不是长期例外，必须按运行单元、API 契约、DDD 分层标准收口。
- 模块和聚合必须保持高内聚低耦合、责任边界清晰、单一职责原则、必要功能注释和代码质量优化。
- 优先按明确场景使用 Java 常见 23 种设计模式，但禁止为套模式制造抽象。
- 结构必须支持高扩展性和长期可维护性：明确变化点预留扩展接口，不确定需求避免过度抽象。

## 6. Standard Module Shape

每个业务模块最终应能归入以下统一模型：

```text
develop-module-{name}/
  develop-module-{name}-api/
  develop-module-{name}-server/
  [develop-module-{name}-gateway/]
```

约束：

- `develop-module-{name}-api` 承载跨模块稳定契约，不承载应用编排、领域决策或持久化逻辑。
- `develop-module-{name}-server` 是常规业务运行单元，内部必须按统一 DDD 层和入口层职责组织。
- `develop-module-{name}-gateway` 仅在该模块确有独立网关、协议接入、设备通信或独立运行职责时存在。
- 同一模块可有多个运行单元，但每个运行单元内部必须遵守相同层级职责和依赖方向。
- 父模块只做 Maven 聚合和依赖治理，不承载业务用例或领域规则。

## 7. API Module Standard

API 模块标准结构：

```text
api/
  {business}/
    XxxCommonApi.java
    dto/
    enums/
    local/
    remote/
```

职责边界：

- `XxxCommonApi.java` 是本地和远程共同遵守的一套稳定契约。
- `dto/` 存放跨模块调用需要的请求、响应、分页或事件 DTO。
- `enums/` 存放契约侧需要暴露的枚举；没有跨模块调用时也应保持标准位置，避免散落成常量包。
- `local/` 存放本地调用适配器、本地实现桥接或本地模式装配。
- `remote/` 存放 Feign/RPC 远程调用适配器。
- API 模块不得包含应用编排、事务边界、领域决策、仓储实现、Mapper、DO 或技术持久化逻辑。
- 契约命名、参数、返回类型、异常语义和注释必须表达业务意图；必要功能注释应说明契约用途、调用边界和兼容约束，不复述显而易见的代码步骤。
- 本地和远程适配优先使用适配器模式隔离调用方式差异，但共同契约必须保持清晰、稳定、单一职责。

## 8. Runtime Unit Internal Standard

`server` 或 `gateway` 运行单元内部统一为：

```text
domain/{aggregate}/
  model/
  valueobject/
  event/
  service/
  repository/
application/{aggregate}/
  command/
  query/
  dto/ 或 result/
  port/
    inbound/
    outbound/
  service/
infrastructure/{aggregate}/
  persistence/
  external/
  rpc/
  cache/
  messaging/
convert/
controller/
job/
mq/
framework/
```

骨架创建规则：

- 迁移或创建聚合时，必须创建上述标准目录骨架；即使当前目录暂无实现，也不能省略标准目录。
- `application/{aggregate}/port/inbound/` 必须定义用例入口接口，例如 `CreateRoleUseCase`、`UpdateRoleUseCase` 或按业务粒度合并后的 `RoleUseCase`。
- `application/{aggregate}/service/` 必须放入站用例实现，例如 `RoleApplicationService`，并实现 inbound port。
- `application/{aggregate}/port/outbound/` 必须作为应用层外部能力端口位置；当前没有外部能力时仍保留目录，后续跨模块、通知、文件、第三方系统等能力必须先在此定义端口。
- `domain/{aggregate}/repository/` 必须定义领域仓储接口；即使当前只有一个 MyBatis 实现，也不能让应用层或领域层依赖 Mapper/DO。
- `infrastructure/{aggregate}/persistence/` 必须放仓储实现和 Mapper/DO 协作适配；`external/rpc/cache/messaging` 作为固定技术适配位置，当前为空也必须保留目录。
- Java 空目录无法被 Git 稳定追踪时，优先用职责明确的接口骨架或 `package-info.java` 固定包边界；禁止使用无业务语义的 `Temp`、`Placeholder`、`Dummy` 类。

职责边界：

- `domain/{aggregate}/`：聚合根、值对象、领域事件、领域仓储接口、领域服务；封装业务规则和不变量，不依赖 Spring、MyBatis、远程客户端、Controller VO 或基础设施实现。
- `application/{aggregate}/`：用例编排、事务边界、权限、租户、数据权限等应用级策略；协调聚合、仓储端口和外部端口，不沉淀领域规则。
- `application/{aggregate}/port/inbound/`：入站用例契约，供 Controller、Job、MQ 或本地适配调用；不得包含 Spring Web、Mapper、DO 或基础设施实现。
- `application/{aggregate}/port/outbound/`：应用编排需要的外部能力端口，例如跨模块查询、通知、文件、第三方系统、远程服务；不得放持久化实现。
- `application/{aggregate}/service/`：入站用例实现和事务边界；可以依赖 domain 仓储接口、outbound 端口和 convert，不直接依赖 Mapper/DO。
- `infrastructure/{aggregate}/`：仓储实现、MyBatis Mapper/DO 协作、缓存、外部系统适配、本地/远程技术适配；只实现技术细节，不反向定义业务规则。
- `infrastructure/{aggregate}/persistence/`：实现 `domain/{aggregate}/repository/` 中的仓储接口，隔离 MyBatis、DO、Mapper 和查询实现。
- `infrastructure/{aggregate}/external/`：第三方系统或外部平台适配实现。
- `infrastructure/{aggregate}/rpc/`：Feign/RPC 远程调用适配实现。
- `infrastructure/{aggregate}/cache/`：缓存读写、缓存键、缓存失效等技术实现。
- `infrastructure/{aggregate}/messaging/`：消息发送、事件发布或消息基础设施适配实现。
- `convert/`：DTO/VO/DO/domain 映射；只做对象转换，不写业务判断、权限判断或持久化访问。
- `controller/`：Web 入口；只处理协议参数、认证上下文、权限注解和响应封装，不直接操作 DO/Mapper。
- `job/`：定时任务入口；只触发应用用例，不复制业务流程。
- `mq/`：消息入口；只完成消息解析、幂等入口和应用用例调用，不承载领域决策。
- `framework/`：Spring 配置、自动装配、拦截器、模块技术配置；只放技术装配，不放业务逻辑。
- `service/`：旧业务逻辑迁移源；迁移期可作为兼容壳或过渡入口，目标状态不再承载核心业务。
- `dal/`：旧持久化技术位置；迁移期可保留 DO/Mapper，目标状态中业务仓储实现应进入 `infrastructure/{aggregate}/persistence/`，`dal` 不做业务决策。

## 9. Design Pattern Guidance

优先按明确场景使用 Java 常见 23 种设计模式，目的是降低耦合、封装变化、复用流程和隔离技术差异。禁止为套模式制造抽象，也禁止为了“显得 DDD”创建无业务价值的空接口、多层转发或过早泛化。

常见选择：

- 创建复杂聚合、值对象或跨字段不变量时，优先考虑工厂方法或抽象工厂，让构造规则集中在领域或工厂中。
- 多算法、多渠道、多支付、多通知、多审批、多设备协议或多租户策略场景，优先考虑策略模式，避免条件分支堆积在应用服务。
- 固定流程中存在可变步骤时，优先考虑模板方法，避免复制整段流程。
- 外部系统、远程 API、旧接口兼容、本地/远程调用切换场景，优先考虑适配器模式或门面模式。
- 领域事件、状态变化通知、跨边界异步协作场景，优先考虑观察者或发布订阅思想。
- 对象构造参数多、存在可读性风险时，可使用建造者模式，但不得绕过领域不变量。
- 需要统一创建一组相关对象时，可使用抽象工厂；如果只是单个简单对象，不要引入额外工厂层。

使用模式前必须能回答：变化点是什么、耦合在哪里、模式如何降低维护成本、是否仍符合单一职责原则。

## 10. Extensibility and Maintainability

结构统一必须服务长期演进，而不是制造额外维护负担。

- 对支付渠道、通知渠道、审批规则、设备协议、外部服务供应商、导入导出格式、权限策略等明确变化点，通过接口、策略、适配器、领域服务或领域事件隔离变化。
- 对稳定业务概念，优先沉淀为聚合、值对象、领域服务和领域事件，让业务语义集中表达。
- 标准目录骨架和标准端口接口是统一结构的一部分，不视为无意义空抽象；迁移聚合时必须创建。
- 对标准骨架之外的不确定需求保持简单实现，不提前创建空接口、空抽象类、多级继承或只转发不增值的中间层。
- 包、类、方法命名必须反映业务语义和层级职责，便于后续快速定位入口、用例、领域规则、持久化实现和外部适配点。
- 必要功能注释用于说明 API 契约用途、应用用例职责、领域不变量、外部适配约束、事务边界和回滚条件；不要用注释复述代码步骤。
- 代码质量优化是结构统一的一部分：迁移时同步消除重复逻辑、模糊命名、过长方法、错误依赖方向、跨层穿透和无意义中转层。
- 高内聚低耦合是验收标准：聚合内部封装规则，跨聚合通过应用服务、领域服务、事件或 API 契约协作，不直接访问对方内部实现。

## 11. Special Module Convergence

特殊模块可以有不同运行单元数量，但不能有不同结构规则。

### develop-module-iot

`develop-module-iot` 按运行单元标准收口：

- `develop-module-iot-server` 保留为业务管理运行单元，内部遵守 `domain/application/infrastructure/convert` 和入口层标准。
- `develop-module-iot-gateway` 保留为协议接入或设备通信运行单元，内部也要统一 DDD 分层，或明确只做技术接入，不承载核心业务规则。
- `develop-module-iot-core` 不能长期作为平级游离模块，必须明确归属：
  - 共享领域能力进入 `iot-api` 的契约/值对象或 `iot-server` 的 domain。
  - 技术基础设施进入 `iot-server/infrastructure` 或 `iot-gateway/infrastructure`。
  - 多运行单元共用且确有必要时，可定义明确的 `iot-common` 或 `iot-shared` 基础包，但不得承载业务用例。

### develop-module-mall

`develop-module-mall` 按业务上下文和运行单元标准收口：

- `product/promotion/trade/statistics` 视为 mall 下的独立业务上下文，每个上下文都必须拥有标准 `api + server`。
- `develop-module-mall-server` 不能长期作为模糊聚合点；要么只做 mall 组合入口，要么拆解或下沉到具体上下文。
- mall 下每个上下文按同一套 API 契约和 DDD 分层标准执行，不因位于 mall 父目录下而采用不同规则。

## 12. Migration Order

推荐迁移顺序：

1. 先建立或读取统一结构标准：修改任何模块结构前，先确认本文件仍覆盖当前目标。
2. 先做 API 契约统一：以 `system-api` 的本地/远程适配方向为样板，逐模块收敛 `CommonApi`、DTO、枚举、`local/remote` 适配器。
3. 再做运行单元内部聚合迁移：每次只迁移一个模块或一个聚合根；必须先有对应聚合 skill，验证 skill 后再改代码，并先创建标准目录与接口骨架。
4. 迁移聚合时同步补齐必要功能注释，尤其是 API 契约用途、应用用例职责、领域不变量、外部适配约束和回滚条件。
5. 迁移聚合时同步做代码质量优化：删除重复路径、收敛命名、拆分过长方法、修正错误依赖方向，确保高内聚低耦合。
6. 迁移聚合时按单一职责原则审查类和方法；一个类同时承担入口、编排、领域规则和持久化适配时，必须拆分到对应层或标注迁移原因。
7. 迁移聚合时识别可复用的设计模式场景；没有明确变化点时，不引入额外模式。
8. 最后收口特殊运行单元：明确 `iot-core` 归属，验收 `iot-gateway`、`mall-server` 和 mall 子上下文。

## 13. Acceptance Criteria

完成任一模块结构、API 契约或 DDD 层改造后，必须满足：

- 模块 Maven 结构能解释为标准运行单元，允许 `server/gateway`，但每个运行单元内部结构一致。
- API 模块具备一套 `CommonApi` 契约和 `local/remote` 双适配结构。
- API 模块不包含应用编排、领域决策、仓储实现、Mapper、DO 或持久化逻辑。
- `controller/job/mq/framework` 只承担入口或技术配置职责，不承载核心业务规则。
- 核心业务进入 `domain/application/infrastructure/convert`，旧 `service/dal` 不再作为核心业务最终承载层。
- 每个迁移后的聚合都具备标准目录与接口骨架：`domain` 的 `repository`，`application` 的 `port/inbound`、`port/outbound`、`service`，以及 `infrastructure` 的 `persistence/external/rpc/cache/messaging`。
- 聚合、应用服务、基础设施适配、入口层责任边界清晰，依赖方向不反转，不出现跨层直接穿透调用。
- 关键 API、用例、领域不变量、事务边界和外部适配点具备必要功能注释。
- 改造后的代码具备高内聚低耦合，没有新增重复逻辑、模糊职责类、过长方法或无意义中转层。
- 类、接口、方法符合单一职责原则；多职责实现必须拆分或说明迁移期暂存原因。
- 设计模式使用有明确业务或技术动机，能降低耦合、封装变化或复用流程；不得为了使用模式而制造抽象。
- 明确变化点具备可扩展接口或替换点，新增渠道、规则、协议或外部适配时不需要修改无关层代码。
- `iot/mall` 等特殊模块没有被视为长期例外，已有明确运行单元、API 契约和 DDD 分层收口路径。
- 后续维护者能通过包结构和命名快速定位入口、用例、领域规则、持久化实现和外部适配点。
- 影响 Java 代码时，至少执行对应 Maven compile/test；只改文档时执行本文件的结构和占位词验证。

## 14. Verification Commands

只修改本标准文档时，至少执行：

```bash
grep -E "^## (1\. Overview|2\. When to Use|3\. When Not to Use|4\. Reproducibility Contract|13\. Acceptance Criteria|14\. Verification Commands|15\. Red Flags|16\. Quick Reference)" .claude/ddd-skills/Module_Structure_Standard.md
git diff -- .claude/ddd-skills/Module_Structure_Standard.md
```

同时执行一次占位内容反向扫描；扫描表达式以当前任务或评审要求为准。

没有业务代码改动时，不要求执行 Maven 编译。

修改 API 契约或模块结构时，按影响范围追加：

```bash
mvn compile -pl develop-module-{name}/develop-module-{name}-api -am
mvn compile -pl develop-module-{name}/develop-module-{name}-server -am
mvn test -pl develop-module-{name}/develop-module-{name}-server
```

修改 `develop-server` 或运行单元依赖时，追加：

```bash
mvn compile -pl develop-server -am
mvn clean package -pl develop-server -am -Dmaven.test.skip=true
```

修改 `develop-gateway` 或模块 gateway 时，追加对应 gateway 编译或打包命令。

## 15. Red Flags

看到以下情况必须停止、缩小范围或先补充事实源：

- 为了统一目录而批量移动多个模块，无法说明业务收益或验收方式。
- API 模块出现应用服务、事务注解、Mapper、DO、领域规则或持久化逻辑。
- `CommonApi` 被本地实现和远程 Feign 分裂成两套不一致契约。
- Controller、Job 或 MQ 直接操作 Mapper/DO，绕过应用层和领域边界。
- 领域层依赖 Spring、MyBatis、Feign、Controller VO、Mapper 或基础设施实现。
- 应用层沉淀核心领域不变量，导致聚合根变成数据容器。
- `service/dal` 被当作新业务最终落位，而不是迁移源。
- `iot/mall` 被描述为“历史原因，长期保持例外”。
- 以“当前为空”“只有一个实现”“避免空抽象”为理由省略标准目录或标准端口接口骨架。
- 在标准骨架之外，为了使用设计模式创建空接口、空抽象类、多层转发或没有变化点的策略层。
- 一个类同时承担入口、编排、领域规则、持久化适配和对象转换。
- 注释只解释代码步骤，缺少业务意图、约束或外部契约说明。
- 无法给出最小 Maven compile/test 命令或无法说明不执行验证的原因。

## 16. Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 跨模块稳定调用契约 | `api/{business}/XxxCommonApi.java` | `server/service` 私有接口 |
| 跨模块请求/响应对象 | `api/{business}/dto/` | `controller/vo`、`dal/dataobject` |
| 契约侧枚举 | `api/{business}/enums/` | 散落在常量包或 server 内部 |
| 本地调用适配 | `api/{business}/local/` 或 server 装配实现 | 复制一套不同契约 |
| 远程 Feign/RPC 适配 | `api/{business}/remote/` | Controller 或应用层手写远程细节 |
| Web 入口 | `controller/` | `domain/`、`infrastructure/` |
| 定时任务入口 | `job/` 调用 application 用例 | Job 内复制业务流程 |
| 消息入口 | `mq/` 调用 application 用例 | MQ 内写领域规则 |
| 业务不变量 | `domain/{aggregate}/` | `controller/`、`convert/`、`dal/` |
| 用例编排和事务 | `application/{aggregate}/service/` | `domain/` 或 `controller/` |
| 入站用例接口 | `application/{aggregate}/port/inbound/` | Controller 私有方法或无契约应用服务 |
| 应用外部能力端口 | `application/{aggregate}/port/outbound/` | `controller/`、`domain/`、`infrastructure/` 反向定义 |
| 仓储接口 | `domain/{aggregate}/repository/` | `infrastructure/` 反向定义业务端口 |
| 仓储实现和持久化适配 | `infrastructure/{aggregate}/persistence/` | `domain/`、`application/port/outbound` |
| 外部/RPC/缓存/消息适配 | `infrastructure/{aggregate}/external|rpc|cache|messaging/` | `domain/` 或 Controller 手写技术细节 |
| 标准空目录固定 | 职责明确的接口骨架或 `package-info.java` | `Temp`、`Placeholder`、`Dummy` 类 |
| 对象转换 | `convert/` | 应用服务中散落手写映射 |
| Spring 技术配置 | `framework/` | `domain/` |
| 旧业务迁移源 | `service/`、`dal/` | 新核心业务最终落位 |
| 明确变化点扩展 | 接口、策略、适配器、领域服务 | 无业务价值的空抽象层 |
