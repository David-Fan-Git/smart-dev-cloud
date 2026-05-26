# API Contract Pilot Design: Member Module

## Goal

Design the first API contract unification pilot for `develop-module-member-api` without changing Java source in this phase.

The pilot design standardizes the boundary between stable contract interfaces and future transport adapters while preserving the current member API behavior and package compatibility. This document is a design artifact only: it does not create `remote` or `local` Java classes, does not rename API interfaces, and does not move DTOs or enums.

## Why Member

`develop-module-member-api` is the recommended first pilot because it is concrete enough to validate the target shape but limited enough to keep migration risk controlled.

Evidence from the current workspace:

- It has five business API groups with contract interfaces:
  - `address/MemberAddressApi.java`
  - `config/MemberConfigApi.java`
  - `level/MemberLevelApi.java`
  - `point/MemberPointApi.java`
  - `user/MemberUserApi.java`
- It has DTO packages under existing business API groups, for example `user/dto/MemberUserRespDTO.java`.
- It has message contracts under `api/message/user/MemberUserCreateMessage.java`.
- It currently has no `remote` adapter package and no `local` adapter package, so the pilot can validate adapter shape without untangling existing adapter implementations.
- It is less special than mall sub-context modules and less infrastructure-sensitive than `infra` or payment-sensitive than `pay`.
- Task 1 audit already marked member as the P0 pilot candidate in `docs/superpowers/reports/2026-05-24-api-contract-audit.md`.

## Current Shape

Current member API module path:

```text
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/
```

Current member API package shape:

```text
api/
  address/
    MemberAddressApi.java
    dto/MemberAddressRespDTO.java
  config/
    MemberConfigApi.java
    dto/MemberConfigRespDTO.java
  level/
    MemberLevelApi.java
    dto/MemberLevelRespDTO.java
  message/
    package-info.java
    user/MemberUserCreateMessage.java
  point/
    MemberPointApi.java
  user/
    MemberUserApi.java
    dto/MemberUserRespDTO.java
  package-info.java
enums/
  ApiConstants.java
  ...
```

Current contract characteristics:

- The public cross-module contract interfaces are named `XxxApi.java`.
- The member `XxxApi.java` interfaces currently carry `@FeignClient(name = ApiConstants.NAME)` directly on the contract interface.
- HTTP mapping annotations such as `@GetMapping`, `@PostMapping`, and `@RequestParam` are also on the contract methods.
- Return values use `CommonResult<...>` and DTOs under each business API package.
- `MemberUserApi` contains a default helper method `getUserMap(Collection<Long> ids)` that derives a map from `getUserList(ids)`.
- There are no member `remote/*RemoteClient.java` files today.
- There are no member `local/*` adapter files today.

Current system remote sample shape:

```text
api/{business}/
  XxxApi.java
  remote/XxxRemoteClient.java
```

The system sample uses `remote/*RemoteClient.java` interfaces that extend a stable `*Api.java` contract, for example:

```text
AdminUserRemoteClient extends AdminUserApi
DictDataRemoteClient extends DictDataApi
PermissionRemoteClient extends PermissionApi
```

Those remote clients carry `@FeignClient(name = ApiConstants.NAME, contextId = "system...RemoteClient")` and contain no duplicated contract methods.

## Naming Decision for Pilot

The first pilot must not rename member `*Api.java` interfaces to `*CommonApi.java`.

Decision:

- Keep `MemberAddressApi.java`, `MemberConfigApi.java`, `MemberLevelApi.java`, `MemberPointApi.java`, and `MemberUserApi.java` as the stable contract interface names.
- Treat `XxxApi.java` as the current repository contract naming standard for this pilot because `develop-module-system-api` already uses that shape with remote adapters.
- Do not introduce `XxxCommonApi.java` during the first member pilot.
- Revisit the `*CommonApi.java` naming only after at least one module-level pilot proves the adapter split, consumer compatibility, and Maven validation path.

Rationale:

- No scanned API module currently contains `*CommonApi.java` files.
- The current working sample is system API with `*Api.java` plus `remote/*RemoteClient.java`.
- Renaming contract interfaces during the first pilot would mix adapter extraction with a broad source-compatibility migration.

## Proposed Pilot Shape

The member module final target shape can eventually cover all five confirmed business API groups: `address`, `config`, `level`, `point`, and `user`. That final module shape is not the scope of the first Java pilot.

Final target shape for member, if approved after incremental pilots:

```text
api/
  address/
    MemberAddressApi.java
    dto/MemberAddressRespDTO.java
    remote/MemberAddressRemoteClient.java
  config/
    MemberConfigApi.java
    dto/MemberConfigRespDTO.java
    remote/MemberConfigRemoteClient.java
  level/
    MemberLevelApi.java
    dto/MemberLevelRespDTO.java
    remote/MemberLevelRemoteClient.java
  point/
    MemberPointApi.java
    remote/MemberPointRemoteClient.java
  user/
    MemberUserApi.java
    dto/MemberUserRespDTO.java
    remote/MemberUserRemoteClient.java
```

First Java pilot minimum slice:

- Select exactly one member API group for the first Java pilot.
- Recommended first slice: `config`, because the current shape confirms `config/MemberConfigApi.java` and `config/dto/MemberConfigRespDTO.java`, and this group has fewer documented coupling signals than `user`, which also has a default helper method and message contract references in the current file.
- Do not expand the first Java pilot from one group to multiple member API groups unless the user explicitly approves the expanded scope.

Contract role:

- `XxxApi.java` remains the stable API contract consumed by callers.
- Contract methods, path constants, request mappings, return types, and default helper methods remain behavior-compatible unless a future migration plan explicitly proves a safe change.
- DTO and message classes remain in their current packages unless a separate compatibility plan proves that moving them is necessary.

Remote adapter role:

- Future `remote/XxxRemoteClient.java` is the remote transport adapter.
- A remote client extends the corresponding `XxxApi.java` contract.
- A remote client owns the Feign client identity, including a member-specific `contextId` such as `memberUserRemoteClient`.
- A remote client should not duplicate contract method signatures already present in `XxxApi.java`.

Local adapter role:

- `local/` is not part of the first Java pilot unless the local-call mode is confirmed.
- `local/` should land only after the project decides how local mode binds API contracts to server-side implementations in monolithic boot mode.
- Creating empty or speculative `local/` packages is outside the pilot.

DTO and enum placement:

- Do not move DTOs or enums only to make directories symmetric.
- Existing DTO packages under business API groups are acceptable for the pilot.
- Existing root-level member enums remain where they are unless a module-specific migration proves a consumer-safe reason to move them.

## Adapter Rules

Future Java pilot adapter rules:

1. Keep `XxxApi.java` stable as the cross-module contract name.
2. Move Feign responsibility out of the contract only as part of an approved Java pilot that updates all affected consumers.
3. Add `remote/XxxRemoteClient.java` beside the contract interface for remote mode.
4. Make each remote client extend exactly one corresponding contract interface.
5. Give each remote client a unique `contextId` to avoid Feign bean collisions.
6. Do not duplicate API method declarations in remote clients.
7. Do not create `local/` until local mode is confirmed by a concrete binding design.
8. Do not move DTOs, messages, or enums for directory symmetry alone.
9. Preserve `CommonResult` response shape unless a separate contract compatibility decision approves a change.
10. Preserve existing request paths and method mappings unless a separate API versioning or compatibility plan approves a change.

## Required Pre-Migration Checks

Before any future Java pilot touches member API source, complete these checks:

1. List all direct consumers of the five member API interfaces across backend modules.
2. Identify whether consumers inject `MemberUserApi`, `MemberAddressApi`, `MemberConfigApi`, `MemberLevelApi`, or `MemberPointApi` directly.
3. Check whether existing Feign scanning depends on `@FeignClient` being present on `XxxApi.java`.
4. Decide whether consumers should inject `XxxApi` or `remote/XxxRemoteClient` after the pilot.
5. Confirm the local/remote strategy for monolithic boot mode versus remote service mode.
6. Confirm whether member server provides controller or service implementations that must be adjusted for local mode.
7. Check whether `MemberUserApi#getUserMap` default behavior stays correct when the Feign annotation moves to a remote adapter.
8. Verify that DTO package names are not hard-coded in serialization, OpenAPI, MapStruct, or external documentation.
9. Check for Maven module dependencies that would be affected by adding Feign-specific adapter classes.
10. Prepare rollback by keeping the contract interface names and package names stable.

## Acceptance Criteria for a Future Java Pilot

A future Java pilot for `develop-module-member-api` is acceptable only if all criteria below pass:

- `XxxApi.java` names remain stable and are not renamed to `XxxCommonApi.java` in the first pilot.
- Each Java pilot migrates only one member API group at a time unless the user approves an expanded scope.
- `remote/XxxRemoteClient.java` exists for each migrated member API contract selected by the pilot.
- Each remote client extends its corresponding `XxxApi.java` contract.
- Feign `contextId` values are unique within the member API module.
- No DTO, message, or enum is moved solely for directory symmetry.
- Existing API paths, method mappings, return wrappers, and DTO package names remain compatible unless an explicit compatibility note is approved.
- `local/` is absent unless local mode has a confirmed implementation design.
- Member API module compilation passes with:

```bash
mvn compile -pl develop-module-member/develop-module-member-api -am
```

- If the future pilot touches member server code, member server compilation also passes with:

```bash
mvn compile -pl develop-module-member/develop-module-member-server -am
```

- Any consumer injection changes are reviewed and validated before merging the Java pilot.

## Non-Goals

- Do not modify Java files in this design phase.
- Do not create `remote` or `local` Java classes in this design phase.
- Do not submit a git commit for this task.
- Do not batch-migrate all member API groups in the first Java pilot.
- Do not rename `*Api.java` to `*CommonApi.java` in the first pilot.
- Do not move DTOs or enums only for directory symmetry.
- Do not migrate all API modules in one pass.
- Do not use the member pilot to solve mall, iot, infra, or pay-specific API boundaries.
- Do not change REST paths, request parameters, response DTOs, or `CommonResult` response wrappers as part of adapter-shape work.
