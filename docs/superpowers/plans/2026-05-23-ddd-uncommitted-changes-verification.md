# DDD Uncommitted Changes Verification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Organize, verify, minimally fix, and commit the current uncommitted DDD refactoring changes one module at a time.

**Architecture:** Freeze the current working tree and treat existing changes as the only implementation scope. For each module, read the matching `.claude/ddd-skills/AggregateRoot_*_Skill.md`, inspect that module's diff, verify domain/application/infrastructure boundaries, run a module-level Maven compile, and commit only the verified module files plus its Skill file. Failed modules remain uncommitted with a precise blocker note rather than forcing a broad cross-module fix.

**Tech Stack:** Java 17, Spring Boot 3.5.x, Maven reactor, MyBatis Plus, Lombok, MapStruct, Git.

---

## Scope and Safety Rules

- Do not start new DDD refactoring beyond files already shown by `git status` at execution start.
- Do not use `git reset --hard`, `git checkout .`, `git clean`, or other destructive cleanup commands.
- Do not commit unrelated local files, secrets, generated build output, or files outside the module being verified.
- Do not bypass commit hooks.
- Do not stage with `git add .` or `git add -A`; stage exact files or exact module directories after checking status.
- If a module fails compile, make only the smallest changes needed inside that module and its corresponding Skill file.
- Use Chinese for user-facing status and module summaries.

## File Structure Map

### Global planning and progress files
- Create: `docs/superpowers/plans/2026-05-23-ddd-uncommitted-changes-verification.md`
  - This implementation plan.
- Optional create or modify only after the first successful module commit: `REFACTOR_PROGRESS.md`
  - Records verified modules, compile commands, commit IDs, and blockers.

### Skill files to verify with module changes
- Modify or stage with module if needed: `.claude/ddd-skills/AggregateRoot_Ai_Skill.md`
- Modify or stage with module if needed: `.claude/ddd-skills/AggregateRoot_Infra_Skill.md`
- Modify or stage with module if needed: `.claude/ddd-skills/AggregateRoot_MallProduct_Skill.md`
- Modify or stage with module if needed: `.claude/ddd-skills/AggregateRoot_MallPromotion_Skill.md`
- Modify or stage with module if needed: `.claude/ddd-skills/AggregateRoot_MallStatistics_Skill.md`
- Modify or stage with module if needed: `.claude/ddd-skills/AggregateRoot_MallTrade_Skill.md`
- Modify or stage with module if needed: `.claude/ddd-skills/AggregateRoot_MemberLevel_Skill.md`
- Modify or stage with module if needed: `.claude/ddd-skills/AggregateRoot_Pay_Skill.md`
- Modify or stage with module if needed: `.claude/ddd-skills/AggregateRoot_Wms_Skill.md`

### Module groups visible in current working tree
- AI: `develop-module-ai/develop-module-ai-server/src/main/java/com/develop/mvp/pk/module/ai/**`
- Infra: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/**`
- Mall Product: `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/**`
- Mall Promotion: `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/**`
- Mall Statistics: `develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics/**`
- Mall Trade: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/**`
- Member: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/**`
- Pay: `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/**`
- WMS: `develop-module-wms/develop-module-wms-server/src/main/java/com/develop/mvp/pk/module/wms/**`
- Project instructions/settings: `CLAUDE.md`, `.claude/settings.json`

---

### Task 1: Freeze and Inventory the Current Working Tree

**Files:**
- Read-only inspect: entire Git working tree
- Do not modify code in this task.

- [ ] **Step 1: Capture concise status**

Run:
```bash
git status --short
```
Expected: output lists only the existing uncommitted DDD changes and possibly this plan file under `docs/superpowers/plans/`.

- [ ] **Step 2: Capture module-level changed file counts**

Run:
```bash
git status --short | cut -c4- | awk -F/ '{print $1"/"$2}' | sort | uniq -c
```
Expected: groups include `.claude/ddd-skills`, `develop-module-ai/develop-module-ai-server`, `develop-module-infra/develop-module-infra-server`, `develop-module-mall/develop-module-product-server`, `develop-module-mall/develop-module-promotion-server`, `develop-module-mall/develop-module-statistics-server`, `develop-module-mall/develop-module-trade-server`, `develop-module-member/develop-module-member-server`, `develop-module-pay/develop-module-pay-server`, and `develop-module-wms/develop-module-wms-server`.

- [ ] **Step 3: Check whether planning docs are untracked**

Run:
```bash
git status --short docs/superpowers/plans/2026-05-23-ddd-uncommitted-changes-verification.md
```
Expected: this plan file may be untracked; do not include it in DDD module commits unless the user explicitly requests committing planning docs.

- [ ] **Step 4: Report inventory to user**

Report in Chinese:
```text
已冻结当前工作区范围：后续只整理现有未提交 DDD 改动。下一步按模块从 AI 开始验证；计划文档不混入业务模块提交。
```

---

### Task 2: Verify and Commit AI Module

**Files:**
- Read: `.claude/ddd-skills/AggregateRoot_Ai_Skill.md`
- Inspect/possibly modify: `develop-module-ai/develop-module-ai-server/src/main/java/com/develop/mvp/pk/module/ai/**`
- Compile module: `develop-module-ai/develop-module-ai-server`

- [ ] **Step 1: Read the AI Skill**

Read `.claude/ddd-skills/AggregateRoot_Ai_Skill.md` and extract:
```text
适用场景、DDD 构造块、职责边界、依赖协作、不变式、验收标准。
```
Expected: Skill describes AI model aggregate boundaries and repository/application responsibilities.

- [ ] **Step 2: Inspect only AI diff**

Run:
```bash
git diff -- .claude/ddd-skills/AggregateRoot_Ai_Skill.md develop-module-ai/develop-module-ai-server/src/main/java/com/develop/mvp/pk/module/ai
```
Expected: diff shows only AI Skill and AI module changes.

- [ ] **Step 3: Verify AI DDD boundaries**

Check these conditions manually in the diff:
```text
- domain/model contains pure domain classes, value objects, events, factory, and repository interfaces.
- domain/model does not import Spring controller/service annotations, MyBatis mapper classes, DO classes, or controller DTO classes.
- application/model orchestrates use cases through domain repository interfaces.
- controller/admin/model delegates to application/model instead of direct domain mutation or infrastructure access.
- infrastructure/model implements repository interfaces and depends on DAL/convert as needed.
```
Expected: each condition is satisfied or has a specific file-level blocker.

- [ ] **Step 4: Compile AI module**

Run:
```bash
mvn compile -pl develop-module-ai/develop-module-ai-server -am
```
Expected: Maven exits with code 0. If it fails, capture the first compiler error and continue to Step 5.

- [ ] **Step 5: Apply minimal AI fix if compile failed**

Only if Step 4 fails, edit the exact AI file causing the first compiler error. Examples of allowed minimal fixes:
```text
- Add a missing import for an AI domain value object used by the changed class.
- Align a repository method signature between `AiModelRepository` and `AiModelRepositoryImpl`.
- Fix constructor parameters between `AiModelFactory` and `AiModel`.
```
Not allowed:
```text
- Changing another module to make AI compile.
- Adding broad compatibility overloads that are not used.
- Rewriting unrelated AI service behavior.
```
After editing, rerun:
```bash
mvn compile -pl develop-module-ai/develop-module-ai-server -am
```
Expected: Maven exits with code 0 or a precise AI blocker remains.

- [ ] **Step 6: Stage only AI files**

Run:
```bash
git add .claude/ddd-skills/AggregateRoot_Ai_Skill.md develop-module-ai/develop-module-ai-server/src/main/java/com/develop/mvp/pk/module/ai
```
Expected: only AI files and AI Skill are staged.

- [ ] **Step 7: Verify staged AI diff**

Run:
```bash
git diff --cached --name-only
```
Expected: every staged path is either `.claude/ddd-skills/AggregateRoot_Ai_Skill.md` or under `develop-module-ai/develop-module-ai-server/src/main/java/com/develop/mvp/pk/module/ai/`.

- [ ] **Step 8: Commit AI module**

Run:
```bash
git commit -m "$(cat <<'EOF'
refactor(ddd): AI 模块重构为 DDD 架构 - 应用技能 AggregateRoot_Ai

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID. If a hook fails, fix the underlying AI issue and create a new commit attempt without `--no-verify`.

---

### Task 3: Verify and Commit Infra Module

**Files:**
- Read: `.claude/ddd-skills/AggregateRoot_Infra_Skill.md`
- Inspect/possibly modify: `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/**`
- Compile module: `develop-module-infra/develop-module-infra-server`

- [ ] **Step 1: Read the Infra Skill**

Read `.claude/ddd-skills/AggregateRoot_Infra_Skill.md` and extract:
```text
Config、File、DataSourceConfig、Codegen、Logger 等基础设施聚合的职责边界、依赖协作、不变式、验收标准。
```
Expected: Skill covers the infra aggregates touched by the current diff.

- [ ] **Step 2: Inspect only Infra diff**

Run:
```bash
git diff -- .claude/ddd-skills/AggregateRoot_Infra_Skill.md develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra
```
Expected: diff shows only Infra Skill and Infra module changes.

- [ ] **Step 3: Verify Infra DDD boundaries**

Check:
```text
- domain/* aggregate classes remain pure Java domain models.
- application/* services orchestrate codegen, db, file, and logger use cases.
- controller/admin/* does not directly access infrastructure repositories.
- infrastructure/* repository implementations contain mapper/DO integration.
- domain layer has no direct dependency on controller DTOs, mapper interfaces, or Spring infrastructure.
```
Expected: each condition is satisfied or has a specific file-level blocker.

- [ ] **Step 4: Compile Infra module**

Run:
```bash
mvn compile -pl develop-module-infra/develop-module-infra-server -am
```
Expected: Maven exits with code 0.

- [ ] **Step 5: Apply minimal Infra fix if compile failed**

Only if compile fails, edit the exact Infra file causing the first compiler error. Allowed fixes:
```text
- Align a domain repository interface with its implementation.
- Fix missing import or incorrect package after DDD relocation.
- Correct application service constructor injection for a repository interface.
```
After editing, rerun:
```bash
mvn compile -pl develop-module-infra/develop-module-infra-server -am
```
Expected: Maven exits with code 0 or a precise Infra blocker remains.

- [ ] **Step 6: Stage only Infra files**

Run:
```bash
git add .claude/ddd-skills/AggregateRoot_Infra_Skill.md develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra
```
Expected: only Infra files and Infra Skill are staged.

- [ ] **Step 7: Verify staged Infra diff**

Run:
```bash
git diff --cached --name-only
```
Expected: every staged path is either `.claude/ddd-skills/AggregateRoot_Infra_Skill.md` or under `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/`.

- [ ] **Step 8: Commit Infra module**

Run:
```bash
git commit -m "$(cat <<'EOF'
refactor(ddd): Infra 模块重构为 DDD 架构 - 应用技能 AggregateRoot_Infra

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID.

---

### Task 4: Verify and Commit Mall Product Module

**Files:**
- Read: `.claude/ddd-skills/AggregateRoot_MallProduct_Skill.md`
- Inspect/possibly modify: `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/**`
- Compile module: `develop-module-mall/develop-module-product-server`

- [ ] **Step 1: Read the Mall Product Skill**

Read `.claude/ddd-skills/AggregateRoot_MallProduct_Skill.md` and extract:
```text
ProductBrand、ProductCategory、ProductSpu 聚合职责、状态变更事件、仓储边界、验收标准。
```
Expected: Skill covers all product module changed aggregates.

- [ ] **Step 2: Inspect only Mall Product diff**

Run:
```bash
git diff -- .claude/ddd-skills/AggregateRoot_MallProduct_Skill.md develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product
```
Expected: diff shows Product Skill and Product module changes only.

- [ ] **Step 3: Verify Product DDD boundaries**

Check:
```text
- ProductBrand/ProductCategory/ProductSpu aggregate classes keep business state and invariants.
- Status change and creation/deletion events are emitted or represented in domain event classes.
- Application services call repositories through domain interfaces.
- Infrastructure repository implementations map between DO and domain objects.
- Domain classes do not import MyBatis mapper, DO, controller VO, or Spring service classes.
```
Expected: each condition is satisfied or has a precise blocker.

- [ ] **Step 4: Compile Mall Product module**

Run:
```bash
mvn compile -pl develop-module-mall/develop-module-product-server -am
```
Expected: Maven exits with code 0.

- [ ] **Step 5: Apply minimal Product fix if compile failed**

Only if compile fails, edit the first failing Product file. Allowed fixes:
```text
- Align event constructor signatures with aggregate calls.
- Align repository interface methods with implementation methods.
- Fix package/import errors caused by new domain/event package locations.
```
After editing, rerun:
```bash
mvn compile -pl develop-module-mall/develop-module-product-server -am
```
Expected: Maven exits with code 0 or a precise Product blocker remains.

- [ ] **Step 6: Stage only Product files**

Run:
```bash
git add .claude/ddd-skills/AggregateRoot_MallProduct_Skill.md develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product
```
Expected: only Product files and Product Skill are staged.

- [ ] **Step 7: Verify staged Product diff**

Run:
```bash
git diff --cached --name-only
```
Expected: every staged path is either `.claude/ddd-skills/AggregateRoot_MallProduct_Skill.md` or under `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/`.

- [ ] **Step 8: Commit Product module**

Run:
```bash
git commit -m "$(cat <<'EOF'
refactor(ddd): Mall Product 模块重构为 DDD 架构 - 应用技能 AggregateRoot_MallProduct

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID.

---

### Task 5: Verify and Commit Mall Promotion Module

**Files:**
- Read: `.claude/ddd-skills/AggregateRoot_MallPromotion_Skill.md`
- Inspect/possibly modify: `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/**`
- Compile module: `develop-module-mall/develop-module-promotion-server`

- [ ] **Step 1: Read the Mall Promotion Skill**

Read `.claude/ddd-skills/AggregateRoot_MallPromotion_Skill.md` and extract:
```text
Banner、CouponTemplate、SeckillActivity 聚合职责、活动状态规则、事件、仓储边界、验收标准。
```
Expected: Skill covers all promotion module changed aggregates.

- [ ] **Step 2: Inspect only Promotion diff**

Run:
```bash
git diff -- .claude/ddd-skills/AggregateRoot_MallPromotion_Skill.md develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion
```
Expected: diff shows Promotion Skill and Promotion module changes only.

- [ ] **Step 3: Verify Promotion DDD boundaries**

Check:
```text
- Banner/CouponTemplate/SeckillActivity domain classes hold domain state and status rules.
- SeckillActivity status event is in domain event package and does not depend on infrastructure.
- Application services orchestrate promotion use cases through repository interfaces.
- Infrastructure repository implementations isolate mapper/DO conversion.
- Domain layer does not import controller VO, mapper, DO, or Spring infrastructure.
```
Expected: each condition is satisfied or has a precise blocker.

- [ ] **Step 4: Compile Mall Promotion module**

Run:
```bash
mvn compile -pl develop-module-mall/develop-module-promotion-server -am
```
Expected: Maven exits with code 0.

- [ ] **Step 5: Apply minimal Promotion fix if compile failed**

Only if compile fails, edit the first failing Promotion file. Allowed fixes:
```text
- Align activity event constructors with aggregate methods.
- Fix repository interface/implementation method mismatch.
- Fix imports after domain relocation.
```
After editing, rerun:
```bash
mvn compile -pl develop-module-mall/develop-module-promotion-server -am
```
Expected: Maven exits with code 0 or a precise Promotion blocker remains.

- [ ] **Step 6: Stage only Promotion files**

Run:
```bash
git add .claude/ddd-skills/AggregateRoot_MallPromotion_Skill.md develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion
```
Expected: only Promotion files and Promotion Skill are staged.

- [ ] **Step 7: Verify staged Promotion diff**

Run:
```bash
git diff --cached --name-only
```
Expected: every staged path is either `.claude/ddd-skills/AggregateRoot_MallPromotion_Skill.md` or under `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/`.

- [ ] **Step 8: Commit Promotion module**

Run:
```bash
git commit -m "$(cat <<'EOF'
refactor(ddd): Mall Promotion 模块重构为 DDD 架构 - 应用技能 AggregateRoot_MallPromotion

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID.

---

### Task 6: Verify and Commit Mall Statistics Module

**Files:**
- Read: `.claude/ddd-skills/AggregateRoot_MallStatistics_Skill.md`
- Inspect/possibly modify: `develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics/**`
- Compile module: `develop-module-mall/develop-module-statistics-server`

- [ ] **Step 1: Read the Mall Statistics Skill**

Read `.claude/ddd-skills/AggregateRoot_MallStatistics_Skill.md` and extract:
```text
ProductStatistics、TradeStatistics 聚合职责、统计值约束、仓储边界、验收标准。
```
Expected: Skill covers all statistics module changed aggregates.

- [ ] **Step 2: Inspect only Statistics diff**

Run:
```bash
git diff -- .claude/ddd-skills/AggregateRoot_MallStatistics_Skill.md develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics
```
Expected: diff shows Statistics Skill and Statistics module changes only.

- [ ] **Step 3: Verify Statistics DDD boundaries**

Check:
```text
- ProductStatistics and TradeStatistics domain classes hold metric state and invariants.
- Repository interfaces are in domain packages.
- Infrastructure implementations isolate mapper/DO access.
- Domain layer does not import controller VO, mapper, DO, or Spring infrastructure.
```
Expected: each condition is satisfied or has a precise blocker.

- [ ] **Step 4: Compile Mall Statistics module**

Run:
```bash
mvn compile -pl develop-module-mall/develop-module-statistics-server -am
```
Expected: Maven exits with code 0.

- [ ] **Step 5: Apply minimal Statistics fix if compile failed**

Only if compile fails, edit the first failing Statistics file. Allowed fixes:
```text
- Align repository interface and implementation method signatures.
- Fix imports after domain relocation.
- Correct domain constructor argument order to match the aggregate definition.
```
After editing, rerun:
```bash
mvn compile -pl develop-module-mall/develop-module-statistics-server -am
```
Expected: Maven exits with code 0 or a precise Statistics blocker remains.

- [ ] **Step 6: Stage only Statistics files**

Run:
```bash
git add .claude/ddd-skills/AggregateRoot_MallStatistics_Skill.md develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics
```
Expected: only Statistics files and Statistics Skill are staged.

- [ ] **Step 7: Verify staged Statistics diff**

Run:
```bash
git diff --cached --name-only
```
Expected: every staged path is either `.claude/ddd-skills/AggregateRoot_MallStatistics_Skill.md` or under `develop-module-mall/develop-module-statistics-server/src/main/java/com/develop/mvp/pk/module/statistics/`.

- [ ] **Step 8: Commit Statistics module**

Run:
```bash
git commit -m "$(cat <<'EOF'
refactor(ddd): Mall Statistics 模块重构为 DDD 架构 - 应用技能 AggregateRoot_MallStatistics

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID.

---

### Task 7: Verify and Commit Mall Trade Module

**Files:**
- Read: `.claude/ddd-skills/AggregateRoot_MallTrade_Skill.md`
- Inspect/possibly modify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/**`
- Compile module: `develop-module-mall/develop-module-trade-server`

- [ ] **Step 1: Read the Mall Trade Skill**

Read `.claude/ddd-skills/AggregateRoot_MallTrade_Skill.md` and extract:
```text
AfterSale、Cart、TradeOrder 聚合职责、订单状态事件、售后/购物车边界、验收标准。
```
Expected: Skill covers all trade module changed aggregates.

- [ ] **Step 2: Inspect only Trade diff**

Run:
```bash
git diff -- .claude/ddd-skills/AggregateRoot_MallTrade_Skill.md develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade
```
Expected: diff shows Trade Skill and Trade module changes only.

- [ ] **Step 3: Verify Trade DDD boundaries**

Check:
```text
- TradeOrder, AfterSale, and Cart aggregate classes hold state and invariant methods.
- TradeOrder domain events do not depend on infrastructure or controller classes.
- Application services orchestrate through domain repository interfaces.
- Infrastructure repository implementations isolate mapper/DO conversion.
- Domain layer does not import controller VO, mapper, DO, or Spring infrastructure.
```
Expected: each condition is satisfied or has a precise blocker.

- [ ] **Step 4: Compile Mall Trade module**

Run:
```bash
mvn compile -pl develop-module-mall/develop-module-trade-server -am
```
Expected: Maven exits with code 0.

- [ ] **Step 5: Apply minimal Trade fix if compile failed**

Only if compile fails, edit the first failing Trade file. Allowed fixes:
```text
- Align TradeOrder event constructor signatures.
- Align repository interface and implementation method signatures.
- Fix imports after domain relocation.
```
After editing, rerun:
```bash
mvn compile -pl develop-module-mall/develop-module-trade-server -am
```
Expected: Maven exits with code 0 or a precise Trade blocker remains.

- [ ] **Step 6: Stage only Trade files**

Run:
```bash
git add .claude/ddd-skills/AggregateRoot_MallTrade_Skill.md develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade
```
Expected: only Trade files and Trade Skill are staged.

- [ ] **Step 7: Verify staged Trade diff**

Run:
```bash
git diff --cached --name-only
```
Expected: every staged path is either `.claude/ddd-skills/AggregateRoot_MallTrade_Skill.md` or under `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/`.

- [ ] **Step 8: Commit Trade module**

Run:
```bash
git commit -m "$(cat <<'EOF'
refactor(ddd): Mall Trade 模块重构为 DDD 架构 - 应用技能 AggregateRoot_MallTrade

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID.

---

### Task 8: Verify and Commit Member Module

**Files:**
- Read: `.claude/ddd-skills/AggregateRoot_MemberLevel_Skill.md`
- Inspect/possibly modify: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/**`
- Compile module: `develop-module-member/develop-module-member-server`

- [ ] **Step 1: Read the Member Skill**

Read `.claude/ddd-skills/AggregateRoot_MemberLevel_Skill.md` and extract:
```text
MemberLevel and related member aggregate boundaries, controller/application separation, convert boundaries, invariants, and acceptance criteria.
```
Expected: Skill covers the member files currently changed, or identifies if another member Skill is required before committing.

- [ ] **Step 2: Inspect only Member diff**

Run:
```bash
git diff -- .claude/ddd-skills/AggregateRoot_MemberLevel_Skill.md develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member
```
Expected: diff shows Member Skill and Member module changes only.

- [ ] **Step 3: Verify Member DDD boundaries**

Check:
```text
- Controllers delegate to application services and do not perform domain persistence directly.
- Converts map VO/DO/domain boundaries without embedding use-case logic.
- Application services orchestrate use cases and call domain repositories where present.
- Domain classes do not import controller VO, mapper, DO, or Spring infrastructure.
```
Expected: each condition is satisfied or has a precise blocker.

- [ ] **Step 4: Compile Member module**

Run:
```bash
mvn compile -pl develop-module-member/develop-module-member-server -am
```
Expected: Maven exits with code 0.

- [ ] **Step 5: Apply minimal Member fix if compile failed**

Only if compile fails, edit the first failing Member file. Allowed fixes:
```text
- Align controller calls with application service method signatures.
- Fix MapStruct convert method signatures after domain model introduction.
- Fix imports after package relocation.
```
After editing, rerun:
```bash
mvn compile -pl develop-module-member/develop-module-member-server -am
```
Expected: Maven exits with code 0 or a precise Member blocker remains.

- [ ] **Step 6: Stage only Member files**

Run:
```bash
git add .claude/ddd-skills/AggregateRoot_MemberLevel_Skill.md develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member
```
Expected: only Member files and Member Skill are staged.

- [ ] **Step 7: Verify staged Member diff**

Run:
```bash
git diff --cached --name-only
```
Expected: every staged path is either `.claude/ddd-skills/AggregateRoot_MemberLevel_Skill.md` or under `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/`.

- [ ] **Step 8: Commit Member module**

Run:
```bash
git commit -m "$(cat <<'EOF'
refactor(ddd): Member 模块重构为 DDD 架构 - 应用技能 AggregateRoot_MemberLevel

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID.

---

### Task 9: Verify and Commit Pay Module

**Files:**
- Read: `.claude/ddd-skills/AggregateRoot_Pay_Skill.md`
- Inspect/possibly modify: `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/**`
- Compile module: `develop-module-pay/develop-module-pay-server`

- [ ] **Step 1: Read the Pay Skill**

Read `.claude/ddd-skills/AggregateRoot_Pay_Skill.md` and extract:
```text
PayApp、PayChannel、PayOrder、PayRefund、PayTransfer、PayWallet 聚合职责、支付状态规则、仓储边界、验收标准。
```
Expected: Skill covers all pay module changed aggregates.

- [ ] **Step 2: Inspect only Pay diff**

Run:
```bash
git diff -- .claude/ddd-skills/AggregateRoot_Pay_Skill.md develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay
```
Expected: diff shows Pay Skill and Pay module changes only.

- [ ] **Step 3: Verify Pay DDD boundaries**

Check:
```text
- Pay domain aggregate classes hold payment/wallet state and invariant methods.
- Controllers delegate to application services and do not directly access infrastructure repositories.
- Application services orchestrate through domain repository interfaces where present.
- Domain layer does not import controller VO, mapper, DO, or Spring infrastructure.
```
Expected: each condition is satisfied or has a precise blocker.

- [ ] **Step 4: Compile Pay module**

Run:
```bash
mvn compile -pl develop-module-pay/develop-module-pay-server -am
```
Expected: Maven exits with code 0.

- [ ] **Step 5: Apply minimal Pay fix if compile failed**

Only if compile fails, edit the first failing Pay file. Allowed fixes:
```text
- Align controller calls with application service signatures.
- Fix domain constructor or event constructor mismatches.
- Fix imports after domain relocation.
```
After editing, rerun:
```bash
mvn compile -pl develop-module-pay/develop-module-pay-server -am
```
Expected: Maven exits with code 0 or a precise Pay blocker remains.

- [ ] **Step 6: Stage only Pay files**

Run:
```bash
git add .claude/ddd-skills/AggregateRoot_Pay_Skill.md develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay
```
Expected: only Pay files and Pay Skill are staged.

- [ ] **Step 7: Verify staged Pay diff**

Run:
```bash
git diff --cached --name-only
```
Expected: every staged path is either `.claude/ddd-skills/AggregateRoot_Pay_Skill.md` or under `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/`.

- [ ] **Step 8: Commit Pay module**

Run:
```bash
git commit -m "$(cat <<'EOF'
refactor(ddd): Pay 模块重构为 DDD 架构 - 应用技能 AggregateRoot_Pay

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID.

---

### Task 10: Verify and Commit WMS Module

**Files:**
- Read: `.claude/ddd-skills/AggregateRoot_Wms_Skill.md`
- Inspect/possibly modify: `develop-module-wms/develop-module-wms-server/src/main/java/com/develop/mvp/pk/module/wms/**`
- Compile module: `develop-module-wms/develop-module-wms-server`

- [ ] **Step 1: Read the WMS Skill**

Read `.claude/ddd-skills/AggregateRoot_Wms_Skill.md` and extract:
```text
WmsInventory 聚合职责、库存数量约束、仓储接口、分页查询边界、验收标准。
```
Expected: Skill covers all WMS changed files.

- [ ] **Step 2: Inspect only WMS diff**

Run:
```bash
git diff -- .claude/ddd-skills/AggregateRoot_Wms_Skill.md develop-module-wms/develop-module-wms-server/src/main/java/com/develop/mvp/pk/module/wms
```
Expected: diff shows WMS Skill and WMS module changes only.

- [ ] **Step 3: Verify WMS DDD boundaries**

Check:
```text
- WmsInventory aggregate enforces inventory invariants.
- WmsInventoryFactory creates valid aggregate instances without infrastructure dependencies.
- WmsInventoryRepository and WmsInventoryPageQuery live in domain repository boundary.
- Infrastructure implementation maps DAL objects to domain objects.
- Domain layer does not import controller VO, mapper, DO, or Spring infrastructure.
```
Expected: each condition is satisfied or has a precise blocker.

- [ ] **Step 4: Compile WMS module**

Run:
```bash
mvn compile -pl develop-module-wms/develop-module-wms-server -am
```
Expected: Maven exits with code 0.

- [ ] **Step 5: Apply minimal WMS fix if compile failed**

Only if compile fails, edit the first failing WMS file. Allowed fixes:
```text
- Align `WmsInventoryRepository` and implementation signatures.
- Fix `WmsInventoryFactory` constructor calls.
- Fix imports after package relocation.
```
After editing, rerun:
```bash
mvn compile -pl develop-module-wms/develop-module-wms-server -am
```
Expected: Maven exits with code 0 or a precise WMS blocker remains.

- [ ] **Step 6: Stage only WMS files**

Run:
```bash
git add .claude/ddd-skills/AggregateRoot_Wms_Skill.md develop-module-wms/develop-module-wms-server/src/main/java/com/develop/mvp/pk/module/wms
```
Expected: only WMS files and WMS Skill are staged.

- [ ] **Step 7: Verify staged WMS diff**

Run:
```bash
git diff --cached --name-only
```
Expected: every staged path is either `.claude/ddd-skills/AggregateRoot_Wms_Skill.md` or under `develop-module-wms/develop-module-wms-server/src/main/java/com/develop/mvp/pk/module/wms/`.

- [ ] **Step 8: Commit WMS module**

Run:
```bash
git commit -m "$(cat <<'EOF'
refactor(ddd): WMS 模块重构为 DDD 架构 - 应用技能 AggregateRoot_Wms

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID.

---

### Task 11: Handle Project Instruction and Settings Changes

**Files:**
- Inspect: `CLAUDE.md`
- Inspect: `.claude/settings.json`

- [ ] **Step 1: Inspect instruction/settings diff**

Run:
```bash
git diff -- CLAUDE.md .claude/settings.json
```
Expected: output shows only Claude Code instructions or local settings changes.

- [ ] **Step 2: Decide whether these files belong in a separate commit**

If `CLAUDE.md` changes document durable project DDD process, stage it separately. If `.claude/settings.json` only contains local permission settings, ask the user before committing it.

- [ ] **Step 3: Commit project instructions if appropriate**

Only if `CLAUDE.md` should be committed, run:
```bash
git add CLAUDE.md
git commit -m "$(cat <<'EOF'
docs: update DDD refactoring guidance

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```
Expected: commit succeeds and prints a new commit ID.

- [ ] **Step 4: Leave local settings unstaged unless explicitly approved**

Run:
```bash
git status --short .claude/settings.json
```
Expected: if `.claude/settings.json` remains untracked or modified, report it as intentionally left uncommitted unless the user asks otherwise.

---

### Task 12: Final Verification and Progress Report

**Files:**
- Optional create/modify: `REFACTOR_PROGRESS.md`
- Read-only inspect: Git history and working tree

- [ ] **Step 1: Check final working tree**

Run:
```bash
git status --short
```
Expected: no uncommitted module changes remain for successfully committed modules. Any remaining changes are known blockers, planning docs, or intentionally uncommitted settings.

- [ ] **Step 2: Run aggregate compile if all module compiles passed**

Only if every module compile in Tasks 2-10 passed, run:
```bash
mvn compile -pl develop-server -am
```
Expected: Maven exits with code 0. If it fails, report the first compiler error and the likely module owner.

- [ ] **Step 3: Write or update progress file if requested by user**

If the user wants a progress record in the repository, create or update `REFACTOR_PROGRESS.md` with this exact structure:
```markdown
# DDD Refactor Progress

## 2026-05-23

| Module | Skill | Compile Command | Result | Commit |
|---|---|---|---|---|
| AI | AggregateRoot_Ai | `mvn compile -pl develop-module-ai/develop-module-ai-server -am` | PASS/FAIL | `<commit-or-blocker>` |
| Infra | AggregateRoot_Infra | `mvn compile -pl develop-module-infra/develop-module-infra-server -am` | PASS/FAIL | `<commit-or-blocker>` |
| Mall Product | AggregateRoot_MallProduct | `mvn compile -pl develop-module-mall/develop-module-product-server -am` | PASS/FAIL | `<commit-or-blocker>` |
| Mall Promotion | AggregateRoot_MallPromotion | `mvn compile -pl develop-module-mall/develop-module-promotion-server -am` | PASS/FAIL | `<commit-or-blocker>` |
| Mall Statistics | AggregateRoot_MallStatistics | `mvn compile -pl develop-module-mall/develop-module-statistics-server -am` | PASS/FAIL | `<commit-or-blocker>` |
| Mall Trade | AggregateRoot_MallTrade | `mvn compile -pl develop-module-mall/develop-module-trade-server -am` | PASS/FAIL | `<commit-or-blocker>` |
| Member | AggregateRoot_MemberLevel | `mvn compile -pl develop-module-member/develop-module-member-server -am` | PASS/FAIL | `<commit-or-blocker>` |
| Pay | AggregateRoot_Pay | `mvn compile -pl develop-module-pay/develop-module-pay-server -am` | PASS/FAIL | `<commit-or-blocker>` |
| WMS | AggregateRoot_Wms | `mvn compile -pl develop-module-wms/develop-module-wms-server -am` | PASS/FAIL | `<commit-or-blocker>` |
```
Replace `PASS/FAIL` and `<commit-or-blocker>` with actual results before writing the file.

- [ ] **Step 4: Report final result to user**

Report in Chinese:
```text
已按模块完成/阻塞清单：
- 模块：验证结论、编译结果、提交 ID 或 blocker
- 剩余未提交文件：原因
- 建议下一步：继续处理 blocker / 运行全量 package / 开始下一个模块
```

---

## Self-Review

- Spec coverage: The plan covers freezing current changes, module grouping, Skill verification, module-level compile, minimal fixes, per-module commits, settings handling, and final reporting.
- Placeholder scan: No `TBD`, `TODO`, or unspecified implementation steps remain. The only angle-bracket examples appear in the optional progress-file template and must be replaced at execution time.
- Type/path consistency: Module paths, Skill filenames, Maven module coordinates, and commit messages are consistent across tasks.
- Scope check: The work is large but decomposed into independently verifiable module tasks; each task can produce a working commit or a precise blocker.
