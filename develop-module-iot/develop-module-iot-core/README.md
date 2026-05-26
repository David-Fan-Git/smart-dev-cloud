# develop-module-iot-core

## 模块定位

iot 模块下，提供 iot-biz 和 iot-gateway 模块的核心功能。例如说： 1. 消息总线：跨 iot-biz 和 iot-gateway 的设备消息。可选择使用 spring event、redis stream、rocketmq、kafka、rabbitmq 等。 2. 查询设备信息的通用 API

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `develop-module-iot/develop-module-iot-core` |
| Maven Artifact | `develop-module-iot-core` |
| Packaging | `jar` |
| Java 源文件数量 | 49 |
| 模块说明 | iot 模块下，提供 iot-biz 和 iot-gateway 模块的核心功能。例如说： 1. 消息总线：跨 iot-biz 和 iot-gateway 的设备消息。可选择使用 spring event、redis stream、rocketmq、kafka、rabbitmq 等。 2. 查询设备信息的通用 API |

## 子模块结构

本模块没有声明 Maven 子模块。

## 主要目录职责

| 目录 | 说明 |
|---|---|
| `enums` | 模块内枚举、错误码、状态值等。 |
| `mq` | 消息生产者、消费者和消息体。 |
| `service` | 传统三层业务服务接口与实现，是 DDD 迁移的重要来源。 |

## 关键依赖

| 依赖 | 说明 |
|---|---|
| `spring-boot-starter` | 外部框架或组件依赖。 |
| `develop-spring-boot-starter-mq` | Redis/RabbitMQ/RocketMQ/Kafka 消息抽象 |
| `rocketmq-spring-boot-starter` | 消息队列客户端能力。 |
| `spring-kafka` | 消息队列客户端能力。 |

## 架构职责

- 本模块提供业务域内部可复用核心抽象、公共模型或协议接入支撑能力。
- Core 模块应避免依赖具体运行入口，保持可复用和可测试。
- 修改公共抽象时需要检查同业务域内 server、gateway 或 adapter 模块的兼容性。

## DDD / 分层说明

本模块当前以传统三层或聚合 POM 管理为主。后续新增核心业务逻辑或进行重构时，应按仓库 DDD 标准逐步收敛到 `domain`、`application`、`infrastructure`、`convert` 分层。

## 构建与验证

```bash
# 编译该模块及其依赖
mvn compile -pl develop-module-iot/develop-module-iot-core -am

# 运行该模块测试
mvn test -pl develop-module-iot/develop-module-iot-core

# 打包该模块及其依赖
mvn clean package -pl develop-module-iot/develop-module-iot-core -am -Dmaven.test.skip=true
```

## 维护建议

- 修改跨模块契约时，优先更新 `api` 子模块，并检查所有调用方兼容性。
- 修改业务实现时，优先补充或更新模块级测试，至少运行当前模块的 `mvn test` 或 `mvn compile`。
- 涉及 DDD 聚合、模块结构或 API 契约调整时，同时更新本 README 与根目录架构文档。
- 不要在聚合 POM 模块中放置业务逻辑；业务逻辑应位于具体 `server` 子模块。
