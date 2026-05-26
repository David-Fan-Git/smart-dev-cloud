# develop-module-mall

## 模块定位

商城聚合模块，由商品、营销、交易、统计等子域组成。

## 基本信息

| 项目 | 内容 |
|---|---|
| 模块路径 | `develop-module-mall` |
| Maven Artifact | `develop-module-mall` |
| Packaging | `pom` |
| Java 源文件数量 | 940 |
| 模块说明 | 商城大模块，由 product 商品、promotion 营销、trade 交易、statistics 统计等组成 |

## 子模块结构

| 子模块 | 职责 |
|---|---|
| `develop-module-product-api` | 跨模块契约、DTO、枚举、CommonApi / RPC API。 |
| `develop-module-product-server` | 业务实现、Controller、Service/ApplicationService、Domain、Infrastructure、DAL、MQ、Job。 |
| `develop-module-promotion-api` | 跨模块契约、DTO、枚举、CommonApi / RPC API。 |
| `develop-module-promotion-server` | 业务实现、Controller、Service/ApplicationService、Domain、Infrastructure、DAL、MQ、Job。 |
| `develop-module-trade-api` | 跨模块契约、DTO、枚举、CommonApi / RPC API。 |
| `develop-module-trade-server` | 业务实现、Controller、Service/ApplicationService、Domain、Infrastructure、DAL、MQ、Job。 |
| `develop-module-statistics-api` | 跨模块契约、DTO、枚举、CommonApi / RPC API。 |
| `develop-module-statistics-server` | 业务实现、Controller、Service/ApplicationService、Domain、Infrastructure、DAL、MQ、Job。 |

## 主要目录职责

| 目录 | 说明 |
|---|---|
| `application` | DDD 应用层，用例编排、事务边界、领域对象和仓储接口调用。 |
| `domain` | DDD 领域层，聚合根、值对象、领域服务、领域事件、仓储接口。 |
| `api` | 跨模块 API、DTO、枚举、RPC / CommonApi 契约。 |
| `enums` | 模块内枚举、错误码、状态值等。 |
| `controller` | REST 控制器，包含管理端、用户端请求入口和 VO。 |
| `convert` | 对象转换层，通常使用 MapStruct 处理 VO / DTO / DO / Domain 转换。 |
| `dal` | 数据访问层，包含 DO、MyBatis Mapper、Redis Key 等。 |
| `framework` | 模块内 Spring 配置、拦截器、扩展点。 |
| `infrastructure` | DDD 基础设施层，仓储实现、外部系统适配、事件发布适配。 |
| `service` | 传统三层业务服务接口与实现，是 DDD 迁移的重要来源。 |
| `job` | XXL-Job 定时任务处理器。 |
| `mq` | 消息生产者、消费者和消息体。 |

## 关键依赖

未发现需要在 README 中强调的直接业务 API 或 Starter 依赖。

## 架构职责

- 本模块是业务域聚合 POM，用于组织该业务域下的 API、Server 或子域模块。
- 聚合模块只负责 Maven reactor 编排和统一构建，不应放置业务代码。
- 跨模块调用应优先依赖 API 子模块，业务实现应收敛在对应 Server 子模块。

## DDD / 分层说明

本模块已出现 DDD / 六边形相关目录。新增或迁移核心业务逻辑时，应优先遵循以下方向：

- `domain` 保持纯 Java，承载聚合根、值对象、领域服务、领域事件和仓储接口。
- `application` 负责编排用例、事务边界和领域对象协作。
- `infrastructure` 负责仓储实现、MyBatis / Redis / 外部系统适配。
- `service` 与 `dal` 中的旧业务逻辑是迁移来源，不应作为新增核心业务规则的最终归宿。

## 构建与验证

```bash
# 编译该聚合模块及其子模块
mvn compile -pl develop-module-mall -am

# 打包该聚合模块及其子模块
mvn clean package -pl develop-module-mall -am -Dmaven.test.skip=true
```

## 维护建议

- 修改跨模块契约时，优先更新 `api` 子模块，并检查所有调用方兼容性。
- 修改业务实现时，优先补充或更新模块级测试，至少运行当前模块的 `mvn test` 或 `mvn compile`。
- 涉及 DDD 聚合、模块结构或 API 契约调整时，同时更新本 README 与根目录架构文档。
- 不要在聚合 POM 模块中放置业务逻辑；业务逻辑应位于具体 `server` 子模块。
