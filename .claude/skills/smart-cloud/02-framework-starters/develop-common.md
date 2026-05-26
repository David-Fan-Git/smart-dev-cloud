---
name: develop-common
description: The shared library — CommonResult, ErrorCode, ServiceException, validation annotations, utility classes, Feign API interfaces, and common enums
type: project
---

# develop-common

## Overview

`develop-common` 是所有模块的共同依赖，提供全局统一的数据契约、异常体系、校验注解、工具类和 Feign 接口契约。它不依赖任何业务模块或 Spring Boot starter 自动配置，是平台**最底层的公共模块**（纯依赖，不含 AutoConfiguration）。

**Package:** `com.develop.mvp.pk.framework.common`

## Core Components

### Unified Response

- **CommonResult<T>** (`com.develop.mvp.pk.framework.common.pojo`)
  - Fields: `code(Integer)`, `msg(String)`, `data(T)`
  - Static factories: `success(data)`, `error(code, msg)`, `error(ErrorCode, params...)`, `error(CommonResult<?>)`, `error(ServiceException)`
  - Instance methods: `isSuccess()`, `isError()`, `checkError()` (throws ServiceException if code != 0), `getCheckedData()` (returns data or throws)
  - `@JsonIgnore` on `checkError()` / `getCheckedData()` — excludes from serialization

- **ErrorCode** (`com.develop.mvp.pk.framework.common.exception`): (code, msg) immutable pair
  - Global constants: `GlobalErrorCodeConstants` — SUCCESS(0), BAD_REQUEST(400), UNAUTHORIZED(401), FORBIDDEN(403), NOT_FOUND(404), INTERNAL_SERVER_ERROR(500)

- **ServiceException** (`com.develop.mvp.pk.framework.common.exception`): extends RuntimeException
  - Created via `ServiceExceptionUtil.exception(ErrorCode, params...)` with message formatting (`{}` placeholders)
  - Caught by `GlobalExceptionHandler` and automatically converted to `CommonResult`

- **ServerException** (`com.develop.mvp.pk.framework.common.exception`): extends RuntimeException
  - For internal server errors (500), distinct from ServiceException (biz logic, 200)

### BeanUtils (`com.develop.mvp.pk.framework.common.util.object`)

Hutool `BeanUtil.toBean()` wrapper with convenient overloads:

```java
// VO <-> DO conversion
PostDO post = BeanUtils.toBean(createReqVO, PostDO.class);
PostRespVO respVO = BeanUtils.toBean(post, PostRespVO.class);
List<PostRespVO> list = BeanUtils.toBean(postList, PostRespVO.class);
PageResult<PostRespVO> page = BeanUtils.toBean(pageResult, PostRespVO.class);
```

Performance: < 1ms per object, suitable for bulk conversion up to ~1000 records.

### Pagination

- **PageParam** (`com.develop.mvp.pk.framework.common.pojo`): `pageNo` (default 1), `pageSize` (default 10, max 400)
- **SortablePageParam**: extends PageParam with `SortingField`
- **PageResult<T>**: `list`, `total` — returned by all paginated queries

### Validation Annotations

| Annotation | Validator | Purpose |
|---|---|---|
| `@InEnum` | InEnumValidator, InEnumCollectionValidator | Validate value is within enum range (uses ArrayValuable interface) |
| `@Mobile` | MobileValidator | Chinese mobile number format validation |
| `@Telephone` | TelephoneValidator | Fixed-line telephone format validation |

All in package `com.develop.mvp.pk.framework.common.validation`.

### Feign Common API Interfaces

Located in `com.develop.mvp.pk.framework.common.biz`:

| API Interface | Package | Purpose |
|---|---|---|
| `PermissionCommonApi` | `biz.system.permission` | Permission check: hasAnyPermissions, hasAnyRoles, getDeptDataPermission |
| `OAuth2TokenCommonApi` | `biz.system.oauth2` | Token operations: createAccessToken, checkAccessToken, removeAccessToken |
| `TenantCommonApi` | `biz.system.tenant` | Tenant query and validation |
| `DictDataCommonApi` | `biz.system.dict` | Dictionary data query |
| `OperateLogCommonApi` | `biz.system.logger` | Operation log recording |
| `ApiAccessLogCommonApi` | `biz.infra.logger` | API access log |
| `ApiErrorLogCommonApi` | `biz.infra.logger` | API error log |

Each has corresponding DTO definitions in `dto` sub-packages:
- `OAuth2AccessTokenCreateReqDTO`, `OAuth2AccessTokenRespDTO`, `OAuth2AccessTokenCheckRespDTO`
- `DeptDataPermissionRespDTO`
- `DictDataRespDTO`
- `ApiAccessLogCreateReqDTO`, `ApiErrorLogCreateReqDTO`
- `OperateLogCreateReqDTO`

### Utility Classes

| Class | Package | Purpose |
|---|---|---|
| `BeanUtils` | `util.object` | Property copy (Hutool wrapper) |
| `JsonUtils` | `util.json` | Jackson ObjectMapper wrapper (holds static ObjectMapper instance) |
| `CacheUtils` | `util.cache` | Guava/Caffeine LoadingCache builder |
| `ServletUtils` | `util.servlet` | HttpServletRequest/Response utilities |
| `SpringUtils` | `util.spring` | ApplicationContextAware access |
| `SpringExpressionUtils` | `util.spring` | SpEL expression parsing |
| `DateUtils` | `util.date` | Date utility methods |
| `LocalDateTimeUtils` | `util.date` | Java 8 time utilities |
| `NumberUtils` | `util.number` | Number formatting/conversion |
| `MoneyUtils` | `util.number` | Money formatting (yuan/fen conversion) |
| `CollectionUtils` | `util.collection` | Collection operations (convertList, convertSet, findFirst, etc.) |
| `MapUtils` | `util.collection` | Map operations |
| `ArrayUtils` | `util.collection` | Array operations |
| `SetUtils` | `util.collection` | Set operations |
| `StrUtils` | `util.string` | String utilities |
| `FileUtils` | `util.io` | File utilities |
| `IoUtils` | `util.io` | I/O utilities |
| `HttpUtils` | `util.http` | HTTP utilities |
| `ObjectUtils` | `util.object` | Object utilities |
| `PageUtils` | `util.object` | Pagination utilities |
| `ValidationUtils` | `util.validation` | Validation utilities |
| `TracerUtils` | `util.monitor` | TraceId accessor (compile dep on apm-toolkit, returns null when no agent) |

### Common Enums

| Enum | Package | Values |
|---|---|---|
| `CommonStatusEnum` | `enums` | ENABLE=0, DISABLE=1 |
| `UserTypeEnum` | `enums` | MEMBER=1, ADMIN=2 |
| `TerminalEnum` | `enums` | Terminal types |
| `WebFilterOrderEnum` | `enums` | Filter order constants (this is an **interface**, not enum) |
| `DateIntervalEnum` | `enums` | Date intervals |
| `DocumentEnum` | `enums` | Document types |
| `RpcConstants` | `enums` | RPC prefix and service name constants |
| `KeyValue` | `core` | Key-value pair |
| `ArrayValuable` | `core` | Interface for enum value arrays |

### Jackson Serializers

Located in `com.develop.mvp.pk.framework.common.util.json.databind`:

- **NumberSerializer**: Long -> String (prevents JS precision loss for Snowflake IDs)
- **TimestampLocalDateTimeSerializer**: LocalDateTime -> epoch millis
- **TimestampLocalDateTimeDeserializer**: epoch millis -> LocalDateTime

These are registered by `develop-spring-boot-starter-web`'s DevelopJacksonAutoConfiguration.

## Auto-Configuration

`develop-common` has **no auto-configuration classes** — it is a pure dependency library. All serializers, enums, and utilities are used directly via static methods/imports. Feign API interfaces are consumed by the corresponding `-rpc` auto-configurations in each framework starter module.

## Key Exception Types

| Exception | When Used | Handled By |
|---|---|---|
| `ServiceException` | Business rule violations | GlobalExceptionHandler -> CommonResult with biz error code |
| `ServerException` | Internal server errors | GlobalExceptionHandler -> CommonResult with 500 |
| `IllegalArgumentException` | Invalid arguments | JVM |

## Package Index

```
com.develop.mvp.pk.framework.common
  ├── biz/                          # Feign Common API interfaces + DTOs
  │   ├── infra/logger/             # ApiAccessLogCommonApi, ApiErrorLogCommonApi
  │   └── system/                   # Permission/OAuth2/Tenant/Dict/OperateLog APIs
  ├── core/                         # ArrayValuable, KeyValue
  ├── enums/                        # CommonStatusEnum, WebFilterOrderEnum, RpcConstants, etc.
  ├── exception/                    # ErrorCode, ServiceException, ServerException
  │   ├── enums/                    # GlobalErrorCodeConstants, ServiceErrorCodeRange
  │   └── util/                     # ServiceExceptionUtil
  ├── pojo/                         # CommonResult, PageParam, PageResult, SortingField
  ├── util/                         # All utility classes
  │   ├── cache/                    # CacheUtils
  │   ├── collection/               # CollectionUtils, MapUtils, ArrayUtils
  │   ├── date/                     # DateUtils, LocalDateTimeUtils
  │   ├── http/                     # HttpUtils
  │   ├── io/                       # FileUtils, IoUtils
  │   ├── json/                     # JsonUtils + databind (serializers/deserializers)
  │   ├── monitor/                  # TracerUtils
  │   ├── number/                   # NumberUtils, MoneyUtils
  │   ├── object/                   # BeanUtils, ObjectUtils, PageUtils
  │   ├── servlet/                  # ServletUtils
  │   ├── spring/                   # SpringUtils, SpringExpressionUtils
  │   └── validation/               # ValidationUtils
  └── validation/                   # @InEnum, @Mobile, @Telephone + validators
```

## Code Examples

```java
// Unified response
@GetMapping("/get")
public CommonResult<UserRespVO> getUser(Long id) {
    UserDO user = userService.get(id);
    if (user == null) {
        return CommonResult.error(GlobalErrorCodeConstants.NOT_FOUND);
    }
    return CommonResult.success(UserConvert.INSTANCE.convert(user));
}

// Throw business exception
ServiceExceptionUtil.exception(ErrorCodeConstants.USER_NOT_FOUND, id);
// => ServiceException(code=1001001, msg="用户(123)不存在")
// => GlobalExceptionHandler catches → CommonResult(1001001, "用户(123)不存在", null)

// @InEnum validation
@InEnum(value = CommonStatusEnum.class)
private Integer status;

// Feign call with auto-check
List<DeptRespDTO> depts = deptApi.getDeptList(ids).getCheckedData();

// Trace ID (works only with SkyWalking agent)
String traceId = TracerUtils.getTraceId();  // null when no agent present
```

## 注意事项

- `CommonResult.getCheckedData()` 在 code 非 0 时抛出 `ServiceException`，用于 Feign 调用方一键解包。调用后不能再继续链式使用该 CommonResult 实例
- 序列化 Long -> String 对 Snowflake ID（18位+）字段至关重要，避免前端 JS Number 精度丢失（JS Number 安全整数为 2^53 ≈ 9e15，Snowflake ID 可能超过该值）
- `ArrayValuable` 枚举需显式实现 `array()` 方法并声明 `ARRAYS` 静态字段，`@InEnum` 通过反射获取该数组
- Feign API 接口必须与 -server 实现类的 URL、HTTP Method、参数签名完全对齐，**特别是 `@RequestParam` 需显式声明 value**（避免编译期参数名丢失）
- `TracerUtils.getTraceId()` 在无 SkyWalking Agent 环境下返回 null，业务代码中需做 null 安全判断或提供兜底值（如 `"N/A"`）
- `WebFilterOrderEnum` 是一个 `interface`（非 `enum`），各过滤器常量定义在其中；此设计使过滤器顺序常量可以被 static import 而不需要引入枚举类型
- `SortablePageParam` 支持多字段排序，通过 `SortingField` 列表传递排序条件，用于复杂列表排序场景
