# develop-gateway

## 模块定位

Spring Cloud Gateway 网关应用，负责统一入口、路由转发、服务发现、配置接入、接口文档聚合和监控接入。

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `develop-gateway` |
| Maven Artifact | `develop-gateway` |
| Packaging | `jar` |
| Java 源文件数量 | 16 |
| 模块说明 | API 服务网关，基于 Spring Cloud Gateway 实现 |

## 子模块结构

本模块没有声明 Maven 子模块。

## 主要目录职责

当前目录未检测到标准业务源码分层目录，主要通过 Maven POM 进行依赖或聚合管理。

## 关键依赖

| 依赖 | 说明 |
|---|---|
| `develop-module-system-api` | 业务模块 API / server / core 依赖。 |
| `spring-cloud-starter-gateway-server-webflux` | 外部框架或组件依赖。 |
| `knife4j-gateway-spring-boot-starter` | 外部框架或组件依赖。 |
| `spring-cloud-starter-loadbalancer` | 外部框架或组件依赖。 |
| `spring-cloud-starter-alibaba-nacos-discovery` | Nacos 注册发现或配置中心。 |
| `spring-cloud-starter-alibaba-nacos-config` | Nacos 注册发现或配置中心。 |
| `develop-spring-boot-starter-monitor` | 链路追踪、指标、监控接入 |

## 架构职责

- 本模块是独立网关运行单元，负责统一入口、路由转发、认证透传、跨域、灰度路由和接口文档聚合。
- Gateway 只处理入口层协议与路由职责，不承载业务规则。
- 后端业务能力由 `develop-server` 或独立业务服务提供，网关通过路由规则转发请求。

## 构建与验证

```bash
# 编译
mvn compile -pl develop-gateway -am

# 打包
mvn clean package -pl develop-gateway -am -Dmaven.test.skip=true

# 启动
mvn spring-boot:run -pl develop-gateway -am
```

## 维护建议

- 修改路由、认证透传或跨域配置时，应同步检查 `develop-gateway/src/main/resources` 下的环境配置。
- 网关只维护入口层能力，不应把业务规则写入 Filter 或路由配置。
- 调整接口文档聚合时，需要确认后端服务 OpenAPI 地址仍然可访问。
