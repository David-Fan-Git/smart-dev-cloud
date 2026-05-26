# Infra DDD Boundary Hardening Design

## 1. 背景与目标

`develop-module-infra` 已经具备 DDD/六边形骨架，但当前仍处于迁移中间态：旧 `service` 承载文件客户端缓存、主配置、日志兜底等生产行为，新 `application/domain/infrastructure` 尚未完整承接所有边界能力。

本设计目标是全面推进 Infra 边界收口，但按小闭环分阶段落地，避免一次性改动 Config、File、Codegen、Logger、WebSocket 等多个上下文。改造必须保持 Controller/API 路径、DTO 字段、`CommonResult` 包装、权限、租户、错误码、文件缓存、日志兜底和动态数据源 master 行为不变。

## 2. 执行范围闸门

本文是 Infra 边界加固总路线，不是单个实施 PR 的全部范围。每次实施必须遵守以下闸门：

1. 单次实施只允许一个子域或一个最小闭环，例如仅 File 链路、仅仓储查询边界、仅 API local/remote 适配。
2. 未完成上一阶段的编译、测试、架构验收和回滚条件检查，不得进入下一阶段。
3. 每个阶段实施前必须列出本阶段修改文件、事实源、验证命令和回滚条件。
4. 如果阶段内发现需要同时修改 Config、File、Logger、DataSource 或 Codegen 多个上下文，必须停止并拆分成独立子任务。
5. 旧 `service/dal` 在对应生产行为被测试证明等价前，只能作为迁移源和兼容壳保留，不能直接删除。

## 3. 设计原则

1. 旧 `service/dal` 是迁移源和生产事实源，不能因为 DDD 目录已存在就直接删除。
2. 优先收口调用边界，不改变业务语义。
3. 保留 `FileConfigServiceImpl` 的 `LoadingCache`、master 伪 key `0L`、缓存失效和文件配置测试上传行为。
4. Controller/API 只调用 application inbound port，不再理解或传递 `FileClient` 等基础设施对象。
5. Repository 实现不依赖 Controller VO，使用 domain query 或 infrastructure 查询对象承接 Mapper 查询。
6. 设计模式只服务明确变化点：文件存储策略、客户端工厂、仓储适配、领域事件发布和 local/remote 适配；不为套模式制造空抽象。
7. 所有阶段必须保护历史修复和兜底逻辑，尤其是文件客户端缓存、无租户日志写入、错误日志吞异常、动态数据源 master 伪 ID。

## 4. 标准骨架约束

阶段实施时必须满足本仓库 DDD/六边形目录约束。Java 空目录无法被 Git 追踪时，使用职责明确的接口或 `package-info.java` 固定边界，禁止使用 `Temp`、`Placeholder`、`Dummy` 类。

```text
domain/{aggregate}/
  model/
  valueobject/
  event/
  service/
  repository/
application/{aggregate}/
  command/
  query/
  dto 或 result/
  port/
    inbound/
    outbound/
  service/
infrastructure/{aggregate}/
  persistence/
  external/
  rpc/
  cache/
  messaging/
convert/
controller/
job/
mq/
framework/
```

强制依赖规则：

1. `domain` 不得依赖 Spring、MyBatis、Feign、Controller VO、Mapper、DO、`FileClient`、动态数据源配置或基础设施实现。
2. `application` 可以依赖 domain repository、inbound/outbound port、convert 和框架事务能力，但不得直接依赖 Mapper/DO。
3. `infrastructure` 实现 repository、outbound port、缓存、RPC、外部系统和技术适配。
4. Controller/API/Job/MQ 只作为入口适配层，不承载领域规则和持久化细节。
5. 当前已有技术债必须在实施阶段记录冲突清单和退场条件，不能继续扩大依赖方向错误。

## 5. 推荐方案

采用“全面推进、分阶段小闭环”方案。

### 阶段 1：文件链路调用边界

- 明确 `application/file/port/inbound/FileUseCase` 是入站用例接口，`application/file/service/FileApplicationService` 是实现。
- 调整 `FileUseCase`，不再暴露 `FileClient`、`Function<Long, FileClient>` 或 `BiFunction<Long, String, byte[]>` 参数。
- Controller/API 上传、删除、批量删除、预签名、下载入口只传业务参数。
- `FileController#getFileContent` 必须纳入 `FileUseCase#getFileContent(Long configId, String path)`，Controller 不再直接获取 `FileClient`。
- `FileController#getFilePresignedUrl` 必须调用 application 预签名用例，Controller 不再直接调用 master client。
- 新增 application outbound port，例如 `FileStoragePort` 或 `FileClientGateway`。
- 基础设施实现继续复用旧 `FileConfigServiceImpl#getMasterFileClient()` 和 `getFileClient(id)`。
- 上传 name/type/path/url 规范化和事务边界仍由 application 承担。
- 不删除旧文件服务和文件配置缓存逻辑。

入口迁移矩阵：

| 入口 | 当前职责 | 目标 FileUseCase 方法 | 旧调用点处理 | 删除旧签名前完成条件 |
|---|---|---|---|---|
| `FileController#uploadFile` | 管理后台后端上传 | `createFile(byte[] content, String name, String directory, String type)` | 移除 Controller 直接获取 master `FileClient` | admin 上传测试、API 上传测试、legacy 上传测试通过 |
| `FileController#getFilePresignedUrl` | 管理后台前端直传预签名 | `presignPutUrl(String name, String directory)` 或等价返回对象方法 | 移除 Controller 直接调用 master client 预签名 | 响应 `configId/path/uploadUrl/url` 与当前一致 |
| `FileController#createFile` | 前端直传后创建记录 | `createFileRecord(Long configId, String name, String path, String url, String type, Long size)` | 保持当前入口和 DTO 不变 | URL 去 query 与 DB 记录行为一致 |
| `FileController#deleteFile` | 管理后台单文件删除 | `deleteFile(Long id)` | 移除 Controller 根据 configId 获取 `FileClient` | 删除顺序和异常语义测试通过 |
| `FileController#deleteFileList` | 管理后台批量删除 | `deleteFileList(List<Long> ids)` | 移除 Controller 传入 client provider | 批量失败策略测试通过 |
| `FileController#getFileContent` | 公开下载 | `getFileContent(Long configId, String path)` | 移除 Controller 直接获取 `FileClient` | 404 和 attachment 行为测试通过 |
| `FileController#getFilePage` | 管理后台分页 | `getFilePage(String path, String type, LocalDateTime[] createTime, Integer pageNo, Integer pageSize)` | 保持入口不变 | 分页结构和权限不变 |
| `AppFileController` 文件上传入口 | App 端文件上传 | `createFile(byte[] content, String name, String directory, String type)` | 移除 App Controller 直接或间接获取 `FileClient` | App 路径、响应和权限不变 |
| `FileApiImpl#createFile` | 跨模块文件上传 API | `createFile(byte[] content, String name, String directory, String type)` | 移除 API 实现直接获取 master `FileClient` | `FileApi` 默认 helper 和 `CommonResult#getCheckedData` 语义不变 |
| `FileApiImpl#presignGetUrl` | 跨模块预签名下载 API | `presignGetUrl(String resourceUrl, Integer expirationSeconds)` | 移除 API 实现直接调用 master client | 完整 URL/path 两类输入均兼容 |

签名迁移策略：

1. 先在 `FileUseCase` 新增无技术客户端参数的新方法，并在 `FileApplicationService` 实现。
2. 再按入口迁移矩阵修改 `FileController`、`AppFileController`、`FileApiImpl` 调用新方法。
3. 三类入口全部切换且回归测试通过后，才删除旧带 `FileClient`、`Function`、`BiFunction` 的签名。
4. 每一步必须保持 `develop-module-infra-server` 可编译。

### 阶段 2：文件客户端能力边界

- 将预签名调用集中到 outbound port 或 facade。
- 暂不破坏 `FileClient` 现有接口，避免大面积改动。
- 对非 S3 等不支持预签名的存储，不静默降级，保持明确失败语义。
- `presignGetUrl` 入参语义固定为 `resourceUrl`，可以是完整 URL 或 path；adapter 必须统一去 query、decode、提取 path，并通过规范表测试覆盖。
- `resourceUrl` 归一化规范表必须至少覆盖：完整 domain URL、纯 path、path + query、URL 编码 path、公开访问 URL、私有访问 URL；每类输入都要定义归一化后的 object key 和预期签名行为。
- `/infra/file/presigned-url` 返回字段必须保持 `configId`、`path`、`uploadUrl`、`url` 不变，且 `configId` 必须与实际签名所用 client 一致，当前为 master client id。
- 后续可单独拆出 `PresignFileClient` 能力接口，当前阶段只收口调用位置。

### 阶段 3：仓储去 Controller VO 依赖

- `ConfigRepositoryImpl`、`FileRepositoryImpl`、`FileConfigRepositoryImpl` 不再构造 Controller `*PageReqVO`。
- Repository 使用 `ConfigPageQuery`、`FilePageQuery`、`FileConfigPageQuery` 承接查询参数。
- 同步改造 `ConfigMapper`、`FileMapper`、`FileConfigMapper` 及对应 XML 或注解查询，新增面向 query 的查询方法，或在 infrastructure 内使用 MyBatis Plus 条件构造器。
- 新查询必须保持与现有 `selectPage(*PageReqVO)` 完全等价的 where、order、page 语义。
- Controller VO 和 API 响应不变。

### 阶段 4：API local/remote 补齐

- API 模块保持稳定契约、默认 helper 方法和 DTO 不变。
- 稳定契约接口只定义业务方法，不承载远程调用身份。
- `remote/*RemoteClient` 承担 Feign 身份。
- `local/*LocalAdapter` 承担本地适配。
- local 与 remote 必须复用同一 DTO、返回类型和默认 helper 方法语义。
- 补齐 local 适配时必须明确 bean 命名和条件装配规则，避免 `NoUniqueBeanDefinition` 或意外替换当前 remote 行为。
- 单体 `develop-server` 模式默认启用 local adapter；remote adapter 仅在显式开启 RPC/Feign 条件时生效。
- local/remote 必须给出 bean 命名规范、`@ConditionalOn...` 或等价条件装配规则，并补充冲突检测测试。
- 默认行为必须与当前一致；如果需要改变注入选择策略，单独写迁移说明。

### 阶段 5：日志与数据源边界补强

- 保留访问日志无租户上下文时 `TenantUtils.executeIgnore` 的行为。
- `ApiErrorLogUseCase` 必须包含 `createApiErrorLog(ApiErrorLogCreateReqDTO)` 能力。
- 错误日志创建实现必须保留旧 `try-catch` 兜底与无租户写入语义，任何日志写入异常不得向上抛出影响主业务。
- 保留访问/错误日志字段截断规则。
- 保留数据源 `id == 0L` 返回 runtime master datasource，列表首位插入 master，创建/更新使用 `JdbcUtils.isConnectionOK`。
- 对 `DynamicDataSourceProperties#getPrimary()` 或对应 `DataSourceProperty` 缺失的情况给出受控异常和明确日志，禁止 NPE 直接暴露。
- 本阶段只做边界与测试补强，不改变外部行为。

## 6. 组件设计

### 6.1 FileUseCase

`FileUseCase` 应表达业务用例，而不是技术客户端编排。目标能力包括：

- `createFile(byte[] content, String name, String directory, String type)`
- `createFileRecord(Long configId, String name, String path, String url, String type, Long size)`
- `deleteFile(Long id)`
- `deleteFileList(List<Long> ids)`
- `presignPutUrl(String name, String directory)` 或等价业务方法
- `presignGetUrl(String resourceUrl, Integer expirationSeconds)`
- `getFileContent(Long configId, String path)`
- `getFilePage(String path, String type, LocalDateTime[] createTime, Integer pageNo, Integer pageSize)`

方法命名以实现阶段现有接口兼容性为准，但原则是不把 `FileClient` 暴露给 Controller/API。

### 6.2 FileStoragePort / FileClientGateway

新增 application outbound port，用于隔离文件存储技术能力。建议最小契约包括：

- `uploadToMaster(byte[] content, String path, String type)`：使用 master client 上传并返回 URL。
- `delete(Long configId, String path)`：按 configId 删除对象。
- `getContent(Long configId, String path)`：按 configId 读取对象内容。
- `presignPutFromMaster(String path)`：使用 master client 生成上传预签名地址。
- `presignGetFromMaster(String resourceUrl, Integer expirationSeconds)`：使用 master client 生成下载预签名地址。
- `getMasterConfigId()`：返回实际 master client id，用于 `/infra/file/presigned-url` 响应。

边界语义：

1. 上传、预签名必须使用 master client。
2. 删除和读取按文件元数据中的 configId 选择 client。
3. `configId == null` 的删除链路保持当前语义：不伪造 client，不静默删除 DB 前的存储对象。
4. 客户端缺失的异常归口只能在 `FileStoragePort` infrastructure adapter 内完成，不允许 Controller/API 处理 `null`。
5. 未引入新错误码前，客户端缺失必须复用当前可观测异常类型和 HTTP 响应映射；如要新增错误码，必须单独做兼容评审。
6. upload、delete、getContent、presign 四条链路都必须有兼容断言，证明从 `null` 到受控失败的响应语义不漂移。
7. 基础设施实现可命名为 `FileStorageAdapter`、`FileClientGatewayImpl` 或项目命名风格一致的名称。实现内部继续依赖 `FileConfigServiceImpl`，以保留当前缓存和 master 行为。

过渡依赖退场准则：

1. 阶段 1 允许 outbound adapter 依赖 legacy `FileConfigServiceImpl`。
2. 后续独立阶段必须把客户端缓存、master 选择和缓存失效能力迁移到 `infrastructure/file/cache` 或等价基础设施适配组件。
3. 等价测试覆盖 master cache、指定 id cache、缓存失效、test upload 行为后，新代码禁止继续直接调用旧 `FileConfigServiceImpl`。

### 6.3 Repository 查询组件

Repository 方法继续接收 domain query。查询实现不得导入 Controller VO。必要时在 Mapper 增加如下风格的方法：

- `selectPageByQuery(ConfigPageQuery query)`
- `selectPageByQuery(FilePageQuery query)`
- `selectPageByQuery(FileConfigPageQuery query)`

也可以在 infrastructure 内使用 MyBatis Plus 条件构造器，前提是不改变分页字段、排序和筛选行为。

请求 VO 到应用查询对象的转换可以在 Controller 或入口 adapter 完成。Domain/application result 到响应 VO 的转换应统一收敛到 `convert/`，不得在 Repository 或 domain 中夹带 Controller 映射和业务判断。

### 6.4 API local/remote 适配

API 模块应形成稳定契约和适配器：

```text
api/{business}/
  XxxCommonApi.java 或当前兼容命名 XxxApi.java
  dto/
  local/
  remote/
```

短期目标是补齐 local 位置和等价适配，不改变现有注入方。远程 Feign client 保持路径、方法和 `CommonResult` 包装。若从 `XxxApi` 迁移为 `XxxCommonApi`，必须单独提供兼容迁移计划，不能混入文件链路阶段。

## 7. 数据流设计

### 7.1 上传文件

1. Controller/API 接收文件内容和业务参数。
2. Controller/API 调用 `FileUseCase.createFile(byte[] content, String name, String directory, String type)`。
3. Application 处理 MIME 类型、文件名、扩展名和 path 生成。
4. Application 调用 outbound port 上传到 master 存储。
5. Infrastructure 通过旧 `FileConfigServiceImpl` 获取 master client。
6. 上传成功后 application 保存 `File` 元数据并发布领域事件。
7. 返回 URL，保持当前外部行为。

### 7.2 前端直传与预签名

1. Controller/API 调用 application 预签名用例。
2. Application 调用 outbound port。
3. Infrastructure 使用 master client 执行预签名。
4. S3 行为保持不变；非支持存储保持明确失败，不伪造成功。
5. 返回 `FilePresignedUrlRespVO` 时保持 `configId`、`path`、`uploadUrl`、`url` 字段语义不变。

### 7.3 下载文件

1. Controller 只解析 `{configId}` 和 path。
2. Controller 调用 `FileUseCase.getFileContent(configId, path)`。
3. Application 通过 outbound port 读取文件内容。
4. Controller 保留当前空内容返回 HTTP 404、非空内容写 attachment 的行为。
5. Controller 不再直接依赖 `FileConfigService` 或 `FileClient`。

### 7.4 删除文件

1. Controller/API 调用 `FileUseCase.deleteFile(id)`。
2. Application 查询文件元数据。
3. Application 通过 outbound port 删除对象存储内容。
4. 删除成功后删除 DB 元数据。
5. 保留当前“先删存储、再删 DB”的语义，不在本阶段引入异步补偿。
6. 批量删除保持当前逐条执行语义：中途失败时允许前序成功项已生效，不回滚外部存储；必须记录失败项并保持当前异常抛出行为一致。
7. 批量删除不得在本阶段改成吞错继续或全部回滚；补偿机制另起专题设计。

### 7.5 分页查询

1. Controller 解析请求参数。
2. Controller 或入口 adapter 构造 application/domain query。
3. Repository 使用 domain query 查询。
4. Controller 通过 `convert/` 将 domain result 转为响应 VO。

## 8. 错误处理设计

1. `FileClientFactory` 找不到客户端时，不让 Controller/API 处理 `null`。基础设施 port 应集中转换为明确异常。在未引入新错误码前，必须保持与现有异常类型和响应映射兼容；若要引入业务错误码，需先补充兼容评审与灰度方案，禁止在同阶段直接替换外部错误语义。
2. 预签名不支持时，继续明确失败，不静默降级。
3. `presignGetUrl` 必须统一处理完整 URL、path、query string 和 URL decode，避免不同入口签名结果不一致。
4. 归一化测试至少覆盖：完整 domain URL、纯 path、path + query、URL 编码 path、公开访问 URL、私有访问 URL。
5. 文件删除外部副作用不可事务回滚，本阶段只保持当前顺序和批量失败策略并用测试锁定；补偿机制另起设计。
5. 日志写入必须保留无租户兜底，错误日志创建失败不得影响主流程。
6. 数据源连接校验失败继续抛 `DATA_SOURCE_CONFIG_NOT_OK`。
7. 动态数据源 master 配置缺失时必须受控失败，禁止 NPE 直接暴露。

## 9. 测试设计

### 9.1 文件链路

- `FileApplicationServiceTest`
  - `fileCreate_emptyNameAndType_normalizesNameTypePathAndStripsUrlQuery`
  - `fileDelete_deletesStorageBeforeMetadata`
  - `fileDeleteList_stopsOnFirstStorageFailure`
  - `fileGetContent_readsThroughStoragePort`
  - `filePresign_nonSupportedClient_keepsExplicitFailure`
  - `filePresignGet_acceptsFullUrlAndPath`
  - `filePresignGet_normalizesDomainUrlPathQueryEncodedPublicAndPrivateInputs`
- `FileControllerTest` 或等价 Web 层测试
  - `/infra/file/upload` 路径和响应结构不变。
  - `/infra/file/presigned-url` 返回 `configId`、`path`、`uploadUrl`、`url`。
  - `/infra/file/create` 前端直传记录创建不变。
  - `/infra/file/delete` 与 `/infra/file/delete-list` 调用 application 用例。
  - `/infra/file/{configId}/get/**` 保持 404 和 attachment 行为。
  - `/infra/file/page` 分页响应结构不变。
- `FileApiImplTest`
  - `FileApi` 默认 helper 方法仍通过 `CommonResult#getCheckedData` 返回数据。
- `FileConfigServiceImplTest`
  - master cache、指定 id cache、缓存失效保持不变。
- `FileClientFactoryImplTest`
  - 找不到客户端时验证现有断言语义或兼容失败语义。

### 9.2 仓储边界

- `ConfigRepositoryImplTest`
- `FileRepositoryImplTest`
- `FileConfigRepositoryImplTest`

验证分页查询不依赖 Controller VO，查询结果与旧 Mapper 行为一致。

### 9.3 API local/remote

必须通过：

```bash
mvn compile -pl develop-module-infra/develop-module-infra-api -am -DskipTests
mvn compile -pl develop-module-infra/develop-module-infra-server -am -DskipTests
mvn compile -pl develop-server -am -DskipTests
```

并验证 `FileApi`、`ConfigApi` 默认 helper 方法仍可用。若不能运行 `develop-server` 编译，必须列出受影响消费者模块和逐模块 compile 清单。

### 9.4 日志与数据源

- `ApiAccessLogApplicationServiceTest`
  - 无租户上下文仍可插入。
  - 字段截断不变。
- `ApiErrorLogApplicationServiceTest`
  - 创建失败不影响主流程。
  - 无租户上下文仍按旧语义写入或吞异常。
- `DataSourceConfigApplicationServiceTest`
  - master `0L` 和列表首位 master 行为不变。
  - primary 或 `DataSourceProperty` 缺失时受控失败。

## 10. 阶段完成定义

每个阶段完成前必须满足：

1. 本阶段声明的 Java 文件编译通过。
2. 本阶段新增或影响的测试通过。
3. Controller/API 契约检查通过。
4. domain/application/infrastructure 依赖方向检查通过。
5. 历史兜底逻辑未被删除或弱化。
6. 回滚条件未触发。
7. 对应子域 legacy 基线测试最小集通过；File 阶段至少包含 `FileServiceImplTest`、`FileConfigServiceImplTest`、`FileApplicationServiceTest`、`FileControllerTest` 或等价覆盖。
8. 完成记录中列出实际执行的命令；无法执行时说明阻塞原因和未验证风险。

## 11. 验收标准

1. Controller/API 路径、HTTP 方法、DTO 字段、`CommonResult` 包装、权限注解不变。
2. `FileUseCase` 不再要求调用方传入 `FileClient`、client provider 或 content provider。
3. `FileController` 上传、预签名、下载、删除、批量删除入口均不直接依赖 `FileClient`。
4. 旧 `FileConfigServiceImpl` 缓存、master 伪 key 和缓存失效逻辑不被删除。
5. 文件上传、预签名、删除、下载、前端直传记录创建行为与当前外部行为一致。
6. Repository 实现不再导入 Controller VO。
7. API local/remote 适配结构补齐后，远程 Feign 行为不变，且不会产生 bean 冲突。
8. 日志无租户兜底、错误日志不影响主流程、数据源 master `0L` 行为保持。
9. domain 层不依赖 Spring、MyBatis、Feign、Controller VO、Mapper、DO、`FileClient`、动态数据源配置或基础设施实现。
10. application 层不直接依赖 Mapper/DO；现有冲突必须记录并给出退场计划。
11. 每个阶段完成后执行对应 Maven compile/test；无法执行时记录命令和阻塞原因。

## 12. 非目标

1. 不一次性迁移 Codegen 全链路。
2. 不删除旧 `service/dal`。
3. 不改数据库结构或错误码编号。
4. 不改变文件删除顺序。
5. 不改变批量删除遇错即失败并中断的当前语义。
6. 不把非 S3 存储的预签名改成静默 fallback。
7. 不立即拆分复杂 `PresignFileClient` 层级。
8. 不引入大规模补偿任务、outbox 或 Saga；这些另起设计。
9. 不把同步主路径改成事件驱动异步编排。

## 13. 风险与回滚条件

出现以下情况应停止并回滚当前阶段：

1. API/server/develop-server 编译失败且无法在当前阶段内修复。
2. Controller/API 契约、DTO 字段、错误码或权限发生非预期变化。
3. 文件 master 配置缓存失效行为丢失。
4. 非支持预签名存储被静默当作成功。
5. 预签名响应 `configId` 与实际签名 client 不一致。
6. 文件下载 404 或 attachment 行为改变。
7. 批量删除失败策略改变。
8. 日志写入异常影响主业务。
9. 数据源 master `0L` 或列表排序改变。
10. domain/application 依赖方向继续恶化。
11. 改造需要删除用户未提交代码、重置分支或批量移动无关模块。
