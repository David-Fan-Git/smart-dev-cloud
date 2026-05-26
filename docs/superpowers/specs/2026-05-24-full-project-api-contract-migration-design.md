# Full Project API Contract Migration Design

## Goal

Define the full-project API contract migration path so every backend module can gradually conform to the repository module structure standard without unsafe batch rewrites.

The migration prioritizes API contract boundaries first: stable `XxxApi` contracts remain the injection type for business consumers, while remote transport responsibility moves into `remote/XxxRemoteClient` adapters. DDD server-layer migration and special runtime-unit convergence remain follow-up tracks after API boundaries are stable.

## Scope

This design covers the first full-project standardization track:

- API contract package shape across `develop-module-*-api` modules.
- Remote Feign adapter extraction from current contract interfaces.
- Consumer scan configuration updates where `@EnableFeignClients` currently registers stable contracts directly.
- Incremental validation and commit boundaries.

This design does not directly refactor server business logic into DDD layers, move DTOs/enums, create local adapters, or converge `iot`/`mall` runtime-unit structure. Those tracks are sequenced after API contract boundaries are stable or handled by separate specs.

## Existing Standard Sources

Implementation must follow these current repository sources:

- `.claude/ddd-skills/Module_Structure_Standard.md`
- `docs/superpowers/reports/2026-05-24-api-contract-audit.md`
- `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`
- `docs/superpowers/reports/2026-05-24-member-config-api-remote-pilot-report.md`
- `docs/superpowers/reports/2026-05-24-member-address-api-remote-pilot-report.md`

The current proven Java pilot shape is:

```text
api/{business}/
  XxxApi.java
  remote/XxxRemoteClient.java
```

The pilot deliberately keeps `XxxApi.java` as the stable contract name. It does not rename to `XxxCommonApi.java` during this migration track.

## Migration Principles

1. Migrate one module and one API group at a time.
2. Keep `XxxApi` as the business-facing stable contract injection type.
3. Move Feign identity into `remote/XxxRemoteClient` only.
4. Make each remote client extend exactly one corresponding `XxxApi` contract.
5. Do not duplicate contract method declarations in remote clients.
6. Preserve existing REST paths, HTTP methods, request parameters, Swagger annotations, return wrappers, DTO packages, and default helper methods.
7. Do not move DTOs, messages, or enums only for directory symmetry.
8. Do not create `local/` packages until the local binding strategy is designed and approved.
9. Update only remote scan configuration to reference `XxxRemoteClient`; business services continue injecting `XxxApi`.
10. Commit each API group separately so changes stay reviewable and reversible.

## Recommended Migration Queue

### Queue 1: Finish `develop-module-member-api`

The member module is already the pilot module. Completed slices:

- `config`: `MemberConfigRemoteClient`
- `address`: `MemberAddressRemoteClient`

Remaining member slices should be planned and implemented separately:

1. `level` → `remote/MemberLevelRemoteClient.java`
2. `point` → `remote/MemberPointRemoteClient.java`
3. `user` → `remote/MemberUserRemoteClient.java`

`user` is intentionally last within member because it has the broadest consumer surface and includes default helper behavior.

### Queue 2: Migrate other modules with real API contracts

After member is complete, use the API contract audit report to select modules with real `api/{business}/XxxApi.java` contracts and existing Feign consumers. Each module must get its own plan before Java changes.

Candidate order should favor lower-risk modules first:

1. Modules with few API groups and few consumers.
2. Modules whose contracts do not contain default helper methods or message coupling.
3. Modules whose consumer scan configuration is easy to compile independently.
4. Payment-sensitive or infrastructure-sensitive modules only after the pattern has passed several lower-risk modules.

### Queue 3: Decide enum-only API modules

Modules such as `mes-api`, `mp-api`, or `wms-api` that are currently enum-only or do not expose stable API contracts should not receive speculative remote adapters. They need a separate decision:

- keep enum-only API module as-is,
- introduce real contracts only when cross-module calls exist,
- or document that no remote adapter is required yet.

## Per-API-Group Implementation Template

For each API group, a plan must perform these steps:

1. Read the stable contract and server implementation.
2. List all consumers of the contract across backend modules.
3. Identify Feign scan registrations that currently reference `XxxApi.class`.
4. Remove `@FeignClient` and its import from `XxxApi.java`.
5. Create `remote/XxxRemoteClient.java` with:

```java
@FeignClient(name = ApiConstants.NAME, contextId = "moduleBusinessRemoteClient")
public interface XxxRemoteClient extends XxxApi {
}
```

6. Update Feign scan registrations to use `XxxRemoteClient.class`.
7. Verify business services still inject `XxxApi`, not the remote client.
8. Verify no DTOs, enums, messages, method mappings, or response wrappers changed.
9. Verify no `local/` package was created.
10. Compile the API module and every touched consumer module.
11. Write a short pilot report with changed files, decisions, validation results, and follow-up.
12. Commit only the intended files for that API group.

## Validation Strategy

Minimum validation per API group:

```bash
mvn compile -pl develop-module-{module}/develop-module-{module}-api -am
```

If any consumer scan or server module changes, also run the narrowest affected server compile, for example:

```bash
mvn compile -pl develop-module-mall/develop-module-trade-server -am
```

If a module has multiple consumers, run compile for each touched consumer runtime unit. If unrelated pre-existing workspace changes affect compilation, record the first failing module and error summary, then decide whether to isolate the target validation or stop and clean the workspace first.

## Commit and Workspace Rules

The current workspace contains many pre-existing modified files. Migration work must not stage broad paths such as `git add .` or `git add -A`.

For every API group commit:

- stage only the contract, new remote client, touched scan configuration, plan, and report files;
- exclude pre-existing DTO/example/documentation diffs unless the current plan explicitly owns them;
- inspect `git diff --cached --name-only` before committing;
- use a project-style commit message such as `DDD重构：试点拆分 member level 远程适配器`.

## Acceptance Criteria

A full-project API contract migration slice is acceptable only if:

- exactly one API group is migrated unless the user explicitly approves a broader scope;
- `XxxApi.java` remains the stable contract name;
- `remote/XxxRemoteClient.java` exists and extends the corresponding contract;
- the remote client has a unique `contextId`;
- remote clients do not duplicate contract methods;
- business consumers keep injecting `XxxApi`;
- Feign scan configuration references the remote client where remote mode is needed;
- no DTO, enum, message, path, request parameter, return wrapper, or default helper method changes accidentally;
- no speculative `local/` package is created;
- required Maven compile commands pass or any unrelated failure is explicitly recorded;
- the commit contains only the intended API group files.

## Non-Goals

- Do not batch-convert all modules in one commit.
- Do not rename `XxxApi` to `XxxCommonApi` in this migration track.
- Do not move DTOs, enums, or messages for symmetry.
- Do not implement local adapters until local binding is designed.
- Do not refactor server DDD layers as part of API remote adapter extraction.
- Do not converge `iot-core`, `iot-gateway`, `mall-server`, or mall sub-context runtime units in this API-contract track.
- Do not clean up unrelated workspace diffs as part of API contract migration commits.

## Next Plan

The next implementation plan should target exactly one remaining member API group. Recommended next slice:

```text
member level → MemberLevelRemoteClient
```

Rationale: `level` has a smaller scope than `user` and can validate the same pattern after `config` and `address` before the broad user API group is touched.
