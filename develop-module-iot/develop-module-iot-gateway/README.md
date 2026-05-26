# develop-module-iot-gateway

## 模块定位

iot 模块下，设备网关： ① 功能一：接收来自设备的消息，并进行解码（decode）后，发送到消息网关，提供给 iot-biz 进行处理 ② 功能二：接收来自消息网关的消息（由 iot-biz 发送），并进行编码（encode）后，发送给设备

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `develop-module-iot/develop-module-iot-gateway` |
| Maven Artifact | `develop-module-iot-gateway` |
| Packaging | `jar` |
| Java 源文件数量 | 111 |
| 模块说明 | iot 模块下，设备网关： ① 功能一：接收来自设备的消息，并进行解码（decode）后，发送到消息网关，提供给 iot-biz 进行处理 ② 功能二：接收来自消息网关的消息（由 iot-biz 发送），并进行编码（encode）后，发送给设备 |

## 子模块结构

本模块没有声明 Maven 子模块。

## 主要目录职责

| 目录 | 说明 |
|---|---|
| `enums` | 模块内枚举、错误码、状态值等。 |
| `websocket` | WebSocket 连接、会话和消息能力。 |
| `service` | 传统三层业务服务接口与实现，是 DDD 迁移的重要来源。 |

## 关键依赖

| 依赖 | 说明 |
|---|---|
| `develop-module-iot-core` | 业务模块 API / server / core 依赖。 |
| `rocketmq-spring-boot-starter` | 消息队列客户端能力。 |
| `develop-spring-boot-starter-test` | 测试基类、断言、随机对象、测试工具 |

## 架构职责

- 本模块是业务域专属网关或协议接入运行单元，负责外部协议接入和请求转发。
- 网关层不应承载核心业务规则，业务规则应下沉到对应领域或应用服务。
- 修改协议接入能力时需要同步检查 server 模块和外部设备 / 客户端兼容性。

## DDD / 分层说明

本模块当前以传统三层或聚合 POM 管理为主。后续新增核心业务逻辑或进行重构时，应按仓库 DDD 标准逐步收敛到 `domain`、`application`、`infrastructure`、`convert` 分层。

## 构建与验证

```bash
# 编译该模块及其依赖
mvn compile -pl develop-module-iot/develop-module-iot-gateway -am

# 运行该模块测试
mvn test -pl develop-module-iot/develop-module-iot-gateway

# 打包该模块及其依赖
mvn clean package -pl develop-module-iot/develop-module-iot-gateway -am -Dmaven.test.skip=true
```

## 维护建议

- 修改跨模块契约时，优先更新 `api` 子模块，并检查所有调用方兼容性。
- 修改业务实现时，优先补充或更新模块级测试，至少运行当前模块的 `mvn test` 或 `mvn compile`。
- 涉及 DDD 聚合、模块结构或 API 契约调整时，同时更新本 README 与根目录架构文档。
- 不要在聚合 POM 模块中放置业务逻辑；业务逻辑应位于具体 `server` 子模块。
