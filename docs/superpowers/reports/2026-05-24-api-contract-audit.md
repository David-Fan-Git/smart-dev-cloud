# API Contract Audit Report

## Scope

This report audits the current API contract structure of all backend API Maven modules in `/Users/david/Desktop/ny/smart-cloud/smart-develop` against `.claude/ddd-skills/Module_Structure_Standard.md`.

The audit covers:

- All `develop-module-*/develop-module-*-api` Maven modules.
- Mall sub-context API modules under `develop-module-mall/develop-module-*-api`.
- API-related package directories named `api`, `dto`, `enums`, `local`, and `remote`.
- Contract interface files matching `*Api.java` or `*CommonApi.java`.
- Remote adapter files matching `*Remote*.java`.

This phase is read-only with respect to Java source. It does not rename interfaces, move DTOs, create adapters, update consumers, or change Maven dependencies.

## Standard Checked

The checked standard file exists at `.claude/ddd-skills/Module_Structure_Standard.md` and contains `## 7. API Module Standard`.

The standard target for API modules is:

```text
api/
  {business}/
    XxxCommonApi.java
    dto/
    enums/
    local/
    remote/
```

The current repository sample, `develop-module-system-api`, uses `*Api.java` contract names with `remote/*RemoteClient.java` adapters. Because of that current shape, Phase 2 does not batch-rename `*Api.java` to `*CommonApi.java`. This audit treats existing `*Api.java` interfaces as the current contract interface shape and focuses recommendations on package boundaries, adapter presence, and module-by-module migration priority.

Evidence summary from the current workspace:

- API Maven modules found: 17.
- API-related directories captured: 103, including `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/remote`.
- Contract interface scan captured 53 lines with the plan command; mall modules appear twice in that command because the generic and mall-specific find roots overlap.
- `*CommonApi.java` files found in API modules: 0.
- `*Remote*.java` files found in API modules: 14, all under `develop-module-system-api`.
- API module `local` directories found: 0.

## API Modules

| Module | Path | Current Shape | API Interfaces | Remote Adapter | Local Adapter | Priority | Recommendation |
|---|---|---|---:|---:|---:|---|---|
| system | `develop-module-system/develop-module-system-api` | `api/{business}`, `dto`, root `enums`, `remote` | 14 | Yes | No | Sample | Use as the current remote-adapter reference; keep `*Api.java` naming during Phase 2. |
| member | `develop-module-member/develop-module-member-api` | `api/{business}`, `dto`, root `enums` | 5 | No | No | P0 Pilot | Use as pilot because it has several API groups and DTOs but no remote adapter packages. |
| pay | `develop-module-pay/develop-module-pay-api` | `api/{business}`, `dto`, root `enums` | 4 | No | No | P1 | Plan after member; payment contracts need compatibility review before adapter work. |
| infra | `develop-module-infra/develop-module-infra-api` | `api/{business}`, `dto`, root `enums` | 3 | No | No | P1 | Plan after member; infra contracts are broadly consumed and should be migrated cautiously. |
| bpm | `develop-module-bpm/develop-module-bpm-api` | `api/task`, `dto`, root `enums` | 1 | No | No | P1 | Plan after member; workflow contracts require consumer and compatibility checks. |
| product | `develop-module-mall/develop-module-product-api` | `api/{business}`, `dto`, root `enums` | 4 | No | No | P2 | Mall sub-context; migrate after the non-special pilot and mall boundary review. |
| promotion | `develop-module-mall/develop-module-promotion-api` | `api/{business}`, `dto`, root `enums` | 8 | No | No | P2 | Mall sub-context; migrate after the non-special pilot and mall boundary review. |
| trade | `develop-module-mall/develop-module-trade-api` | `api/order`, `dto`, root `enums` | 1 | No | No | P2 | Mall sub-context; migrate after the non-special pilot and mall boundary review. |
| statistics | `develop-module-mall/develop-module-statistics-api` | `api`, root `enums` | 0 | No | No | P2 | Confirm whether it has stable cross-module contracts before adding adapter shape. |
| ai | `develop-module-ai/develop-module-ai-api` | `api`, root `enums` | 0 | No | No | P2 | Audit concrete consumers before adding contract adapters. |
| crm | `develop-module-crm/develop-module-crm-api` | `api`, root `enums` | 0 | No | No | P2 | Audit concrete consumers before adding contract adapters. |
| erp | `develop-module-erp/develop-module-erp-api` | `api`, root `enums` | 0 | No | No | P2 | Audit concrete consumers before adding contract adapters. |
| iot | `develop-module-iot/develop-module-iot-api` | `api`, root `enums` | 0 | No | No | P2 Special | Coordinate with `iot-core` and `iot-gateway` convergence before API adapter changes. |
| report | `develop-module-report/develop-module-report-api` | `api`, root `enums` | 0 | No | No | P2 | Audit concrete consumers before adding contract adapters. |
| mes | `develop-module-mes/develop-module-mes-api` | root `enums` only | 0 | No | No | P3 | Decide whether real cross-module contracts exist or keep it enum-only under a documented standard shape. |
| mp | `develop-module-mp/develop-module-mp-api` | root `enums` only | 0 | No | No | P3 | Decide whether real cross-module contracts exist or keep it enum-only under a documented standard shape. |
| wms | `develop-module-wms/develop-module-wms-api` | root `enums` only | 0 | No | No | P3 | Decide whether real cross-module contracts exist or keep it enum-only under a documented standard shape. |

## Findings

1. `develop-module-system-api` is the only current API module with `remote/*RemoteClient.java` files. It contains 14 contract interfaces named `*Api.java` and 14 remote client files.
2. No API module currently contains `local` package directories in the API module shape.
3. No scanned API module currently contains `*CommonApi.java` files. Current contract interfaces use `*Api.java` naming.
4. The current `system-api` sample uses `*Api.java` plus `remote/*RemoteClient.java`; therefore Phase 2 should not bulk-rename `*Api.java` to `*CommonApi.java`.
5. Member, pay, infra, bpm, product, promotion, and trade already contain concrete `*Api.java` interfaces and are structurally closer to the target contract model than enum-only modules.
6. Member has multiple API groups and DTO directories but no remote or local adapters, making it a practical first pilot.
7. Mall contexts already behave as independent API Maven modules under `develop-module-mall`, but product, promotion, trade, and statistics should be handled after the first non-special pilot because mall has explicit special convergence guidance in the standard.
8. AI, CRM, ERP, IOT, report, statistics, MES, MP, and WMS have no scanned `*Api.java` or `*CommonApi.java` files in their API modules. They need consumer and contract-intent review before adapter scaffolding.
9. MES, MP, and WMS are enum-only API modules in the scanned structure and need an explicit standard-shape decision before any API contract migration.

## Recommended Phase 2 Pilot

Use `develop-module-member-api` as the recommended Phase 2 pilot.

Reasons:

- It has several business API groups: `address`, `config`, `level`, `point`, and `user`.
- It already has DTO directories under most API groups and root-level contract enums.
- It lacks `remote` and `local` adapter packages, so it can validate adapter-shape decisions without broad Java movement.
- It is less special than `iot` and mall sub-contexts.
- It is less infrastructure-sensitive than `infra` and less payment-risk-sensitive than `pay`.

Pilot constraints:

- Keep existing member `*Api.java` interface names for the first pilot design.
- Add or move no Java files in this audit task.
- Validate current consumers before any future Java migration.
- Treat `system-api` remote clients as the reference for remote adapter shape, not as approval for a repository-wide batch conversion.

## Non-Goals

- Do not rename `*Api.java` to `*CommonApi.java` in Phase 2.
- Do not create remote Feign adapters for all modules in one pass.
- Do not create `local` adapters without confirming the local-call implementation pattern.
- Do not move DTOs or enums without a module-specific migration plan.
- Do not change consumers, controllers, server implementations, Maven dependencies, or Java source in this audit phase.
- Do not treat `iot` or mall as permanent exceptions; handle them through their standard convergence paths after the pilot.
