---
name: project-structure
description: Smart Cloud root project structure, Maven module layout, directory conventions, and module dependency graph
type: project
---

# Project Structure

## Overview

Smart Cloud (develop) is a Spring Cloud Alibaba 微服务快速开发平台采用 Maven 多模块架构。根项目以 `com.develop.mvp` 为 groupId，`develop` 为 artifactId，版本号由 `<revision>` 属性统一管理（当前 `2026.04-SNAPSHOT`）。通过 `develop-dependencies` BOM 集中管理所有第三方依赖版本，确保全项目版本一致性。

## Root-Level Maven Modules

```xml
<groupId>com.develop.mvp</groupId>
<artifactId>develop</artifactId>
<version>${revision}</version>
<packaging>pom</packaging>
<modules>
    <!-- 核心基础设施 -->
    <module>develop-dependencies</module>   <!-- BOM — 版本管理 -->
    <module>develop-framework</module>      <!-- 17 framework starters -->
    <module>develop-gateway</module>        <!-- Spring Cloud Gateway -->
    <module>develop-server</module>         <!-- 单体聚合启动器 -->
    <!-- 业务模块 -->
    <module>develop-module-system</module>
    <module>develop-module-infra</module>
    <module>develop-module-member</module>
    <module>develop-module-bpm</module>
    <module>develop-module-pay</module>
    <module>develop-module-report</module>
    <module>develop-module-mp</module>
    <module>develop-module-mall</module>
    <module>develop-module-crm</module>
    <module>develop-module-erp</module>
    <module>develop-module-iot</module>
    <module>develop-module-mes</module>
    <module>develop-module-wms</module>
    <module>develop-module-ai</module>
</modules>
```

根 `pom.xml` 定义关键编译属性:
- `java.version` = 17
- `spring.boot.version` = 3.5.9
- `lombok.version` = 1.18.42
- `mapstruct.version` = 1.6.3
- `maven-surefire-plugin.version` = 3.5.3
- `maven-compiler-plugin.version` = 3.14.0
- `flatten-maven-plugin.version` = 1.7.2

版本管理委托给 `develop-dependencies` BOM，通过 `<dependencyManagement>` 的 `<type>pom</type><scope>import</scope>` 导入（`pom.xml` 根 pom 不直接定义第三方依赖版本）。

## Module Dependency Graph

```
                      +---------------------+
                      | develop-dependencies| (BOM - version lock)
                      +----------+----------+
                                 |
          +----------------------+----------------------+
          |                      |                      |
  develop-framework       develop-gateway        develop-server (monolithic)
  (17 starters)          (WebFlux Gateway)       (depends on *-server modules)
          |                                         |
          +---> develop-common                      +---> develop-module-system-server
          +---> starter-* (web, security,           +---> develop-module-infra-server
          |          mybatis, redis, ...)            +---> (optional) develop-module-*-server
          |
          +---> All develop-module-*-server modules
```

## develop-dependencies (BOM)

单一 `pom.xml` 文件，集中管理所有第三方依赖版本号:
- Spring Boot (`spring.boot.version` = 3.5.9)
- Spring Cloud 2025.0.1
- Spring Cloud Alibaba 2025.0.0.0
- MyBatis Plus 3.5.16
- Redis / Redisson 4.3.1
- Druid 1.2.28
- RocketMQ / RabbitMQ / Kafka
- XXL-Job 2.4.0
- Flowable 7.2.0
- Easy-Trans
- SpringDoc 2.8.17 / Knife4j 4.5.0
- FastExcel 1.3.0
- Lock4j
- Sentinel
- SkyWalking 9.6.0
- Spring AI 1.1.5
- 其他工具库 (Hutool, Guava, Caffeine...)

作为 Bill of Materials (BOM) 模块，被其他模块通过 `<dependencyManagement>` 的 `import` scope 引用。

## develop-framework (17 Starters)

框架层包含 **17 个 Maven 子模块** 加 **develop-common 公共库**:

| Starter/模块 | Maven artifactId | 说明 |
|---|---|---|
| develop-common | `develop-common` | 公共工具类、CommonResult、ErrorCode、BeanUtils、PageResult、Feign API 接口契约 |
| Web | `develop-spring-boot-starter-web` | 全局异常处理、Jackson 配置、CORS、Swagger/Knife4j、XSS 过滤、API 日志、API 加密 |
| Security | `develop-spring-boot-starter-security` | Spring Security Token 认证授权、操作日志 |
| MyBatis | `develop-spring-boot-starter-mybatis` | MyBatis Plus、动态数据源 Druid、BaseDO/BaseMapperX/LambdaQueryWrapperX |
| Redis | `develop-spring-boot-starter-redis` | Redis 缓存、TimeoutRedisCacheManager、Redisson 分布式锁 |
| MQ | `develop-spring-boot-starter-mq` | 消息队列抽象（Redis Stream/PubSub + RabbitMQ） |
| RPC | `develop-spring-boot-starter-rpc` | Feign 远程调用、LoadBalancer |
| Job | `develop-spring-boot-starter-job` | XXL-Job 分布式调度、@Async TTL 线程池 |
| Tenant | `develop-spring-boot-starter-biz-tenant` | SaaS 多租户（Web/DB/Redis/MQ/Job/RPC 全链路） |
| Data Permission | `develop-spring-boot-starter-biz-data-permission` | 行级数据权限（JSQLParser 注入） |
| IP | `develop-spring-boot-starter-biz-ip` | IP 地区解析 |
| Env | `develop-spring-boot-starter-env` | 环境隔离（标签路由） |
| Excel | `develop-spring-boot-starter-excel` | Excel 导入导出（FastExcel）+ 字典转换 |
| Protection | `develop-spring-boot-starter-protection` | Sentinel 限流熔断 + 幂等性 + Lock4j + API 签名 |
| Monitor | `develop-spring-boot-starter-monitor` | 监控（SkyWalking + Micrometer + Actuator） |
| WebSocket | `develop-spring-boot-starter-websocket` | WebSocket 实时消息 |
| Test | `develop-spring-boot-starter-test` | 测试基类 + RedisTestConfiguration |

每个 starter 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置；部分 starter 通过 `META-INF/spring.factories` 注册 `EnvironmentPostProcessor`。

## develop-gateway (API Gateway)

基于 Spring Cloud Gateway (WebFlux)，作为独立进程运行:

```
develop-gateway/src/main/java/com/develop/mvp/pk/gateway/
  GatewayServerApplication.java               # 启动类 (@SpringBootApplication)
  filter/
    cors/CorsFilter.java                      # CORS 跨域处理（WebFlux）
    grey/GrayLoadBalancer.java                # 灰度路由负载均衡
    grey/GrayReactiveLoadBalancerClientFilter.java
    logging/AccessLog.java                    # 访问日志模型
    logging/AccessLogFilter.java              # 访问日志过滤器
    security/LoginUser.java                   # 网关层登录用户模型
    security/TokenAuthenticationFilter.java   # Token 认证过滤器（WebFlux）
  handler/GlobalExceptionHandler.java         # 全局异常处理（WebFlux）
  jackson/GatewayJacksonAutoConfiguration.java# Jackson 配置
  route/dynamic/                              # 动态路由
  util/BannerApplicationRunner.java           # 启动横幅
  util/EnvUtils.java                          # 环境工具
  util/SecurityFrameworkUtils.java            # 安全工具
  util/WebFrameworkUtils.java                 # Web 工具
```

核心职责:
- 请求路由转发到各微服务
- Token 认证鉴权（基于 Redis 缓存）
- CORS 统一处理
- 灰度发布路由（标签路由）
- Knife4j 接口文档聚合
- Nacos 服务发现 + LoadBalancer 负载均衡
- 访问日志记录

网关的包路径为 `com.develop.mvp.pk.gateway.*`，独立于框架的 `com.develop.mvp.pk.framework.*`。

## develop-server (Monolithic Aggregator)

作为单体部署的入口应用，本质是一个"空壳"容器:

```xml
<artifactId>develop-server</artifactId>
<packaging>jar</packaging>
```

关键特征:
- **无 `@SpringBootApplication`** — 不含 `main()` 方法，通过 Maven 依赖聚合各 `-server` 模块
- 默认只启用 `develop-module-system-server` + `develop-module-infra-server`（最小化编译）
- 其他业务模块通过取消 `pom.xml` 中 `<dependency>` 注释来启用
- 扫描基础路径: `@SpringBootApplication(scanBasePackages = "${develop.info.base-package}")` — 即 `com.develop.mvp.pk`
- 单体模式下排除 OpenFeign（rpc starter），禁用 Nacos 服务发现和配置中心
- 单体模式下通过 `application-local.yaml` 排除所有 RPC 相关 AutoConfiguration
- 默认端口 48080

```xml
<!-- develop-server/pom.xml 中排除 Feign -->
<dependency>
    <groupId>com.develop.mvp</groupId>
    <artifactId>develop-spring-boot-starter-rpc</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## Business Modules 标准结构

每个 `develop-module-{name}` 遵循统一组织模式:

```
develop-module-{name}/
  pom.xml
  develop-module-{name}-api/                     # API 层（Feign 接口、DTO、Enum、ErrorCodeConstants）
    src/main/java/com/develop/mvp/pk/module/{name}/
      api/                                       # Feign 接口定义 (XxxApi)
      enums/                                     # ErrorCodeConstants、枚举
  develop-module-{name}-server/                  # 服务端实现
    src/main/java/com/develop/mvp/pk/module/{name}/
      controller/
        admin/                                   # 管理后台接口 (@Tag("管理后台 - XXX"))
        app/                                     # 移动端/APP 接口 (@Tag("APP - XXX"))
      service/                                   # Service 接口 + Impl（同包）
      dal/
        mysql/                                   # MyBatis Plus Mapper 接口
        dataobject/                              # 实体类 (XxxDO)
        redis/                                   # Redis Key 定义
      convert/                                   # MapStruct VO/DO 转换器
      framework/                                 # 模块级 Spring 配置
      job/                                       # XXL-Job 任务定义
      mq/                                        # 消息消费者
```

完整业务模块列表:

| 模块 | artifactId | 说明 |
|---|---|---|
| system | `develop-module-system` | 系统核心：用户、角色、权限、部门、岗位、字典、通知、OAuth2、验证码 |
| infra | `develop-module-infra` | 基础设施：配置管理、数据源、API 日志、文件存储、定时任务、Swagger |
| member | `develop-module-member` | 会员中心 |
| bpm | `develop-module-bpm` | 工作流（Flowable 7.2.0） |
| pay | `develop-module-pay` | 支付服务 |
| report | `develop-module-report` | 数据报表（大屏） |
| mp | `develop-module-mp` | 微信公众号管理 |
| mall | `develop-module-mall` | 商城系统（4 个子域） |
| crm | `develop-module-crm` | 客户关系管理 |
| erp | `develop-module-erp` | 企业资源计划 |
| iot | `develop-module-iot` | 物联网（含 TDengine 时序数据） |
| mes | `develop-module-mes` | 制造执行系统 |
| wms | `develop-module-wms` | 仓库管理系统 |
| ai | `develop-module-ai` | AI 大模型（Spring AI 1.1.5, 支持 Qdrant/Milvus 向量库） |

**大模块的子域拆分模式** (e.g. `develop-module-mall`):

```
develop-module-mall/
  develop-module-product-api/        # 商品 api
  develop-module-product-server/     # 商品 server
  develop-module-promotion-api/      # 营销 api
  develop-module-promotion-server/   # 营销 server
  develop-module-trade-api/          # 交易 api
  develop-module-trade-server/       # 交易 server
  develop-module-statistics-api/     # 统计 api
  develop-module-statistics-server/  # 统计 server
```

每个子域有独立的 api/server 对，小模块（如 system、infra）保持单一 api/server 结构。

## UI 前端项目 (`develop-ui/`)

| 项目 | 技术栈 | 说明 |
|---|---|---|
| `develop-ui-admin-vue3` | Vue 3 + Element Plus | 管理后台 |
| `develop-ui-admin-vben` | Vue 3 + Vben Admin | 管理后台 |
| `develop-ui-admin-vue2` | Vue 2 | 管理后台 |
| `develop-ui-admin-uniapp` | uni-app | 管理后台（移动端） |
| `develop-ui-mall-uniapp` | uni-app | 商城（H5/小程序/App） |

## SQL 脚本 (`sql/`)

支持 9 种数据库方言 DDL/DML:
```
sql/
  mysql/       # 主推，默认开发环境
  oracle/
  postgresql/
  sqlserver/
  dm/          # 达梦数据库
  kingbase/    # 人大金仓
  opengauss/   # 华为 openGauss
  db2/         # IBM DB2
  tools/       # 数据库工具脚本
```

## 全局配置文件

- `lombok.config`：项目级 Lombok 设置 — `chain=true`（setter 链式调用）、`callsuper=CALL`（toString/equals/hashCode 包含父类）
- `pom.xml`：`<revision>` 属性统一版本、Maven Compiler Plugin 解决 Lombok + MapStruct 编译顺序
- `pom.xml` 根属性指定 `<develop.info.base-package>` 为 `com.develop.mvp.pk`，用于 `@SpringBootApplication` 的 `scanBasePackages` 和 MyBatis `type-aliases-package`

## 注意事项

- 根 `pom.xml` 不直接定义任何第三方依赖版本号，全部委托给 `develop-dependencies` BOM
- `develop-server` 打包为 jar 但 **不含** `@SpringBootApplication` 和 `main()` 方法；单体模式下通过各业务模块的扫描生效
- 微服务模式与单体模式通过 Maven 依赖和 `spring.autoconfigure.exclude` 切换（见 deployment-modes 技能）
- API 模块 (api) 与 Server 模块分离，API 模块不包含任何实现代码，仅暴露 Feign 接口、DTO、枚举和 ErrorCodeConstants
- 网关代码位于独立的包路径 `com.develop.mvp.pk.gateway.*`，而非框架通用包路径
- Mall 模块的子域拆分模式适用于大型业务模块复用；system 等小模块保持单一 api/server 结构
- AI 模块强依赖 Qdrant/Milvus 向量数据库，本地开发时需在 `application-local.yaml` 中排除相关 AutoConfiguration
