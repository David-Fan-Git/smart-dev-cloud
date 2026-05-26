# develop-dependencies

## 模块定位

统一依赖版本治理 BOM，集中管理 Spring、MyBatis、Redis、Flowable、MQ、AI、IoT、工具库等第三方依赖版本。

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `develop-dependencies` |
| Maven Artifact | `develop-dependencies` |
| Packaging | `pom` |
| Java 源文件数量 | 0 |
| 模块说明 | 基础 bom 文件，管理整个项目的依赖版本 |

## 子模块结构

本模块没有声明 Maven 子模块。

## 主要目录职责

当前目录未检测到标准业务源码分层目录，主要通过 Maven POM 进行依赖或聚合管理。

## 关键依赖

| 依赖 | 说明 |
|---|---|
| `develop-spring-boot-starter-biz-tenant` | 多租户上下文、租户过滤、租户透传 |
| `develop-spring-boot-starter-biz-data-permission` | 数据权限、部门数据范围、SQL 过滤 |
| `develop-spring-boot-starter-biz-ip` | IP 区域与城市编码能力 |
| `develop-spring-boot-starter-env` | 环境标识与环境透传 |
| `develop-spring-boot-starter-web` | Web、统一异常、Swagger/Knife4j、Jackson |
| `develop-spring-boot-starter-security` | 认证授权、登录用户上下文、操作日志 |
| `develop-spring-boot-starter-websocket` | WebSocket 会话与多节点广播 |
| `knife4j-openapi3-jakarta-spring-boot-starter` | 外部框架或组件依赖。 |
| `knife4j-gateway-spring-boot-starter` | 外部框架或组件依赖。 |
| `develop-spring-boot-starter-mybatis` | MyBatis Plus、多数据源、分页、数据翻译 |
| `easy-trans-spring-boot-starter` | 外部框架或组件依赖。 |
| `develop-spring-boot-starter-redis` | Redis、Redisson、缓存配置 |
| `redisson-spring-boot-starter` | 外部框架或组件依赖。 |
| `develop-spring-boot-starter-rpc` | OpenFeign、负载均衡、跨模块/跨服务调用 |
| `develop-spring-boot-starter-job` | XXL-Job 定时任务 |
| `develop-spring-boot-starter-mq` | Redis/RabbitMQ/RocketMQ/Kafka 消息抽象 |
| `rocketmq-spring-boot-starter` | 消息队列客户端能力。 |
| `develop-spring-boot-starter-protection` | 分布式锁、幂等、限流、服务保护 |
| `lock4j-redisson-spring-boot-starter` | 外部框架或组件依赖。 |
| `develop-spring-boot-starter-monitor` | 链路追踪、指标、监控接入 |
| `develop-spring-boot-starter-test` | 测试基类、断言、随机对象、测试工具 |
| `flowable-spring-boot-starter-process` | Flowable 工作流引擎能力。 |
| `flowable-spring-boot-starter-actuator` | Flowable 工作流引擎能力。 |
| `develop-spring-boot-starter-excel` | Excel 导入导出、字典格式化 |
| `captcha-spring-boot-starter` | 外部框架或组件依赖。 |
| `justauth-spring-boot-starter` | 外部框架或组件依赖。 |
| `wx-java-mp-spring-boot-starter` | 微信生态 SDK 能力。 |
| `wx-java-miniapp-spring-boot-starter` | 微信生态 SDK 能力。 |
| `jimureport-spring-boot3-starter` | JimuReport / JimuBI 报表能力。 |
| `jimubi-spring-boot3-starter` | JimuReport / JimuBI 报表能力。 |

## 架构职责

- 本模块是全仓库 Maven BOM，集中治理 Spring、Spring Cloud、数据库、缓存、MQ、AI、IoT 与工具库版本。
- 业务模块和框架模块应通过父 POM / dependencyManagement 继承版本，避免在子模块重复声明版本。
- 调整依赖版本时，应优先验证 `develop-server`、`develop-gateway` 与受影响业务模块的编译结果。

## 构建与验证

```bash
# 编译该聚合模块及其子模块
mvn compile -pl develop-dependencies -am

# 打包该聚合模块及其子模块
mvn clean package -pl develop-dependencies -am -Dmaven.test.skip=true
```

## 维护建议

- 升级依赖版本时，优先确认上游兼容矩阵，并运行受影响模块的 Maven 编译或测试。
- 不要在业务模块中绕过 BOM 单独固定版本，除非存在明确兼容性原因。
- 修改基础依赖后，应同步检查根 POM、框架 Starter 和启动模块。
