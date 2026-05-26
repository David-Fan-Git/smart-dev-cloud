---
name: iot-module
description: Internet of Things platform — device management, thing model, MQTT/CoAP/Modbus protocol gateways, TDengine time-series data, OTA firmware, rule engine, alerting, PF4J plugins
type: project
---

# develop-module-iot

## 概述

物联网平台模块。提供完整的 IoT 能力，包括设备管理、物模型、协议适配（MQTT/CoAP/Modbus）、OTA 固件升级、规则引擎、告警、时序数据（TDengine）、插件框架（PF4J）。

- **包路径**: `com.develop.mvp.pk.module.iot`
- **多租户**: 是（Entity 继承 `TenantBaseDO`）

## 模块结构

```
develop-module-iot/
├── develop-module-iot-api/              # Feign 接口 + DTO
├── develop-module-iot-core/             # 核心领域模型（物模型、协议抽象）
├── develop-module-iot-server/           # 管理 REST API
└── develop-module-iot-gateway/          # 协议网关（MQTT Broker、CoAP、Modbus 网关）
```

## 核心功能与 Controller 清单

### 1. 产品管理
| Controller | 路由 | 方法 |
|---|---|---|
| `IotProductController` | `/iot/product` | 产品 CRUD + `/page` |
| `IotProductCategoryController` | `/iot/product-category` | 产品分类 CRUD |

### 2. 物模型 (Thing Model)
| Controller | 路由 | 方法 |
|---|---|---|
| `IotThingModelController` | `/iot/thing-model` | 物模型定义（属性、服务、事件） |

### 3. 设备管理
| Controller | 路由 | 方法 |
|---|---|---|
| `IotDeviceController` | `/iot/device` | 设备 CRUD + `/page`、注册、激活、禁用 |
| `IotDeviceGroupController` | `/iot/device-group` | 设备分组 |
| `IotDeviceGroupRelationController` | `/iot/device-group-relation` | 分组关联 |
| `IotDevicePropertyController` | `/iot/device-property` | 设备属性最新值 + 历史趋势 |
| `IotDeviceMessageController` | `/iot/device-message` | 设备上下行消息记录 |
| `IotDeviceModbusConfigController` | `/iot/device-modbus-config` | Modbus 点位配置 |
| `IotDeviceModbusPointController` | `/iot/device-modbus-point` | Modbus 采集点位 |

### 4. 协议网关 (develop-module-iot-gateway)
- **MQTT**: Vert.x 高性能 Broker + Eclipse Paho 客户端
- **CoAP**: Eclipse Californium
- **Modbus**: j2mod（支持 RTU/TCP）
- 网关统一管理设备上下线、消息编解码、协议转换

### 5. 规则引擎
| Controller | 路由 | 方法 |
|---|---|---|
| `IotSceneRuleController` | `/iot/scene-rule` | 场景联动规则（IF-THEN） |
| `IotDataRuleController` | `/iot/data-rule` | 数据转发规则（→Kafka/MQTT/HTTP） |
| `IotDataSinkController` | `/iot/data-sink` | 数据目的地配置 |

### 6. OTA 升级
| Controller | 路由 | 方法 |
|---|---|---|
| `IotOtaFirmwareController` | `/iot/ota-firmware` | 固件管理（上传、版本管理） |
| `IotOtaTaskController` | `/iot/ota-task` | 升级任务（批量、灰度） |
| `IotOtaTaskRecordController` | `/iot/ota-task-record` | 升级任务记录 |

### 7. 告警管理
| Controller | 路由 | 方法 |
|---|---|---|
| `IotAlertConfigController` | `/iot/alert-config` | 告警规则配置 |
| `IotAlertRecordController` | `/iot/alert-record` | 告警记录 |

### 8. 统计分析
| Controller | 路由 | 方法 |
|---|---|---|
| `IotStatisticsController` | `/iot/statistics` | 设备统计（在线数、活跃度、消息量） |

## 数据库表

| 表 | 说明 |
|---|---|
| `iot_product` | 产品 |
| `iot_product_category` | 产品分类 |
| `iot_thing_model` | 物模型 |
| `iot_thing_model_property` | 物模型属性 |
| `iot_thing_model_service` | 物模型服务 |
| `iot_thing_model_event` | 物模型事件 |
| `iot_device` | 设备 |
| `iot_device_group` | 设备分组 |
| `iot_device_group_relation` | 分组关联 |
| `iot_device_property` | 设备属性值 |
| `iot_device_message` | 设备消息 |
| `iot_device_modbus_config` | Modbus 配置 |
| `iot_device_modbus_point` | Modbus 点位 |
| `iot_scene_rule` | 场景联动 |
| `iot_data_rule` | 数据转发 |
| `iot_data_sink` | 数据目的地 |
| `iot_ota_firmware` | OTA 固件 |
| `iot_ota_task` | 升级任务 |
| `iot_ota_task_record` | 升级记录 |
| `iot_alert_config` | 告警配置 |
| `iot_alert_record` | 告警记录 |
| `iot_plugin` | PF4J 插件 |

## 关键 Services

| Service | 职责 |
|---|---|
| `IotDeviceService` | 设备核心业务（注册、认证、上下线、状态管理） |
| `IotThingModelService` | 物模型定义（属性/服务/事件） |
| `IotProductService` | 产品管理 |
| `IotSceneRuleService` | 场景联动规则引擎 |
| `IotOtaTaskService` | OTA 升级任务 |
| `IotAlertConfigService` | 告警规则 |

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis`
- `develop-spring-boot-starter-biz-tenant`
- `pf4j` / `pf4j-spring` — 插件框架
- TDengine JDBC Driver — 时序数据库
- Vert.x — MQTT Broker
- Eclipse Paho — MQTT Client
- Eclipse Californium — CoAP
- j2mod — Modbus

## 关键点

- 四模块架构（api + core + server + gateway）分离管理面和网关面
- 协议网关支持 MQTT/CoAP/Modbus 三种主流协议
- TSDB 时序数据使用 TDengine 存储设备属性历史
- PF4J 插件框架支持协议适配器的热插拔
- 场景联动规则：IF（设备触发）THEN（设备动作/通知）
- 无独立的 Feign API 暴露给其他业务模块
