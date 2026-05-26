# Member Config API Remote Pilot Phase 3 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the smallest Java API-contract pilot by splitting `develop-module-member-api` config remote transport into `remote/MemberConfigRemoteClient` while preserving `MemberConfigApi` as the stable contract.

**Architecture:** The pilot changes only the member `config` API group. `MemberConfigApi` remains the stable cross-module contract with path, operation, and return type; the new `MemberConfigRemoteClient` becomes the Feign transport adapter and extends `MemberConfigApi`. Consumer wiring is updated only where remote Feign scanning currently references `MemberConfigApi` directly; local/server implementation remains `MemberConfigApiImpl`.

**Tech Stack:** Java 17, Spring Cloud OpenFeign, Maven multi-module build, Spring Web annotations, project `CommonResult` API wrapper, DDD module structure standard.

---

## File Structure

- Modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java`
  - Responsibility: stable config API contract.
  - Change: remove `@FeignClient` and Feign import; keep `@Tag`, `PREFIX`, `@GetMapping`, `@Operation`, and `CommonResult<MemberConfigRespDTO> getConfig()` unchanged.

- Create: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java`
  - Responsibility: remote Feign adapter for the config API contract.
  - Change: add `@FeignClient(name = ApiConstants.NAME, contextId = "memberConfigRemoteClient")` and extend `MemberConfigApi`; no duplicated methods.

- Modify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`
  - Responsibility: trade module Feign client scanning configuration.
  - Change: replace `MemberConfigApi.class` in the general `@EnableFeignClients` list with `MemberConfigRemoteClient.class`, and import the new remote client. Leave other member APIs unchanged.

- Read-only: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApiImpl.java`
  - Responsibility: local/server implementation of `MemberConfigApi`.
  - Change: no modification in this phase.

- Read-only references:
  - `.claude/ddd-skills/Module_Structure_Standard.md`
  - `docs/superpowers/reports/2026-05-24-api-contract-audit.md`
  - `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`
  - `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/remote/AdminUserRemoteClient.java`
  - `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dict/remote/DictDataRemoteClient.java`

---

### Task 1: Verify current member config API contract and remote sample

**Files:**
- Read: `.claude/ddd-skills/Module_Structure_Standard.md`
- Read: `docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/dto/MemberConfigRespDTO.java`
- Read: `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApiImpl.java`
- Read: `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/remote/AdminUserRemoteClient.java`
- Read: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

- [ ] **Step 1: Confirm Phase 3 is scoped to one API group**

Run:

```bash
grep -n "Recommended first slice: \\`config\\`" docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md && grep -n "Each Java pilot migrates only one member API group" docs/superpowers/specs/2026-05-24-api-contract-pilot-design.md
```

Expected output includes both lines, proving the pilot is limited to member `config`.

- [ ] **Step 2: Confirm current config contract shape**

Run:

```bash
grep -nE "@FeignClient|interface MemberConfigApi|String PREFIX|@GetMapping|CommonResult<MemberConfigRespDTO> getConfig" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java
```

Expected output includes:

```text
@FeignClient(name = ApiConstants.NAME) // TODO David：fallbackFactory =
public interface MemberConfigApi {
String PREFIX = ApiConstants.PREFIX + "/config";
@GetMapping(PREFIX + "/get")
CommonResult<MemberConfigRespDTO> getConfig();
```

- [ ] **Step 3: Confirm server implementation depends on the stable contract**

Run:

```bash
grep -nE "class MemberConfigApiImpl implements MemberConfigApi|CommonResult<MemberConfigRespDTO> getConfig" develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApiImpl.java
```

Expected output includes:

```text
public class MemberConfigApiImpl implements MemberConfigApi {
public CommonResult<MemberConfigRespDTO> getConfig() {
```

- [ ] **Step 4: Confirm system remote sample pattern**

Run:

```bash
grep -nE "@FeignClient\(name = ApiConstants.NAME, contextId = \"systemAdminUserRemoteClient\"\)|interface AdminUserRemoteClient extends AdminUserApi" develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/remote/AdminUserRemoteClient.java
```

Expected output includes:

```text
@FeignClient(name = ApiConstants.NAME, contextId = "systemAdminUserRemoteClient")
public interface AdminUserRemoteClient extends AdminUserApi {
```

- [ ] **Step 5: Confirm current consumers and Feign scan references**

Run:

```bash
grep -R "MemberConfigApi" -n develop-module-* develop-server develop-gateway --include='*.java'
```

Expected current output includes these references:

```text
develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/price/calculator/TradePointGiveCalculator.java
develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/price/calculator/TradePointUsePriceCalculator.java
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java
develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApiImpl.java
```

- [ ] **Step 6: Confirm no remote client already exists**

Run:

```bash
! test -e develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java
```

Expected: command exits with code `0` and no output.

---

### Task 2: Add member config remote adapter and keep contract stable

**Files:**
- Modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java`
- Create: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java`

- [ ] **Step 1: Modify `MemberConfigApi.java` to remove Feign transport responsibility**

Edit `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java` from:

```java
package com.develop.mvp.pk.module.member.api.config;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.api.config.dto.MemberConfigRespDTO;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = ApiConstants.NAME) // TODO David：fallbackFactory =
@Tag(name = "RPC 服务 - 用户配置")
public interface MemberConfigApi {

    String PREFIX = ApiConstants.PREFIX + "/config";

    @GetMapping(PREFIX + "/get")
    @Operation(summary = "获得用户配置")
    CommonResult<MemberConfigRespDTO> getConfig();

}
```

to:

```java
package com.develop.mvp.pk.module.member.api.config;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.api.config.dto.MemberConfigRespDTO;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "RPC 服务 - 用户配置")
public interface MemberConfigApi {

    String PREFIX = ApiConstants.PREFIX + "/config";

    @GetMapping(PREFIX + "/get")
    @Operation(summary = "获得用户配置")
    CommonResult<MemberConfigRespDTO> getConfig();

}
```

- [ ] **Step 2: Create the `remote` package directory**

Run:

```bash
mkdir -p develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote
```

Expected: command exits with code `0`.

- [ ] **Step 3: Create `MemberConfigRemoteClient.java`**

Create `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java` with this exact content:

```java
package com.develop.mvp.pk.module.member.api.config.remote;

import com.develop.mvp.pk.module.member.api.config.MemberConfigApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberConfigRemoteClient")
public interface MemberConfigRemoteClient extends MemberConfigApi {
}
```

- [ ] **Step 4: Verify the contract still owns the API method and path**

Run:

```bash
grep -nE "@Tag|String PREFIX|@GetMapping|CommonResult<MemberConfigRespDTO> getConfig" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java
```

Expected output includes all four contract elements and does not include `@FeignClient`.

- [ ] **Step 5: Verify the remote adapter owns Feign identity only**

Run:

```bash
grep -nE "@FeignClient\(name = ApiConstants.NAME, contextId = \"memberConfigRemoteClient\"\)|interface MemberConfigRemoteClient extends MemberConfigApi|getConfig" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java
```

Expected output includes the `@FeignClient` and `extends MemberConfigApi` lines. It must not include `getConfig`.

- [ ] **Step 6: Check no accidental `local` package was created**

Run:

```bash
! test -d develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/local
```

Expected: command exits with code `0` and no output.

---

### Task 3: Update the only remote Feign scan reference for config

**Files:**
- Modify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

- [ ] **Step 1: Replace the config import only**

Edit `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`.

Replace this import:

```java
import com.develop.mvp.pk.module.member.api.config.MemberConfigApi;
```

with:

```java
import com.develop.mvp.pk.module.member.api.config.remote.MemberConfigRemoteClient;
```

Leave `MemberUserApi`, `MemberPointApi`, `MemberLevelApi`, and `MemberAddressApi` imports unchanged.

- [ ] **Step 2: Replace the config Feign client class only**

In the same file, replace this entry in the first `@EnableFeignClients` list:

```java
MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class, MemberAddressApi.class, MemberConfigApi.class,
```

with:

```java
MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class, MemberAddressApi.class, MemberConfigRemoteClient.class,
```

Do not change the existing nested `SystemRemoteRpcConfiguration` block.

- [ ] **Step 3: Verify the scan list uses the remote client only for config**

Run:

```bash
grep -nE "MemberConfig(Api|RemoteClient)|MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class, MemberAddressApi.class" develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
```

Expected output includes:

```text
import com.develop.mvp.pk.module.member.api.config.remote.MemberConfigRemoteClient;
MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class, MemberAddressApi.class, MemberConfigRemoteClient.class,
```

Expected output must not include `import com.develop.mvp.pk.module.member.api.config.MemberConfigApi;`.

- [ ] **Step 4: Verify service consumers still depend on the stable contract**

Run:

```bash
grep -R "private MemberConfigApi memberConfigApi\|import com.develop.mvp.pk.module.member.api.config.MemberConfigApi" -n develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/service/price/calculator
```

Expected output still includes `TradePointGiveCalculator.java` and `TradePointUsePriceCalculator.java`. These services should continue depending on `MemberConfigApi`, not the remote client.

---

### Task 4: Compile and verify the member config pilot

**Files:**
- Verify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java`
- Verify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java`
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

Expected: Maven exits with code `0`. If this fails because of pre-existing unrelated workspace Java changes, capture the first failing module and error summary, then decide whether to narrow compile validation to the affected API module plus targeted source inspection.

- [ ] **Step 3: Verify no extra member API groups were migrated**

Run:

```bash
find develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api -path '*/remote/*.java' -type f | sort
```

Expected output is exactly:

```text
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java
```

- [ ] **Step 4: Verify no `local` package was created**

Run:

```bash
find develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api -path '*/local/*' -type f | sort
```

Expected: no output.

- [ ] **Step 5: Verify only intended Java files changed in this pilot**

Run:

```bash
git diff --name-only -- develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
```

Expected output includes only:

```text
develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java
```

Because the new `MemberConfigRemoteClient.java` is untracked, also run:

```bash
git status --short -- develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java
```

Expected output:

```text
?? develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java
```

---

### Task 5: Update Phase 3 notes and commit if requested

**Files:**
- Create: `docs/superpowers/reports/2026-05-24-member-config-api-remote-pilot-report.md`
- Stage if requested: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java`
- Stage if requested: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java`
- Stage if requested: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`
- Stage if requested: `docs/superpowers/plans/2026-05-24-member-config-api-remote-pilot-phase3.md`
- Stage if requested: `docs/superpowers/reports/2026-05-24-member-config-api-remote-pilot-report.md`

- [ ] **Step 1: Write the pilot report**

Create `docs/superpowers/reports/2026-05-24-member-config-api-remote-pilot-report.md` with this content, replacing Maven result lines with the actual command outcomes:

```markdown
# Member Config API Remote Pilot Report

## Scope

This pilot migrated only the `develop-module-member-api` config API group to the contract + remote adapter shape.

## Changed Files

- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java`
- `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java`
- `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`

## Decisions

- `MemberConfigApi` remains the stable contract name.
- `MemberConfigRemoteClient` owns Feign remote transport identity with `contextId = "memberConfigRemoteClient"`.
- Trade price services continue depending on `MemberConfigApi`.
- No `local` package was created because local mode binding is not confirmed yet.
- No DTOs or enums were moved.
- No other member API groups were migrated.

## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: record actual result here.
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: record actual result here.

## Follow-Up

- Decide whether the next member API group should follow the same remote adapter split.
- Define the concrete local adapter binding strategy before creating any `local/` packages.
- Do not expand to all member API groups without explicit approval.
```

- [ ] **Step 2: Replace report validation placeholders with real outcomes**

Edit the two validation bullets to include the actual Maven outcomes from Task 4. If both commands passed, the final validation section must be:

```markdown
## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: passed.
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: passed.
```

If a command failed due to pre-existing unrelated Java changes, record the command, failing module, and first error summary instead of writing `passed`.

- [ ] **Step 3: Verify report has no unresolved placeholder wording**

Run:

```bash
! grep -nE 'record actual result here|TBD|TODO|待补充|适当|implement later|fill in details' docs/superpowers/reports/2026-05-24-member-config-api-remote-pilot-report.md
```

Expected: command exits with code `0` and no output.

- [ ] **Step 4: Ask for commit confirmation**

Say:

```text
Phase 3 member config API remote pilot is ready. Do you want me to commit only these files?
```

Do not stage or commit until the user explicitly says yes.

- [ ] **Step 5: If the user confirms, inspect target status**

Run:

```bash
git status --short -- develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java docs/superpowers/plans/2026-05-24-member-config-api-remote-pilot-phase3.md docs/superpowers/reports/2026-05-24-member-config-api-remote-pilot-report.md
```

Expected: only intended files are listed. Pre-existing unrelated workspace changes are not staged.

- [ ] **Step 6: Stage only intended files**

Run:

```bash
git add develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/MemberConfigApi.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/config/remote/MemberConfigRemoteClient.java develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java docs/superpowers/plans/2026-05-24-member-config-api-remote-pilot-phase3.md docs/superpowers/reports/2026-05-24-member-config-api-remote-pilot-report.md
```

Expected: command exits with code `0`.

- [ ] **Step 7: Commit with project-style message**

Run:

```bash
git commit -m "$(cat <<'EOF'
DDD重构：试点拆分 member config 远程适配器

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```

Expected: commit succeeds. If hooks fail, fix the underlying issue and create a new commit attempt; do not use `--no-verify`.

---

## Self-Review

- Spec coverage: The plan implements the Phase 2 pilot design's smallest Java slice: member `config` only, no rename to `*CommonApi`, no DTO/enums movement, no local package, one remote adapter extending the stable contract, and Maven compile validation.
- Placeholder scan: The implementation steps contain exact paths, code snippets, commands, and expected outputs. The only report placeholder is intentionally resolved in Task 5 Step 2 before validation and commit.
- Scope check: The plan does not migrate address, level, point, or user; it does not modify member server implementation; it does not solve mall, iot, infra, or pay boundaries; it updates only the trade Feign scan reference that currently points at `MemberConfigApi.class`.
