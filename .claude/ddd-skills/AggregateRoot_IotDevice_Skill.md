---
name: aggregate-root-iot-device-skill
description: Use when creating, auditing, or refactoring the IoT device aggregate in develop-module-iot-server.
---

# IoT Device Aggregate Skill

## Overview

IoT Device 聚合负责设备实例、设备密钥、设备状态、网关/子设备拓扑、分组、固件、定位、动态注册、认证和缓存边界，复现目标是在保持现有后台 API、core API、设备认证、MQ 消息和租户缓存语义不变的前提下，把已有 device DDD 起点和旧 `service/device` 行为收口到标准 DDD/六边形结构。

## When to Use

- 修改设备创建、更新、删除、批量删除、状态、导入、认证、动态注册、拓扑、网关绑定、定位或固件逻辑时使用。
- 迁移 `IotDeviceServiceImpl`、`IotDeviceApplicationService`、`IotDeviceRepositoryImpl` 到标准 `application/device/service` 和 `infrastructure/device/persistence` 时使用。
- 创建或标准化 device `CommonApi`、DTO、local/remote 适配时使用。
- 审计设备缓存、租户忽略、网关下线联动、设备名/序列号唯一性时使用。

## When Not to Use

- 只修改 Product、ThingModel、Property 存储、Device Message 或 Command ACK 时不用本 skill。
- 只修改网关协议编解码且不改变设备认证/状态/拓扑语义时不用本 skill。
- 需要改变 core API `com.develop.mvp.pk.module.iot.core.biz.IotDeviceCommonApi` 契约时，先写单独 API 迁移计划。

## Reproducibility Contract

1. 写代码前必须读取 Current Source Anchors 中 Device 事实源，不得按通用 IoT 设备 CRUD 猜测行为。
2. 当前设备逻辑同时存在 DDD 起点和旧 service；迁移时以现有外部行为为准，不以新 DDD 类覆盖旧行为。
3. 设备名唯一、序列号全局唯一、网关拓扑、认证、动态注册、租户缓存均是生产契约。
4. 当前可编译代码与本 skill 冲突时，停止实现，先修订 skill。
5. 每次只迁移 Device 聚合；DeviceGroup、Property、Message、Modbus、OTA 仅作为协作边界。

## AI Execution Contract

| Item | Contract |
|---|---|
| Scope | 仅处理 IoT Device 聚合：设备实例、状态、密钥、缓存、导入、认证、动态注册、网关/子设备拓扑、固件、定位和设备 API。 |
| Must Read | `IotDeviceController.java`、device VO、`IotDeviceDO.java`、`IotDeviceMapper.java`、`IotDeviceService.java`、`IotDeviceServiceImpl.java`、`IotDeviceApplicationService.java`、`IotDevice.java`、`IotDeviceRepository.java`、`IotDeviceRepositoryImpl.java`、`IoTDeviceApiImpl.java`、core `IotDeviceCommonApi.java`、`ErrorCodeConstants.java`。 |
| Must Preserve | `/admin-api/iot/device/**` 路径、HTTP 方法、权限、Excel 导入导出、core API 方法签名、设备认证签名语义、动态注册、拓扑消息语义、缓存 `@TenantIgnore`、网关下线联动子设备下线。 |
| Allowed Changes | `domain/device/**`、`application/device/**`、`infrastructure/device/**`、device convert、Device Controller/API 注入 inbound port、device tests。 |
| Forbidden Changes | 禁止删除旧 core API；禁止把新核心业务写回 `service/device`；禁止让 Domain 依赖 Spring/MyBatis/Redis/MQ/Controller VO/DO；禁止改变设备名唯一范围、序列号唯一范围、认证算法、动态注册 product secret 校验。 |
| Dependency Rules | Domain 只表达设备状态和拓扑不变量；Application 编排 Product、Group、Message、Property、OTA 协作；Infrastructure 适配 Mapper/DO/Redis/MQ；Controller/API/MQ 只能调用 inbound port 或兼容壳。 |
| Verification Gate | 至少运行 Device 测试、Device Controller/API 测试、domain 纯净 grep、IoT server compile；认证/拓扑路径需补行为测试。 |
| Stop Conditions | 认证签名或动态注册语义不清、租户缓存边界不清、网关拓扑规则不清、core API 需要改签名、Excel 导入导出契约可能变化、验证失败时停止。 |

## Current Source Anchors

| Layer | Current Path |
|---|---|
| Admin Controller | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/device/IotDeviceController.java` |
| Controller HTTP | No dedicated `IotDeviceController.http` is present in current code; use `IotDeviceController.java` and device tests as the Device API facts. |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/device/vo/device/IotDeviceSaveReqVO.java` |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/device/vo/device/IotDeviceRespVO.java` |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/device/vo/device/IotDevicePageReqVO.java` |
| VO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/controller/admin/device/vo/device/IotDeviceImportExcelVO.java` |
| DO | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/dataobject/device/IotDeviceDO.java` |
| Mapper | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/dal/mysql/device/IotDeviceMapper.java` |
| Legacy Service | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/device/IotDeviceService.java` |
| Legacy Impl | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/service/device/IotDeviceServiceImpl.java` |
| Application | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/application/device/IotDeviceApplicationService.java` |
| Domain | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/domain/device/IotDevice.java` |
| Domain Factory | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/domain/device/IotDeviceFactory.java` |
| Domain Repository | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/domain/device/repository/IotDeviceRepository.java` |
| Infrastructure | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/infrastructure/device/IotDeviceRepositoryImpl.java` |
| API Impl | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/api/device/IoTDeviceApiImpl.java` |
| Core API | `develop-module-iot/develop-module-iot-core/src/main/java/com/develop/mvp/pk/module/iot/core/biz/IotDeviceCommonApi.java` |
| Message Subscriber | `develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/mq/consumer/device/IotDeviceMessageSubscriber.java` |
| ErrorCode | `develop-module-iot/develop-module-iot-api/src/main/java/com/develop/mvp/pk/module/iot/enums/ErrorCodeConstants.java` |
| Tests | `develop-module-iot/develop-module-iot-server/src/test`，若无 Device 专用测试，迁移前创建最小测试。 |

## Standard Skeleton Contract

必须创建或收口到以下结构：

```text
domain/device/model/IotDevice.java
domain/device/valueobject/IotDeviceId.java
domain/device/valueobject/IotDeviceName.java
domain/device/valueobject/IotDeviceSecret.java
domain/device/valueobject/IotDeviceState.java
domain/device/valueobject/IotDeviceIdentity.java
domain/device/event/IotDeviceCreatedEvent.java
domain/device/event/IotDeviceStateChangedEvent.java
domain/device/event/IotDeviceDeletedEvent.java
domain/device/service/IotDevicePolicy.java
domain/device/repository/IotDeviceRepository.java
application/device/command/CreateIotDeviceCommand.java
application/device/command/UpdateIotDeviceCommand.java
application/device/command/UpdateIotDeviceStateCommand.java
application/device/command/BindIotDeviceGatewayCommand.java
application/device/command/RegisterIotDeviceCommand.java
application/device/query/IotDevicePageQuery.java
application/device/result/IotDeviceResult.java
application/device/result/IotDeviceAuthInfoResult.java
application/device/port/inbound/IotDeviceUseCase.java
application/device/port/outbound/IotDeviceProductPort.java
application/device/port/outbound/IotDeviceGroupPort.java
application/device/port/outbound/IotDeviceMessagePort.java
application/device/service/IotDeviceApplicationService.java
infrastructure/device/persistence/IotDeviceRepositoryImpl.java
infrastructure/device/external/package-info.java
infrastructure/device/rpc/package-info.java
infrastructure/device/cache/package-info.java
infrastructure/device/messaging/package-info.java
```

现有 `domain/device/IotDevice.java` 可迁移到 `domain/device/model/IotDevice.java` 或保留兼容壳，但标准骨架必须完整。

## Fixed Data Model

| Field | Current Type | Meaning | Nullable / Default | Mapping |
|---|---|---|---|---|
| `id` | `Long` | 设备 ID | DB generated | `IotDeviceId` |
| `deviceName` | `String` | 设备名称，产品内唯一 | 创建必填；更新不可变 | `IotDeviceName` |
| `nickname` | `String` | 备注名 | 可空 | Domain field |
| `serialNumber` | `String` | 设备序列号，全局唯一 | 可空；非空时唯一 | Domain field |
| `picUrl` | `String` | 设备图片 | 可空 | Domain field |
| `groupIds` | `Set<Long>` | 分组集合 | 可空/空集合 | Domain field |
| `productId` | `Long` | 产品 ID | 创建时来自 Product | Product reference |
| `productKey` | `String` | 产品标识冗余 | 创建时来自 Product | Identity |
| `deviceType` | `Integer` | 设备类型冗余 | 创建时来自 Product | Domain field |
| `gatewayId` | `Long` | 网关设备 ID | 子设备可填 | Gateway reference |
| `state` | `Integer` | 设备状态 | 创建默认 `INACTIVE` | `IotDeviceState` |
| `onlineTime` | `LocalDateTime` | 上线时间 | ONLINE 时更新 | Domain field |
| `offlineTime` | `LocalDateTime` | 离线时间 | OFFLINE 时更新 | Domain field |
| `activeTime` | `LocalDateTime` | 激活时间 | 首次 ONLINE 时设置 | Domain field |
| `firmwareId` | `Long` | 固件编号 | 可空 | Domain field |
| `deviceSecret` | `String` | 设备密钥 | 创建时 `IdUtil.fastSimpleUUID()` | `IotDeviceSecret` |
| `latitude` | `BigDecimal` | 纬度 | 可空 | Domain field |
| `longitude` | `BigDecimal` | 经度 | 可空 | Domain field |
| `config` | `String` | 设备配置 JSON | 可空 | Domain field |
| tenant fields | inherited | 租户隔离 | `TenantBaseDO` | Infrastructure only |

## Method Signatures

### Domain

```java
public final class IotDevice {
    public static IotDevice create(String deviceName, String nickname, String serialNumber,
                                   String picUrl, Set<Long> groupIds, Long productId,
                                   String productKey, Integer deviceType, Long gatewayId,
                                   IotDeviceSecret deviceSecret, String config,
                                   BigDecimal latitude, BigDecimal longitude);
    public void updateProfile(String nickname, String serialNumber, String picUrl,
                              Set<Long> groupIds, Long gatewayId, String config,
                              BigDecimal latitude, BigDecimal longitude);
    public void goOnline();
    public void goOffline();
    public void updateGroups(Set<Long> groupIds);
    public void updateFirmware(Long firmwareId);
    public void updateLocation(BigDecimal latitude, BigDecimal longitude);
    public void bindGateway(Long gatewayId);
    public void unbindGateway();
    public void markDeleted();
}
```

### Repository

```java
public interface IotDeviceRepository {
    IotDevice findById(IotDeviceId id);
    IotDevice findByProductKeyAndDeviceName(String productKey, String deviceName);
    IotDevice findBySerialNumber(String serialNumber);
    List<IotDevice> findByIds(Collection<Long> ids);
    List<IotDevice> findByProductId(Long productId);
    List<IotDevice> findByGatewayId(Long gatewayId);
    PageResult<IotDevice> findPage(IotDevicePageQuery query);
    long countByProductId(Long productId);
    long countByGroupId(Long groupId);
    void save(IotDevice device);
    void delete(IotDeviceId id);
    void deleteByIds(Collection<Long> ids);
    void evictDeviceCache(IotDevice device);
}
```

### Application / Inbound Port

```java
public interface IotDeviceUseCase {
    Long createDevice(CreateIotDeviceCommand command);
    void updateDevice(UpdateIotDeviceCommand command);
    void deleteDevice(Long id);
    void deleteDeviceList(Collection<Long> ids);
    void updateDeviceState(UpdateIotDeviceStateCommand command);
    void updateDeviceGroup(Collection<Long> ids, Set<Long> groupIds);
    void bindDeviceGateway(Collection<Long> subIds, Long gatewayId);
    void unbindDeviceGateway(Collection<Long> subIds, Long gatewayId);
    boolean authDevice(IotDeviceAuthReqDTO authReqDTO);
    IotDeviceRegisterRespDTO registerDevice(IotDeviceRegisterReqDTO command);
    IotDeviceResult getDevice(Long id);
    PageResult<IotDeviceResult> getDevicePage(IotDevicePageQuery query);
}
```

## Business Rules

| ID | Rule | Layer | Verification |
|---|---|---|---|
| D-BR-001 | 创建设备前产品必须存在，否则 `PRODUCT_NOT_EXISTS`。 | Application | Create missing product test |
| D-BR-002 | 设备名称在同一产品 `productKey` 下唯一，当前校验忽略租户。 | Application/Repository | Duplicate productKey+deviceName test expects `DEVICE_NAME_EXISTS` |
| D-BR-003 | 非空 `serialNumber` 全局唯一，更新时排除自身。 | Application | Duplicate serial test expects `DEVICE_SERIAL_NUMBER_EXISTS` |
| D-BR-004 | 创建设备从 Product 冗余 `productKey`、`deviceType`，生成 `deviceSecret`，默认 `INACTIVE`。 | Application/Domain Factory | Create test |
| D-BR-005 | 更新设备不得修改 `deviceName` 和 `productId`。 | Application | Update immutable fields test |
| D-BR-006 | 网关子设备绑定时，父设备必须存在且为网关。 | Application | Gateway validation tests |
| D-BR-007 | 删除网关设备前，如果存在绑定子设备，抛 `DEVICE_GATEWAY_HAS_SUB`。 | Application | Delete gateway with sub test |
| D-BR-008 | 设备 ONLINE 时更新 `onlineTime`，首次上线设置 `activeTime`；OFFLINE 时更新 `offlineTime`。 | Domain/Application | State transition test |
| D-BR-009 | 网关设备下线时，所有在线子设备联动下线，单个子设备失败只记录日志继续处理。 | Application | Gateway offline cascade test |
| D-BR-010 | 设备缓存 `DEVICE` 按 id 和 `productKey_deviceName` 两种 key，当前读取忽略租户。 | Infrastructure/Cache | Cache contract test/review |
| D-BR-011 | 导入设备空列表抛 `DEVICE_IMPORT_LIST_IS_EMPTY`，导入过程在事务内。 | Application | Import empty test |
| D-BR-012 | 动态注册必须校验产品开启注册和 product secret/sign，重复设备抛当前错误码。 | Application | Register tests |
| D-BR-013 | 拓扑 add/delete/get 必须校验子设备身份、网关绑定关系和参数格式。 | Application | Topo tests |

## Error Code Contract

| Scenario | ErrorCodeConstants | Parameters | Throwing Layer |
|---|---|---|---|
| Device missing | `DEVICE_NOT_EXISTS` | none | Application |
| Duplicate device name in product | `DEVICE_NAME_EXISTS` | none | Application |
| Gateway has sub devices on delete | `DEVICE_GATEWAY_HAS_SUB` | none | Application |
| Duplicate device key legacy scenario | `DEVICE_KEY_EXISTS` | none | Application if current code path requires it |
| Gateway missing | `DEVICE_GATEWAY_NOT_EXISTS` | none | Application |
| Parent is not gateway | `DEVICE_NOT_GATEWAY` | none | Application |
| Empty import list | `DEVICE_IMPORT_LIST_IS_EMPTY` | none | Application |
| Downstream serverId missing | `DEVICE_DOWNSTREAM_FAILED_SERVER_ID_NULL` | none | Message collaborator |
| Duplicate serial number | `DEVICE_SERIAL_NUMBER_EXISTS` | none | Application |
| Device is not gateway sub type | `DEVICE_NOT_GATEWAY_SUB` | productKey, deviceName | Application |
| Sub device already bound | `DEVICE_GATEWAY_BINDTO_EXISTS` | productKey, deviceName | Application |
| Topology invalid params | `DEVICE_TOPO_PARAMS_INVALID` | none | Application |
| Sub device username invalid | `DEVICE_TOPO_SUB_DEVICE_USERNAME_INVALID` | none | Application |
| Sub device auth failed | `DEVICE_TOPO_SUB_DEVICE_AUTH_FAILED` | none | Application |
| Sub not bound to gateway | `DEVICE_TOPO_SUB_NOT_BINDTO_GATEWAY` | productKey, deviceName | Application |
| Sub register params invalid | `DEVICE_SUB_REGISTER_PARAMS_INVALID` | none | Application |
| Product is not gateway sub type | `DEVICE_SUB_REGISTER_PRODUCT_NOT_GATEWAY_SUB` | productKey | Application |
| Dynamic register disabled | `DEVICE_REGISTER_DISABLED` | none | Application |
| Product secret invalid | `DEVICE_REGISTER_SECRET_INVALID` | none | Application |
| Device already exists on register | `DEVICE_REGISTER_ALREADY_EXISTS` | none | Application |

## Transaction Contract

| Use Case | Current Transaction | Required Contract |
|---|---|---|
| createDevice | no explicit transaction | Product/group/unique 校验和 insert 保持当前顺序；若事件加入再加事务。 |
| updateDevice | no explicit transaction | 存在、网关、分组、序列号校验，更新和缓存清理保持同一用例。 |
| updateDeviceGroup | `@Transactional(rollbackFor = Exception.class)` | 批量分组更新和缓存清理同事务。 |
| deleteDevice | no explicit transaction | 网关子设备检查、删除、缓存清理保持顺序。 |
| deleteDeviceList | `@Transactional(rollbackFor = Exception.class)` | 批量检查、批量删除、缓存清理同事务。 |
| importDevice | `@Transactional(rollbackFor = Exception.class)` | 当前注释说明异常回滚所有导入，必须保留。 |
| bind/unbind gateway | 当前实现以事实源为准 | 批量拓扑变更需同一应用事务。 |
| update state | no explicit transaction | 状态更新时间、缓存清理、网关下线联动保持当前行为。 |

## Integration Contract

- Product：创建设备、导入、动态注册依赖 Product 存在、productKey、deviceType、registerEnabled、productSecret。
- Group：创建/更新/导入分组需调用 `IotDeviceGroupService.validateDeviceGroupExists`。
- Message：设备动态注册、拓扑和状态消息会通过 `IotDeviceMessageService` 协作，Device 聚合不能直接写消息存储。
- Property：下行 serverId 和属性缓存由 property/message 协作处理，不进入 Device Domain。
- OTA：固件版本字段仅保存 firmwareId，OTA 业务不并入 Device 聚合。
- Cache：`RedisKeyConstants.DEVICE`，id 和 `productKey_deviceName` 两类 key，当前均 `@TenantIgnore`。
- Core API：`IotDeviceCommonApi` 的 `authDevice/getDevice/registerDevice/registerSubDevices/getModbusDeviceConfigList` 必须保持兼容。
- Excel：导入、模板、导出路径和 VO 字段不得改变。

## Mapping Rules

| Mapping | Rule |
|---|---|
| `IotDeviceSaveReqVO -> Create/Update Command` | Controller/Convert 层完成；更新命令不得携带可改 `deviceName/productId`。 |
| `IotDeviceDO -> IotDevice` | Infrastructure adapter 完成，Domain 不依赖 DO。 |
| `IotDevice -> IotDeviceDO` | RepositoryImpl 保存前转换，保留冗余 productKey/deviceType。 |
| latitude/longitude | 本 skill 的领域签名统一使用 latitude 在前、longitude 在后；调用当前旧 service `updateDeviceLocation(IotDeviceDO device, BigDecimal longitude, BigDecimal latitude)` 时必须在 adapter 中显式转换顺序。 |
| Core DTO -> Command/Result | API adapter 完成，禁止 Domain 依赖 core DTO。 |
| Import Excel VO -> Command | Application adapter 逐条转换，保留失败列表语义。 |
| Domain events -> publisher | Application 或 infrastructure messaging 发布，Domain 只产出事件。 |

## Acceptance Criteria

- 架构 AC：Device 标准骨架完整，`IotDeviceApplicationService` 实现 `IotDeviceUseCase`，RepositoryImpl 位于 `infrastructure/device/persistence` 或有兼容迁移说明。
- 业务 AC：设备创建、更新、删除、批删、状态、网关拓扑、导入、认证、动态注册、缓存行为不变。
- 契约 AC：Admin Controller、Core API、Excel、MQ 消息字段和错误码不变。
- 编译 AC：Device 测试、架构测试、IoT server compile 成功。

## Verification Commands

```bash
grep -RInE "org\.springframework|Mapper|RedisTemplate|RabbitTemplate|RocketMQ|KafkaTemplate|HttpServlet|controller\.admin|dal\.dataobject|core\.mq" develop-module-iot/develop-module-iot-server/src/main/java/com/develop/mvp/pk/module/iot/domain/device
mvn -f develop-module-iot/pom.xml -pl develop-module-iot-server test -Dtest=*Device*Test,*Device*Controller*Test,*Architecture*Test
mvn -f develop-module-iot/pom.xml -pl develop-module-iot-server compile -DskipTests
```

## Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 设备状态不变量 | `domain/device/model` | Controller、Mapper |
| 设备用例编排 | `application/device/service` | Domain、Controller |
| Product/Group/Message 协作端口 | `application/device/port/outbound` | Domain 直接调 Spring Service |
| 设备持久化 | `infrastructure/device/persistence` | Domain、Controller |
| 缓存适配 | `infrastructure/device/cache` 或 RepositoryImpl 封装 | Domain |
| Core API 兼容桥 | `api/device` 或 API local adapter | Domain |
| MQ 入口 | `mq/consumer/device` 调 inbound port | MQ 内写领域规则 |

## Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| 把 Device 当简单 CRUD | 认证、拓扑、动态注册、缓存回归 | 逐条迁移现有 service 行为 |
| 设备名做全局唯一 | 破坏产品内唯一语义 | 保持 productKey+deviceName 唯一 |
| 序列号只做产品内唯一 | 放宽当前全局唯一规则 | 保持全局唯一 |
| 删除网关不检查子设备 | 子设备悬挂 | 保留 gateway sub count 检查 |
| 网关下线不联动子设备 | 在线状态错误 | 保留 cascade offline |
| Domain 引用 core DTO/MQ message | 领域层污染 | 在 Application/API adapter 转换 |
| 去掉 `@TenantIgnore` 缓存 | 缓存契约变化 | 另行设计租户安全迁移 |

## Rationalization Table

| Pressure Scenario | Likely Bad Shortcut | Required Response |
|---|---|---|
| 赶时间迁移 Device | 只迁移 create/update/delete | Device 聚合必须覆盖认证、状态、拓扑、导入、缓存边界 |
| 看到已有 DDD 类 | 用 `IotDeviceApplicationService` 覆盖旧 service 行为 | 先对齐旧 service 全部外部行为 |
| 认为 core API 旧了 | 删除或改包名 | P0 必须兼容旧 core API |
| 觉得缓存忽略租户危险 | 直接删除注解 | 保留现有契约并补测试/说明 |
| 只有一个 Mapper 实现 | 省略 Repository/infrastructure | 标准骨架必须完整 |

## Red Flags

- Device skill 或实现未读取 `IotDeviceServiceImpl.java` 和 core `IotDeviceCommonApi.java`。
- Domain 中出现 Spring、Mapper、DO、VO、Redis、MQ message、core DTO。
- 设备名唯一范围从 productKey+deviceName 变成全局或租户内。
- 序列号全局唯一被放宽。
- 网关下线不再联动子设备下线。
- 删除旧 core API 或修改方法签名。
- Excel 导入导出字段变化。

## Rollback Conditions

1. IoT server 编译失败。
2. Device Admin API、Core API 或 Excel 契约变化。
3. 设备认证、动态注册、拓扑或缓存行为回归。
4. 错误码编号、消息或参数变化。
5. Domain 依赖技术框架或持久化实现。
6. 网关/子设备状态联动丢失。

## AI Self-Check

- [ ] 已读取 Device Controller、VO、DO、Mapper、legacy service、ApplicationService、Domain、Repository、API Impl、Core API、ErrorCode。
- [ ] 已保留 productKey+deviceName 唯一和 serialNumber 全局唯一。
- [ ] 已保留 deviceName/productId 更新不可变。
- [ ] 已保留网关删除检查和网关下线联动。
- [ ] 已保留动态注册、认证和拓扑错误码语义。
- [ ] 已保留 DEVICE 缓存 key 与 `@TenantIgnore` 语义。
- [ ] 已执行 domain 纯净 grep、Device 测试和 IoT server compile。
