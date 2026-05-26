---
name: aggregate-root-iot-command-skill
description: Use when creating, auditing, or refactoring the IoT command aggregate in develop-module-iot-server.
---

# IoT Command Aggregate Skill

## Overview

IoT Command 聚合在当前代码中尚未以独立 `iot_command` 表或独立聚合存在，现有命令/服务调用/ACK 语义主要由 `IotDeviceMessage`、`IotDeviceMessageServiceImpl`、TDengine 消息日志、`requestId`、reply message、`serverId` 路由和 core MQ producer 承载。本 skill 的目标是在后续引入 Command 聚合时保持现有设备消息契约，不凭空替换或破坏现有 message/reply 行为。

## When to Use

- 新增或迁移设备服务调用、命令下发、命令 ACK、命令状态查询、超时/失败状态机时使用。
- 将当前 `IotDeviceMessageServiceImpl.sendDeviceMessage` 和 reply handling 拆分出 Command 聚合时使用。
- 创建 `application/command`、`domain/command`、`infrastructure/command` 标准骨架时使用。
- 审计下行消息 `serverId` 路由、`requestId` 关联、reply 禁用方法、TDengine 消息日志时使用。

## When Not to Use

- 只处理普通属性上报、事件上报、设备状态上报而不引入命令状态机时不用本 skill。
- 只修改 Gateway 协议编解码且不改变 server command/reply 语义时不用本 skill。
- 需要新增数据库表、改变 MQ topic 或改变 codec 字段前，必须先写数据/API 迁移计划。

## Reproducibility Contract

1. 必须先承认当前没有独立 Command 聚合；当前事实源是 Device Message 链路。
2. 不得凭计划中的理想状态创建与现有 `IotDeviceMessage` 契约冲突的命令字段。
3. `requestId` 是请求/回复关联关键字段；`identifier` 当前由 `IotDeviceMessageUtils.getIdentifier(message)` 从 method/params 解析，不是所有消息的顶层必填字段。
4. `serverId` 缺失时下行会抛 `DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL`，不得静默降级。
5. 当前代码与本文档冲突时，停止实现，先修订 skill。

## AI Execution Contract

| Item | Contract |
|---|---|
| Scope | 仅处理 IoT command/service-invoke 概念：下行消息、reply/ACK、requestId 关联、状态机、命令日志、Gateway 路由和 Command API。 |
| Must Read | `IotDeviceMessageService.java`、`IotDeviceMessageServiceImpl.java`、`IotDeviceMessageDO.java`、core `IotDeviceMessage.java`、`IotDeviceMessageUtils.java`、`IotDeviceMessageMethodEnum.java`、`IotDeviceMessageProducer.java`、`IotDeviceMessageSubscriber.java`、`IotDeviceMessageController.java`、message VO、`ErrorCodeConstants.java`。 |
| Must Preserve | `IotDeviceMessage` codec 字段：`requestId/method/params/data/code/msg`；`id/reportTime/deviceId/tenantId/serverId` 后端字段；replyOf 语义；reply-disabled 方法；TDengine message log；serverId 路由失败错误码。 |
| Allowed Changes | `domain/command/**`、`application/command/**`、`infrastructure/command/**`、message/command convert、Command API 契约、Command tests、MQ consumer 调 inbound port。 |
| Forbidden Changes | 禁止删除或重命名现有 `IotDeviceMessage` 字段；禁止把 ACK 强行改成独立不兼容 DTO；禁止 Gateway 直接更新 DB 最终状态；禁止把 `STATE_UPDATE`、`OTA_PROGRESS` 等禁用回复方法改为必须 reply。 |
| Dependency Rules | Domain command 不依赖 Spring/MQ/TDengine/Controller VO/DO；Application 负责编排状态机、设备查询、消息端口；Infrastructure 适配 TDengine/Mapper/MQ producer；MQ/Controller/Gateway 只调用 inbound port 或 message port。 |
| Verification Gate | 至少运行 Command/Message 测试、DeviceMessage Controller 测试、core test、IoT server compile；新增状态机必须先写失败测试。 |
| Stop Conditions | 现有 reply 语义无法映射、需要改 MQ topic 或 codec 字段、requestId/identifier/serverId 语义不清、TDengine 表结构需要变更、验证失败时停止。 |

## Current Source Anchors

| Layer | Current Path |
|---|---|
| Message Controller | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/device/IotDeviceMessageController.java` |
| Message VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/device/vo/message/IotDeviceMessageSendReqVO.java` |
| Message VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/device/vo/message/IotDeviceMessageRespVO.java` |
| Message DO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/dataobject/device/IotDeviceMessageDO.java` |
| TDengine Mapper | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/tdengine/IotDeviceMessageMapper.java` |
| Service | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/device/message/IotDeviceMessageService.java` |
| Service Impl | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/device/message/IotDeviceMessageServiceImpl.java` |
| MQ Subscriber | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/mq/consumer/device/IotDeviceMessageSubscriber.java` |
| Core Message | `develop-module-iot/develop-module-iot-core/src/main/java/com/develop/mvp/pk/module/iot/core/mq/message/IotDeviceMessage.java` |
| Core Producer | `develop-module-iot/develop-module-iot-core/src/main/java/com/develop/mvp/pk/module/iot/core/mq/producer/IotDeviceMessageProducer.java` |
| Core Utils | `develop-module-iot/develop-module-iot-core/src/main/java/com/develop/mvp/pk/module/iot/core/util/IotDeviceMessageUtils.java` |
| Core Method Enum | `develop-module-iot/develop-module-iot-core/src/main/java/com/develop/mvp/pk/module/iot/core/enums/IotDeviceMessageMethodEnum.java` |
| Device Service | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/device/IotDeviceService.java` |
| ErrorCode | `develop-module-iot/develop-module-iot-api/src/main/java/com/develop/mvp/pk/module/iot/enums/ErrorCodeConstants.java` |
| Tests | `develop-module-iot/develop-module-iot-server/src/test` 和 `develop-module-iot/develop-module-iot-core/src/test`，若无 Command/Message 测试，先创建最小测试。 |

## Standard Skeleton Contract

如果任务明确要求引入独立 Command 聚合，必须创建：

```text
domain/command/model/IotDeviceCommand.java
domain/command/valueobject/IotCommandId.java
domain/command/valueobject/IotCommandRequestId.java
domain/command/valueobject/IotCommandStatus.java
domain/command/valueobject/IotCommandPayload.java
domain/command/event/IotCommandCreatedEvent.java
domain/command/event/IotCommandSentEvent.java
domain/command/event/IotCommandAckedEvent.java
domain/command/event/IotCommandFailedEvent.java
domain/command/service/IotCommandStatePolicy.java
domain/command/repository/IotDeviceCommandRepository.java
application/command/command/CreateIotDeviceCommand.java
application/command/command/AckIotDeviceCommand.java
application/command/command/MarkIotDeviceCommandTimeout.java
application/command/query/IotDeviceCommandQuery.java
application/command/result/IotDeviceCommandResult.java
application/command/port/inbound/IotDeviceCommandUseCase.java
application/command/port/outbound/IotDeviceCommandMessagePort.java
application/command/service/IotDeviceCommandApplicationService.java
infrastructure/command/persistence/IotDeviceCommandRepositoryImpl.java
infrastructure/command/messaging/IotDeviceCommandMessageAdapter.java
infrastructure/command/external/package-info.java
infrastructure/command/rpc/package-info.java
infrastructure/command/cache/package-info.java
```

若当前任务只是收口现有 Device Message，不得空造数据库状态机；先以 message inbound port 和 adapter 隔离现有行为。

## Fixed Data Model

### Current Message Fields

| Field | Current Type | Meaning | Nullable / Default | Mapping |
|---|---|---|---|---|
| `id` | `String` | 消息编号 | `IotDeviceMessageUtils.generateMessageId()` | Message id / command message id |
| `reportTime` | `LocalDateTime` / stored `Long` | 上报/发送时间 | append 时设置 now | Command sent/reported time |
| `ts` | `Long` | TDengine 存储时间戳 | 写日志时默认 current millis | Infrastructure only |
| `deviceId` | `Long` | 设备编号 | append 或 requestOf 设置 | Device reference |
| `tenantId` | `Long` | 租户编号 | 来自设备或 message | Tenant context |
| `serverId` | `String` | 网关服务标识 | 下行 push 必需 | Gateway route key |
| `upstream` | `Boolean` | 是否上行 | 写日志时计算 | Message log |
| `reply` | `Boolean` | 是否回复 | 写日志时计算 | ACK/reply marker |
| `identifier` | `String` | 服务/事件标识符 | 从 message 解析 | Command identifier if present |
| `requestId` | `String` | 请求编号 | 空时用 message id | Command correlation id |
| `method` | `String` | 设备消息方法 | 必填 | Command method |
| `params` | `Object` | 请求参数 | 可空 | Command payload |
| `data` | `Object` | 响应数据 | reply 时使用 | ACK data |
| `code` | `Integer` | 响应错误码 | reply 错误时使用 | ACK code |
| `msg` | `String` | 响应提示 | reply 错误时使用 | ACK message |

### Future Command Fields

| Field | Source | Rule |
|---|---|---|
| commandId | new or mapped from message id | 不得替代现有 requestId 关联。 |
| requestId | existing message field | 必须作为 reply/ACK 关联键。 |
| status | new state machine | 必须可由 sent/reply/timeout/failure 推导，不能破坏现有 message log。 |
| productKey/deviceName | device identity | 可冗余，但不能替代 deviceId 和 existing message fields。 |
| payload | params/data | 保持 JSON 兼容。 |

## Method Signatures

### Domain

```java
public final class IotDeviceCommand {
    public static IotDeviceCommand create(Long deviceId, String requestId, String method, Object params);
    public void markPending();
    public void markSent(String messageId, String serverId);
    public void ack(Object data, Integer code, String msg);
    public void fail(Integer code, String msg);
    public void timeout();
    public boolean isFinalState();
}
```

### Repository

```java
public interface IotDeviceCommandRepository {
    IotDeviceCommand findById(IotCommandId id);
    IotDeviceCommand findByRequestId(Long deviceId, String requestId);
    PageResult<IotDeviceCommand> findPage(IotDeviceCommandQuery query);
    void save(IotDeviceCommand command);
}
```

### Application / Inbound Port

```java
public interface IotDeviceCommandUseCase {
    IotDeviceCommandResult createCommand(CreateIotDeviceCommand command);
    void handleAck(AckIotDeviceCommand command);
    void markTimeout(MarkIotDeviceCommandTimeout command);
    IotDeviceCommandResult getCommand(Long commandId);
    PageResult<IotDeviceCommandResult> getCommandPage(IotDeviceCommandQuery query);
}
```

### Message Port

```java
public interface IotDeviceCommandMessagePort {
    IotDeviceMessage sendToDevice(Long deviceId, String method, Object params, String requestId);
    void recordReply(Long deviceId, String requestId, Object data, Integer code, String msg);
}
```

## Business Rules

| ID | Rule | Layer | Verification |
|---|---|---|---|
| C-BR-001 | 下行消息发送前必须补齐 deviceId、tenantId、requestId、reportTime 和 message id。 | Application/Message Port | Send command test |
| C-BR-002 | requestId 空时当前 append 逻辑使用 message id。 | Message Port | Missing requestId test |
| C-BR-003 | 下行 push 需要 serverId；缺失时抛 `DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL`。 | Message Port | Missing serverId test |
| C-BR-004 | 下行消息发送到 gateway 后必须记录消息日志。 | Infrastructure | Log test |
| C-BR-005 | 上行 reply 不得再次 reply，reply-disabled method 不得回复。 | Application/Message Handling | Reply-disabled tests |
| C-BR-006 | 上行业务异常应生成 reply code/msg，非业务异常按当前行为抛出。 | Application | ServiceException reply test |
| C-BR-007 | ACK/reply 按 deviceId + requestId 关联，重复 ACK 应保持最终状态稳定。 | Domain/Application | Duplicate ACK idempotency test |
| C-BR-008 | `STATE_UPDATE` 和 `OTA_PROGRESS` 当前属于禁用回复方法，不得强制命令 ACK。 | Message Handling | No reply test |
| C-BR-009 | `identifier` 从 method/params 解析并存入日志，不能要求所有 command 顶层都有 identifier。 | Infrastructure | Identifier extraction test |
| C-BR-010 | Gateway 只转发和翻译 ACK，不直接裁决 DB 最终状态。 | Gateway/Application boundary | Architecture test |

## Error Code Contract

| Scenario | ErrorCodeConstants | Parameters | Throwing Layer |
|---|---|---|---|
| Device missing before send | `DEVICE_NOT_EXISTS` | none | Device collaborator / Application |
| Downstream serverId missing | `DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL` | none | Message Port/Application |
| Upstream service exception | original `ServiceException.code/message` | original | Reply message construction |
| Command not found | no current dedicated code | Stop and add explicit migration plan before inventing code | Application |
| Command timeout/failure | no current dedicated code | Stop and define contract with tests before adding public API | Application |

不得为了 Command 聚合擅自新增对外错误码并混入 DDD 重构；新增错误码需要 API 迁移说明。

## Transaction Contract

| Use Case | Current Transaction | Required Contract |
|---|---|---|
| sendDeviceMessage | no explicit transaction | 补消息字段、路由、发送 MQ、记录下行日志保持当前顺序。 |
| handleUpstreamDeviceMessage | no explicit transaction | 处理业务、记录日志、必要时 reply；业务异常转 reply，非业务异常抛出。 |
| createDeviceLogAsync | `@Async` | 异步写 TDengine 日志，异常只记录不影响主流程。 |
| future createCommand | should be transactional | 命令创建、状态、消息发送意图需同一应用事务或明确 outbox。 |
| future ackCommand | should be transactional | ACK 幂等检查和状态更新同一事务。 |

## Integration Contract

- Core MQ：使用 `IotDeviceMessageProducer.sendDeviceMessage` 和 `sendDeviceMessageToGateway(serverId, message)`。
- Gateway：通过 `serverId` 接收下行消息；HTTP/PULL 模型在当前代码中没有完整实现，不得假装已实现。
- TDengine：消息日志由 `IotDeviceMessageMapper` 写入，查询分页遇到 `Table does not exist` 当前返回空页。
- Device：发送消息前通过 `IotDeviceService.validateDeviceExists(deviceId)` 获取设备和租户信息。
- Property/OTA/Topo：上行处理当前仍委托 `IotDevicePropertyService`、`IotOtaTaskRecordService`、`IotDeviceService`。
- Tenant：消息携带 `tenantId`；subscriber 若使用租户上下文，必须保留现有 `TenantUtils.execute(...)` 语义。

## Mapping Rules

| Mapping | Rule |
|---|---|
| `IotDeviceMessageSendReqVO -> IotDeviceMessage` | Controller/Convert 层完成，保留 method/params/requestId。 |
| `IotDeviceMessage -> Command` | Application adapter 可映射，但不得丢失 requestId/serverId/code/msg/data。 |
| `Command ACK -> IotDeviceMessage.replyOf` | 必须兼容当前 reply message 字段。 |
| `IotDeviceMessage -> IotDeviceMessageDO` | Infrastructure log adapter 完成，upstream/reply/identifier 通过 utils 计算。 |
| `IotDeviceMessageDO -> RespVO` | Controller convert 完成，禁止 Domain 依赖 VO。 |

## Acceptance Criteria

- 架构 AC：如引入 Command 聚合，标准骨架完整；Domain 不依赖 Spring、MQ、TDengine、DO、VO、Mapper。
- 业务 AC：下行发送、serverId 路由、requestId 关联、reply/ACK、reply-disabled 方法、消息日志行为与迁移前一致。
- 契约 AC：`IotDeviceMessage` 字段、MQ 主题、Controller VO、错误码不变。
- 编译 AC：Core test、Command/Message tests、IoT server compile 成功。

## Verification Commands

```bash
grep -RInE "org\.springframework|Mapper|RedisTemplate|RabbitTemplate|RocketMQ|KafkaTemplate|HttpServlet|controller\.admin|dal\.dataobject|core\.mq" develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/domain/command
mvn -f develop-module-iot/pom.xml -pl develop-module-iot-core test
mvn -f develop-module-iot/pom.xml -pl develop-module-iot-server test -Dtest=*Command*Test,*DeviceMessage*Test,*Message*Controller*Test
mvn -f develop-module-iot/pom.xml -pl develop-module-iot-server compile -DskipTests
```

## Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 命令状态不变量 | `domain/command/model`、`domain/command/service` | Controller、Gateway |
| 命令用例编排 | `application/command/service` | Domain、MQ consumer |
| 下行消息发送端口 | `application/command/port/outbound` | Domain 直接调 producer |
| MQ producer 适配 | `infrastructure/command/messaging` | Domain、Controller |
| TDengine 日志适配 | `infrastructure/command/persistence` 或 message infrastructure | Domain |
| Gateway ACK 转换 | Gateway adapter 调 server/core message port | Gateway 写业务 DB |
| request/reply 查询 | Application query + repository | Controller 直接查 Mapper |

## Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| 假设已有 Command 表 | 实现脱离当前代码 | 先以 DeviceMessage 为事实源 |
| 用 commandId 替代 requestId | reply 关联断裂 | 保留 requestId 作为关联键 |
| 缺 serverId 时静默入队 | 当前错误语义回归 | 保留 `DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL` |
| 所有上行都 reply | `STATE_UPDATE`/`OTA_PROGRESS` 行为回归 | 使用 `IotDeviceMessageMethodEnum.isReplyDisabled` |
| ACK DTO 不兼容 `replyOf` | Gateway/server 协议断裂 | ACK 映射回 `data/code/msg/requestId` |
| Domain 直接依赖 `IotDeviceMessage` | 领域层污染 | Application adapter 转换 |

## Rationalization Table

| Pressure Scenario | Likely Bad Shortcut | Required Response |
|---|---|---|
| 计划要求 Command 聚合 | 直接造全新表和 DTO | 先冻结现有 DeviceMessage 行为，再逐步引入 |
| 觉得 requestId 不像命令 ID | 改用 commandId 对外关联 | requestId 是现有 reply 关联契约，必须保留 |
| Gateway 实现方便 | Gateway 直接更新 command DB | Gateway 只转发，最终状态由 server application 裁决 |
| 本地没有 TDengine | 删除消息日志 | 保留 adapter 和测试替身，不删契约 |
| reply-disabled 看起来特殊 | 删除特例统一 reply | 保留现有枚举语义 |

## Red Flags

- Command skill 或实现未读取 `IotDeviceMessageServiceImpl.java` 和 core `IotDeviceMessage.java`。
- 新 Command DTO 删除或重命名 `requestId/method/params/data/code/msg`。
- 缺 serverId 时不再抛 `DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL`。
- `STATE_UPDATE`、`OTA_PROGRESS` 等禁用回复方法开始产生 reply。
- Gateway 直接写 server DB 或 Mapper。
- Domain 依赖 MQ producer、TDengine Mapper、DO、VO 或 Spring。

## Rollback Conditions

1. Core 或 server 编译失败。
2. `IotDeviceMessage` codec 字段或 MQ 契约变化。
3. requestId/reply 关联行为变化。
4. serverId 路由错误语义变化。
5. TDengine 消息日志写入或查询行为回归。
6. reply-disabled 方法行为回归。
7. Domain 依赖技术框架或消息实现。

## AI Self-Check

- [ ] 已读取 DeviceMessage Controller、VO、DO、Service、Impl、MQ Subscriber、Core Message、Producer、Utils、MethodEnum、ErrorCode。
- [ ] 已确认当前没有独立 Command 聚合并以 DeviceMessage 为事实源。
- [ ] 已保留 requestId、method、params、data、code、msg 字段语义。
- [ ] 已保留 serverId 下行路由和缺失错误码。
- [ ] 已保留 reply-disabled 方法语义。
- [ ] 已保留 TDengine 消息日志行为。
- [ ] 已执行 core test、Command/Message 测试和 IoT server compile。
