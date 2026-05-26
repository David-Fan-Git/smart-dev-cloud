---
name: naming-conventions
description: Complete naming conventions for Smart Cloud — packages, classes, methods, tables, enums, error codes, configuration properties, and VO/DTO suffixes
type: project
---

# Naming Conventions

## Overview

Smart Cloud 遵循一套严格的命名规范，覆盖包命名、类命名、方法命名、数据库表/列命名、枚举、错误码、配置属性、RPC 契约等各个层面。遵循规范是保证代码一致性、可读性和可维护性的基础。

## 1. Package Naming Convention

**基础包:** `com.develop.mvp.pk`

**模块内包结构:**
```
com.develop.mvp.pk.module.{module-name}.{layer}.{sub-domain}
```

| Layer | Package Path | Description |
|---|---|---|
| Controller (Admin) | `module.{name}.controller.admin.{entity}` | 管理后台接口 |
| Controller (App) | `module.{name}.controller.app.{entity}` | 移动端/APP 接口 |
| Controller VO | `module.{name}.controller.admin.{entity}.vo` | 请求/响应 VO（与 controller 路径对应） |
| Service | `module.{name}.service.{entity}` | Service 接口 + 实现（同包） |
| DAL Entity | `module.{name}.dal.dataobject.{entity}` | 数据实体 (DO) |
| DAL Mapper | `module.{name}.dal.mysql.{entity}` | MyBatis Mapper 接口 |
| DAL Redis | `module.{name}.dal.redis` | Redis Key 定义 |
| Convert | `module.{name}.convert.{entity}` | MapStruct 对象转换器 |
| API Feign | `module.{name}.api.{entity}` | Feign 接口定义 |
| API Enum | `module.{name}.enums` | 枚举、ErrorCodeConstants |
| Framework | `module.{name}.framework` | 模块级 Spring 配置 |
| Job | `module.{name}.job` | XXL-Job 任务 |
| MQ | `module.{name}.mq` | 消息消费者 |

**Example:** System module's Post management:
```
com.develop.mvp.pk.module.system.controller.admin.dept.vo.post.PostSaveReqVO
com.develop.mvp.pk.module.system.service.dept.PostService
com.develop.mvp.pk.module.system.dal.dataobject.dept.PostDO
com.develop.mvp.pk.module.system.dal.mysql.dept.PostMapper
com.develop.mvp.pk.module.system.convert.dept.DeptConvert
```

**Framework layer base packages:**
```
com.develop.mvp.pk.framework.{module}.{sub-package}
com.develop.mvp.pk.framework.common.pojo             -- CommonResult, PageResult, PageParam
com.develop.mvp.pk.framework.common.exception         -- ErrorCode, ServiceException, ServerException
com.develop.mvp.pk.framework.common.util.object       -- BeanUtils
com.develop.mvp.pk.framework.common.enums             -- WebFilterOrderEnum, CommonStatusEnum, etc.
com.develop.mvp.pk.framework.mybatis.core.dataobject  -- BaseDO
com.develop.mvp.pk.framework.mybatis.core.mapper       -- BaseMapperX
com.develop.mvp.pk.framework.mybatis.core.query       -- LambdaQueryWrapperX
com.develop.mvp.pk.framework.security.config          -- Security Configuration
com.develop.mvp.pk.framework.security.core            -- LoginUser, TokenAuthenticationFilter
com.develop.mvp.pk.framework.tenant.core.db           -- TenantBaseDO
```

**Gateway package (independent of framework):**
```
com.develop.mvp.pk.gateway.filter.security.TokenAuthenticationFilter
com.develop.mvp.pk.gateway.filter.cors.CorsFilter
com.develop.mvp.pk.gateway.util.SecurityFrameworkUtils
```

## 2. Class Naming Convention

| Category | Rule | Example |
|---|---|---|
| Controller (Admin) | `{Entity}Controller` | `PostController` |
| Controller (App) | `App{Entity}Controller` | `AppTenantController` |
| Service Interface | `{Entity}Service` | `PostService` |
| Service Implementation | `{Entity}ServiceImpl` | `PostServiceImpl` |
| DO (Data Object) | `{Entity}DO` | `PostDO` |
| Mapper | `{Entity}Mapper` | `PostMapper` |
| Feign API | `{Entity}Api` | `DictDataApi` |
| API Implementation | `{Entity}ApiImpl` | `DictDataApiImpl` |
| Convert | `{Entity}Convert` | `UserConvert` |
| SaveReqVO | `{Entity}SaveReqVO` | `PostSaveReqVO` |
| PageReqVO | `{Entity}PageReqVO` | `PostPageReqVO` |
| RespVO | `{Entity}RespVO` | `PostRespVO` |
| SimpleRespVO | `{Entity}SimpleRespVO` | `PostSimpleRespVO` |
| ReqDTO | `{Entity}ReqDTO` | `TenantReqDTO` |
| RespDTO | `{Entity}RespDTO` | `TenantRespDTO` |
| Excel VO | `{Entity}ExcelVO` | `UserExcelVO` |
| ApiConstants | `ApiConstants` | Fixed name, not entity-based |
| ErrorCodeConstants | `ErrorCodeConstants` | Fixed name, not entity-based |
| Enum | `{Name}Enum` | `CommonStatusEnum`, `SocialTypeEnum` |
| Message | `{Entity}Message` | `OrderCreateMessage` |
| Message Listener | `{Entity}MessageListener` | `OrderCreateMessageListener` |
| Configuration | `Develop{Name}AutoConfiguration` / `Develop{Name}Configuration` | `DevelopSecurityAutoConfiguration`, `DevelopLock4jConfiguration` |
| Properties | `{Name}Properties` | `SecurityProperties`, `TenantProperties`, `WebProperties` |
| Global Exception Handler | `GlobalExceptionHandler` | Fixed name |
| Application Entry | `{Module}ServerApplication` | `SystemServerApplication` |
| Spring Security Filter Chain | `DevelopWebSecurityConfigurerAdapter` | Fixed name |
| Permission Customizer | `{Entity}DataPermissionCustomizer` | `OrderDataPermissionCustomizer` |

## 3. Method Naming Convention

**Controller 方法:**
| HTTP Method | Method Name | URL | Description |
|---|---|---|---|
| POST | `create{Entity}` | `/{entity}/create` | 创建资源 |
| PUT | `update{Entity}` | `/{entity}/update` | 修改资源 |
| DELETE | `delete{Entity}` | `/{entity}/delete` | 删除单个 |
| DELETE | `delete{Entity}List` | `/{entity}/delete-list` | 批量删除 |
| GET | `get{Entity}` | `/{entity}/get` | 获取单个 |
| GET | `get{Entity}Page` | `/{entity}/page` | 分页查询 |
| GET | `getSimple{Entity}List` | `/{entity}/list-all-simple` | 全量列表（下拉选择框） |
| GET | `export{Entity}` | `/{entity}/export-excel` | Excel 导出 |

**Service 方法:**
| Method | Returns | Description |
|---|---|---|
| `create{Entity}(CreateReqVO)` | `Long` / entity ID | 创建 |
| `update{Entity}(UpdateReqVO)` | `void` | 修改 |
| `delete{Entity}(Long id)` | `void` | 删除单个 |
| `delete{Entity}List(List<Long> ids)` | `void` | 批量删除 |
| `get{Entity}(Long id)` | `DO` | 按 ID 获取 |
| `get{Entity}List(...)` | `List<DO>` | 条件查询列表 |
| `get{Entity}Page(PageReqVO)` | `PageResult<DO>` | 分页查询 |
| `validate{Entity}...(..)` | `void` | 业务校验（抛异常时为不通过） |
| `validate{Entity}List(Collection<Long> ids)` | `void` | 批量校验存在性和状态 |

**Mapper 方法:**
| Method | Description |
|---|---|
| `selectBy{Field}(...)` | 单字段等值查询 |
| `selectList(...)` | 条件查询列表 |
| `selectPage(PageReqVO, LambdaQueryWrapperX)` | 分页查询 |
| `selectCount(...)` | 计数查询 |
| `exist{Field}(...)` | 是否存在判断 |
| `selectById(Long id)` | 继承自 BaseMapperX |
| `selectByIds(Collection<Long> ids)` | 继承自 BaseMapperX |
| `insert(T entity)` | 继承自 BaseMapperX |
| `updateById(T entity)` | 继承自 BaseMapperX |
| `deleteById(Long id)` | 继承自 BaseMapperX |

## 4. Controller URL Convention

```yaml
# HTTP Method + URL Pattern
POST   /{module}/{entity}/create
PUT    /{module}/{entity}/update
DELETE /{module}/{entity}/delete          # Single delete
DELETE /{module}/{entity}/delete-list     # Batch delete
GET    /{module}/{entity}/get             # Single query
GET    /{module}/{entity}/page            # Paginated query
GET    /{module}/{entity}/list-all-simple # Full list (dropdown)

# Examples
/system/post/create
/system/post/update
/system/post/delete
/system/user/page
/infra/config/create
```

`@RequestMapping` at class level:
```java
@RequestMapping("/system/post")
public class PostController { ... }
```

URL 风格: **全小写**、连字符分隔、不使用下划线、复数形态可选（`/system/post` / `/system/user`）。

## 5. Database Naming Convention

**Table Naming:**
```sql
-- Pattern: {module}_{entity}, lowercase snake_case
system_users          -- System module - users
system_roles          -- System module - roles
system_depts          -- System module - departments
system_post           -- System module - posts
system_dict_data      -- System module - dict data
infra_config          -- Infra module - config
infra_file            -- Infra module - files
infra_api_access_log  -- Infra module - API access log
bpm_process_instance  -- BPM module - process instances
pay_order             -- Pay module - orders
```

**Column Naming:**
```sql
-- General columns
id              -- Primary key (single table, no prefix)
name            -- Name
status          -- Status (0=normal, 1=disabled)
sort            -- Sort order
remark          -- Remark
creator         -- Creator (auto-filled by BaseDO)
create_time     -- Creation time (auto-filled by BaseDO)
updater         -- Updater (auto-filled by BaseDO)
update_time     -- Update time (auto-filled by BaseDO)
deleted         -- Logical delete flag (auto-filled by BaseDO, 0=active, 1=deleted)
tenant_id       -- Tenant ID (auto-filled by TenantBaseDO)

-- Foreign keys use {referenced_entity}_id
dept_id         -- Department ID
user_id         -- User ID
role_id         -- Role ID
dict_type       -- Dict type (string-typed foreign key)
```

**Sequence Naming (Oracle/PostgreSQL/Kingbase):**
```sql
-- Pattern: {table_name}_seq
system_post_seq
system_user_seq
system_role_seq
```

对应 DO 类中的 `@KeySequence`:
```java
@KeySequence("system_post_seq")
public class PostDO extends BaseDO { ... }
```

**Join Table Naming:**
```sql
-- Junction tables use underscore
system_user_role      -- User-Role association
system_role_menu      -- Role-Menu association
system_user_post      -- User-Post association
```

## 6. Enum Naming Convention

**Enum class 必须实现 `ArrayValuable<T>` 接口**（位于 `com.develop.mvp.pk.framework.common.core.ArrayValuable`），便于通过 `@InEnum` 进行参数校验:

```java
public interface ArrayValuable<T> {
    T[] array();
}
```

```java
public enum CommonStatusEnum implements ArrayValuable<Integer> {
    ENABLE(0, "开启"),
    DISABLE(1, "关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values())
        .map(CommonStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    CommonStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }
    // getters ...
}
```

**Enum in VO:**
```java
@Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
@NotNull @InEnum(CommonStatusEnum.class)
private Integer status;
```

**Enum File Location:**
- 通用枚举（CommonStatusEnum, UserTypeEnum, TerminalEnum, WebFilterOrderEnum）在 `develop-common` 的 `enums` 包
- 模块业务枚举在 `develop-module-{name}-api` 模块的 `enums` 包

## 7. Error Code Naming Convention

**ErrorCodeConstants 接口:**
```java
// Single ErrorCodeConstants interface per module in -api module's enums package
public interface ErrorCodeConstants {
    // ========== AUTH Module 1-002-000-000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1_002_000_000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1_002_000_001, "登录失败，账号被禁用");

    // ========== User Module 1-002-003-000 ==========
    ErrorCode USER_USERNAME_EXISTS = new ErrorCode(1_002_003_000, "用户账号已经存在");
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1_002_003_003, "用户不存在");

    // ========== Role Module 1-002-002-000 ==========
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1_002_002_000, "角色不存在");
    ErrorCode ROLE_NAME_DUPLICATE = new ErrorCode(1_002_002_001, "已经存在名为【{}】的角色");
}
```

**Error Code Numbering Convention:**
```
1 - 002 - 000 - 000
├── ── ── ── ── ── └── Sequential error number (000-999)
├── ── ── ── ── ───── Module sub-domain (000-999)
├── ── ── ── ── ─────── Business module ID: 002=system, 003=infra, 004=member...
└── ── ── ── ── ──────── System identifier (1=biz exception, 2=biz warn)
```

**Usage:**
```java
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;
throw exception(POST_NAME_DUPLICATE);                        // No args
throw exception(POST_NOT_ENABLE, post.getName());            // With message format args
throw exception(USER_IS_DISABLE, user.getNickname(), id);    // Multiple args
```

**Global Error Codes** (from `GlobalErrorCodeConstants`):
```java
int SUCCESS = 0;
int BAD_REQUEST = 400;
int UNAUTHORIZED = 401;
int FORBIDDEN = 403;
int NOT_FOUND = 404;
int INTERNAL_SERVER_ERROR = 500;
```

## 8. Feign API Naming

**Interface Naming:**
```java
@FeignClient(name = ApiConstants.NAME, contextId = "{entity}Api")
@Tag(name = "RPC 服务 - 字典数据")
public interface DictDataApi extends DictDataCommonApi {
    String PREFIX = ApiConstants.PREFIX + "/dict-data";
    // Feign method definitions
}
```

**Service Name Constants:**
```java
public class ApiConstants {
    public static final String NAME = "system-server";
    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/system"; // "/rpc-api/system"
    public static final String VERSION = "1.0.0";
}
```

**DTO Naming:**
- Request DTO: `{Entity}ReqDTO` (Feign 接口参数)
- Response DTO: `{Entity}RespDTO` (Feign 接口返回值)
- All in `develop-module-{name}-api` module

## 9. Configuration Class Naming

```java
// AutoConfiguration — registered in AutoConfiguration.imports
@AutoConfiguration
public class DevelopSecurityAutoConfiguration { ... }

@AutoConfiguration
public class DevelopMybatisAutoConfiguration { ... }

// Module-level Configuration — @Configuration (not auto-registered)
@Configuration
public class SystemModuleConfiguration { ... }

// Properties
@ConfigurationProperties(prefix = "develop.security")
public class SecurityProperties { ... }

@ConfigurationProperties(prefix = "develop.tenant")
public class TenantProperties { ... }
```

**Registration:** `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
```
com.develop.mvp.pk.framework.security.config.DevelopSecurityAutoConfiguration
com.develop.mvp.pk.framework.mybatis.config.DevelopMybatisAutoConfiguration
```

## 10. Permission Expression Convention

```java
// Format: {module}:{entity}:{action}
@PreAuthorize("@ss.hasPermission('system:post:create')")
@PreAuthorize("@ss.hasPermission('system:post:update')")
@PreAuthorize("@ss.hasPermission('system:post:delete')")
@PreAuthorize("@ss.hasPermission('system:post:query')")
@PreAuthorize("@ss.hasPermission('system:post:export')")
```

`"ss"` 是 `SecurityFrameworkService` Bean 的名称（缩写自 Spring Security），注入权限校验逻辑。

## 11. VO/DTO Suffix Quick Reference

| Suffix | Purpose | Package |
|---|---|---|
| `ReqVO` | Generic request body | `controller.admin.{entity}.vo` |
| `SaveReqVO` | Create/Update shared request | `controller.admin.{entity}.vo` |
| `PageReqVO` | Pagination request | `controller.admin.{entity}.vo` |
| `RespVO` | Generic response body | `controller.admin.{entity}.vo` |
| `SimpleRespVO` | Simplified response (dropdown) | `controller.admin.{entity}.vo` |
| `ExcelVO` | Excel import/export VO | `controller.admin.{entity}.vo` |
| `ReqDTO` | RPC request DTO | `-api` module |
| `RespDTO` | RPC response DTO | `-api` module |
| `Message` | MQ message | `mq` package in -server module |
| `CreateReqDTO` | RPC create request | `-api` module (DTO) |

## 12. MQ Message Naming

```java
public class OrderCreateMessage extends AbstractRedisStreamMessage {
    public static final String STREAM_KEY = "order:create";
    // fields...
}
```

Message class naming: `{Entity}{Action}Message`, 继承 `AbstractRedisStreamMessage`（Redis Stream）或 `AbstractRedisChannelMessage`（Redis Pub/Sub，向后兼容）。

## 13. Cache Key Naming

```java
// Redis Key pattern: {module}:{entity}:{identifier}
// In dal/redis/ package:
public class RedisKeyConstants {
    public static final String LOGIN_USER = "system:login_user:%s";
    public static final String CAPTCHA = "system:captcha:%s";
}
```

## 14. Async Method Naming

```java
@Async
public CompletableFuture<Void> sendNotificationAsync(Long userId) { ... }

@Async
public CompletableFuture<List<RemoteData>> fetchRemoteDataAsync(Collection<Long> ids) { ... }
```

异步方法推荐 `Async` 后缀。

## 15. Configuration Property Naming (develop.* prefix)

| Property Prefix | Properties Class | Purpose |
|---|---|---|
| `develop.web` | `WebProperties` | Web configuration (API prefixes) |
| `develop.security` | `SecurityProperties` | Token header, mock mode, permit-all URLs |
| `develop.tenant` | `TenantProperties` | Multi-tenant: enable, ignore-urls, ignore-tables, ignore-caches |
| `develop.xss` | `XssProperties` | XSS filter enable, exclude URLs |
| `develop.api-encrypt` | `ApiEncryptProperties` | API encryption enable |
| `develop.access-log` | - | Access log enable (conditional on DevelopApiLogAutoConfiguration) |
| `develop.captcha` | - | Captcha enable |
| `develop.demo` | - | Demo mode |
| `develop.tracer` | `TracerProperties` | SkyWalking tracer enable |
| `develop.websocket` | `WebSocketProperties` | WebSocket path, senderType, enable |
| `develop.dict` | - | Dict framework enable |
| `develop.protection` | - | Rate limiter, idempotent, lock, api-signature sub-switches |
| `develop.info` | - | `base-package` for component scanning and type aliases |
| `develop.cache` | `DevelopCacheProperties` | Redis scan batch size |
| `develop.data-permission` | - | Data permission enable |
| `develop.feign` | - | Per-service Feign URL overrides |

## 注意事项

- DO 命名统一以 `DO` 结尾，**不使用** `Entity` 字样（如 `UserDO` 而非 `UserEntity`）
- Controller 中**不得**直接返回 DO，必须通过 `BeanUtils.toBean` 转换为 VO
- VO 包路径对应 Controller 目录结构（`controller.admin.{entity}.vo`），非 DO 路径
- `@RequestMapping` 路径始终小写，使用单数形式（`/system/post` 而非 `/system/posts`）
- 枚举类必须实现 `ArrayValuable<T>` 接口以支持 `@InEnum` 参数校验
- ErrorCode 编号使用 Java 下划线分隔的数字字面量（如 `1_002_000_000`），提高可读性
- Service 接口和实现放在**同一个包**下（接口名 `XxxService`，实现名 `XxxServiceImpl`）
- 所有 Feign 接口定义在 `-api` 模块的 `api` 子包中，实现类在 `-server` 模块中
- `WebFilterOrderEnum` 是一个 `interface`（非 `enum`），各过滤器顺序常数在其中集中定义
- `@Async` 方法建议返回 `CompletableFuture` 或 `Future`，以便调用方处理异步结果或异常
