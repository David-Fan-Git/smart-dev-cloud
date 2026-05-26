# System API Remote Adapters Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Continue the one-contract/two-adapter migration for all remaining `develop-module-system-api` Feign-bound interfaces.

**Architecture:** Each `XxxApi` remains the single cross-module contract and no longer carries `@FeignClient`. Each remote adapter is created in the same API package's `remote` subpackage as `XxxRemoteClient extends XxxApi`. Existing server-side `XxxApiImpl` beans remain local adapters, and consumer business code keeps injecting `XxxApi`; only `@EnableFeignClients` registration classes switch to remote client classes.

**Tech Stack:** Java 17, Spring Boot 3.5.x, Spring Cloud OpenFeign, Maven multi-module.

---

## Scope

Migrate the remaining system API interfaces that still declare `@FeignClient`:

- `dept/DeptApi.java` → `dept/remote/DeptRemoteClient.java`
- `dept/PostApi.java` → `dept/remote/PostRemoteClient.java`
- `logger/LoginLogApi.java` → `logger/remote/LoginLogRemoteClient.java`
- `logger/OperateLogApi.java` → `logger/remote/OperateLogRemoteClient.java`
- `mail/MailSendApi.java` → `mail/remote/MailSendRemoteClient.java`
- `notify/NotifyMessageSendApi.java` → `notify/remote/NotifyMessageSendRemoteClient.java`
- `permission/PermissionApi.java` → `permission/remote/PermissionRemoteClient.java`
- `permission/RoleApi.java` → `permission/remote/RoleRemoteClient.java`
- `sms/SmsCodeApi.java` → `sms/remote/SmsCodeRemoteClient.java`
- `sms/SmsSendApi.java` → `sms/remote/SmsSendRemoteClient.java`
- `social/SocialClientApi.java` → `social/remote/SocialClientRemoteClient.java`
- `social/SocialUserApi.java` → `social/remote/SocialUserRemoteClient.java`
- `user/AdminUserApi.java` → `user/remote/AdminUserRemoteClient.java`

`dict/DictDataApi.java` was already migrated in the pilot and is not repeated here.

## Task 1: Create remote adapters and clean contracts

- [ ] Remove `org.springframework.cloud.openfeign.FeignClient` imports and `@FeignClient(...)` annotations from each scoped `XxxApi.java`.
- [ ] For each scoped API, create `remote/XxxRemoteClient.java` in the same business API package.
- [ ] Each remote adapter must use this shape:

```java
package com.develop.mvp.pk.module.system.api.<area>.remote;

import com.develop.mvp.pk.module.system.api.<area>.XxxApi;
import com.develop.mvp.pk.module.system.enums.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = ApiConstants.NAME, contextId = "systemXxxRemoteClient")
public interface XxxRemoteClient extends XxxApi {
}
```

## Task 2: Update system API remote registrations

- [ ] Replace `XxxApi.class` entries with `XxxRemoteClient.class` in these registration files:
  - `develop-module-ai/develop-module-ai-server/src/main/java/com/develop/mvp/pk/module/ai/framework/rpc/config/RpcConfiguration.java`
  - `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/rpc/config/RpcConfiguration.java`
  - `develop-module-crm/develop-module-crm-server/src/main/java/com/develop/mvp/pk/module/crm/framework/rpc/config/RpcConfiguration.java`
  - `develop-module-erp/develop-module-erp-server/src/main/java/com/develop/mvp/pk/module/erp/framework/rpc/config/RpcConfiguration.java`
  - `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/framework/rpc/config/RpcConfiguration.java`
  - `develop-module-mes/develop-module-mes-server/src/main/java/com/develop/mvp/pk/module/mes/framework/rpc/config/RpcConfiguration.java`
  - `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/rpc/config/RpcConfiguration.java`
  - `develop-module-wms/develop-module-wms-server/src/main/java/com/develop/mvp/pk/module/wms/framework/rpc/config/RpcConfiguration.java`
- [ ] Do not update business service imports unless they are in `@EnableFeignClients` registration contexts.

## Task 3: Verify system API migration

- [ ] Compile system API:

```bash
mvn compile -pl develop-module-system/develop-module-system-api -am
```

- [ ] Compile representative consumers that register system remote APIs:

```bash
mvn compile -pl develop-module-bpm/develop-module-bpm-server -am
mvn compile -pl develop-module-member/develop-module-member-server -am
mvn compile -pl develop-module-pay/develop-module-pay-server -am
```

- [ ] Search for old `@EnableFeignClients` registrations that still point to migrated system contract interfaces:

```bash
grep -R "@EnableFeignClients.*\(.*AdminUserApi\|@EnableFeignClients.*\(.*DeptApi\|@EnableFeignClients.*\(.*PostApi\|@EnableFeignClients.*\(.*RoleApi\|@EnableFeignClients.*\(.*SmsSendApi\|@EnableFeignClients.*\(.*SmsCodeApi\|@EnableFeignClients.*\(.*LoginLogApi\|@EnableFeignClients.*\(.*SocialUserApi\|@EnableFeignClients.*\(.*SocialClientApi" -n develop-module-* --include='*.java' --exclude-dir=target
```

- [ ] Inspect `git diff` for expected scope only.
