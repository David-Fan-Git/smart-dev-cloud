# System API Remote Adapter 与本地/远程闭环变更说明

**已提交迁移：** `42641e5 DDD重构：拆分 system API 远程适配器`

**闭环改造：** 当前工作区待提交改动

**日期：** 2026-05-23

## 1. 背景

本次改造是为了支持“**一套 API 契约 + 两种适配器**”的模块调用方式：

- `XxxApi` 保持为跨模块调用契约，不再直接绑定 OpenFeign。
- `XxxApiImpl` 继续作为服务端/本地实现。
- `remote/XxxRemoteClient` 作为远程 RPC 适配器，负责承载 `@FeignClient`。
- 业务代码继续注入 `XxxApi`；只有 `@EnableFeignClients` 注册点切换到 remote client。

这样可以让项目在前期以本地模块调用为主，后期按模块拆微服务时再启用远程 RPC 适配器。

## 2. 改造统计

### 2.1 已提交 remote adapter 迁移

提交 `42641e5` 共修改或新增 **40 个文件**：

- 修改消费者 Feign 注册配置：11 个
- 修改 system API 契约接口：14 个
- 新增 system API remote client：14 个
- 新增迁移计划文档：1 个

提交统计：

```text
40 files changed, 257 insertions(+), 73 deletions(-)
```

### 2.2 当前闭环改造

当前工作区在 remote adapter 迁移基础上继续补齐本地/远程切换闭环：

- `develop-server` 显式关闭 system provider 的远程 Feign 注册。
- system-only 的消费者 `RpcConfiguration` 增加条件注册。
- mixed 的消费者 `RpcConfiguration` 拆分为非 system 远程注册和 system 远程注册两段，避免关闭 system 远程时误伤其他 provider 的远程调用。

## 3. system API 契约接口改动

以下 14 个接口从“Feign 客户端 + 契约”改为“纯契约接口”。

具体改动统一为：

```diff
- import org.springframework.cloud.openfeign.FeignClient;
- @FeignClient(name = ApiConstants.NAME)
```

### 3.1 Dept

| 文件 | 改动 |
|---|---|
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dept/DeptApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dept/PostApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |

### 3.2 Dict

| 文件 | 改动 |
|---|---|
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dict/DictDataApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |

### 3.3 Logger

| 文件 | 改动 |
|---|---|
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/logger/LoginLogApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/logger/OperateLogApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |

### 3.4 Mail / Notify

| 文件 | 改动 |
|---|---|
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/mail/MailSendApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/notify/NotifyMessageSendApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |

### 3.5 Permission

| 文件 | 改动 |
|---|---|
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/PermissionApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/RoleApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |

### 3.6 Sms

| 文件 | 改动 |
|---|---|
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/sms/SmsCodeApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/sms/SmsSendApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |

### 3.7 Social / User

| 文件 | 改动 |
|---|---|
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/social/SocialClientApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/social/SocialUserApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/AdminUserApi.java` | 移除 `FeignClient` import 和 `@FeignClient` 注解 |

## 4. 新增 remote client

以下 14 个远程适配器被新增到原 API 包的 `remote` 子包下。

统一结构：

```java
@FeignClient(name = ApiConstants.NAME, contextId = "systemXxxRemoteClient")
public interface XxxRemoteClient extends XxxApi {
}
```

| Remote Client | 继承契约 | contextId |
|---|---|---|
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dept/remote/DeptRemoteClient.java` | `DeptApi` | `systemDeptRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dept/remote/PostRemoteClient.java` | `PostApi` | `systemPostRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dict/remote/DictDataRemoteClient.java` | `DictDataApi` | `systemDictDataRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/logger/remote/LoginLogRemoteClient.java` | `LoginLogApi` | `systemLoginLogRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/logger/remote/OperateLogRemoteClient.java` | `OperateLogApi` | `systemOperateLogRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/mail/remote/MailSendRemoteClient.java` | `MailSendApi` | `systemMailSendRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/notify/remote/NotifyMessageSendRemoteClient.java` | `NotifyMessageSendApi` | `systemNotifyMessageSendRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/remote/PermissionRemoteClient.java` | `PermissionApi` | `systemPermissionRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/permission/remote/RoleRemoteClient.java` | `RoleApi` | `systemRoleRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/sms/remote/SmsCodeRemoteClient.java` | `SmsCodeApi` | `systemSmsCodeRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/sms/remote/SmsSendRemoteClient.java` | `SmsSendApi` | `systemSmsSendRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/social/remote/SocialClientRemoteClient.java` | `SocialClientApi` | `systemSocialClientRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/social/remote/SocialUserRemoteClient.java` | `SocialUserApi` | `systemSocialUserRemoteClient` |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/user/remote/AdminUserRemoteClient.java` | `AdminUserApi` | `systemAdminUserRemoteClient` |

## 5. Feign 注册配置改动

消费者模块的 `@EnableFeignClients` 不再注册 system API 契约接口，而是注册对应 remote client。

示例：

```diff
- import com.develop.mvp.pk.module.system.api.dept.DeptApi;
+ import com.develop.mvp.pk.module.system.api.dept.remote.DeptRemoteClient;

- @EnableFeignClients(clients = {DeptApi.class})
+ @EnableFeignClients(clients = {DeptRemoteClient.class})
```

### 5.1 AI

| 文件 | 改动 |
|---|---|
| `develop-module-ai/develop-module-ai-server/src/main/java/com/develop/mvp/pk/module/ai/framework/rpc/config/RpcConfiguration.java` | system API class 注册切换为 remote client class |

### 5.2 BPM

| 文件 | 改动 |
|---|---|
| `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/rpc/config/RpcConfiguration.java` | `RoleApi`、`DeptApi`、`PostApi`、`AdminUserApi`、`SmsSendApi`、`DictDataApi`、`PermissionApi` 切换为对应 remote client |

### 5.3 CRM

| 文件 | 改动 |
|---|---|
| `develop-module-crm/develop-module-crm-server/src/main/java/com/develop/mvp/pk/module/crm/framework/rpc/config/RpcConfiguration.java` | system API class 注册切换为 remote client class |

### 5.4 ERP

| 文件 | 改动 |
|---|---|
| `develop-module-erp/develop-module-erp-server/src/main/java/com/develop/mvp/pk/module/erp/framework/rpc/config/RpcConfiguration.java` | system API class 注册切换为 remote client class |

### 5.5 IoT

| 文件 | 改动 |
|---|---|
| `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/framework/rpc/config/RpcConfiguration.java` | `AdminUserApi`、`SmsSendApi`、`MailSendApi`、`NotifyMessageSendApi` 切换为对应 remote client |

### 5.6 Mall Promotion

| 文件 | 改动 |
|---|---|
| `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/framework/rpc/config/RpcConfiguration.java` | `AdminUserApi`、`SocialClientApi` 切换为对应 remote client |

### 5.7 Mall Trade

| 文件 | 改动 |
|---|---|
| `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java` | `AdminUserApi`、`NotifyMessageSendApi`、`SocialClientApi`、`SocialUserApi` 切换为对应 remote client |

### 5.8 Member

| 文件 | 改动 |
|---|---|
| `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/framework/rpc/config/RpcConfiguration.java` | system API class 注册切换为 remote client class |

### 5.9 MES

| 文件 | 改动 |
|---|---|
| `develop-module-mes/develop-module-mes-server/src/main/java/com/develop/mvp/pk/module/mes/framework/rpc/config/RpcConfiguration.java` | system API class 注册切换为 remote client class |

### 5.10 Pay

| 文件 | 改动 |
|---|---|
| `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/rpc/config/RpcConfiguration.java` | `SocialClientApi` 切换为 `SocialClientRemoteClient` |

### 5.11 WMS

| 文件 | 改动 |
|---|---|
| `develop-module-wms/develop-module-wms-server/src/main/java/com/develop/mvp/pk/module/wms/framework/rpc/config/RpcConfiguration.java` | system API class 注册切换为 remote client class |

## 6. 本地/远程闭环改造

### 6.1 新增切换开关

在 `develop-server/src/main/resources/application.yaml` 中新增 system provider 远程注册开关：

```yaml
develop:
  rpc:
    remote:
      system:
        enabled: false
```

语义：

- `false`：不注册 system remote client，单体 `develop-server` 依赖 classpath 中的 system-server 本地 `XxxApiImpl` Bean。
- `true` 或未配置：注册 system remote client，独立模块服务和微服务模式默认仍走远程 RPC。

### 6.2 条件注册方式

消费者模块使用 Spring Boot 原生条件注解控制 system remote client 注册：

```java
@ConditionalOnProperty(prefix = "develop.rpc.remote.system", name = "enabled", havingValue = "true", matchIfMissing = true)
```

`matchIfMissing = true` 保证未配置该属性的独立模块服务保持原远程调用默认行为。

### 6.3 system-only RpcConfiguration

以下配置只注册 system remote client，因此直接在整个 `RpcConfiguration` 类上增加条件：

| 文件 | 闭环改动 |
|---|---|
| `develop-module-erp/develop-module-erp-server/src/main/java/com/develop/mvp/pk/module/erp/framework/rpc/config/RpcConfiguration.java` | 整个配置受 `develop.rpc.remote.system.enabled` 控制 |
| `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/framework/rpc/config/RpcConfiguration.java` | 整个配置受 `develop.rpc.remote.system.enabled` 控制 |
| `develop-module-member/develop-module-member-server/src/main/java/com/develop/mvp/pk/module/member/framework/rpc/config/RpcConfiguration.java` | 整个配置受 `develop.rpc.remote.system.enabled` 控制 |
| `develop-module-mes/develop-module-mes-server/src/main/java/com/develop/mvp/pk/module/mes/framework/rpc/config/RpcConfiguration.java` | 整个配置受 `develop.rpc.remote.system.enabled` 控制 |
| `develop-module-pay/develop-module-pay-server/src/main/java/com/develop/mvp/pk/module/pay/framework/rpc/config/RpcConfiguration.java` | 整个配置受 `develop.rpc.remote.system.enabled` 控制 |
| `develop-module-wms/develop-module-wms-server/src/main/java/com/develop/mvp/pk/module/wms/framework/rpc/config/RpcConfiguration.java` | 整个配置受 `develop.rpc.remote.system.enabled` 控制 |

### 6.4 mixed RpcConfiguration

以下配置同时注册 system 和非 system 远程 client，因此不能条件化整个类，只将 system remote client 移入内部配置类 `SystemRemoteRpcConfiguration`：

| 文件 | 外层保留 | 内部条件配置注册 |
|---|---|---|
| `develop-module-ai/develop-module-ai-server/src/main/java/com/develop/mvp/pk/module/ai/framework/rpc/config/RpcConfiguration.java` | `FileApi` | `AdminUserRemoteClient` |
| `develop-module-bpm/develop-module-bpm-server/src/main/java/com/develop/mvp/pk/module/bpm/framework/rpc/config/RpcConfiguration.java` | CRM 状态 listener Bean | `RoleRemoteClient`、`DeptRemoteClient`、`PostRemoteClient`、`AdminUserRemoteClient`、`SmsSendRemoteClient`、`DictDataRemoteClient`、`PermissionRemoteClient` |
| `develop-module-crm/develop-module-crm-server/src/main/java/com/develop/mvp/pk/module/crm/framework/rpc/config/RpcConfiguration.java` | `BpmProcessInstanceApi` | `AdminUserRemoteClient`、`DeptRemoteClient`、`PostRemoteClient`、`OperateLogRemoteClient` |
| `develop-module-mall/develop-module-promotion-server/src/main/java/com/develop/mvp/pk/module/promotion/framework/rpc/config/RpcConfiguration.java` | product/member/trade/infra 远程 client | `AdminUserRemoteClient`、`SocialClientRemoteClient` |
| `develop-module-mall/develop-module-trade-server/src/main/java/com/develop/mvp/pk/module/trade/framework/rpc/config/RpcConfiguration.java` | promotion/member/product/pay 远程 client | `AdminUserRemoteClient`、`NotifyMessageSendRemoteClient`、`SocialClientRemoteClient`、`SocialUserRemoteClient` |

## 7. 迁移计划文件

新增：

```text
docs/superpowers/plans/2026-05-23-system-api-remote-adapters.md
```

用途：记录本次批量迁移计划、范围、验证命令和检查项。

## 8. 未包含在提交 `42641e5` 中的改动

以下文件在工作区中仍有改动，但**没有包含进提交 `42641e5`**。这些改动只是作者或 OpenAPI 示例文案变化，不属于本次 remote adapter 架构迁移。

| 文件 | 未提交改动类型 |
|---|---|
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/dept/dto/PostRespDTO.java` | `@author` 文案变化 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/logger/dto/OperateLogRespDTO.java` | Swagger 示例文案变化 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/mail/dto/MailSendSingleToUserReqDTO.java` | `@author` 文案变化 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/social/dto/SocialUserRespDTO.java` | Swagger 示例文案变化 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/social/dto/SocialWxaOrderNotifyConfirmReceiveReqDTO.java` | `@author` 文案变化 |
| `develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api/social/dto/SocialWxaOrderUploadShippingInfoReqDTO.java` | `@author` 文案变化 |

## 9. 验证结果

### 9.1 system API 编译

命令：

```bash
mvn compile -pl develop-module-system/develop-module-system-api -am
```

结果：

```text
BUILD SUCCESS
```

### 9.2 旧 Feign 注册残留检查

命令：

```bash
python3 - <<'PY'
from pathlib import Path
names = ['AdminUserApi','DeptApi','PostApi','RoleApi','SmsSendApi','SmsCodeApi','LoginLogApi','SocialUserApi','SocialClientApi','PermissionApi','OperateLogApi','MailSendApi','NotifyMessageSendApi']
for path in Path('.').glob('develop-module-*/**/*.java'):
    if 'target' in path.parts:
        continue
    text = path.read_text(errors='ignore')
    if '@EnableFeignClients' in text:
        for name in names:
            if name + '.class' in text:
                print(f'{path}: stale {name}.class')
PY
```

结果：无输出，表示未发现这些已迁移 system API 契约接口仍被 `@EnableFeignClients` 直接注册。

### 9.3 system API 契约中 Feign 注解残留检查

命令：

```bash
grep -R "org.springframework.cloud.openfeign.FeignClient\|@FeignClient" -n develop-module-system/develop-module-system-api/src/main/java/com/develop/mvp/pk/module/system/api --include='*.java' --exclude-dir=target | grep -v '/remote/'
```

结果：无输出，表示 system API 契约接口中未发现 `@FeignClient` 残留；`@FeignClient` 只保留在 `remote` 子包的 remote client 中。

### 9.4 本地/远程闭环编译验证

代表性闭环验证命令：

```bash
mvn compile -pl develop-server -am && \
mvn compile -pl develop-module-bpm/develop-module-bpm-server -am && \
mvn compile -pl develop-module-mall/develop-module-trade-server -am && \
mvn compile -pl develop-module-iot/develop-module-iot-server -am
```

结果：4 个 Maven compile 均为 `BUILD SUCCESS`。

补充覆盖所有本轮修改过的剩余模块配置：

```bash
mvn compile -pl develop-module-ai/develop-module-ai-server -am && \
mvn compile -pl develop-module-crm/develop-module-crm-server -am && \
mvn compile -pl develop-module-erp/develop-module-erp-server -am && \
mvn compile -pl develop-module-member/develop-module-member-server -am && \
mvn compile -pl develop-module-mes/develop-module-mes-server -am && \
mvn compile -pl develop-module-pay/develop-module-pay-server -am && \
mvn compile -pl develop-module-wms/develop-module-wms-server -am && \
mvn compile -pl develop-module-mall/develop-module-promotion-server -am
```

结果：8 个 Maven compile 均为 `BUILD SUCCESS`。

### 9.5 system remote client 业务注入残留检查

命令：

```bash
grep -R "module.system.api.*remote.*RemoteClient" -n develop-module-* --include='*.java' --exclude-dir=target \
  | grep -v "/framework/rpc/config/" \
  | grep -v "/api/.*/remote/"
```

结果：无输出，表示除 remote client 定义和 RPC 配置注册外，未发现业务代码直接依赖 system remote client。

## 10. 影响范围总结

本轮改造改变的是 **RPC 注册边界**，不是业务调用契约：

- 调用方业务代码仍应依赖 `XxxApi`。
- 只有远程调用注册点使用 `XxxRemoteClient`。
- `XxxApiImpl` 等本地实现保持原有职责。
- 后续如果继续迁移其他模块 API，应复用同样结构：

```text
api/<area>/XxxApi.java                 # 契约，不带 @FeignClient
api/<area>/remote/XxxRemoteClient.java # 远程适配器，带 @FeignClient，extends XxxApi
server/.../XxxApiImpl.java             # 本地/服务端实现，implements XxxApi
```
