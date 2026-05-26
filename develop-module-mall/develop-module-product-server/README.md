# develop-module-product-server

## 模块定位

product 模块，主要实现商品相关功能 例如：品牌、商品分类、spu、sku等功能。

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `develop-module-mall/develop-module-product-server` |
| Maven Artifact | `develop-module-product-server` |
| Packaging | `jar` |
| Java 源文件数量 | 148 |
| 模块说明 | product 模块，主要实现商品相关功能 例如：品牌、商品分类、spu、sku等功能。 |

## 子模块结构

本模块没有声明 Maven 子模块。

## 主要目录职责

| 目录 | 说明 |
|---|---|
| `api` | 跨模块 API、DTO、枚举、RPC / CommonApi 契约。 |
| `application` | DDD 应用层，用例编排、事务边界、领域对象和仓储接口调用。 |
| `controller` | REST 控制器，包含管理端、用户端请求入口和 VO。 |
| `convert` | 对象转换层，通常使用 MapStruct 处理 VO / DTO / DO / Domain 转换。 |
| `dal` | 数据访问层，包含 DO、MyBatis Mapper、Redis Key 等。 |
| `domain` | DDD 领域层，聚合根、值对象、领域服务、领域事件、仓储接口。 |
| `framework` | 模块内 Spring 配置、拦截器、扩展点。 |
| `infrastructure` | DDD 基础设施层，仓储实现、外部系统适配、事件发布适配。 |
| `service` | 传统三层业务服务接口与实现，是 DDD 迁移的重要来源。 |

## 关键依赖

| 依赖 | 说明 |
|---|---|
| `develop-spring-boot-starter-env` | 环境标识与环境透传 |
| `develop-module-product-api` | 业务模块 API / server / core 依赖。 |
| `develop-module-member-api` | 业务模块 API / server / core 依赖。 |
| `develop-spring-boot-starter-biz-tenant` | 多租户上下文、租户过滤、租户透传 |
| `develop-spring-boot-starter-web` | Web、统一异常、Swagger/Knife4j、Jackson |
| `develop-spring-boot-starter-security` | 认证授权、登录用户上下文、操作日志 |
| `develop-spring-boot-starter-mybatis` | MyBatis Plus、多数据源、分页、数据翻译 |
| `develop-spring-boot-starter-rpc` | OpenFeign、负载均衡、跨模块/跨服务调用 |
| `spring-cloud-starter-alibaba-nacos-discovery` | Nacos 注册发现或配置中心。 |
| `spring-cloud-starter-alibaba-nacos-config` | Nacos 注册发现或配置中心。 |
| `develop-spring-boot-starter-test` | 测试基类、断言、随机对象、测试工具 |
| `develop-spring-boot-starter-excel` | Excel 导入导出、字典格式化 |
| `develop-spring-boot-starter-monitor` | 链路追踪、指标、监控接入 |

## 架构职责

- 本模块承载具体业务实现，包括 REST 入口、应用服务、领域模型、基础设施适配、DAL、MQ 和 Job。
- 新增或迁移核心业务逻辑时，应优先落到 `domain`、`application`、`infrastructure`、`convert` 分层。
- `service` 与 `dal` 中的旧逻辑是 DDD 迁移来源，不应作为新增核心业务规则的最终归宿。

## DDD / 分层说明

本模块已出现 DDD / 六边形相关目录。新增或迁移核心业务逻辑时，应优先遵循以下方向：

- `domain` 保持纯 Java，承载聚合根、值对象、领域服务、领域事件和仓储接口。
- `application` 负责编排用例、事务边界和领域对象协作。
- `infrastructure` 负责仓储实现、MyBatis / Redis / 外部系统适配。
- `service` 与 `dal` 中的旧业务逻辑是迁移来源，不应作为新增核心业务规则的最终归宿。

## 构建与验证

```bash
# 编译该模块及其依赖
mvn compile -pl develop-module-mall/develop-module-product-server -am

# 运行该模块测试
mvn test -pl develop-module-mall/develop-module-product-server

# 打包该模块及其依赖
mvn clean package -pl develop-module-mall/develop-module-product-server -am -Dmaven.test.skip=true
```

## 维护建议

- 修改跨模块契约时，优先更新 `api` 子模块，并检查所有调用方兼容性。
- 修改业务实现时，优先补充或更新模块级测试，至少运行当前模块的 `mvn test` 或 `mvn compile`。
- 涉及 DDD 聚合、模块结构或 API 契约调整时，同时更新本 README 与根目录架构文档。
- 不要在聚合 POM 模块中放置业务逻辑；业务逻辑应位于具体 `server` 子模块。
