# Member Level API Remote Pilot Phase 5 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Continue the full-project API contract migration by splitting only the `develop-module-member-api` level API group's Feign transport into `remote/MemberLevelRemoteClient` while preserving `MemberLevelApi` as the stable contract.

**Architecture:** This phase follows the proven member `config` and `address` pilot shape. `MemberLevelApi` keeps its path constants, Spring MVC mappings, Swagger annotations, request parameters, response wrappers, and DTO package; the new `MemberLevelRemoteClient` owns only Feign identity and extends `MemberLevelApi`. Product and trade Feign scanning switch the level entry to the remote adapter, while trade business services keep injecting `MemberLevelApi`.

**Tech Stack:** Java 17, Spring Cloud OpenFeign, Spring Web annotations, Maven multi-module build, project `CommonResult` wrapper, member API contract migration standard.

---

## File Structure

- Modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
  - Responsibility: stable level API contract.
  - Change: remove `@FeignClient` and Feign import; keep `@Tag`, `PREFIX`, `@GetMapping`, `@PostMapping`, `@RequestParam`, `@Parameter`, `@Parameters`, `CommonResult<MemberLevelRespDTO> getMemberLevel(...)`, `CommonResult<Boolean> addExperience(...)`, and `CommonResult<Boolean> reduceExperience(...)` unchanged.

- Create: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java`
  - Responsibility: remote Feign adapter for the level API contract.
  - Change: add `@FeignClient(name = ApiConstants.NAME, contextId = "memberLevelRemoteClient")` and extend `MemberLevelApi`; no duplicated methods.

- Modify: `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`
  - Responsibility: product module Feign client scanning configuration.
  - Change: replace `MemberLevelApi.class` with `MemberLevelRemoteClient.class`, and import the new remote client. Leave `MemberUserApi` unchanged.

- Modify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`
  - Responsibility: trade module Feign client scanning configuration.
  - Change: replace `MemberLevelApi.class` with `MemberLevelRemoteClient.class`, and import the new remote client. Leave `MemberUserApi`, `MemberPointApi`, `MemberAddressRemoteClient`, and `MemberConfigRemoteClient` unchanged.

- Read-only: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApiImpl.java`
  - Responsibility: server implementation of `MemberLevelApi`.
  - Change: no modification in this phase.

- Create: `docs/superpowers/reports/2026-05-24-member-level-api-remote-pilot-report.md`
  - Responsibility: record Phase 5 scope, decisions, validation, and follow-up.

---

### Task 1: Verify current member level API contract and consumers

**Files:**
- Read: `docs/superpowers/specs/2026-05-24-full-project-api-contract-migration-design.md`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
- Read: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApiImpl.java`
- Read: `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`
- Read: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

- [ ] **Step 1: Confirm the full-project design names `level` as the next member slice**

Run:

```bash
grep -n "member level → MemberLevelRemoteClient" docs/superpowers/specs/2026-05-24-full-project-api-contract-migration-design.md && grep -n "Migrate one module and one API group at a time" docs/superpowers/specs/2026-05-24-full-project-api-contract-migration-design.md
```

Expected output includes both the next-slice line and one-group migration principle.

- [ ] **Step 2: Confirm current level contract shape**

Run:

```bash
grep -nE "@FeignClient|interface MemberLevelApi|String PREFIX|@GetMapping|@PostMapping|CommonResult<MemberLevelRespDTO> getMemberLevel|CommonResult<Boolean> addExperience|CommonResult<Boolean> reduceExperience" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java
```

Expected output includes `@FeignClient(name = ApiConstants.NAME)`, `MemberLevelApi`, `PREFIX`, one `@GetMapping`, two `@PostMapping` mappings, and the three contract methods.

- [ ] **Step 3: Confirm server implementation still implements the stable contract**

Run:

```bash
grep -nE "class MemberLevelApiImpl implements MemberLevelApi|CommonResult<MemberLevelRespDTO> getMemberLevel|CommonResult<Boolean> addExperience|CommonResult<Boolean> reduceExperience" develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApiImpl.java
```

Expected output includes the implementation class and all three method implementations.

- [ ] **Step 4: Confirm all current level references**

Run:

```bash
grep -R "MemberLevelApi" -n develop-module-* develop-server develop-gateway --include='*.java'
```

Expected output includes:

```text
develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java
develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/order/handler/TradeMemberPointOrderHandler.java
develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/price/calculator/TradeDiscountActivityPriceCalculator.java
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java
develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApiImpl.java
```

- [ ] **Step 5: Confirm no level remote client already exists**

Run:

```bash
! test -e develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java
```

Expected: command exits with code `0` and no output.

---

### Task 2: Add member level remote adapter and keep contract stable

**Files:**
- Modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
- Create: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java`

- [ ] **Step 1: Remove Feign transport responsibility from `MemberLevelApi.java`**

Edit `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java` from:

```java
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ApiConstants.NAME) // TODO David：fallbackFactory =
@Tag(name = "RPC 服务 - 会员等级")
public interface MemberLevelApi {
```

to:

```java
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "RPC 服务 - 会员等级")
public interface MemberLevelApi {
```

Do not change `PREFIX`, mappings, parameters, return types, or method names.

- [ ] **Step 2: Create the level remote package**

Run:

```bash
mkdir -p develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote
```

Expected: command exits with code `0`.

- [ ] **Step 3: Create `MemberLevelRemoteClient.java`**

Create `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java` with this exact content:

```java
package com.develop.mvp.pk.module.member.api.level.remote;

import com.develop.mvp.pk.module.member.api.level.MemberLevelApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberLevelRemoteClient")
public interface MemberLevelRemoteClient extends MemberLevelApi {
}
```

- [ ] **Step 4: Verify level contract still owns methods and paths**

Run:

```bash
grep -nE "@Tag|String PREFIX|@GetMapping|@PostMapping|CommonResult<MemberLevelRespDTO> getMemberLevel|CommonResult<Boolean> addExperience|CommonResult<Boolean> reduceExperience|@FeignClient" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java
```

Expected output includes `@Tag`, `PREFIX`, mappings, and three methods. It must not include `@FeignClient`.

- [ ] **Step 5: Verify level remote adapter owns Feign identity only**

Run:

```bash
grep -nE "@FeignClient\(name = ApiConstants.NAME, contextId = \"memberLevelRemoteClient\"\)|interface MemberLevelRemoteClient extends MemberLevelApi|getMemberLevel|addExperience|reduceExperience" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java
```

Expected output includes only the `@FeignClient` line and `extends MemberLevelApi` line. It must not include `getMemberLevel`, `addExperience`, or `reduceExperience`.

- [ ] **Step 6: Check no level `local` package was created**

Run:

```bash
! test -d develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/local
```

Expected: command exits with code `0` and no output.

---

### Task 3: Update product and trade Feign scan for level only

**Files:**
- Modify: `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`
- Modify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

- [ ] **Step 1: Update product scan import**

Edit `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`.

Replace:

```java
import com.develop.mvp.pk.module.member.api.level.MemberLevelApi;
```

with:

```java
import com.develop.mvp.pk.module.member.api.level.remote.MemberLevelRemoteClient;
```

Leave `MemberUserApi` unchanged.

- [ ] **Step 2: Update product scan class**

In the same file, replace:

```java
@EnableFeignClients(clients = {MemberUserApi.class, MemberLevelApi.class})
```

with:

```java
@EnableFeignClients(clients = {MemberUserApi.class, MemberLevelRemoteClient.class})
```

- [ ] **Step 3: Update trade scan import**

Edit `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`.

Replace:

```java
import com.develop.mvp.pk.module.member.api.level.MemberLevelApi;
```

with:

```java
import com.develop.mvp.pk.module.member.api.level.remote.MemberLevelRemoteClient;
```

Leave `MemberUserApi`, `MemberPointApi`, `MemberAddressRemoteClient`, and `MemberConfigRemoteClient` unchanged.

- [ ] **Step 4: Update trade scan class**

In the same file, replace:

```java
MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class, MemberAddressRemoteClient.class, MemberConfigRemoteClient.class,
```

with:

```java
MemberUserApi.class, MemberPointApi.class, MemberLevelRemoteClient.class, MemberAddressRemoteClient.class, MemberConfigRemoteClient.class,
```

- [ ] **Step 5: Verify product and trade scan use level remote client**

Run:

```bash
grep -nE "MemberLevel(Api|RemoteClient)|MemberUserApi.class" develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java && grep -nE "MemberLevel(Api|RemoteClient)|MemberUserApi.class, MemberPointApi.class" develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
```

Expected output includes `MemberLevelRemoteClient` imports and scan entries. It must not include `import com.develop.mvp.pk.module.member.api.level.MemberLevelApi;`.

- [ ] **Step 6: Verify business consumers still depend on the stable contract**

Run:

```bash
grep -R "private MemberLevelApi\|import com.develop.mvp.pk.module.member.api.level.MemberLevelApi" -n develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service
```

Expected output still includes `TradeMemberPointOrderHandler.java` and `TradeDiscountActivityPriceCalculator.java`. These services continue depending on `MemberLevelApi`, not `MemberLevelRemoteClient`.

---

### Task 4: Compile and verify the level pilot scope

**Files:**
- Verify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
- Verify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java`
- Verify: `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`
- Verify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

- [ ] **Step 1: Compile member API module**

Run:

```bash
mvn compile -pl develop-module-member/develop-module-member-api -am
```

Expected: Maven exits with code `0`.

- [ ] **Step 2: Compile product server because it imports the new remote client**

Run:

```bash
mvn compile -pl develop-module-mall/develop-module-product-server -am
```

Expected: Maven exits with code `0`.

- [ ] **Step 3: Compile trade server because it imports the new remote client**

Run:

```bash
mvn compile -pl develop-module-mall/develop-module-trade-server -am
```

Expected: Maven exits with code `0`.

- [ ] **Step 4: Verify only config, address, and level remote clients exist for member API**

Run:

```bash
find develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api -path '*/remote/*.java' -type f | sort
```

Expected output is exactly:

```text
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java
```

- [ ] **Step 5: Verify no `local` package was created**

Run:

```bash
find develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api -path '*/local/*' -type f | sort
```

Expected: no output.

- [ ] **Step 6: Verify only intended Phase 5 Java files changed**

Run:

```bash
git diff --name-only -- develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
```

Expected output includes only:

```text
develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java
develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java
```

Because the new `MemberLevelRemoteClient.java` is untracked, also run:

```bash
git status --short -- develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java
```

Expected output:

```text
?? develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java
```

---

### Task 5: Write Phase 5 report and commit if requested

**Files:**
- Create: `docs/superpowers/reports/2026-05-24-member-level-api-remote-pilot-report.md`
- Stage if requested: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
- Stage if requested: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java`
- Stage if requested: `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`
- Stage if requested: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`
- Stage if requested: `docs/superpowers/plans/2026-05-24-member-level-api-remote-pilot-phase5.md`
- Stage if requested: `docs/superpowers/reports/2026-05-24-member-level-api-remote-pilot-report.md`

- [ ] **Step 1: Write the pilot report**

Create `docs/superpowers/reports/2026-05-24-member-level-api-remote-pilot-report.md` with this content after replacing the validation lines with actual command outcomes:

```markdown
# Member Level API Remote Pilot Report

## Scope

This pilot migrated only the `develop-module-member-api` level API group to the contract + remote adapter shape.

## Changed Files

- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java`
- `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

## Decisions

- `MemberLevelApi` remains the stable contract name.
- `MemberLevelRemoteClient` owns Feign remote transport identity with `contextId = "memberLevelRemoteClient"`.
- Product and trade Feign scans use `MemberLevelRemoteClient` for remote mode.
- Trade level consumers continue depending on `MemberLevelApi`.
- No `local` package was created because local mode binding is not confirmed yet.
- No DTOs or enums were moved.
- No `user` or `point` member API groups were migrated.

## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: record actual result here.
- `mvn compile -pl develop-module-mall/develop-module-product-server -am`: record actual result here.
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: record actual result here.

## Follow-Up

- Plan `member point` separately.
- Keep `member user` for last because it has the broadest consumer surface and default helper behavior.
- Define the concrete local adapter binding strategy before creating any `local` packages.
```

- [ ] **Step 2: Replace report validation placeholders with real outcomes**

If all Maven commands pass, the final validation section must be:

```markdown
## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: passed.
- `mvn compile -pl develop-module-mall/develop-module-product-server -am`: passed.
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: passed.
```

- [ ] **Step 3: Verify report has no unresolved placeholder wording**

Run:

```bash
! grep -nE 'record actual result here|TBD|TODO|待补充|适当|implement later|fill in details' docs/superpowers/reports/2026-05-24-member-level-api-remote-pilot-report.md
```

Expected: command exits with code `0` and no output.

- [ ] **Step 4: Ask for commit confirmation**

Say:

```text
Phase 5 member level API remote pilot is ready. Do you want me to commit only these files?
```

Do not stage or commit until the user explicitly confirms.

- [ ] **Step 5: If the user confirms, stage only intended files**

Run:

```bash
git add develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java docs/superpowers/plans/2026-05-24-member-level-api-remote-pilot-phase5.md docs/superpowers/reports/2026-05-24-member-level-api-remote-pilot-report.md
```

Expected: command exits with code `0`.

- [ ] **Step 6: Commit with project-style message**

Run:

```bash
git commit -m "$(cat <<'EOF'
DDD重构：试点拆分 member level 远程适配器

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```

Expected: commit succeeds. If hooks fail, fix the underlying issue and create a new commit attempt; do not use `--no-verify`.

---

## Self-Review

- Spec coverage: This plan implements the approved full-project API migration design's next member slice: one API group only, stable `MemberLevelApi`, one `MemberLevelRemoteClient`, product/trade scan updates, service consumers still inject `MemberLevelApi`, no DTO/enum movement, no `local` package, and isolated commit boundaries.
- Placeholder scan: The only placeholder text appears inside the report template and is explicitly replaced before report validation; implementation steps include exact paths, code, commands, and expected outputs.
- Type consistency: `MemberLevelApi`, `MemberLevelRemoteClient`, `MemberLevelRespDTO`, `MemberUserApi`, `MemberPointApi`, `MemberAddressRemoteClient`, `MemberConfigRemoteClient`, and `memberLevelRemoteClient` are used consistently across all tasks.
