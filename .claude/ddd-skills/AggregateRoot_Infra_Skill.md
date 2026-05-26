---
name: aggregate-root-infra-skill
description: Use when modifying or reviewing Infra config, datasource, file, file config, codegen, logger, or WebSocket DDD migration boundaries.
type: ddd-aggregate-skill
status: production-review
---

# AggregateRoot Infra Skill

## AI Execution Contract

- **Scope:** 每次只处理本文件声明的一个聚合、一个子域或一个最小闭环；多聚合文件必须先拆分到目标子聚合后再实现。
- **Must Read:** 修改前读取本 skill 的 Current Source Anchors，以及对应 Controller、VO/DTO、DO、Mapper、Convert、Service/Application、Repository、ErrorCode、测试文件。
- **Must Preserve:** Controller 路径、HTTP 方法、VO/DTO 字段、CommonApi/Feign/RPC 契约、权限、租户、数据权限、错误码、分页、Excel、MQ、Job、缓存、第三方回调和 OpenAPI 可见行为。
- **Allowed Changes:** 只在目标聚合的 `domain`、`application`、`infrastructure`、`convert`、入口适配和对应测试内做最小必要修改，并按标准骨架补齐端口或 package 边界。
- **Forbidden Changes:** 禁止批量改无关聚合；禁止把新核心业务写入旧 `service/dal`；禁止让 domain 依赖 Spring、MyBatis、Feign、Mapper、DO、Controller VO、RPC client 或基础设施实现。
- **Dependency Rules:** domain 只依赖领域对象和值对象；application 编排用例、事务和端口；infrastructure 适配 Mapper/DO/外部系统；controller/job/mq 只做入口。
- **Verification Gate:** 完成前运行本 skill 的 Verification Commands；无法运行时写明命令、阻塞原因和未验证风险。
- **Stop Conditions:** 事实源缺失、skill 与当前代码冲突、外部契约可能变化、字段/错误码/事务需要猜测、验证失败时停止并先修订 skill 或缩小范围。

## Standard Skeleton Contract

目标聚合必须固定以下职责边界；Java 空目录用职责明确的接口或 `package-info.java` 固定，禁止 `Temp`/`Placeholder`/`Dummy`：

```text
domain/{aggregate}/model,valueobject,event,service,repository
application/{aggregate}/command,query,dto|result,port/inbound,port/outbound,service
infrastructure/{aggregate}/persistence,external,rpc,cache,messaging
convert/
controller/ job/ mq/ framework/
```

旧 `service/dal` 是迁移源，不是新核心业务最终落位。

## Quick Reference

| 要做什么 | 正确位置 | 禁止位置 |
|---|---|---|
| 业务不变量 | `domain/{aggregate}` | `controller`、`convert`、`dal` |
| 用例编排和事务 | `application/{aggregate}/service` | `domain` 或 Controller |
| 入站用例契约 | `application/{aggregate}/port/inbound` | Controller 私有方法 |
| 外部能力端口 | `application/{aggregate}/port/outbound` | domain 或 infrastructure 反向定义 |
| 仓储接口 | `domain/{aggregate}/repository` | infrastructure 反向定义业务端口 |
| Mapper/DO 适配 | `infrastructure/{aggregate}/persistence` | domain/application 直接调用 |
| 对象转换 | `convert` | domain 聚合内 |

## AI Self-Check

- 已读取当前事实源，而不是只依据本 skill 猜测。
- 未改变 Controller/API/VO/DTO/权限/租户/数据权限/错误码/分页/Excel/MQ/Job/缓存/回调契约。
- domain 未依赖 Spring、MyBatis、Feign、Mapper、DO、VO、DTO 或基础设施实现。
- 标准目录、入站端口、出站端口、领域仓储和 infrastructure 适配边界没有因“当前为空”被省略。
- 已运行本文件列出的验证命令，或明确记录无法验证的原因。

## 0. Overview

Infra 是平台基础设施上下文，承载参数配置、数据源配置、文件存储、代码生成、API 访问日志、API 错误日志和 WebSocket 发送契约。任何 DDD 或 API local/remote 重构必须保持 Controller/API/DTO/错误码/租户/缓存/FileClient/代码生成/日志兜底行为不变。

## 1. When to Use / Not Use

Use when:

- 重构或验证 `develop-module-infra` 的 DDD 分层、聚合、应用服务、仓储或转换层。
- 拆分 `ConfigApi`、`FileApi`、`WebSocketSenderApi` 为稳定契约 + local/remote 适配器。
- 迁移 `config`、`db`、`file`、`codegen`、`logger` 旧 `service/dal` 逻辑到 `domain/application/infrastructure/convert`。
- 修改文件上传、文件配置、FileClient 缓存、代码生成、数据库表结构同步、API 日志清理或错误日志处理逻辑。
- 处理 Infra 与租户、动态数据源、文件存储、WebSocket、代码生成模板和日志记录的集成边界。

Do not use when:

- 只修改普通配置值、SQL 初始化数据、README 文案或 demo 示例页面。
- 只调整 `develop-framework` 的通用 starter，且不改变 Infra 模块外部契约。
- 只运行构建、启动或排查环境问题，不修改 Infra 代码或 skill。
- 修改 `job` 相关独立调度能力时；该错误码段存在于 Infra，但本 skill 不覆盖 XXL-Job 模块重构。
- 修改 demo 示例聚合时；demo 代码是样例，不作为 Infra 核心生产聚合迁移目标。

## 2. Baseline Failure Findings

升级前草稿暴露的问题：

1. 没有 YAML frontmatter，不能被稳定发现和判定 production-ready。
2. 只列聚合和值对象，缺少 Controller、API、VO/DTO、DO、Mapper、Service、Repository、ErrorCode、测试路径事实锚点。
3. 把 Infra 七个聚合混在一起描述，但没有说明哪些仍由 legacy service 承载生产行为。
4. 未记录稳定 API 目前仍带 `@FeignClient`，与 local/remote 契约标准冲突。
5. 未记录 Config DDD create 流使用 `ConfigFactory.create(null, ...)` 的 ID-null 风险。
6. 未记录 FileConfig DDD `testFileConfig` 当前只返回 `test ok`，与 legacy 实际上传测试文件行为不等价。
7. 未记录 Codegen DDD application service 远未覆盖 legacy `CodegenServiceImpl` 的导入、同步、生成、主子表和模板引擎行为。
8. 未记录 `FileConfigServiceImpl` 的 FileClient Guava cache、`CACHE_MASTER_ID = 0L` 和缓存失效行为。
9. 未记录 API access/error log 在无租户上下文时必须 `TenantUtils.executeIgnore(...)`，错误日志创建异常不能影响主流程。
10. 未给出错误码契约、事务边界、测试命令、红旗和回滚条件，容易让 AI 直接按文档猜代码。

## 3. Reproducibility Contract

执行本 skill 必须遵守：

1. 先读本文件，再读 `.claude/ddd-skills/DDD_Skill_Production_Readiness_Standard.md` 和 `.claude/ddd-skills/Module_Structure_Standard.md`。
2. 修改 Java 前必须读取本 skill 中列出的事实源；不要凭包名、旧草稿或相似模块猜字段、错误码、事务、缓存和集成行为。
3. 当前可编译代码的外部行为优先：Controller 路径、HTTP 方法、VO/DTO 字段、`CommonResult` 包装、权限、租户、错误码、分页、Excel、FileClient、WebSocket、代码生成输出和日志兜底不得被 DDD 设计覆盖。
4. 如果 skill 与当前代码冲突，先停止实现，读取当前事实源，修订 skill，再改代码。
5. 每批只处理一个小上下文：API 契约拆分、Config、FileConfig/File、DataSourceConfig、Codegen、Logger 或 WebSocket，不一次性改全 Infra。
6. `service/dal` 是迁移源，不是最终目标；但在迁移完成前，legacy service 仍是生产行为事实源。
7. 没有业务代码改动时不要求 Maven；修改 Java 后至少编译 `develop-module-infra-api` 和 `develop-module-infra-server`。

## 4. Current Source Anchors

### 4.1 API module contracts

- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/api/config/ConfigApi.java`
- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/api/file/FileApi.java`
- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/api/file/dto/FileCreateReqDTO.java`
- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/api/websocket/WebSocketSenderApi.java`
- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/api/websocket/dto/WebSocketSendReqDTO.java`
- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/enums/ApiConstants.java`
- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/enums/ErrorCodeConstants.java`
- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/enums/config/ConfigTypeEnum.java`
- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/enums/codegen/*.java`
- `develop-module-infra/develop-module-infra-api/src/main/java/com/develop/mvp/pk/module/infra/enums/logger/ApiErrorLogProcessStatusEnum.java`

Current API conflict: `ConfigApi`、`FileApi`、`WebSocketSenderApi` 仍直接带 `@FeignClient(name = ApiConstants.NAME)`。API local/remote 重构时必须把 Feign 身份移动到 `remote/*RemoteClient`，稳定契约本身保持方法签名和 DTO 不变。

### 4.2 Server API implementations

- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/config/ConfigApiImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/file/FileApiImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/logger/ApiAccessLogApiImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/logger/ApiErrorLogApiImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/api/websocket/WebSocketSenderApiImpl.java`

### 4.3 Controllers and VOs

- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/config/ConfigController.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/config/vo/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/db/DataSourceConfigController.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/db/vo/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileConfigController.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/file/FileController.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/file/vo/**/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/app/file/AppFileController.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/app/file/vo/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/codegen/CodegenController.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/codegen/vo/**/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/logger/ApiAccessLogController.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/logger/ApiErrorLogController.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/logger/vo/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/controller/admin/redis/RedisController.java`

### 4.4 Legacy production behavior sources

- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/config/ConfigServiceImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/db/DataSourceConfigServiceImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/db/DatabaseTableServiceImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/file/FileConfigServiceImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/file/FileServiceImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/codegen/CodegenServiceImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/codegen/inner/CodegenBuilder.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/codegen/inner/CodegenEngine.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/logger/ApiAccessLogServiceImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/service/logger/ApiErrorLogServiceImpl.java`

### 4.5 Current DDD layer

- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/domain/event/DomainEvent.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/domain/event/DomainEventPublisher.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/SpringDomainEventPublisher.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/domain/config/**/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/config/ConfigApplicationService.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/config/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/domain/db/**/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/db/DataSourceConfigApplicationService.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/db/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/domain/file/**/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/file/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/file/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/domain/codegen/**/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/codegen/CodegenApplicationService.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/codegen/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/domain/logger/**/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/application/logger/*.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/infrastructure/logger/*.java`

### 4.6 DAL and conversion

- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/dataobject/config/ConfigDO.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/dataobject/db/DataSourceConfigDO.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/dataobject/file/FileConfigDO.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/dataobject/file/FileDO.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/dataobject/file/FileContentDO.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/dataobject/codegen/CodegenTableDO.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/dataobject/codegen/CodegenColumnDO.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/dataobject/logger/ApiAccessLogDO.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/dataobject/logger/ApiErrorLogDO.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/mysql/config/ConfigMapper.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/mysql/db/DataSourceConfigMapper.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/mysql/file/FileConfigMapper.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/mysql/file/FileMapper.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/mysql/file/FileContentMapper.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/mysql/codegen/CodegenTableMapper.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/mysql/codegen/CodegenColumnMapper.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/mysql/logger/ApiAccessLogMapper.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/dal/mysql/logger/ApiErrorLogMapper.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/convert/config/ConfigConvert.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/convert/file/FileConfigConvert.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/convert/codegen/CodegenConvert.java`

### 4.7 Framework, jobs, integrations

- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/config/DevelopFileAutoConfiguration.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/client/FileClient.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/client/FileClientFactory.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/client/FileClientFactoryImpl.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/client/db/DBFileClient.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/client/local/LocalFileClient.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/client/s3/S3FileClient.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/client/ftp/FtpFileClient.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/client/sftp/SftpFileClient.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/enums/FileStorageEnum.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/file/core/utils/FileTypeUtils.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/codegen/config/CodegenConfiguration.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/codegen/config/CodegenProperties.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/framework/rpc/config/RpcConfiguration.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/job/logger/AccessLogCleanJob.java`
- `develop-module-infra/develop-module-infra-server/src/main/java/com/develop/mvp/pk/module/infra/job/logger/ErrorLogCleanJob.java`

### 4.8 Tests

- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/service/config/ConfigServiceImplTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/service/db/DataSourceConfigServiceImplTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/service/db/DatabaseTableServiceImplTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/service/file/FileConfigServiceImplTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/service/file/FileServiceImplTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/service/codegen/CodegenServiceImplTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/service/codegen/inner/*.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/service/logger/ApiAccessLogServiceImplTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/service/logger/ApiErrorLogServiceImplTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/application/db/DataSourceConfigApplicationServiceTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/application/file/FileConfigApplicationServiceTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/domain/db/DataSourceConfigTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/domain/file/FileConfigTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/domain/file/FileTest.java`
- `develop-module-infra/develop-module-infra-server/src/test/java/com/develop/mvp/pk/module/infra/framework/file/core/**/*.java`
- `develop-module-infra/develop-module-infra-server/src/test/resources/application-unit-test.yaml`
- `develop-module-infra/develop-module-infra-server/src/test/resources/sql/create_tables.sql`
- `develop-module-infra/develop-module-infra-server/src/test/resources/sql/clean.sql`

## 5. Fixed Data Model

### 5.1 ConfigDO → Config

`ConfigDO` (`infra_config`, `@TenantIgnore`) fields:

| Field | Type | Meaning | Notes |
|---|---|---|---|
| `id` | `Long` | 配置 ID | DB generated |
| `category` | `String` | 参数分类 | must map |
| `name` | `String` | 参数名称 | must map |
| `configKey` | `String` | 参数键名 | not Java `key`; unique |
| `value` | `String` | 参数值 | must map |
| `type` | `Integer` | `ConfigTypeEnum` | SYSTEM/CUSTOM |
| `visible` | `Boolean` | 是否可见 | invisible cannot be exposed through `getConfigValueByKey` |
| `remark` | `String` | 备注 | must map |

### 5.2 DataSourceConfigDO → DataSourceConfig

`DataSourceConfigDO` (`infra_data_source_config`, `@TenantIgnore`) fields:

| Field | Type | Meaning | Notes |
|---|---|---|---|
| `ID_MASTER` | `Long = 0L` | runtime master datasource pseudo-id | built from `DynamicDataSourceProperties`, not DB row |
| `id` | `Long` | 数据源配置 ID | DB generated except master pseudo-id |
| `name` | `String` | 连接名 | master uses dynamic datasource primary name |
| `url` | `String` | JDBC URL | connection validation required |
| `username` | `String` | 用户名 | required for validation |
| `password` | `String` | 密码 | encrypted by `EncryptTypeHandler` |

### 5.3 FileConfigDO → FileConfig

`FileConfigDO` (`infra_file_config`, `@TenantIgnore`) fields:

| Field | Type | Meaning | Notes |
|---|---|---|---|
| `id` | `Long` | 文件配置 ID | DB generated |
| `name` | `String` | 配置名 | must map |
| `storage` | `Integer` | `FileStorageEnum` | selects concrete `FileClientConfig` class |
| `remark` | `String` | 备注 | must map |
| `master` | `Boolean` | 是否主配置 | only one global master; master cannot be deleted |
| `config` | `FileClientConfig` | 存储客户端配置 | JSON type handler supports legacy class names |

### 5.4 FileDO → File

`FileDO` (`infra_file`, `@TenantIgnore`) fields:

| Field | Type | Meaning | Notes |
|---|---|---|---|
| `id` | `Long` | 文件 ID | DB generated |
| `configId` | `Long` | 文件配置 ID | controls which client reads/deletes file |
| `name` | `String` | 原文件名 | empty name fallback uses sha256 |
| `path` | `String` | 存储路径 | generated path must remain unique |
| `url` | `String` | 访问地址 | query string stripped before persistence |
| `type` | `String` | MIME type | infer from content/name if empty |
| `size` | `Long` | 文件大小 | content length |

### 5.5 CodegenTableDO and CodegenColumnDO

`CodegenTableDO` (`infra_codegen_table`, `@TenantIgnore`) key fields:

| Field | Type | Meaning |
|---|---|---|
| `id` | `Long` | 表定义 ID |
| `dataSourceConfigId` | `Long` | 数据源 ID |
| `scene` | `Integer` | `CodegenSceneEnum` |
| `tableName` / `tableComment` / `remark` | `String` | DB 表信息 |
| `moduleName` / `businessName` / `className` / `classComment` / `author` | `String` | Java 类和业务生成信息 |
| `templateType` | `Integer` | `CodegenTemplateTypeEnum` |
| `frontType` | `Integer` | `CodegenFrontTypeEnum` |
| `parentMenuId` | `Long` | 菜单生成父 ID |
| `masterTableId` / `subJoinColumnId` / `subJoinMany` | `Long` / `Long` / `Boolean` | 主子表关系 |
| `treeParentColumnId` / `treeNameColumnId` | `Long` | 树表关系 |

`CodegenColumnDO` (`infra_codegen_column`, `@TenantIgnore`) key fields:

| Field | Type | Meaning |
|---|---|---|
| `id` | `Long` | 字段定义 ID |
| `tableId` | `Long` | 所属表定义 ID |
| `columnName` / `dataType` / `columnComment` | `String` | DB 字段信息 |
| `nullable` / `primaryKey` | `Boolean` | DB 约束 |
| `ordinalPosition` | `Integer` | 字段顺序 |
| `javaType` / `javaField` | `String` | Java 字段信息 |
| `dictType` / `example` | `String` | 生成辅助信息 |
| `createOperation` / `updateOperation` / `listOperation` / `listOperationResult` | `Boolean` | CRUD 生成开关 |
| `listOperationCondition` | `String` | `CodegenColumnListConditionEnum` |
| `htmlType` | `String` | `CodegenColumnHtmlTypeEnum` |

### 5.6 Logger DOs

`ApiAccessLogDO` (`infra_api_access_log`) max constants:

- `REQUEST_PARAMS_MAX_LENGTH = 8000`
- `RESULT_MSG_MAX_LENGTH = 512`

Fields include `traceId`、`userId`、`userType`、`applicationName`、request fields、response fields、operation fields、`beginTime`、`endTime`、`duration`、`resultCode`、`resultMsg`.

`ApiErrorLogDO` (`infra_api_error_log`) max constants:

- `REQUEST_PARAMS_MAX_LENGTH = 8000`

Fields include user/request fields, exception fields, `processStatus`、`processTime`、`processUserId`.

## 6. Required Method Signatures and Capabilities

Required capabilities before replacing legacy entry paths. Signatures describe behavior parity; they do not mean Controller `ReqVO` types belong in final DDD application layer.

### 6.1 API contracts

Current stable contracts must keep method names, paths and return types until an API migration plan says otherwise:

```java
CommonResult<String> ConfigApi.getConfigValueByKey(String key);
CommonResult<String> FileApi.createFile(FileCreateReqDTO createReqDTO);
CommonResult<String> FileApi.presignGetUrl(String url, Integer expirationSeconds);
CommonResult<Boolean> WebSocketSenderApi.send(WebSocketSendReqDTO message);
```

Default helper methods on `FileApi` and `WebSocketSenderApi` are part of caller ergonomics and must continue to work after local/remote split.

### 6.2 Config capabilities

```java
Long createConfig(String key, String value, String name, String category, Integer type, Boolean visible, String remark);
void updateConfig(Long id, String key, String value, String name, String category, Integer type, Boolean visible, String remark);
void deleteConfig(Long id);
void deleteConfigList(List<Long> ids);
Config getConfig(Long id);
Config getConfigByKey(String key);
String getConfigValueByKey(String key);
PageResult<Config> getConfigPage(String name, String configKey, Integer type, LocalDateTime[] createTime, Integer pageNo, Integer pageSize);
```

### 6.3 DataSourceConfig capabilities

```java
Long createDataSourceConfig(String name, String url, String username, String password);
void updateDataSourceConfig(Long id, String name, String url, String username, String password);
void deleteDataSourceConfig(Long id);
void deleteDataSourceConfigList(List<Long> ids);
DataSourceConfig getDataSourceConfig(Long id);
List<DataSourceConfig> getDataSourceConfigList();
```

`id == DataSourceConfigDO.ID_MASTER` / `DataSourceConfig.ID_MASTER` must return runtime master datasource from `DynamicDataSourceProperties`, not DB.

### 6.4 FileConfig and File capabilities

```java
Long createFileConfig(String name, Integer storage, Boolean master, Map<String, Object> clientConfig, String remark);
void updateFileConfig(Long id, String name, Integer storage, Map<String, Object> clientConfig, String remark);
void updateFileConfigMaster(Long id);
void deleteFileConfig(Long id);
void deleteFileConfigList(List<Long> ids);
FileConfig getFileConfig(Long id);
PageResult<FileConfig> getFileConfigPage(String name, Integer storage, LocalDateTime[] createTime, Integer pageNo, Integer pageSize);
String testFileConfig(Long id) throws Exception;

String createFile(byte[] content, String name, String directory, String type) throws Exception;
String presignGetUrl(String url, Integer expirationSeconds) throws Exception;
String presignPutUrl(String path, Integer expirationSeconds) throws Exception;
void deleteFile(Long id) throws Exception;
void deleteFileList(List<Long> ids) throws Exception;
byte[] getFileContent(Long configId, String path) throws Exception;
PageResult<File> getFilePage(...);
```

### 6.5 Codegen capabilities

Legacy `CodegenServiceImpl` behavior must be preserved before Controller replacement:

```java
List<Long> createCodegenList(Long dataSourceConfigId, List<String> tableNames);
void updateCodegen(CodegenUpdateReqVO updateReqVO);
void syncCodegenFromDB(Long tableId);
void deleteCodegen(Long id);
void deleteCodegenList(List<Long> ids);
Map<String, String> generationCodes(Long tableId);
PageResult<CodegenTableDO> getCodegenTablePage(CodegenPageReqVO pageReqVO);
List<DatabaseTableRespVO> getDatabaseTableList(Long dataSourceConfigId, String name, String comment);
```

### 6.6 Logger capabilities

```java
void createApiAccessLog(ApiAccessLogCreateReqDTO createDTO);
PageResult<ApiAccessLog> getApiAccessLogPage(ApiAccessLogPageQuery query);
Integer cleanAccessLog(Integer exceedDay, Integer deleteLimit);

void createApiErrorLog(ApiErrorLogCreateReqDTO createDTO);
void processApiErrorLog(Long id, Integer processStatus, Long processUserId);
PageResult<ApiErrorLog> getApiErrorLogPage(ApiErrorLogPageQuery query);
Integer cleanErrorLog(Integer exceedDay, Integer deleteLimit);
```

Error log creation must catch/log failures in production logging path so API error logging never breaks the caller.

## 7. Business Rules

### 7.1 Config

| Rule | Layer | Required behavior |
|---|---|---|
| INF-CFG-01 | application/domain | create/update validates global key uniqueness, excluding current id on update |
| INF-CFG-02 | application/domain | create defaults type to `CUSTOM` where legacy create path does so |
| INF-CFG-03 | application/domain | SYSTEM config cannot be deleted individually or in batch |
| INF-CFG-04 | application/API | `getConfigValueByKey` returns `null` when missing |
| INF-CFG-05 | application/API | `getConfigValueByKey` throws `CONFIG_GET_VALUE_ERROR_IF_VISIBLE` for invisible config |
| INF-CFG-06 | API | stable contract path remains `/infra/config/get-value-by-key` until API migration plan changes it |

### 7.2 DataSourceConfig

| Rule | Layer | Required behavior |
|---|---|---|
| INF-DB-01 | application | create/update validates JDBC connectivity through `JdbcUtils.isConnectionOK` |
| INF-DB-02 | application | `id == 0L` returns master datasource from `DynamicDataSourceProperties` |
| INF-DB-03 | application | datasource list prepends master datasource before DB records |
| INF-DB-04 | infrastructure | password remains encrypted/decrypted through `EncryptTypeHandler` at DO layer |

### 7.3 FileConfig and File

| Rule | Layer | Required behavior |
|---|---|---|
| INF-FC-01 | application/infrastructure | file client cache uses master pseudo-key `0L` in legacy path |
| INF-FC-02 | application | create file config defaults master to false unless caller explicitly supports master behavior with equivalent semantics |
| INF-FC-03 | application | update config invalidates the specific client cache and master cache if needed |
| INF-FC-04 | application | setting master clears all current masters, then sets target master in one transaction |
| INF-FC-05 | application | deleting master config throws `FILE_CONFIG_DELETE_FAIL_MASTER` |
| INF-FC-06 | application | `testFileConfig` must upload `file/erweima.jpg` using target `FileClient` and return uploaded URL; returning only `test ok` is not production-equivalent |
| INF-F-01 | application | file upload uses master file client |
| INF-F-02 | domain/application | empty MIME type is inferred from content/name |
| INF-F-03 | domain/application | empty name uses SHA256 of content |
| INF-F-04 | domain/application | name without extension appends extension from MIME type when available |
| INF-F-05 | domain/application | generated path keeps date prefix behavior and uniqueness behavior |
| INF-F-06 | application/infrastructure | persisted URL removes query string |
| INF-F-07 | application/infrastructure | delete removes object storage content before deleting DB metadata |
| INF-F-08 | API | `FileApi.createFile` returns file URL/path exactly as before; caller default methods keep working |

### 7.4 Codegen

| Rule | Layer | Required behavior |
|---|---|---|
| INF-CG-01 | application | importing table validates table exists and columns exist |
| INF-CG-02 | application/domain | table name must be unique per datasource |
| INF-CG-03 | application/domain | table comment and column comments are required |
| INF-CG-04 | application | create defaults scene to `ADMIN` and front type from `CodegenProperties` |
| INF-CG-05 | application/domain | if DB table has no primary key, first column is marked primary key |
| INF-CG-06 | application/domain | sub-table template requires existing master table and join column |
| INF-CG-07 | application | sync compares DB metadata and throws `CODEGEN_SYNC_NONE_CHANGE` if nothing changed |
| INF-CG-08 | application/infrastructure | sync preserves existing column IDs where matching fields remain |
| INF-CG-09 | application | delete removes table definition and all column definitions in one transaction |
| INF-CG-10 | application | generation validates columns exist, resolves DB type, loads sub tables, validates sub join columns, then delegates to `CodegenEngine` |
| INF-CG-11 | application | `DatabaseTableServiceImpl` must continue excluding views and configured system/workflow/job tables where current code does so |

### 7.5 Logger

| Rule | Layer | Required behavior |
|---|---|---|
| INF-LOG-01 | application | access log truncates `requestParams` to 8000 and `resultMsg` to 512 |
| INF-LOG-02 | application | error log truncates `requestParams` to 8000 |
| INF-LOG-03 | application | log insert with no tenant context uses `TenantUtils.executeIgnore(...)` |
| INF-LOG-04 | application | error log create defaults `processStatus` to `INIT` |
| INF-LOG-05 | domain/application | error log can be processed only from INIT to DONE/IGNORE |
| INF-LOG-06 | application | access/error clean jobs delete in batches until deleted count is below limit |
| INF-LOG-07 | application | API error log creation failure logs and swallows exception in production logging path |

### 7.6 WebSocket

| Rule | Layer | Required behavior |
|---|---|---|
| INF-WS-01 | API/application | if `sessionId` present, send by session |
| INF-WS-02 | API/application | else if `userType` and `userId` present, send to user |
| INF-WS-03 | API/application | else if only `userType` present, broadcast to user type |
| INF-WS-04 | API/application | current implementation returns `CommonResult.success(true)` even when no route matched |
| INF-WS-05 | API | object helper methods serialize content with `JsonUtils.toJsonString` |

## 8. Error Code Contract

| Scenario | ErrorCodeConstants | Parameters | Throw layer |
|---|---|---|---|
| Config not found | `CONFIG_NOT_EXISTS` | none | application/service |
| Config key duplicate | `CONFIG_KEY_DUPLICATE` | none | application/service |
| Delete system config | `CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE` | none | application/service |
| Read invisible config value | `CONFIG_GET_VALUE_ERROR_IF_VISIBLE` | none | API/application |
| File config not found | `FILE_CONFIG_NOT_EXISTS` | none | application/service |
| Delete master file config | `FILE_CONFIG_DELETE_FAIL_MASTER` | none | application/service |
| File not found | `FILE_NOT_EXISTS` | none | application/service |
| Empty file content | `FILE_IS_EMPTY` | none | boundary/application when enforced |
| Datasource config not found | `DATA_SOURCE_CONFIG_NOT_EXISTS` | none | application/service |
| Datasource connection invalid | `DATA_SOURCE_CONFIG_NOT_OK` | none | application/service |
| Codegen table duplicate | `CODEGEN_TABLE_EXISTS` | none | application/service |
| Imported table missing | `CODEGEN_IMPORT_TABLE_NULL` | none | application/service |
| Imported columns missing | `CODEGEN_IMPORT_COLUMNS_NULL` | none | application/service |
| Codegen table not found | `CODEGEN_TABLE_NOT_EXISTS` | none | application/service |
| Codegen column missing | `CODEGEN_COLUMN_NOT_EXISTS` | none | application/service |
| Sync columns missing | `CODEGEN_SYNC_COLUMNS_NULL` | none | application/service |
| Sync no changes | `CODEGEN_SYNC_NONE_CHANGE` | none | application/service |
| DB table comment missing | `CODEGEN_TABLE_INFO_TABLE_COMMENT_IS_NULL` | none | application/service |
| DB column comment missing | `CODEGEN_TABLE_INFO_COLUMN_COMMENT_IS_NULL` | column name | application/service |
| Master table missing | `CODEGEN_MASTER_TABLE_NOT_EXISTS` | master table id | application/service |
| Sub join column missing | `CODEGEN_SUB_COLUMN_NOT_EXISTS` | column id | application/service |
| Master generation without sub table | `CODEGEN_MASTER_GENERATION_FAIL_NO_SUB_TABLE` | none | application/service |
| API error log not found | `API_ERROR_LOG_NOT_FOUND` | none | application/service |
| API error log already processed | `API_ERROR_LOG_PROCESSED` | none | application/service |

Current duplicate-code note: `CODEGEN_TABLE_EXISTS` and `CODEGEN_IMPORT_COLUMNS_NULL` both use numeric code `1_001_004_002`; preserve this external behavior unless a separate error-code migration is approved.

## 9. Transaction Contract

| Use case | Required transaction |
|---|---|
| Config create/update/delete/deleteList | `@Transactional(rollbackFor = Exception.class)` or equivalent rollback behavior |
| DataSourceConfig create/update/delete/deleteList | transactional; connection validation occurs before persistence |
| FileConfig update master | transactional; clear all masters and set target as one unit |
| FileConfig create/update/delete/deleteList | transactional when moving to DDD; cache invalidation must still happen after mutation |
| File upload | transactional around DB metadata; external file upload side effect must not be hidden or retried blindly |
| File delete/deleteList | delete object storage plus metadata; avoid committing DB delete if storage deletion fails unless current behavior is explicitly changed |
| Codegen import/update/sync/delete/generation | transactional with rollback for `Exception` as current `CodegenServiceImpl` does |
| ApiAccessLog clean | transactional batch deletion |
| ApiErrorLog process/clean | transactional |
| ApiAccessLog/ErrorLog create | logging path must not break caller; tenant ignore and swallow behavior for error log must be preserved |
| WebSocket send | no DB transaction; route and delegate to `WebSocketMessageSender` |

## 10. Integration Contract

- **API local/remote:** stable `ConfigApi`、`FileApi`、`WebSocketSenderApi` must keep method signatures; Feign identity moves to `remote/*RemoteClient` only when doing API contract split.
- **FileClient:** `FileConfigServiceImpl` currently owns Guava `LoadingCache<Long, FileClient>` with async reload and `CACHE_MASTER_ID = 0L`. A DDD migration must preserve cache semantics or move them to an infrastructure adapter with identical behavior.
- **File config parsing:** `FileStorageEnum.getByStorage(storage).getConfigClass()` + JSON map conversion + `ValidationUtils.validate(validator, clientConfig)` must remain.
- **FileConfigDO type handler:** `FileClientConfigTypeHandler` supports legacy `@class` names for `DBFileClientConfig`、`FtpFileClientConfig`、`LocalFileClientConfig`、`SftpFileClientConfig`、`S3FileClientConfig`; do not remove compatibility accidentally.
- **Dynamic datasource:** master datasource is runtime config from `DynamicDataSourceProperties`, not a normal DB row.
- **JDBC validation:** data source create/update must keep `JdbcUtils.isConnectionOK` behavior.
- **Codegen:** `DatabaseTableService`、`CodegenBuilder`、`CodegenEngine`、`CodegenProperties` and MyBatis-Plus Generator are production behavior sources.
- **Tenant:** Config/DataSource/File/Codegen DOs use `@TenantIgnore`; access/error logs require special no-tenant insertion handling.
- **Jobs:** `AccessLogCleanJob` and `ErrorLogCleanJob` must continue invoking application/service clean use cases rather than duplicating cleanup logic.
- **WebSocket:** `WebSocketSenderApiImpl` delegates to framework `WebSocketMessageSender`; do not move WebSocket send decisions into domain.
- **Domain events:** current DDD services publish via `DomainEventPublisher` implemented by `SpringDomainEventPublisher`. Domain must not import Spring publisher.

## 11. Mapping Rules

- `controller/vo` objects may be used by Controller and legacy service during migration, but final domain repositories must not import Controller VOs.
- `dal/dataobject/*DO` and Mapper stay in infrastructure/DAL; domain must not import MyBatis annotations, Mapper, DO, Controller VO, Feign, Spring, or framework clients.
- `FileConfig` domain currently imports `FileClientConfig`, which is a framework infrastructure type. This is current technical debt; future migration should introduce a value object or adapter boundary rather than deepening the dependency.
- `ApiAccessLogApplicationService` currently imports `ApiAccessLogDO.REQUEST_PARAMS_MAX_LENGTH`; final target should move max length constants into domain/value object or an application constant, not import DO from application.
- `ConfigDO.configKey` maps to domain/key concept; do not map it to Java field name `key` without handling DB column limitations.
- `FileDO.url` persisted value must be query-stripped; `FileDO.path` is storage key, not public URL.
- `CodegenTable` must own `CodegenColumn` lifecycle conceptually; persistence may still use separate Mapper/DO.
- API DTOs (`FileCreateReqDTO`, `WebSocketSendReqDTO`) stay in API module; do not replace them with Controller VOs.

## 12. Current Conflict Notes

1. `ConfigApi`、`FileApi`、`WebSocketSenderApi` still include `@FeignClient`; this violates the target local/remote split but is current code. API split must preserve stable signatures and move annotations only to remote adapters.
2. `ConfigApplicationService#createConfig` calls `ConfigFactory.create(null, ...)`; if `ConfigId` disallows null or returns null ID after save, create-return-ID behavior can break.
3. `FileConfigApplicationService#testFileConfig` reads `file/erweima.jpg` but returns `"test ok"`; legacy `FileConfigServiceImpl#testFileConfig` uploads the sample file through the selected `FileClient` and returns URL.
4. `CodegenApplicationService#createCodegenList` accepts providers but does not use `tableName` correctly and does not reproduce legacy DB introspection/build/validation behavior. Do not replace `CodegenServiceImpl` entry paths with it until parity exists.
5. `CodegenApplicationService#syncCodegenFromDB` deletes all columns and saves supplied columns, while legacy sync computes new/changed/deleted columns and throws `CODEGEN_SYNC_NONE_CHANGE` if nothing changed.
6. `DataSourceConfigApplicationService` imports dynamic datasource and `JdbcUtils` directly in application layer. This is acceptable migration debt but final target should isolate technical checks behind an infrastructure port if needed.
7. `FileApplicationService` takes `FileClient` as parameter; domain must not know FileClient, and final application boundary should keep file storage as an infrastructure port.
8. `ApiAccessLogApplicationService` imports Mapper/DO in current code according to grep results; this violates target application dependency direction and must be fixed before treating it as final DDD.
9. `FileConfig` domain imports `FileClientConfig`, a framework type. This is current conflict with pure-domain rule.
10. Demo controllers/DO/Mapper are present under Infra but are not core production aggregate targets for this skill.

## 13. Acceptance Criteria

### Architecture AC

- Infra API module exposes stable contracts plus local/remote adapters when API split is performed.
- Stable API contracts do not carry Feign identity after split; Feign annotations live only in `remote/*RemoteClient`.
- Domain classes do not import Spring, MyBatis, Feign, Controller VO, Mapper, DO, `FileClient`, `JdbcUtils`, `DynamicDataSourceProperties`, or other infrastructure implementation types.
- Application services own use-case orchestration and transaction boundaries but not core domain invariants.
- Infrastructure implements repositories, FileClient adapters, dynamic datasource adapters, codegen engine adapters and cache/client integration.
- Controller/Job/API impls call application or compatibility service entry points and do not directly operate Mapper/DO.

### Behavior AC

- Config key uniqueness, SYSTEM delete protection and invisible config read protection match legacy behavior.
- DataSourceConfig master pseudo-id `0L`, list ordering and connection validation match legacy behavior.
- FileConfig master switching, master delete protection, config parsing, FileClient cache invalidation and `testFileConfig` upload behavior match legacy behavior.
- File upload name/type/path/url normalization, presign behavior, storage delete and metadata persistence match legacy behavior.
- Codegen import/update/sync/delete/generation behavior matches `CodegenServiceImpl`, including error codes and template/sub-table validation.
- Access/error logs keep truncation limits, tenant ignore insertion, batch cleanup and error-log creation swallow behavior.
- WebSocket send routing and helper methods match current `WebSocketSenderApi` and `WebSocketSenderApiImpl` behavior.

### Verification AC

- API-only split compiles `develop-module-infra-api` and `develop-module-infra-server`.
- Each migrated subdomain has targeted tests for its production behavior and current conflict notes.
- Existing legacy service tests continue passing until the corresponding entry path is intentionally replaced.
- If Maven cannot run due to environment, the blocker is recorded with exact command and failure.

Required regression names when implementing code changes:

- `configCreate_generatesNonNullIdAndRejectsDuplicateKey`
- `configGetValueByKey_invisible_throwsConfigGetValueErrorIfVisible`
- `dataSourceGetMaster_returnsRuntimePrimaryAndListPrependsMaster`
- `fileConfigUpdateMaster_clearsPreviousMasterInvalidatesMasterCache`
- `fileConfigTest_uploadsSampleFileAndReturnsUrl`
- `fileCreate_emptyNameAndType_normalizesNameTypePathAndStripsUrlQuery`
- `fileDelete_deletesStorageBeforeMetadata`
- `codegenCreate_rejectsDuplicateTableAndMissingComments`
- `codegenSync_noChange_throwsCodegenSyncNoneChange`
- `codegenGeneration_masterWithoutSubTable_throwsCodegenMasterGenerationFailNoSubTable`
- `apiAccessLog_noTenant_insertsWithTenantIgnoreAndTruncatesFields`
- `apiErrorLog_createFailure_doesNotBreakCaller`
- `websocketSend_routesBySessionThenUserThenUserType`

## 14. Verification Commands

For this skill document only:

```bash
git diff --check -- .claude/ddd-skills/AggregateRoot_Infra_Skill.md
grep -n "^## " .claude/ddd-skills/AggregateRoot_Infra_Skill.md
```

For API local/remote split:

```bash
mvn compile -pl develop-module-infra/develop-module-infra-api -am -DskipTests
mvn compile -pl develop-module-infra/develop-module-infra-server -am -DskipTests
```

For focused service/application behavior:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=ConfigServiceImplTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=DataSourceConfigServiceImplTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileConfigServiceImplTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileServiceImplTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=CodegenServiceImplTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=ApiAccessLogServiceImplTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=ApiErrorLogServiceImplTest
```

For current DDD units:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=DataSourceConfigApplicationServiceTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileConfigApplicationServiceTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=DataSourceConfigTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileConfigTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FileTest
```

For file client framework changes:

```bash
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=LocalFileClientTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=FtpFileClientTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=SftpFileClientTest
mvn test -pl develop-module-infra/develop-module-infra-server -Dtest=S3FileClientTest
```

## 15. Quick Reference

| Task | Correct place | Forbidden place |
|---|---|---|
| Stable infra API contract | `develop-module-infra-api/src/main/java/.../api/{business}/XxxApi.java` or future `XxxCommonApi.java` | server-only service interface |
| Remote Feign identity | API `remote/*RemoteClient` after split | stable contract itself |
| Local implementation | server API impl or API `local` adapter | copying a second different contract |
| Config invariant | `domain/config` + `application/config` | Controller/Mapper |
| FileClient cache | infrastructure adapter or compatibility service | domain aggregate |
| File storage upload/delete | application orchestrating infrastructure port | domain aggregate directly using FileClient |
| Codegen engine/template | infrastructure/service adapter | domain aggregate |
| Dynamic datasource runtime config | infrastructure/application adapter | domain aggregate |
| Tenant ignore log insert | application/infrastructure logging path | Controller |
| Object mapping | `convert/` or infrastructure mapper helpers | scattered in Controller/domain |
| Cleanup jobs | `job/` calls application/service clean use case | Job duplicating delete loops |
| WebSocket send routing | API impl/application facade | domain aggregate |

## 16. Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| Removing `@FeignClient` from stable API without adding remote adapter | remote callers break | create `remote/*RemoteClient` extending stable contract first |
| Treating current DDD application services as behavior-complete | codegen/file test behavior regresses | compare against legacy service tests before replacing entry paths |
| Moving FileClient into domain | domain depends on infrastructure | define application/infrastructure port and keep domain on file metadata/invariants |
| Returning `test ok` for file config test | UI/API no longer verifies real storage config | upload `file/erweima.jpg` through target FileClient and return URL |
| Ignoring FileClient cache invalidation | stale file storage config after update/master switch/delete | preserve invalidation for id and master key `0L` |
| Rewriting Codegen sync as delete-all/reinsert | loses column IDs and no-change behavior | preserve diff algorithm and `CODEGEN_SYNC_NONE_CHANGE` |
| Dropping `TenantUtils.executeIgnore` for logs | logging fails without tenant context | keep tenant-aware insert fallback |
| Letting logging exception escape | business API fails because log write failed | catch/log/swallow in production error log create path |
| Moving Controller VO into domain repository | cross-layer dependency remains | introduce domain query objects and map in application/convert |
| Deleting duplicate numeric error code as cleanup | external error handling may change | keep unless separate migration approved |

## 17. Rationalization Table

| Excuse | Reality |
|---|---|
| “Infra 已经有 DDD 目录，可以直接替换旧 service。” | Codegen、FileConfig、Logger 仍有 legacy-only behavior; first prove parity. |
| “FileClientConfig 是配置对象，放 domain 没事。” | It is a framework/infrastructure type; domain dependency direction is wrong. |
| “代码生成只是工具，不需要严格测试。” | Codegen emits source code and validates DB metadata; regressions are high blast radius. |
| “日志失败不重要，可以抛异常。” | 当前契约是日志记录不能影响主业务。 |
| “无租户时插入日志失败也没关系。” | 当前代码显式用 `TenantUtils.executeIgnore` 保护无租户日志。 |
| “local/remote split 只是移动注解。” | 还要保持默认方法、DTO、`CommonResult`、server impl 和消费者注入稳定。 |
| “批量重构整个 Infra 更快。” | Infra 同时含文件、代码生成、数据源、日志、WebSocket；必须分批。 |

## 18. Red Flags

Stop immediately if:

- Stable API method names, paths, request/response DTOs or `CommonResult` wrappers change without migration plan.
- Domain imports Spring, MyBatis, Feign, Controller VO, Mapper, DO, FileClient, dynamic datasource properties or codegen engine classes.
- FileConfig master switching no longer invalidates cache or no longer uses master key `0L` equivalent.
- `testFileConfig` no longer performs real upload through target client.
- Codegen entry path no longer calls DB introspection, builder and engine behavior equivalent to legacy service.
- `syncCodegenFromDB` loses no-change detection or column ID preservation.
- Logger create path can fail caller because tenant context is absent or DB insert throws.
- Error code constants, parameter order or duplicate numeric code behavior are “cleaned up” casually.
- Controller, job or API impl directly manipulates Mapper/DO in new code.
- A migration touches Config, File, Codegen and Logger in one batch.

## 19. Rollback Conditions

Rollback or stop the batch if:

1. `mvn compile -pl develop-module-infra/develop-module-infra-api -am -DskipTests` or server compile fails from the change.
2. Any Controller/API contract path, HTTP method, DTO field, error code or `CommonResult` behavior changes unintentionally.
3. Existing `ConfigServiceImplTest`、`FileConfigServiceImplTest`、`FileServiceImplTest`、`CodegenServiceImplTest`、logger tests regress.
4. File upload/delete leaves DB metadata inconsistent with object storage behavior.
5. Code generation output changes without an explicit expected-output test update.
6. Datasource master `0L` behavior or list ordering changes.
7. Access/error log insertion fails in no-tenant contexts.
8. WebSocket send no longer routes by session/user/userType with current precedence.
9. Migration requires deleting user’s uncommitted changes, resetting branches, or broad moving unrelated modules.

## 20. AI Self-Check

Before claiming Infra skill or code work is complete:

- [ ] Did I read current source anchors instead of relying on old draft text?
- [ ] Did I preserve stable API method signatures, paths, DTOs and `CommonResult`?
- [ ] Did I record or handle current `@FeignClient` conflict for local/remote split?
- [ ] Did I compare target DDD behavior against legacy service behavior before replacing entry paths?
- [ ] Did I preserve FileClient cache/master/test upload behavior?
- [ ] Did I preserve Codegen DB introspection, validation, sync diff and engine generation behavior?
- [ ] Did I preserve logger truncation, tenant ignore and no-fail logging behavior?
- [ ] Did I keep domain free of infrastructure imports, or document current conflict before fixing it?
- [ ] Did I run the verification commands appropriate to document-only or Java-code changes?
- [ ] Did I avoid widening scope beyond the selected Infra subdomain?
