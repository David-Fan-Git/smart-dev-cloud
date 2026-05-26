# Member Address API Remote Pilot Phase 4 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Continue the member API contract pilot by splitting only the `address` API group's Feign transport into `remote/MemberAddressRemoteClient` while preserving `MemberAddressApi` as the stable contract.

**Architecture:** This phase follows the completed member `config` pilot shape. `MemberAddressApi` keeps the existing path constants, Spring MVC mappings, Swagger annotations, parameters, return wrappers, and DTO package; the new `MemberAddressRemoteClient` owns only Feign identity and extends `MemberAddressApi`. Trade Feign scanning switches only the address entry to the remote adapter, while trade services keep injecting `MemberAddressApi`.

**Tech Stack:** Java 17, Spring Cloud OpenFeign, Spring Web annotations, Maven multi-module build, project `CommonResult` wrapper, member API contract standard.

---

## File Structure

- Modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java`
  - Responsibility: stable address API contract.
  - Change: remove `@FeignClient` and Feign import; keep all contract methods and request mappings unchanged.

- Create: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java`
  - Responsibility: remote Feign adapter for the address API contract.
  - Change: add `@FeignClient(name = ApiConstants.NAME, contextId = "memberAddressRemoteClient")` and extend `MemberAddressApi`; do not duplicate contract methods.

- Modify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`
  - Responsibility: trade module Feign client scanning configuration.
  - Change: replace `MemberAddressApi.class` in the main `@EnableFeignClients` list with `MemberAddressRemoteClient.class`, and import the new remote client. Leave `MemberUserApi`, `MemberPointApi`, `MemberLevelApi`, and `MemberConfigRemoteClient` unchanged.

- Read-only: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApiImpl.java`
  - Responsibility: server implementation of `MemberAddressApi`.
  - Change: no modification in this phase.

- Create: `docs/superpowers/reports/2026-05-24-member-address-api-remote-pilot-report.md`
  - Responsibility: record Phase 4 scope, decisions, validation, and follow-up.

---

### Task 1: Verify current member address API contract and consumers

**Files:**
- Read: `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java`
- Read: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApiImpl.java`
- Read: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

- [ ] **Step 1: Confirm this phase stays within one API group**

Run:

```bash
grep -n "Each Java pilot migrates only one member API group" docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md && grep -n "MemberAddressRemoteClient.java" docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md
```

Expected output includes the one-group rule and the final target mention of `MemberAddressRemoteClient.java`.

- [ ] **Step 2: Confirm current address contract shape**

Run:

```bash
grep -nE "@FeignClient|interface MemberAddressApi|String PREFIX|@GetMapping|CommonResult<MemberAddressRespDTO> getAddress|CommonResult<MemberAddressRespDTO> getDefaultAddress" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java
```

Expected output includes `@FeignClient(name = ApiConstants.NAME)`, `MemberAddressApi`, `PREFIX`, both `@GetMapping` annotations, `getAddress`, and `getDefaultAddress`.

- [ ] **Step 3: Confirm server implementation still implements the stable contract**

Run:

```bash
grep -nE "class MemberAddressApiImpl implements MemberAddressApi|CommonResult<MemberAddressRespDTO> getAddress|CommonResult<MemberAddressRespDTO> getDefaultAddress" develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApiImpl.java
```

Expected output includes the implementation class and both method implementations.

- [ ] **Step 4: Confirm trade references address API only in scan and service consumers**

Run:

```bash
grep -R "MemberAddressApi" -n develop-module-mall/develop-module-trade-server/src/main/java --include='*.java'
```

Expected output includes `RpcConfiguration.java`, `TradeOrderUpdateServiceImpl.java`, and `TradeDeliveryPriceCalculator.java`.

- [ ] **Step 5: Confirm no address remote client already exists**

Run:

```bash
! test -e develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java
```

Expected: command exits with code `0` and no output.

---

### Task 2: Add member address remote adapter and keep contract stable

**Files:**
- Modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java`
- Create: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java`

- [ ] **Step 1: Remove Feign transport responsibility from `MemberAddressApi.java`**

Edit `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java` from:

```java
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ApiConstants.NAME) // TODO David：fallbackFactory =
@Tag(name = "RPC 服务 - 用户收件地址")
public interface MemberAddressApi {
```

to:

```java
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "RPC 服务 - 用户收件地址")
public interface MemberAddressApi {
```

Do not change `PREFIX`, `getAddress`, `getDefaultAddress`, `@RequestParam`, `@Parameter`, `@Parameters`, or return types.

- [ ] **Step 2: Create the address remote package**

Run:

```bash
mkdir -p develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote
```

Expected: command exits with code `0`.

- [ ] **Step 3: Create `MemberAddressRemoteClient.java`**

Create `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java` with this exact content:

```java
package com.develop.mvp.pk.module.member.api.address.remote;

import com.develop.mvp.pk.module.member.api.address.MemberAddressApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberAddressRemoteClient")
public interface MemberAddressRemoteClient extends MemberAddressApi {
}
```

- [ ] **Step 4: Verify address contract still owns methods and paths**

Run:

```bash
grep -nE "@Tag|String PREFIX|@GetMapping|CommonResult<MemberAddressRespDTO> getAddress|CommonResult<MemberAddressRespDTO> getDefaultAddress|@FeignClient" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java
```

Expected output includes `@Tag`, `PREFIX`, both method signatures, and no `@FeignClient` line.

- [ ] **Step 5: Verify address remote adapter owns Feign identity only**

Run:

```bash
grep -nE "@FeignClient\(name = ApiConstants.NAME, contextId = \"memberAddressRemoteClient\"\)|interface MemberAddressRemoteClient extends MemberAddressApi|getAddress|getDefaultAddress" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java
```

Expected output includes only the `@FeignClient` line and `extends MemberAddressApi` line. It must not include `getAddress` or `getDefaultAddress`.

- [ ] **Step 6: Check no address `local` package was created**

Run:

```bash
! test -d develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/local
```

Expected: command exits with code `0` and no output.

---

### Task 3: Update trade Feign scan for address only

**Files:**
- Modify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

- [ ] **Step 1: Replace the address import only**

Edit `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`.

Replace:

```java
import com.develop.mvp.pk.module.member.api.address.MemberAddressApi;
```

with:

```java
import com.develop.mvp.pk.module.member.api.address.remote.MemberAddressRemoteClient;
```

Leave `MemberUserApi`, `MemberPointApi`, `MemberLevelApi`, and `MemberConfigRemoteClient` imports unchanged.

- [ ] **Step 2: Replace the address Feign client class only**

In the same file, replace:

```java
MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class, MemberAddressApi.class, MemberConfigRemoteClient.class,
```

with:

```java
MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class, MemberAddressRemoteClient.class, MemberConfigRemoteClient.class,
```

Do not change the nested `SystemRemoteRpcConfiguration` block.

- [ ] **Step 3: Verify the scan list uses the remote client only for address and config**

Run:

```bash
grep -nE "MemberAddress(Api|RemoteClient)|MemberConfigRemoteClient|MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class" develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
```

Expected output includes `import com.develop.mvp.pk.module.member.api.address.remote.MemberAddressRemoteClient;` and `MemberAddressRemoteClient.class, MemberConfigRemoteClient.class`. It must not include `import com.develop.mvp.pk.module.member.api.address.MemberAddressApi;`.

- [ ] **Step 4: Verify service consumers still depend on the stable contract**

Run:

```bash
grep -R "private MemberAddressApi\|import com.develop.mvp.pk.module.member.api.address.MemberAddressApi" -n develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service
```

Expected output still includes `TradeOrderUpdateServiceImpl.java` and `TradeDeliveryPriceCalculator.java`. These services continue depending on `MemberAddressApi`, not `MemberAddressRemoteClient`.

---

### Task 4: Compile and verify the address pilot scope

**Files:**
- Verify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java`
- Verify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java`
- Verify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

- [ ] **Step 1: Compile member API module**

Run:

```bash
mvn compile -pl develop-module-member/develop-module-member-api -am
```

Expected: Maven exits with code `0`.

- [ ] **Step 2: Compile trade server because it imports the new remote client**

Run:

```bash
mvn compile -pl develop-module-mall/develop-module-trade-server -am
```

Expected: Maven exits with code `0`.

- [ ] **Step 3: Verify only config and address remote clients exist for member API**

Run:

```bash
find develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api -path '*/remote/*.java' -type f | sort
```

Expected output is exactly:

```text
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java
```

- [ ] **Step 4: Verify no `local` package was created**

Run:

```bash
find develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api -path '*/local/*' -type f | sort
```

Expected: no output.

- [ ] **Step 5: Verify only intended Phase 4 Java files changed**

Run:

```bash
git diff --name-only -- develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
```

Expected output includes only:

```text
develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java
```

Because the new `MemberAddressRemoteClient.java` is untracked, also run:

```bash
git status --short -- develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java
```

Expected output:

```text
?? develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java
```

---

### Task 5: Write Phase 4 report and commit if requested

**Files:**
- Create: `docs/superpowers/reports/2026-05-24-member-address-api-remote-pilot-report.md`
- Stage if requested: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java`
- Stage if requested: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java`
- Stage if requested: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`
- Stage if requested: `docs/superpowers/plans/2026-05-24-member-address-api-remote-pilot-phase4.md`
- Stage if requested: `docs/superpowers/reports/2026-05-24-member-address-api-remote-pilot-report.md`

- [ ] **Step 1: Write the pilot report**

Create `docs/superpowers/reports/2026-05-24-member-address-api-remote-pilot-report.md` with this content after replacing the validation lines with actual command outcomes:

```markdown
# Member Address API Remote Pilot Report

## Scope

This pilot migrated only the `develop-module-member-api` address API group to the contract + remote adapter shape.

## Changed Files

- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java`
- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

## Decisions

- `MemberAddressApi` remains the stable contract name.
- `MemberAddressRemoteClient` owns Feign remote transport identity with `contextId = "memberAddressRemoteClient"`.
- Trade address consumers continue depending on `MemberAddressApi`.
- No `local` package was created because local mode binding is not confirmed yet.
- No DTOs or enums were moved.
- No `user`, `level`, or `point` member API groups were migrated.

## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: record actual result here.
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: record actual result here.

## Follow-Up

- Plan the next member API group separately instead of batching multiple groups.
- Define the concrete local adapter binding strategy before creating any `local/` packages.
```

- [ ] **Step 2: Replace report validation placeholders with real outcomes**

If both Maven commands pass, the final validation section must be:

```markdown
## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: passed.
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: passed.
```

- [ ] **Step 3: Verify report has no unresolved placeholder wording**

Run:

```bash
! grep -nE 'record actual result here|TBD|TODO|待补充|适当|implement later|fill in details' docs/superpowers/reports/2026-05-24-member-address-api-remote-pilot-report.md
```

Expected: command exits with code `0` and no output.

- [ ] **Step 4: Ask for commit confirmation**

Say:

```text
Phase 4 member address API remote pilot is ready. Do you want me to commit only these files?
```

Do not stage or commit until the user explicitly confirms.

- [ ] **Step 5: If the user confirms, stage only intended files**

Run:

```bash
git add develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/MemberAddressApi.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/address/remote/MemberAddressRemoteClient.java develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java docs/superpowers/plans/2026-05-24-member-address-api-remote-pilot-phase4.md docs/superpowers/reports/2026-05-24-member-address-api-remote-pilot-report.md
```

Expected: command exits with code `0`.

- [ ] **Step 6: Commit with project-style message**

Run:

```bash
git commit -m "$(cat <<'EOF'
DDD重构：试点拆分 member address 远程适配器

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```

Expected: commit succeeds. If hooks fail, fix the underlying issue and create a new commit attempt; do not use `--no-verify`.

---

## Self-Review

- Spec coverage: This plan continues the member API contract pilot one API group at a time, keeps `MemberAddressApi` as the stable contract, creates only `MemberAddressRemoteClient`, updates only trade Feign scanning for address, preserves service injection on `MemberAddressApi`, avoids DTO/enum movement, and creates no `local` package.
- Placeholder scan: The only placeholder text appears inside the report template and is explicitly replaced before report validation; implementation steps include exact paths, code, commands, and expected outputs.
- Type consistency: `MemberAddressApi`, `MemberAddressRemoteClient`, `MemberAddressRespDTO`, `MemberConfigRemoteClient`, and `memberAddressRemoteClient` are used consistently across all tasks.
