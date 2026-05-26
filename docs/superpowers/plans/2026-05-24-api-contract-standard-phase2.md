# API Contract Standard Phase 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce a current-code API contract audit and one concrete pilot migration plan for standardizing module API contracts with `CommonApi` + `local/remote` adapters.

**Architecture:** Phase 2 is documentation and planning first, not bulk business-code migration. It reads every `develop-module-*-api` module, compares it against `.claude/ddd-skills/Module_Structure_Standard.md`, uses `develop-module-system-api` as the current remote-adapter sample, then creates an audit report and pilot-module migration plan that can be reviewed before any Java API files are moved.

**Tech Stack:** Markdown documentation, Maven multi-module structure, Java API contracts, Spring Cloud OpenFeign/RPC conventions, DDD module structure standard.

---

## File Structure

- Read: `.claude/ddd-skills/Module_Structure_Standard.md`
  - Responsibility: canonical structure standard for API contracts and module layout.

- Read: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/**`
  - Responsibility: current sample for API contracts with `remote` adapters.

- Create: `docs/superpowers/reports/2026-05-24-api-contract-audit.md`
  - Responsibility: current-state audit of every API Maven module, including package shape, CommonApi/API interface presence, DTO/enums location, local/remote presence, and recommended priority.

- Create: `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`
  - Responsibility: pilot migration design for one API module. The recommended pilot is `develop-module-member-api` because it already has multiple business API packages and DTOs but lacks `remote` adapter packages, making it representative without being as broad as `system` or as special as `iot/mall`.

- No Java source changes in this phase:
  - Do not move API interfaces.
  - Do not create `remote` or `local` Java classes.
  - Do not rename `*Api.java` or DTO classes.
  - Do not modify Maven dependencies.

---

### Task 1: Generate API contract audit report

**Files:**
- Read: `.claude/ddd-skills/Module_Structure_Standard.md`
- Read: `develop-module-*/develop-module-*-api/**`
- Read: `develop-module-mall/develop-module-*-api/**`
- Create: `docs/superpowers/reports/2026-05-24-api-contract-audit.md`

- [ ] **Step 1: Confirm the module structure standard is present**

Run:

```bash
test -f .claude/ddd-skills/Module_Structure_Standard.md && grep -n "## 7. API Module Standard" .claude/ddd-skills/Module_Structure_Standard.md
```

Expected output includes:

```text
## 7. API Module Standard
```

- [ ] **Step 2: Capture the API module list**

Run:

```bash
find develop-module-* -maxdepth 2 -type d -name 'develop-module-*-api' | sort
```

Expected output includes exactly these API modules in the current repository:

```text
develop-module-ai/develop-module-ai-api
develop-module-bpm/develop-module-bpm-api
develop-module-crm/develop-module-crm-api
develop-module-erp/develop-module-erp-api
develop-module-infra/develop-module-infra-api
develop-module-iot/develop-module-iot-api
develop-module-mall/develop-module-product-api
develop-module-mall/develop-module-promotion-api
develop-module-mall/develop-module-statistics-api
develop-module-mall/develop-module-trade-api
develop-module-member/develop-module-member-api
develop-module-mes/develop-module-mes-api
develop-module-mp/develop-module-mp-api
develop-module-pay/develop-module-pay-api
develop-module-report/develop-module-report-api
develop-module-system/develop-module-system-api
develop-module-wms/develop-module-wms-api
```

- [ ] **Step 3: Capture API-related package directories**

Run:

```bash
find develop-module-* -path '*/src/main/java/*' -type d \( -name remote -o -name local -o -name api -o -name dto -o -name enums \) | sort > /tmp/api-contract-dirs.txt
```

Expected: `/tmp/api-contract-dirs.txt` exists and includes `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/remote`.

- [ ] **Step 4: Capture API interface files**

Run:

```bash
find develop-module-*/develop-module-*-api develop-module-mall/develop-module-*-api -path '*/src/main/java/*' -type f \( -name '*Api.java' -o -name '*CommonApi.java' \) | sort > /tmp/api-contract-interfaces.txt
```

Expected: `/tmp/api-contract-interfaces.txt` exists. In the current repository, `*CommonApi.java` may have no matches because the current sample uses names like `AdminUserApi.java`, `DeptApi.java`, and `MailSendApi.java`.

- [ ] **Step 5: Capture remote adapter files**

Run:

```bash
find develop-module-*/develop-module-*-api develop-module-mall/develop-module-*-api -path '*/src/main/java/*' -type f -name '*Remote*.java' | sort > /tmp/api-contract-remote-files.txt
```

Expected: `/tmp/api-contract-remote-files.txt` exists and currently includes system files such as:

```text
develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/remote/AdminUserRemoteClient.java
```

- [ ] **Step 6: Create the audit report directory**

Run:

```bash
mkdir -p docs/superpowers/reports
```

Expected: command exits with code `0`.

- [ ] **Step 7: Write the audit report**

Create `docs/superpowers/reports/2026-05-24-api-contract-audit.md` with this content, updating only the evidence bullets if command output differs from the current repository state:

```markdown
# API Contract Audit Report

## Scope

This report audits API contract structure for all backend API Maven modules against `.claude/ddd-skills/Module_Structure_Standard.md`.

This phase is read-only with respect to Java source. It does not rename interfaces, move DTOs, create adapters, or change Maven dependencies.

## Standard Checked

API modules should converge toward:

```text
api/
  {business}/
    XxxApi.java or XxxCommonApi.java
    dto/
    enums/
    local/
    remote/
```

The current repository standard document says the stable contract may be named `CommonApi`, but the current `system-api` sample uses `*Api.java` contract names with `remote/*RemoteClient.java` adapters. Until a naming migration is explicitly approved, this audit treats existing `*Api.java` names as the current contract interface shape and focuses Phase 2 on package boundaries and adapter shape.

## API Modules

| Module | Current Shape | Remote Adapter | Local Adapter | Priority | Recommendation |
|---|---|---:|---:|---|---|
| develop-module-system-api | `api/{business}`, `dto`, `remote`, `enums` | Yes | No | Sample | Use as current remote-adapter reference. |
| develop-module-member-api | `api/{business}`, `dto`, `enums` | No | No | P0 Pilot | Use as pilot because it has multiple API groups and DTOs but lacks `remote`. |
| develop-module-pay-api | `api/{business}`, `dto`, `enums` | No | No | P1 | Migrate after pilot; high business risk requires contract review first. |
| develop-module-infra-api | `api/{business}`, `dto`, `enums` | No | No | P1 | Migrate after pilot; infra contracts are widely consumed. |
| develop-module-bpm-api | `api/task`, `dto`, `enums` | No | No | P1 | Migrate after pilot; workflow contracts require compatibility checks. |
| develop-module-product-api | `api/{business}`, `dto`, `enums` | No | No | P2 | Mall context; migrate after pilot and mall-specific boundary decision. |
| develop-module-promotion-api | `api/{business}`, `dto`, `enums` | No | No | P2 | Mall context; migrate after pilot and mall-specific boundary decision. |
| develop-module-trade-api | `api/{business}`, `dto`, `enums` | No | No | P2 | Mall context; migrate after pilot and mall-specific boundary decision. |
| develop-module-statistics-api | `api`, `enums` | No | No | P2 | Mall context; first confirm whether cross-module contracts exist. |
| develop-module-ai-api | `api`, `enums` | No | No | P2 | Audit concrete consumers before adding adapters. |
| develop-module-crm-api | `api`, `enums` | No | No | P2 | Audit concrete consumers before adding adapters. |
| develop-module-erp-api | `api`, `enums` | No | No | P2 | Audit concrete consumers before adding adapters. |
| develop-module-iot-api | `api`, `enums` | No | No | P2 Special | Coordinate with `iot-core` and `iot-gateway` convergence. |
| develop-module-report-api | `api`, `enums` | No | No | P2 | Audit concrete consumers before adding adapters. |
| develop-module-mes-api | `enums` only | No | No | P3 | Decide whether it has real cross-module contracts or should remain enum-only under standard layout. |
| develop-module-mp-api | `enums` only | No | No | P3 | Decide whether it has real cross-module contracts or should remain enum-only under standard layout. |
| develop-module-wms-api | `enums` only | No | No | P3 | Decide whether it has real cross-module contracts or should remain enum-only under standard layout. |

## Findings

1. `develop-module-system-api` is the only clear current sample with `remote/*RemoteClient.java` packages.
2. No API module currently exposes `local` packages in the API module shape.
3. Most API modules already have `api/{business}/dto` shape but lack `remote` adapters.
4. `mes-api`, `mp-api`, and `wms-api` are enum-only modules and need an explicit decision before adding API contract scaffolding.
5. Mall contexts already behave like independent API modules under `develop-module-mall`, but should be handled after the first non-special pilot.

## Recommended Phase 2 Pilot

Use `develop-module-member-api` as the pilot design target.

Reasons:

- It has several API groups: address, config, level, user.
- It already has DTOs and enums, so the structure is representative.
- It is less special than `iot` and `mall`.
- It is less infrastructure-sensitive than `infra` and less high-risk than `pay`.

## Non-Goals

- Do not rename `*Api.java` to `*CommonApi.java` in Phase 2.
- Do not create remote Feign adapters for all modules in one pass.
- Do not move DTOs or enums without a module-specific migration plan.
- Do not change consumers, controllers, server implementations, or Maven dependencies in this audit phase.
```

- [ ] **Step 8: Verify audit report has no placeholder text**

Run:

```bash
! grep -nE 'TBD|TODO|待补充|适当|implement later|fill in details' docs/superpowers/reports/2026-05-24-api-contract-audit.md
```

Expected: command exits with code `0` and no output.

- [ ] **Step 9: Review audit report diff**

Run:

```bash
git diff -- docs/superpowers/reports/2026-05-24-api-contract-audit.md
```

Expected: if the report is tracked, diff shows only the new audit report. If the file is untracked, use `git status --short -- docs/superpowers/reports/2026-05-24-api-contract-audit.md` and confirm it appears as `??`.

---

### Task 2: Write member API pilot design

**Files:**
- Read: `docs/superpowers/reports/2026-05-24-api-contract-audit.md`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/**`
- Read: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/**/remote/*.java`
- Create: `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`

- [ ] **Step 1: Capture member API files**

Run:

```bash
find develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api -type f | sort
```

Expected output includes member API groups such as:

```text
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user
```

- [ ] **Step 2: Capture system remote adapter sample files**

Run:

```bash
find develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api -path '*/remote/*.java' -type f | sort
```

Expected output includes:

```text
develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/remote/AdminUserRemoteClient.java
```

- [ ] **Step 3: Write the pilot design**

Create `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md` with this content:

```markdown
# Member API Contract Pilot Design

## Goal

Design the first API contract standardization pilot for `develop-module-member-api` without changing Java source in this phase.

## Why Member

`develop-module-member-api` is the recommended pilot because it already contains multiple business API groups and DTOs, but it does not yet contain `remote` or `local` adapter packages. It is representative enough to validate the API standard while avoiding the special handling required by `iot`, `mall`, `infra`, and `pay`.

## Current Shape

Current module:

```text
develop-module-member/develop-module-member-api/
  src/main/java/com/develop/mvp/pk/module/member/api/
    address/
    config/
    level/
    user/
  src/main/java/com/develop/mvp/pk/module/member/enums/
```

Current standard target from `.claude/ddd-skills/Module_Structure_Standard.md`:

```text
api/
  {business}/
    XxxApi.java or XxxCommonApi.java
    dto/
    enums/
    local/
    remote/
```

## Naming Decision for Pilot

Do not rename existing `*Api.java` interfaces to `*CommonApi.java` in the pilot.

Reason: the current repository sample, `develop-module-system-api`, uses names like `AdminUserApi.java`, `DeptApi.java`, and `MailSendApi.java` plus `remote/*RemoteClient.java`. Renaming contract interfaces would create a broader compatibility migration and should not be mixed into the first package-shape pilot.

## Proposed Pilot Shape

For each member API group that has a cross-module contract, converge toward:

```text
api/{business}/
  XxxApi.java
  dto/
  remote/
  local/
```

Pilot Java migration should be planned in a later implementation plan. This design phase only decides the shape and acceptance criteria.

## Adapter Rules

- `XxxApi.java` remains the stable contract interface.
- `remote/XxxRemoteClient.java` is the remote Feign/RPC adapter and must implement or delegate to the same contract semantics.
- `local/` is reserved for local in-process adapter or local mode bridge when the project confirms the exact local implementation pattern.
- DTO classes remain under the existing `dto/` packages unless a module-specific compatibility review approves movement.
- Enums remain under `com.develop.mvp.pk.module.member.enums` unless a later compatibility review proves they should move under `api/{business}/enums`.

## Required Pre-Migration Checks

Before changing member Java source, read and record:

- Existing member API interfaces under `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/**`.
- Existing member server implementations under `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/**`.
- Current consumers of member API interfaces using `grep -R "module.member.api" develop-module-* develop-server develop-gateway`.
- Current system remote adapter annotations and method signatures under `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/**/remote/*.java`.

## Acceptance Criteria for a Future Java Pilot

A later Java pilot is acceptable only if:

- It changes one member API group at a time.
- It preserves existing interface method signatures unless a separate compatibility migration is approved.
- It adds `remote` and, if a concrete local pattern is confirmed, `local` packages without duplicating business logic.
- It keeps DTO fields, enum values, validation annotations, and return types compatible.
- It compiles with `mvn compile -pl develop-module-member/develop-module-member-api -am`.
- If server implementations are touched, it also compiles with `mvn compile -pl develop-module-member/develop-module-member-server -am`.

## Non-Goals

- Do not rename member `*Api.java` interfaces in the first pilot.
- Do not migrate all member API groups in one task.
- Do not add remote adapters before confirming current Feign/RPC annotations and server-side provider expectations.
- Do not change member server business logic as part of API package shape work.
- Do not move enums or DTOs only for cosmetic package symmetry.
```

- [ ] **Step 4: Verify pilot design has no placeholder text**

Run:

```bash
! grep -nE 'TBD|TODO|待补充|适当|implement later|fill in details' docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md
```

Expected: command exits with code `0` and no output.

- [ ] **Step 5: Review pilot design diff**

Run:

```bash
git diff -- docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md
```

Expected: if the design is tracked, diff shows only the new pilot design. If the file is untracked, use `git status --short -- docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md` and confirm it appears as `??`.

---

### Task 3: Validate Phase 2 stayed audit/design-only

**Files:**
- Check: `docs/superpowers/reports/2026-05-24-api-contract-audit.md`
- Check: `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`

- [ ] **Step 1: Confirm target documentation files exist**

Run:

```bash
test -f docs/superpowers/reports/2026-05-24-api-contract-audit.md && test -f docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md
```

Expected: command exits with code `0`.

- [ ] **Step 2: Confirm no Java file was changed by Phase 2**

Run:

```bash
git status --short -- docs/superpowers/reports/2026-05-24-api-contract-audit.md docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md && git diff --name-only -- develop-module-*/develop-module-*-api/**/*.java develop-module-mall/develop-module-*-api/**/*.java
```

Expected: the first command shows only the two documentation files as new or modified. The Java diff command may show pre-existing API Java changes if the working tree already had them before this phase; if it does, report them as pre-existing and do not modify them.

- [ ] **Step 3: Confirm the audit and pilot design reference the standard**

Run:

```bash
grep -n "Module_Structure_Standard.md" docs/superpowers/reports/2026-05-24-api-contract-audit.md docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md
```

Expected: both files include `.claude/ddd-skills/Module_Structure_Standard.md`.

- [ ] **Step 4: Confirm the pilot is member-only**

Run:

```bash
grep -n "develop-module-member-api" docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md && ! grep -nE "develop-module-pay-api|develop-module-infra-api|develop-module-iot-api|develop-module-product-api" docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md
```

Expected: command exits with code `0`; the design focuses on member and does not accidentally scope in pay, infra, iot, or mall product as implementation targets.

- [ ] **Step 5: Review full Phase 2 documentation diff**

Run:

```bash
git diff -- docs/superpowers/reports/2026-05-24-api-contract-audit.md docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md
```

Expected: if files are tracked, diff shows only the two documentation files. If untracked, use `git status --short -- docs/superpowers/reports/2026-05-24-api-contract-audit.md docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`.

---

### Task 4: Commit Phase 2 audit/design files if requested by the user

**Files:**
- Stage if requested: `docs/superpowers/reports/2026-05-24-api-contract-audit.md`
- Stage if requested: `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`
- Stage if requested: `docs/superpowers/plans/2026-05-24-api-contract-standard-phase2.md`

- [ ] **Step 1: Ask for commit confirmation**

Say:

```text
Phase 2 audit/design files are ready. Do you want me to commit only these files?
```

Expected: user explicitly says whether to commit. Do not commit without explicit confirmation.

- [ ] **Step 2: If the user says no, stop before staging**

Expected: no git staging or commit commands are run.

- [ ] **Step 3: If the user says yes, inspect status and diff**

Run:

```bash
git status --short -- docs/superpowers/reports/2026-05-24-api-contract-audit.md docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md docs/superpowers/plans/2026-05-24-api-contract-standard-phase2.md
```

Expected: only the intended Phase 2 documentation files are selected for staging.

- [ ] **Step 4: Stage only intended files**

Run:

```bash
git add docs/superpowers/reports/2026-05-24-api-contract-audit.md docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md docs/superpowers/plans/2026-05-24-api-contract-standard-phase2.md
```

Expected: command exits with code `0`.

- [ ] **Step 5: Commit with project-style message**

Run:

```bash
git commit -m "$(cat <<'EOF'
DDD重构：规划 API 契约统一试点

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```

Expected: commit succeeds. If hooks fail, fix the underlying issue and create a new commit attempt; do not use `--no-verify`.

- [ ] **Step 6: Verify target status**

Run:

```bash
git status --short -- docs/superpowers/reports/2026-05-24-api-contract-audit.md docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md docs/superpowers/plans/2026-05-24-api-contract-standard-phase2.md
```

Expected: no output for the three committed files. Pre-existing unrelated worktree changes may remain.

---

## Self-Review

- Spec coverage: This plan implements the approved migration order by starting with API contract audit and a single pilot design before Java migration. It references the committed module structure standard, uses `system-api` as current sample, and recommends `member-api` as the pilot without touching Java source.
- Placeholder scan: The plan avoids `TBD`, `TODO`, `implement later`, `fill in details`, `适当`, and `待补充` in task instructions and target document content.
- Scope check: Phase 2 intentionally creates audit/design artifacts only. It does not batch-modify API modules, create adapters, rename contracts, move DTOs/enums, or alter Maven dependencies.
