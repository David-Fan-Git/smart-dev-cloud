# Module API Local/Remote Adapter Pilot Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Pilot the agreed one-contract/two-adapter module API pattern on `DictDataApi`.

**Architecture:** Keep `DictDataApi` as the single cross-module contract and remove Feign binding from it. Add `DictDataRemoteClient` under the same API module's `remote` subpackage as the remote Feign adapter. Keep `DictDataApiImpl` in the server module as the local adapter; consumers keep depending on `DictDataApi`.

**Tech Stack:** Java 17, Spring Boot 3.5.x, Spring Cloud OpenFeign, Maven multi-module.

---

## Files

- Modify: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dict/DictDataApi.java`
- Create: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dict/remote/DictDataRemoteClient.java`
- Modify: `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/rpc/config/RpcConfiguration.java`

## Task 1: Split the DictData API contract from Feign

- [ ] Remove `@FeignClient` and its import from `DictDataApi.java`.
- [ ] Keep `DictDataApi` package, constants, request mappings, Swagger annotations, and inherited `DictDataCommonApi` contract unchanged.
- [ ] Create `DictDataRemoteClient.java` in `com.develop.mvp.pk.module.system.api.dict.remote`.
- [ ] Make `DictDataRemoteClient extends DictDataApi` and annotate it with `@FeignClient(name = ApiConstants.NAME, contextId = "systemDictDataRemoteClient")`.

## Task 2: Update remote Feign registration

- [ ] In BPM `RpcConfiguration`, replace the import of `DictDataApi` with `DictDataRemoteClient`.
- [ ] In `@EnableFeignClients`, replace `DictDataApi.class` with `DictDataRemoteClient.class`.
- [ ] Do not change business consumers; they should still inject `DictDataApi`.

## Task 3: Verify pilot compile

- [ ] Compile the affected API module:

```bash
mvn compile -pl develop-module-system/develop-module-system-api -am
```

- [ ] Compile the affected remote consumer module:

```bash
mvn compile -pl develop-module-bpm/develop-module-bpm-server -am
```

- [ ] Inspect `git diff` to confirm only the pilot files and this plan changed.
