# Member API Local/Remote Batch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the first API local/remote refactor batch by auditing and validating the remaining member `level`, `point`, and `user` API contracts and their remote adapters.

**Architecture:** Member API contracts remain the stable business-facing injection types. Remote transport identity lives only in `remote/MemberXxxRemoteClient`, which extends the corresponding contract and has a unique Feign `contextId`. Product, promotion, and trade consumers scan remote clients while business code continues injecting `MemberXxxApi`.

**Tech Stack:** Java 17, Spring Cloud OpenFeign, Spring Web annotations, Maven multi-module build, project `CommonResult`, existing member API contract migration standard.

---

## File Structure

- Read/verify: `docs/superpowers/specs/2026-05-24-api-contract-local-remote-refactor-design.md`
  - Responsibility: confirmed full-project API local/remote refactor design.

- Verify or modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
  - Responsibility: stable level API contract.
  - Required state: no `@FeignClient`; retains `PREFIX`, mappings, annotations, and method signatures.

- Verify or modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java`
  - Responsibility: remote Feign adapter for level.
  - Required state: `@FeignClient(name = ApiConstants.NAME, contextId = "memberLevelRemoteClient")`; extends `MemberLevelApi`; no duplicated methods.

- Verify or modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/MemberPointApi.java`
  - Responsibility: stable point API contract.
  - Required state: no `@FeignClient`; retains `PREFIX`, mappings, validation annotations, and method signatures.

- Verify or modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/remote/MemberPointRemoteClient.java`
  - Responsibility: remote Feign adapter for point.
  - Required state: `@FeignClient(name = ApiConstants.NAME, contextId = "memberPointRemoteClient")`; extends `MemberPointApi`; no duplicated methods.

- Verify or modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/MemberUserApi.java`
  - Responsibility: stable user API contract.
  - Required state: no `@FeignClient`; retains `PREFIX`, mappings, default `getUserMap(Collection<Long>)`, and method signatures.

- Verify or modify: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/remote/MemberUserRemoteClient.java`
  - Responsibility: remote Feign adapter for user.
  - Required state: `@FeignClient(name = ApiConstants.NAME, contextId = "memberUserRemoteClient")`; extends `MemberUserApi`; no duplicated methods.

- Verify or modify: `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`
  - Responsibility: product remote scan config.
  - Required state: scans `MemberUserRemoteClient.class` and `MemberLevelRemoteClient.class`.

- Verify or modify: `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/framework/rpc/config/RpcConfiguration.java`
  - Responsibility: promotion remote scan config.
  - Required state: scans `MemberUserRemoteClient.class` for member user remote mode.

- Verify or modify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`
  - Responsibility: trade remote scan config.
  - Required state: scans `MemberUserRemoteClient.class`, `MemberPointRemoteClient.class`, and `MemberLevelRemoteClient.class`.

- Create: `docs/superpowers/reports/2026-05-24-member-api-local-remote-batch-report.md`
  - Responsibility: record member batch audit results, validation commands, and next batch recommendation.

---

### Task 1: Audit member API contracts and remote adapters

**Files:**
- Read: `docs/superpowers/specs/2026-05-24-api-contract-local-remote-refactor-design.md`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/MemberPointApi.java`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/remote/MemberPointRemoteClient.java`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/MemberUserApi.java`
- Read: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/remote/MemberUserRemoteClient.java`

- [ ] **Step 1: Confirm the approved design says member is Batch 1**

Run:

```bash
grep -n "### Batch 1: Finish `member`" docs/superpowers/specs/2026-05-24-api-contract-local-remote-refactor-design.md && grep -n "检查 `address/config/level/point/user`" docs/superpowers/specs/2026-05-24-api-contract-local-remote-refactor-design.md
```

Expected: output includes the Batch 1 heading and the line naming `address/config/level/point/user`.

- [ ] **Step 2: Confirm member contracts do not own Feign identity**

Run:

```bash
! grep -R "@FeignClient" -n develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/MemberPointApi.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/MemberUserApi.java
```

Expected: command exits with code `0` and no output.

- [ ] **Step 3: Confirm remote adapters own Feign identity**

Run:

```bash
grep -n "@FeignClient(name = ApiConstants.NAME, contextId = \"memberLevelRemoteClient\")" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java && grep -n "@FeignClient(name = ApiConstants.NAME, contextId = \"memberPointRemoteClient\")" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/remote/MemberPointRemoteClient.java && grep -n "@FeignClient(name = ApiConstants.NAME, contextId = \"memberUserRemoteClient\")" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/remote/MemberUserRemoteClient.java
```

Expected: output includes one Feign line for each remote client.

- [ ] **Step 4: Confirm remote adapters extend contracts without duplicated methods**

Run:

```bash
grep -n "interface MemberLevelRemoteClient extends MemberLevelApi" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java && grep -n "interface MemberPointRemoteClient extends MemberPointApi" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/remote/MemberPointRemoteClient.java && grep -n "interface MemberUserRemoteClient extends MemberUserApi" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/remote/MemberUserRemoteClient.java && ! grep -R "CommonResult<" -n develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/remote/MemberPointRemoteClient.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/remote/MemberUserRemoteClient.java
```

Expected: output includes the three `extends` lines, and no remote client method declaration is reported.

- [ ] **Step 5: Confirm no speculative member local package exists**

Run:

```bash
! find develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api -path '*/local/*' -print | grep .
```

Expected: command exits with code `0` and no output.

---

### Task 2: Fix member API contract files only if audit fails

**Files:**
- Modify if needed: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java`
- Modify if needed: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/MemberPointApi.java`
- Modify if needed: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/MemberUserApi.java`

- [ ] **Step 1: If `MemberLevelApi.java` still contains Feign imports or annotations, remove only Feign identity**

Required beginning of `MemberLevelApi.java` after the fix:

```java
package com.develop.mvp.pk.module.member.api.level;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.api.level.dto.MemberLevelRespDTO;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "RPC 服务 - 会员等级")
public interface MemberLevelApi {
```

Do not change `PREFIX`, mappings, parameters, return types, or method names.

- [ ] **Step 2: If `MemberPointApi.java` still contains Feign imports or annotations, remove only Feign identity**

Required beginning of `MemberPointApi.java` after the fix:

```java
package com.develop.mvp.pk.module.member.api.point;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.constraints.Min;

@Tag(name = "RPC 服务 - 用户积分")
public interface MemberPointApi {
```

Do not change `PREFIX`, mappings, `@Min`, parameters, return types, or method names.

- [ ] **Step 3: If `MemberUserApi.java` still contains Feign imports or annotations, remove only Feign identity**

Required beginning of `MemberUserApi.java` after the fix:

```java
package com.develop.mvp.pk.module.member.api.user;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.api.user.dto.MemberUserRespDTO;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "RPC 服务 - 会员用户")
public interface MemberUserApi {
```

Do not change `PREFIX`, mappings, default `getUserMap(Collection<Long>)`, parameters, return types, or method names.

- [ ] **Step 4: Re-run the no-Feign contract check**

Run:

```bash
! grep -R "@FeignClient" -n develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/MemberLevelApi.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/MemberPointApi.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/MemberUserApi.java
```

Expected: command exits with code `0` and no output.

---

### Task 3: Fix member remote clients only if audit fails

**Files:**
- Modify if needed: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java`
- Modify if needed: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/remote/MemberPointRemoteClient.java`
- Modify if needed: `develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/remote/MemberUserRemoteClient.java`

- [ ] **Step 1: Ensure `MemberLevelRemoteClient.java` has the exact remote adapter content**

If the file is missing or mismatched, replace it with:

```java
package com.develop.mvp.pk.module.member.api.level.remote;

import com.develop.mvp.pk.module.member.api.level.MemberLevelApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberLevelRemoteClient")
public interface MemberLevelRemoteClient extends MemberLevelApi {
}
```

- [ ] **Step 2: Ensure `MemberPointRemoteClient.java` has the exact remote adapter content**

If the file is missing or mismatched, replace it with:

```java
package com.develop.mvp.pk.module.member.api.point.remote;

import com.develop.mvp.pk.module.member.api.point.MemberPointApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberPointRemoteClient")
public interface MemberPointRemoteClient extends MemberPointApi {
}
```

- [ ] **Step 3: Ensure `MemberUserRemoteClient.java` has the exact remote adapter content**

If the file is missing or mismatched, replace it with:

```java
package com.develop.mvp.pk.module.member.api.user.remote;

import com.develop.mvp.pk.module.member.api.user.MemberUserApi;
import com.develop.mvp.pk.module.member.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "memberUserRemoteClient")
public interface MemberUserRemoteClient extends MemberUserApi {
}
```

- [ ] **Step 4: Re-run the remote adapter check**

Run:

```bash
grep -n "@FeignClient(name = ApiConstants.NAME, contextId = \"memberLevelRemoteClient\")" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java && grep -n "@FeignClient(name = ApiConstants.NAME, contextId = \"memberPointRemoteClient\")" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/remote/MemberPointRemoteClient.java && grep -n "@FeignClient(name = ApiConstants.NAME, contextId = \"memberUserRemoteClient\")" develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/remote/MemberUserRemoteClient.java && ! grep -R "CommonResult<" -n develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/level/remote/MemberLevelRemoteClient.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/point/remote/MemberPointRemoteClient.java develop-module-member/develop-module-member-api/src/main/java/com/develop/mvp/pk/module/member/api/user/remote/MemberUserRemoteClient.java
```

Expected: output includes the three Feign lines; no duplicated method declarations are reported.

---

### Task 4: Verify member consumers scan remote clients but inject stable contracts

**Files:**
- Verify or modify: `develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java`
- Verify or modify: `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/framework/rpc/config/RpcConfiguration.java`
- Verify or modify: `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java`
- Read: all Java consumers matching `MemberLevelApi`, `MemberPointApi`, or `MemberUserApi`

- [ ] **Step 1: Confirm product scans level and user remote clients**

Run:

```bash
grep -n "import com.develop.mvp.pk.module.member.api.level.remote.MemberLevelRemoteClient;" develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java && grep -n "import com.develop.mvp.pk.module.member.api.user.remote.MemberUserRemoteClient;" develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java && grep -n "@EnableFeignClients(clients = {MemberUserRemoteClient.class, MemberLevelRemoteClient.class})" develop-module-mall/develop-module-product-server/src/main/java/com/develop/mvp/pk/module/product/framework/rpc/config/RpcConfiguration.java
```

Expected: output includes both imports and the remote-client scan list.

- [ ] **Step 2: If product still scans stable contracts, replace only the scan config**

Required complete product RPC config:

```java
package com.develop.mvp.pk.module.product.framework.rpc.config;

import com.develop.mvp.pk.module.member.api.level.remote.MemberLevelRemoteClient;
import com.develop.mvp.pk.module.member.api.user.remote.MemberUserRemoteClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "productRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {MemberUserRemoteClient.class, MemberLevelRemoteClient.class})
public class RpcConfiguration {
}
```

- [ ] **Step 3: Confirm promotion scans user remote client**

Run:

```bash
grep -n "import com.develop.mvp.pk.module.member.api.user.remote.MemberUserRemoteClient;" develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/framework/rpc/config/RpcConfiguration.java && grep -n "MemberUserRemoteClient.class" develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/framework/rpc/config/RpcConfiguration.java
```

Expected: output includes the user remote import and `MemberUserRemoteClient.class` in `@EnableFeignClients`.

- [ ] **Step 4: Confirm trade scans level, point, and user remote clients**

Run:

```bash
grep -n "import com.develop.mvp.pk.module.member.api.level.remote.MemberLevelRemoteClient;" develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java && grep -n "import com.develop.mvp.pk.module.member.api.point.remote.MemberPointRemoteClient;" develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java && grep -n "import com.develop.mvp.pk.module.member.api.user.remote.MemberUserRemoteClient;" develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java && grep -n "MemberUserRemoteClient.class, MemberPointRemoteClient.class, MemberLevelRemoteClient.class" develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java
```

Expected: output includes all three imports and the scan list segment.

- [ ] **Step 5: Confirm business consumers still inject stable contracts, not remote clients**

Run:

```bash
! grep -R "private .*RemoteClient\|@Resource.*RemoteClient\|@Autowired.*RemoteClient" -n develop-module-mall/develop-module-product-server/src/main/java develop-module-mall/develop-module-promotion-server/src/main/java develop-module-mall/develop-module-trade-server/src/main/java --include='*.java'
```

Expected: command exits with code `0` and no output. Existing business code should inject `MemberUserApi`, `MemberPointApi`, or `MemberLevelApi` where needed.

---

### Task 5: Compile member batch and affected consumers

**Files:**
- No source modifications expected in this task.

- [ ] **Step 1: Compile member API module**

Run:

```bash
mvn compile -pl develop-module-member/develop-module-member-api -am
```

Expected: build ends with `BUILD SUCCESS`. If it fails because of unrelated existing workspace changes, capture the first failing module and first compiler error in the report from Task 6.

- [ ] **Step 2: Compile member server module**

Run:

```bash
mvn compile -pl develop-module-member/develop-module-member-server -am
```

Expected: build ends with `BUILD SUCCESS`. If it fails, capture the first failing module and first compiler error in the report.

- [ ] **Step 3: Compile product consumer module**

Run:

```bash
mvn compile -pl develop-module-mall/develop-module-product-server -am
```

Expected: build ends with `BUILD SUCCESS`. If it fails, capture the first failing module and first compiler error in the report.

- [ ] **Step 4: Compile promotion consumer module**

Run:

```bash
mvn compile -pl develop-module-mall/develop-module-promotion-server -am
```

Expected: build ends with `BUILD SUCCESS`. If it fails, capture the first failing module and first compiler error in the report.

- [ ] **Step 5: Compile trade consumer module**

Run:

```bash
mvn compile -pl develop-module-mall/develop-module-trade-server -am
```

Expected: build ends with `BUILD SUCCESS`. If it fails, capture the first failing module and first compiler error in the report.

---

### Task 6: Write member batch report and prepare Batch 2 handoff

**Files:**
- Create: `docs/superpowers/reports/2026-05-24-member-api-local-remote-batch-report.md`

- [ ] **Step 1: Create the report file**

Write `docs/superpowers/reports/2026-05-24-member-api-local-remote-batch-report.md` with this exact content after Task 5 passes:

```markdown
# Member API Local/Remote Batch Report

## Scope

Audited and validated the member `level`, `point`, and `user` API contracts plus their remote adapters as Batch 1 of the full-project API local/remote refactor.

## Contract Status

| Contract | Stable API | Remote Adapter | Consumer Scan Status |
|---|---|---|---|
| level | `MemberLevelApi` has no Feign identity | `MemberLevelRemoteClient` extends `MemberLevelApi` | product/trade scan remote client |
| point | `MemberPointApi` has no Feign identity | `MemberPointRemoteClient` extends `MemberPointApi` | trade scans remote client |
| user | `MemberUserApi` has no Feign identity | `MemberUserRemoteClient` extends `MemberUserApi` | product/promotion/trade scan remote client |

## Decisions

- Kept business consumers injecting stable member API contracts.
- Kept remote transport identity in `remote/*RemoteClient` classes only.
- Did not create `local/` packages because this batch only validates the current local server implementation and remote Feign adapter split.
- Did not move DTOs, enums, messages, VO, DO, Mapper, or domain objects.

## Validation

- `mvn compile -pl develop-module-member/develop-module-member-api -am`: PASS
- `mvn compile -pl develop-module-member/develop-module-member-server -am`: PASS
- `mvn compile -pl develop-module-mall/develop-module-product-server -am`: PASS
- `mvn compile -pl develop-module-mall/develop-module-promotion-server -am`: PASS
- `mvn compile -pl develop-module-mall/develop-module-trade-server -am`: PASS

## Next Batch

Proceed to Batch 2: `system + infra`. Start with a read-only audit of existing `remote/*RemoteClient` classes and remaining Feign scans that still point at stable API contracts such as `FileApi`, `ConfigApi`, `WebSocketSenderApi`, or framework `CommonApi` contracts.
```

- [ ] **Step 2: Check report validation results are explicit**

Run:

```bash
grep -n "PASS" docs/superpowers/reports/2026-05-24-member-api-local-remote-batch-report.md
```

Expected: output includes all five Maven validation lines, each ending with `PASS`.

- [ ] **Step 3: Inspect source diff for unintended files**

Run:

```bash
git diff --name-only -- develop-module-member develop-module-mall docs/superpowers/reports/2026-05-24-member-api-local-remote-batch-report.md docs/superpowers/plans/2026-05-24-member-api-local-remote-batch.md
```

Expected: output includes only files intentionally touched by this batch. Do not stage or commit unless the user explicitly asks for a commit.

---

## Self-Review Checklist

- Spec coverage: this plan covers Batch 1 from `docs/superpowers/specs/2026-05-24-api-contract-local-remote-refactor-design.md` and prepares Batch 2 handoff.
- No speculative `local/` package is created.
- No API method signatures, DTO fields, request paths, return wrappers, or default helper behavior are changed.
- Remote clients own Feign identity and extend stable contracts without duplicated methods.
- Product, promotion, and trade scan remote clients while business code keeps injecting stable contracts.
- Validation commands cover member API/server plus affected product, promotion, and trade consumers.
- No commit is included because the user has not explicitly requested one.
