# Module Structure Standard Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish the repository-level module structure standard by updating Claude guidance and adding a dedicated DDD skill/standard document, without changing business code.

**Architecture:** This phase creates one canonical detailed standard in `.claude/ddd-skills/Module_Structure_Standard.md` and adds a concise mandatory pointer in `CLAUDE.md`. The detailed standard owns the rules; `CLAUDE.md` only tells future agents when to read it and what constraints are non-negotiable.

**Tech Stack:** Markdown documentation, Claude Code project instructions, Java 17/Spring Cloud Alibaba project conventions, DDD skill standards.

---

## File Structure

- Modify: `CLAUDE.md`
  - Responsibility: repository-level instruction entry point loaded by Claude Code.
  - Change: add a short “Module Structure Standard” subsection under the DDD Architecture section, immediately after the current aggregate refactoring process.

- Create: `.claude/ddd-skills/Module_Structure_Standard.md`
  - Responsibility: detailed executable standard for module/API/server structure unification.
  - Change: encode the approved spec as a reusable production-grade standard with frontmatter, trigger conditions, rules, migration order, acceptance criteria, red flags, and verification commands.

- Read-only reference: `docs/superpowers/specs/2026-05-23-module-structure-standard-design.md`
  - Responsibility: approved design source for this plan.

- No business code changes:
  - Do not modify `develop-module-*` Java source files.
  - Do not move Maven modules.
  - Do not refactor API, server, controller, service, dal, domain, application, infrastructure, or convert classes in this phase.

---

### Task 1: Add detailed module structure standard skill

**Files:**
- Create: `.claude/ddd-skills/Module_Structure_Standard.md`
- Reference: `docs/superpowers/specs/2026-05-23-module-structure-standard-design.md`
- Reference: `.claude/ddd-skills/DDD_Skill_Production_Readiness_Standard.md`

- [ ] **Step 1: Confirm the target file does not already exist**

Run:

```bash
test ! -e .claude/ddd-skills/Module_Structure_Standard.md
```

Expected: command exits with code `0` and no output. If the file exists, stop and read it before continuing so existing project guidance is not overwritten.

- [ ] **Step 2: Create the standard document**

Write this exact content to `.claude/ddd-skills/Module_Structure_Standard.md`:

```markdown
---
name: module-structure-standard
description: Use when auditing, creating, or refactoring module structure, API contracts, DDD layers, or runtime units in this repository.
---

# Module Structure Standard

## 1. Overview

本标准定义 Smart Cloud 后端模块统一结构。目标是让所有模块最终在 Maven 运行单元、API 契约、server/gateway 内部分层上具备一致职责边界。

本标准不直接要求一次性迁移所有模块。它要求每次结构调整、API 契约调整、DDD 聚合迁移前，先确认目标位置、职责边界、依赖方向和验收方式。

## 2. When to Use

使用本标准的场景：

- 新增业务模块、API 模块、server 模块或 gateway 运行单元。
- 调整 `develop-module-*` Maven 子模块结构。
- 统一 `develop-module-*-api` 的 CommonApi、DTO、enum、local、remote 包结构。
- 将旧 `service/dal` 业务逻辑迁移到 `domain/application/infrastructure/convert`。
- 判断 `iot`、`mall` 等特殊模块是否符合统一运行单元标准。
- 编写或升级 `.claude/ddd-skills/AggregateRoot_*_Skill.md` 时需要明确模块落位。

## 3. When Not to Use

不要用本标准替代以下工作：

- 具体聚合的业务规则分析。
- 具体 Controller、VO、DTO、DO、Mapper、错误码、权限、租户、缓存、MQ、Job 的事实源核对。
- Maven 依赖冲突排查。
- 单个 bug 的系统性调试。

具体聚合重构仍必须先读取并验证对应 `AggregateRoot_*_Skill.md`。聚合 skill 不满足 `.claude/ddd-skills/DDD_Skill_Production_Readiness_Standard.md` 时，先升级 skill，再实现。

## 4. Reproducibility Contract

执行者必须遵守：

1. 先读取当前代码和本标准，再提出或执行结构调整。
2. 当前可编译代码的外部行为优先于文档描述。
3. 不猜测字段、错误码、事务、权限、租户、缓存、MQ、Job 或外部 API 契约。
4. 不为了“目录看起来统一”进行无业务价值的大规模移动。
5. 每次只迁移一个模块、一个运行单元、一个 API 契约组或一个聚合根。
6. 每次迁移都要能说明责任边界、依赖方向、扩展点和验证命令。

## 5. Unified Target

统一结构分为三层：

1. Maven 运行单元。
2. API 契约。
3. server/gateway 内部分层。

最终所有模块都要符合统一标准，不设置长期例外。`iot`、`mall` 等特殊结构只能作为迁移过程中的临时状态。统一目标不是机械地让所有目录完全同名，而是让每个可运行单元、每个 API 契约、每个业务聚合都有一致职责边界。

核心规则：

- Maven 层以运行单元为统一口径，允许存在 `server`、`gateway` 这类运行单元，但每个运行单元必须结构一致。
- API 层采用 `local/remote` 双适配：一套契约同时支持本地模块调用和远程 Feign 调用。
- server 层保留 `controller/job/mq/framework` 作为入口和技术配置，核心业务进入 `application/domain/infrastructure/convert`。
- `service/dal` 不作为最终业务逻辑承载层；旧代码迁移后应只剩入口适配、兼容过渡或被删除。
- 每个模块和聚合必须保持高内聚低耦合：聚合内部封装业务规则，跨聚合通过应用服务、领域服务或 API 契约协作，不直接穿透访问内部实现。
- 代码质量优化是结构统一的一部分：迁移时同步消除重复逻辑、模糊命名、过长方法、错误依赖方向和无意义适配层。
- 责任边界必须可验证：每个类应能明确说明属于入口、应用编排、领域规则、基础设施适配或对象转换之一。
- 需要保留必要功能注释：面向 API 契约、应用用例、领域不变量和关键适配点说明业务意图与约束，避免解释显而易见的代码步骤。
- 代码必须遵守单一职责原则：一个类、接口、方法只承担一种变化原因；发现多职责类时优先按领域职责、应用用例、基础设施适配或对象转换拆分。
- 优先使用 Java 常见 23 种设计模式解决明确问题：按场景选择工厂、策略、模板方法、适配器、门面、观察者等模式，但禁止为了套模式引入无业务价值的抽象层。
- 代码需要具备高扩展性和长期可维护性：对稳定业务概念建模，对明确变化点预留扩展接口，对不确定需求避免过度抽象。
- 维护成本是验收标准之一：结构应让后续开发者能快速定位入口、用例、领域规则、持久化实现和外部适配点。

## 6. Standard Module Shape

每个业务模块最终应能归入下面的统一模型：

```text
develop-module-{name}/
  develop-module-{name}-api/
  develop-module-{name}-server/
  [develop-module-{name}-gateway/]
```

`develop-module-{name}-gateway` 仅当该模块确实有独立网关、协议接入或独立运行职责时存在。

## 7. API Module Standard

API 模块负责稳定契约，不承载业务编排：

```text
api/
  {business}/
    XxxCommonApi.java
    dto/
    enums/
    local/
    remote/
```

约束：

- `XxxCommonApi.java` 是本地和远程共同遵守的契约。
- `dto/` 存放跨模块调用需要的请求和响应 DTO。
- `enums/` 存放契约侧需要暴露的枚举。
- `local/` 存放本地调用适配器或本地适配标记。
- `remote/` 存放 Feign/远程调用适配器。
- api 模块不放应用编排、领域决策或持久化逻辑。
- api 契约中可使用适配器模式隔离本地调用和远程 Feign 调用，但共同契约必须保持稳定、清晰、单一职责。

## 8. Runtime Unit Internal Standard

`server` 或 `gateway` 运行单元内部统一为：

```text
domain/{aggregate}/
application/{aggregate}/
infrastructure/{aggregate}/
convert/
controller/
job/
mq/
framework/
```

职责边界：

- `domain/{aggregate}/`：聚合根、值对象、领域事件、领域仓储接口、领域服务；保持业务规则高内聚，不依赖 Spring、MyBatis、远程客户端或其他基础设施。
- `application/{aggregate}/`：用例编排、事务边界、权限、租户等应用级策略；负责协调聚合和外部端口，不沉淀领域规则。
- `infrastructure/{aggregate}/`：仓储实现、外部系统适配、DO/Mapper 协作；只实现技术细节，不反向定义业务规则。
- `convert/`：DTO/DO/domain 映射；只做对象转换，不写业务判断。
- `controller/`：Web 入口；只处理协议参数、认证上下文和响应封装，不直接操作 DO/Mapper。
- `job/`：定时任务入口；只触发应用用例，不复制业务流程。
- `mq/`：消息入口；只完成消息解析和应用用例调用，不承载领域决策。
- `framework/`：Spring 配置和模块技术配置；只放技术装配，不放业务逻辑。

`controller/job/mq/framework` 可以保留，但只承担入口或技术配置职责，不承载核心业务规则。`service` 和 `dal` 是迁移源，不是目标结构。迁移过程中可以暂存；每个聚合完成后，对应业务逻辑应从 `service` 移走，对应持久化实现应进入 `infrastructure`。如果还保留 `dal`，只能作为 MyBatis DO/Mapper 的技术位置，不再放业务决策。

## 9. Design Pattern Guidance

优先使用 Java 常见 23 种设计模式解决明确问题，但禁止为了套模式引入无业务价值的抽象层。

- 创建复杂聚合或值对象时，优先考虑工厂方法或抽象工厂，避免构造逻辑散落在应用层。
- 多算法、多渠道、多支付/通知/审批规则场景，优先考虑策略模式，避免大量条件分支堆积。
- 固定流程中存在可变步骤时，优先考虑模板方法，避免复制整段流程。
- 外部系统、远程 API、旧接口兼容场景，优先考虑适配器或门面模式，隔离技术差异。
- 领域事件、状态变化通知、跨边界异步协作场景，优先考虑观察者/发布订阅思想。
- 只有存在明确变化点、重复结构或依赖隔离需求时才引入模式；模式不能破坏单一职责或增加无意义耦合。

## 10. Extensibility and Maintainability

- 对支付渠道、通知渠道、审批规则、设备协议、外部服务供应商等明确变化点，应通过接口、策略、适配器或领域服务隔离变化。
- 对稳定业务概念，应优先沉淀为聚合、值对象、领域服务和领域事件，让业务语义集中表达。
- 对尚不明确的未来需求，不提前创建空接口、空抽象类或多层转发，避免维护者无法判断真实职责。
- 包、类、方法命名必须反映业务语义和层级职责，便于后续按结构快速定位和替换实现。

## 11. Special Module Convergence

### develop-module-iot

`develop-module-iot` 按运行单元标准收口：

- `develop-module-iot-server` 保留为业务管理运行单元。
- `develop-module-iot-gateway` 保留为协议接入或设备通信运行单元，但内部也要统一 DDD 分层，或明确只做技术接入。
- `develop-module-iot-core` 不能长期作为平级游离模块，必须明确归属：
  - 如果是共享领域能力，沉入 `iot-api` 的契约/值对象或 `iot-server` 的 domain。
  - 如果是技术基础设施，沉入 `iot-server/infrastructure` 或 `iot-gateway/infrastructure`。
  - 如果确实多个运行单元共用，定义为明确的 `iot-common` 或 `iot-shared` 基础包，但不承载业务用例。

### develop-module-mall

`develop-module-mall` 按业务上下文和运行单元标准收口：

- `product/promotion/trade/statistics` 视为 mall 下的独立业务上下文，每个上下文都必须拥有标准 `api + server`。
- `develop-module-mall-server` 不能长期作为模糊聚合点；要么只做 mall 组合入口，要么拆解或下沉到具体上下文。
- mall 下每个上下文按同一套 API 契约和 DDD 分层标准执行，不因位于 mall 父目录下而采用不同规则。

特殊模块可以有不同运行单元数量，但不能有不同结构规则。

## 12. Migration Order

1. 先写统一结构标准。
2. 先做 API 契约统一。
3. 再做 server 聚合迁移。
4. 最后收口特殊运行单元。

执行细则：

- 每次只迁移一个模块或一个聚合根。
- 必须先有对应聚合 skill，验证 skill 后再改代码。
- 迁移时同步补齐必要功能注释，尤其是 API 契约用途、应用用例职责、领域不变量、外部适配约束和回滚条件。
- 迁移时同步做代码质量优化，删除重复路径、收敛命名、拆分过长方法、修正错误依赖方向，确保高内聚低耦合。
- 迁移时按单一职责原则审查类和方法；如果一个类同时承担入口、编排、领域规则和持久化适配，应拆分到对应层。
- 迁移时识别可复用的设计模式场景，优先使用 Java 常见设计模式表达稳定结构；如果没有明确变化点，不引入额外模式。
- 迁移时识别后续高概率变化点，为明确变化点预留接口或扩展点；对不确定需求保持简单实现。
- 迁移后核心业务不再留在旧 `service`，仓储实现进入 `infrastructure`。

## 13. Acceptance Criteria

结构改造完成前必须满足：

- 模块 Maven 结构能解释为标准运行单元。
- api 模块具备统一契约和 `local/remote` 适配结构。
- server/gateway 内部具备 DDD 分层，入口层只做入口，不承载核心业务。
- 聚合、应用服务、基础设施适配、入口层的责任边界清晰，依赖方向不反转，不出现跨层直接穿透调用。
- 关键 API、用例、领域不变量和外部适配点具备必要功能注释，注释说明业务意图和约束，不复述代码步骤。
- 改造后的代码具备更高内聚和更低耦合，没有新增重复逻辑、模糊职责类、过长方法或无意义中转层。
- 类、接口、方法符合单一职责原则；任何多职责实现都必须拆分或说明暂存原因。
- 设计模式使用有明确业务或技术动机，能降低耦合、封装变化或复用流程；不得为了使用模式而制造抽象。
- 明确变化点具备可扩展接口或替换点，新增渠道、规则、协议或外部适配时不需要修改无关层代码。
- 后续维护者能通过包结构和命名快速定位对应职责；新增需求有明确落位，不需要猜测应放在旧 `service`、`dal` 还是新 DDD 层。
- 每次改造后至少执行对应 Maven compile/test。
- 不为“看起来统一”进行无业务价值的大规模移动；每次迁移要对应到一个聚合或一个明确结构问题。

## 14. Verification Commands

文档标准本身修改后运行：

```bash
git diff -- CLAUDE.md .claude/ddd-skills/Module_Structure_Standard.md docs/superpowers/specs/2026-05-23-module-structure-standard-design.md
```

模块或聚合代码迁移后，按影响范围运行：

```bash
mvn compile -pl develop-module-system/develop-module-system-server -am
mvn test -pl develop-module-system/develop-module-system-server
```

替换命令中的模块路径为实际被迁移模块。没有业务代码改动时，不要求执行 Maven 编译。

## 15. Red Flags

出现以下情况必须停止并重新分析：

- 为统一目录而移动大量文件，但无法说明业务边界或聚合边界。
- API 契约变化混入 server 聚合迁移。
- 修改 Controller 路径、VO、权限、租户、错误码、缓存、MQ、Job，却没有单独说明外部行为变化。
- 新增空接口、空抽象类、多层转发，只是为了显得可扩展。
- 设计模式让代码更难定位职责，或导致单一职责被破坏。
- `domain` 依赖 Spring、MyBatis、Feign、Controller VO、Mapper 或 DO。
- `controller/job/mq` 直接访问 Mapper/DO 并承载业务决策。
- `service/dal` 在聚合迁移后继续承载核心业务规则。

## 16. Quick Reference

| 问题 | 正确位置 | 禁止位置 |
|---|---|---|
| 业务不变量 | `domain/{aggregate}` | `controller`、`job`、`mq`、`convert` |
| 用例编排 | `application/{aggregate}` | `domain`、`convert` |
| 仓储接口 | `domain/{aggregate}/repository` | `infrastructure` 单独定义业务端口 |
| 仓储实现 | `infrastructure/{aggregate}` | `domain` |
| DTO/DO/domain 映射 | `convert` | `controller`、`domain` |
| Web 参数和响应封装 | `controller` | `domain`、`infrastructure` |
| 定时任务触发 | `job` 调用 application | `job` 复制业务流程 |
| 消息消费触发 | `mq` 调用 application | `mq` 承载领域决策 |
| Feign/远程调用适配 | `api/{business}/remote` | `domain` |
| 本地调用适配 | `api/{business}/local` 或 server adapter | `domain` |
| 技术配置 | `framework` | `domain`、`application` |
```

- [ ] **Step 3: Verify the document has required production sections**

Run:

```bash
grep -E "^## (1\. Overview|2\. When to Use|3\. When Not to Use|4\. Reproducibility Contract|13\. Acceptance Criteria|14\. Verification Commands|15\. Red Flags|16\. Quick Reference)" .claude/ddd-skills/Module_Structure_Standard.md
```

Expected output includes these eight headings:

```text
## 1. Overview
## 2. When to Use
## 3. When Not to Use
## 4. Reproducibility Contract
## 13. Acceptance Criteria
## 14. Verification Commands
## 15. Red Flags
## 16. Quick Reference
```

- [ ] **Step 4: Verify no placeholder language exists**

Run:

```bash
! grep -nE "TBD|TODO|implement later|fill in details|适当|待补充" .claude/ddd-skills/Module_Structure_Standard.md
```

Expected: command exits with code `0` and no output.

- [ ] **Step 5: Review the diff for the new file**

Run:

```bash
git diff -- .claude/ddd-skills/Module_Structure_Standard.md
```

Expected: diff shows only the new standard document. It does not show Java source changes.

---

### Task 2: Add CLAUDE.md entry point for the module structure standard

**Files:**
- Modify: `CLAUDE.md`
- Reference: `.claude/ddd-skills/Module_Structure_Standard.md`

- [ ] **Step 1: Re-read the DDD Architecture section before editing**

Read `CLAUDE.md` around the DDD Architecture section and confirm the aggregate process still ends with this line:

```markdown
5. Compile/test and check each acceptance criterion; if validation fails, revisit the analysis instead of broadening scope.
```

Expected: the line exists under `## DDD Architecture (current, in-progress)`.

- [ ] **Step 2: Insert the module structure subsection**

Immediately after the line from Step 1, insert this markdown:

```markdown

### Module structure standard

Before changing module structure, API contracts, runtime units, or DDD layer placement, read `.claude/ddd-skills/Module_Structure_Standard.md`.

The repository standard is:
- Maven structure is organized by runtime units. `server` and `gateway` may both exist when they have independent runtime responsibilities, but every runtime unit must follow the same internal structure rules.
- API modules use one stable contract with `local/remote` adapters for local module calls and remote Feign calls.
- `controller/job/mq/framework` may remain as entry and technical configuration layers. Core business logic belongs in `domain/application/infrastructure/convert`.
- `service/dal` are migration sources, not final homes for core business logic.
- Module and aggregate code must preserve high cohesion, low coupling, clear responsibility boundaries, single responsibility, necessary functional comments, maintainability, and extensibility.
- Use Java design patterns only for clear variation points or dependency isolation; do not add abstractions just to use a pattern.
- `iot` and `mall` are not permanent exceptions. They must converge to the same runtime-unit, API-contract, and DDD-layer standards.
```

- [ ] **Step 3: Verify the subsection was inserted once**

Run:

```bash
grep -n "### Module structure standard" CLAUDE.md
```

Expected output has exactly one line:

```text
102:### Module structure standard
```

The line number may differ if earlier content changed; the heading must appear exactly once.

- [ ] **Step 4: Verify the new entry points to the skill document**

Run:

```bash
grep -n "Module_Structure_Standard.md" CLAUDE.md
```

Expected output includes:

```text
Before changing module structure, API contracts, runtime units, or DDD layer placement, read `.claude/ddd-skills/Module_Structure_Standard.md`.
```

- [ ] **Step 5: Review the CLAUDE.md diff**

Run:

```bash
git diff -- CLAUDE.md
```

Expected: diff only adds the `### Module structure standard` subsection under DDD Architecture. It does not rewrite unrelated instructions.

---

### Task 3: Validate documentation-only scope

**Files:**
- Check: `CLAUDE.md`
- Check: `.claude/ddd-skills/Module_Structure_Standard.md`
- Check: `docs/superpowers/specs/2026-05-23-module-structure-standard-design.md`

- [ ] **Step 1: Confirm only documentation/instruction files changed in this phase**

Run:

```bash
git status --short
```

Expected: this phase's relevant changed files include only:

```text
 M CLAUDE.md
?? .claude/ddd-skills/Module_Structure_Standard.md
```

It is acceptable if pre-existing user changes from before this phase are also listed. Do not stage or modify unrelated files.

- [ ] **Step 2: Confirm no Java source files changed because of this phase**

Run:

```bash
git diff --name-only -- '*.java'
```

Expected: no output from this phase. If output appears, stop and inspect because phase 1 must not change business code.

- [ ] **Step 3: Confirm the approved design constraints are represented in the new standard**

Run:

```bash
grep -nE "高内聚低耦合|单一职责|23 种设计模式|高扩展性|长期可维护性|local/remote|iot|mall" .claude/ddd-skills/Module_Structure_Standard.md
```

Expected: output includes lines covering high cohesion/low coupling, single responsibility, Java design patterns, extensibility/maintainability, local/remote API adapters, and special handling for `iot` and `mall`.

- [ ] **Step 4: Confirm docs do not require Maven validation for this phase**

Run:

```bash
grep -n "没有业务代码改动时，不要求执行 Maven 编译" .claude/ddd-skills/Module_Structure_Standard.md
```

Expected output includes:

```text
没有业务代码改动时，不要求执行 Maven 编译。
```

- [ ] **Step 5: Final diff review**

Run:

```bash
git diff -- CLAUDE.md .claude/ddd-skills/Module_Structure_Standard.md docs/superpowers/specs/2026-05-23-module-structure-standard-design.md
```

Expected: diff reflects the approved module structure standard and the user's supplemental requirements. No Java or Maven module changes are present.

---

### Task 4: Commit phase 1 documentation changes if requested by the user

**Files:**
- Stage if requested: `CLAUDE.md`
- Stage if requested: `.claude/ddd-skills/Module_Structure_Standard.md`
- Stage if requested: `docs/superpowers/specs/2026-05-23-module-structure-standard-design.md`
- Stage if requested: `docs/superpowers/plans/2026-05-23-module-structure-standard-phase1.md`

- [ ] **Step 1: Ask for commit confirmation**

Say:

```text
Phase 1 documentation changes are ready. Do you want me to commit only these files?
```

Expected: user explicitly says whether to commit. Do not commit without explicit confirmation.

- [ ] **Step 2: If the user says no, stop before staging**

Expected: no git staging or commit commands are run.

- [ ] **Step 3: If the user says yes, inspect status and diff**

Run:

```bash
git status --short && git diff -- CLAUDE.md .claude/ddd-skills/Module_Structure_Standard.md docs/superpowers/specs/2026-05-23-module-structure-standard-design.md docs/superpowers/plans/2026-05-23-module-structure-standard-phase1.md
```

Expected: only the intended documentation and instruction files are selected for staging.

- [ ] **Step 4: Stage only intended files**

Run:

```bash
git add CLAUDE.md .claude/ddd-skills/Module_Structure_Standard.md docs/superpowers/specs/2026-05-23-module-structure-standard-design.md docs/superpowers/plans/2026-05-23-module-structure-standard-phase1.md
```

Expected: command exits with code `0`.

- [ ] **Step 5: Commit with project-style message**

Run:

```bash
git commit -m "$(cat <<'EOF'
DDD重构：定义模块统一结构标准

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```

Expected: commit succeeds. If hooks fail, fix the underlying issue and create a new commit attempt; do not use `--no-verify`.

- [ ] **Step 6: Verify clean status for staged files**

Run:

```bash
git status --short
```

Expected: the committed files no longer appear as staged or unstaged. Pre-existing unrelated user changes may still appear.

---

## Self-Review

- Spec coverage: The plan covers the approved design's first migration step: update `CLAUDE.md`, create `.claude/ddd-skills/Module_Structure_Standard.md`, preserve local/remote API guidance, runtime unit structure, DDD boundaries, `iot/mall` convergence, high cohesion/low coupling, comments, single responsibility, design patterns, extensibility, maintainability, and validation expectations.
- Placeholder scan: The plan contains no `TBD`, `TODO`, `implement later`, `fill in details`, or unspecified implementation steps.
- Scope check: This phase is documentation and instruction only. It explicitly excludes business Java code, Maven module movement, and API/server refactoring.
