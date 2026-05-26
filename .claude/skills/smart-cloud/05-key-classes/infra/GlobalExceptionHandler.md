---
name: GlobalExceptionHandler
description: Unified @RestControllerAdvice that maps all exceptions to CommonResult with proper HTTP status codes
type: project
---

# GlobalExceptionHandler

## 功能定位

GlobalExceptionHandler 是平台统一的异常处理中心，位于 `develop-spring-boot-starter-web` 的 `core.handler` 包下。使用 `@RestControllerAdvice` 拦截所有 Controller 层抛出的异常，将其转换为统一的 `CommonResult` 响应格式。

核心职责：
- **异常谱系分类**：覆盖 15+ 种异常类型，从参数校验到系统宕机的完整谱系
- **模块未安装检测**：智能检测"表不存在"的错误，精准提示用户开启对应模块
- **Filter 层复用**：`allExceptionHandler` 方法同时为 Filter 层提供异常处理能力
- **异步错误日志**：自动记录未预期的系统异常到 `ApiErrorLog`，方便监控和排查
- **Security 异常处理**：将 `AccessDeniedException` 转换为 403 响应

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **@RestControllerAdvice** | Spring MVC 全局异常处理机制 | 类级别注解 + `@ExceptionHandler` |
| **Chain of Responsibility** | `allExceptionHandler` 依次检查异常类型 | `if-else if` 链, 从具体到通用 |
| **Template Method** | 每个异常处理方法遵循统一模式 | 日志记录 -> 错误码转换 -> CommonResult |
| **Mediator** | 集中协调多种异常类型 | 一个入口处理所有异常 |
| **Table Name Pattern Matching** | 智能模块识别 | 异常消息中的表名前缀匹配 |

## 核心逻辑流程

### 异常分发体系

`@ExceptionHandler` 注解的方法用于处理 Spring MVC 请求流程中的异常，而 `allExceptionHandler` 是公共入口，同时供 Filter 使用。

```
[Spring MVC @ExceptionHandler 流程]
  Controller 抛出异常
    |
    +-- @ExceptionHandler(MissingServletRequestParameterException.class) -> 400
    +-- @ExceptionHandler(MethodArgumentTypeMismatchException.class)     -> 400
    +-- @ExceptionHandler(MethodArgumentNotValidException.class)         -> 400
    +-- @ExceptionHandler(BindException.class)                          -> 400
    +-- @ExceptionHandler(ConstraintViolationException.class)            -> 400
    +-- @ExceptionHandler(ValidationException.class)                    -> 400
    +-- @ExceptionHandler(MaxUploadSizeExceededException.class)         -> 400
    +-- @ExceptionHandler(HttpMessageNotReadableException.class)        -> 400
    +-- @ExceptionHandler(NoHandlerFoundException.class)                -> 404
    +-- @ExceptionHandler(NoResourceFoundException.class)              -> 404
    +-- @ExceptionHandler(HttpRequestMethodNotSupportedException.class) -> 405
    +-- @ExceptionHandler(HttpMediaTypeNotSupportedException.class)     -> 400
    +-- @ExceptionHandler(AccessDeniedException.class)                   -> 403
    +-- @ExceptionHandler(ServiceException.class)                       -> 业务错误码
    +-- @ExceptionHandler(UncheckedExecutionException.class)            -> 解包重新处理
    +-- @ExceptionHandler(Exception.class)                              -> 500 + 错误日志

[Filter 使用 allExceptionHandler]
  TokenAuthenticationFilter (不在 Spring MVC 流程中)
    -> catch (Throwable ex)
    -> globalExceptionHandler.allExceptionHandler(request, ex)
    -> 同样的异常处理逻辑 (if-else 链)
```

### 默认异常处理 (500 兜底)

```
defaultExceptionHandler(request, ex)
  |
  +-- 情况一: 解包 ServiceException
  |    当 ex.getCause() instanceof ServiceException 时，直接交由 serviceExceptionHandler 处理
  |    解决某些框架将 ServiceException 包装后抛出导致无法正确识别的问题
  |
  +-- 情况二: 检测表不存在的异常
  |    handleTableNotExists(ex)
  |    -> 从异常消息中匹配 "doesn't exist"
  |    -> 按表名前缀识别模块:
  |       report_    -> "[报表模块] 表结构未导入, 参考..."
  |       bpm_       -> "[工作流模块] 表结构未导入, 参考..."
  |       mp_        -> "[微信公众号] 表结构未导入, 参考..."
  |       product_/promotion_/trade_ -> "[商城系统] 已禁用, 参考..."
  |       erp_/wms_/crm_/mes_/im_/pay_/ai_/iot_ -> 对应模块提示
  |    -> return 501 NOT_IMPLEMENTED + 开启文档链接
  |
  +-- 情况三: 真正的未知异常
  |    log.error("[defaultExceptionHandler]", ex)
  |    createExceptionLog(req, ex) -> 异步记录错误日志到数据库
  |    return 500 INTERNAL_SERVER_ERROR
```

## 关键代码剖析

### ServiceException 处理

```java
@ExceptionHandler(value = ServiceException.class)
public CommonResult<?> serviceExceptionHandler(ServiceException ex) {
    // 不在忽略列表中的异常，打印 warn 日志(仅第一行)
    if (!IGNORE_ERROR_MESSAGES.contains(ex.getMessage())) {
        try {
            StackTraceElement[] stackTraces = ex.getStackTrace();
            for (StackTraceElement stackTrace : stackTraces) {
                if (ObjUtil.notEqual(stackTrace.getClassName(), ServiceExceptionUtil.class.getName())) {
                    log.warn("[serviceExceptionHandler]\n\t{}", stackTrace);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }
    return CommonResult.error(ex.getCode(), ex.getMessage());
}
```

关键设计：
1. **忽略列表 `IGNORE_ERROR_MESSAGES`**: `SetUtils.asSet("无效的刷新令牌")` 中的异常不打印堆栈，避免日志刷屏
2. **仅打印第一行**: 通过跳过 `ServiceExceptionUtil` 自身的堆栈，找到业务调用方的第一行异常位置
3. **返回业务错误码**: 不固定为 500，而是使用 ServiceException 自定义的 code（如 500100）

### 表不存在检测 (handleTableNotExists)

```java
private CommonResult<?> handleTableNotExists(Throwable ex) {
    String message = ExceptionUtil.getRootCauseMessage(ex);
    if (!message.contains("doesn't exist")) {
        return null;  // 非表不存在异常，不处理
    }
    // 按表名前缀匹配模块
    if (message.contains("report_")) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                "[报表模块 develop-module-report - 表结构未导入]");
    }
    if (message.contains("bpm_")) {
        return CommonResult.error(NOT_IMPLEMENTED.getCode(),
                "[工作流模块 develop-module-bpm - 表结构未导入]");
    }
    // ... 共 12 种模块前缀匹配
    return null;
}
```

支持的表名前缀和对应模块：

| 前缀 | 模块 | 文档链接 |
|------|------|----------|
| `report_` | develop-module-report | 数据报表 |
| `bpm_` | develop-module-bpm | 工作流 |
| `mp_` | develop-module-mp | 微信公众号 |
| `product_`, `promotion_`, `trade_` | develop-module-mall | 商城系统 |
| `erp_` | develop-module-erp | ERP 系统 |
| `wms_` | develop-module-wms | WMS 仓库 |
| `crm_` | develop-module-crm | CRM 客户 |
| `mes_` | develop-module-mes | MES 生产 |
| `im_` | develop-module-im | IM 即时通讯 |
| `pay_` | develop-module-pay | 支付平台 |
| `ai_` | develop-module-ai | AI 大模型 |
| `iot_` | develop-module-iot | IoT 物联网 |

### AccessDeniedException 处理

```java
@ExceptionHandler(value = AccessDeniedException.class)
public CommonResult<?> accessDeniedExceptionHandler(HttpServletRequest req, AccessDeniedException ex) {
    log.warn("[accessDeniedExceptionHandler][userId({}) 无法访问 url({})]",
            WebFrameworkUtils.getLoginUserId(req), req.getRequestURL(), ex);
    return CommonResult.error(FORBIDDEN);  // 403
}
```

在日志中记录当前登录用户 ID 和尝试访问的 URL，方便安全审计。

### 异常日志记录

```java
private void createExceptionLog(HttpServletRequest req, Throwable e) {
    try {
        ApiErrorLogCreateReqDTO errorLog = new ApiErrorLogCreateReqDTO();
        buildExceptionLog(errorLog, req, e);  // 构建日志内容
        apiErrorLogApi.createApiErrorLogAsync(errorLog);  // 异步 Feign 调用
    } catch (Throwable th) {
        log.error("[createExceptionLog][url({}) log({}) 发生异常]",
                req.getRequestURI(), JsonUtils.toJsonString(errorLog), th);
    }
}
```

错误日志包含的信息：
- 用户上下文：userId, userType
- 异常信息：exceptionName, message, rootCauseMessage, stackTrace
- 异常位置：className, fileName, methodName, lineNumber
- 请求信息：url, method, params, userAgent, ip
- 链路追踪：traceId (SkyWalking / Sleuth)
- 应用标识：applicationName

## 调用链

```
[Spring MVC 流程]
  Controller 抛出异常
    -> GlobalExceptionHandler (本类, 通过 @ExceptionHandler)
       -> CommonResult.error(code, message)
       -> HTTP Response JSON

[Filter 流程]
  TokenAuthenticationFilter / Security Filter
    -> catch (Throwable)
    -> globalExceptionHandler.allExceptionHandler(request, ex)
       -> 内部 if-else 分发到具体处理方法
       -> CommonResult 直接写入 response

[异步错误日志]
  defaultExceptionHandler
    -> createExceptionLog()
       -> apiErrorLogApi.createApiErrorLogAsync() (Feign -> infra-server)
          -> ApiErrorLogServiceImpl.createApiErrorLog()
             -> ApiErrorLogMapper.insert()
```

## 配置与条件

### 异常类型与响应映射

| 异常类型 | HTTP Code | CommonResult Code | 说明 |
|----------|-----------|-------------------|------|
| 参数校验异常 (所有) | 400 | 400 | BAD_REQUEST |
| HttpMessageNotReadableException | 400 | 400 | 请求体缺失/类型错误 |
| AccessDeniedException | 403 | 403 | FORBIDDEN |
| NoHandlerFoundException | 404 | 404 | NOT_FOUND |
| NoResourceFoundException | 404 | 404 | NOT_FOUND |
| HttpRequestMethodNotSupportedException | 405 | 405 | METHOD_NOT_ALLOWED |
| ServiceException | 200 | 业务自定义 | 如 500100 |
| 表不存在 | 200 | 501 | NOT_IMPLEMENTED |
| 其他异常 | 200 | 500 | INTERNAL_SERVER_ERROR |

### 配置项

| 配置 | 说明 |
|------|------|
| `spring.mvc.throw-exception-if-no-handler-found=true` | 404 时抛出 NoHandlerFoundException |
| `spring.mvc.static-path-pattern=/statics/**` | 避免静态资源被 NoHandlerFoundException 拦截 |
| `IGNORE_ERROR_MESSAGES` | 静态集合, 配置忽略堆栈打印的异常消息 |

## 生产级关注点

### 1. 参数校验异常的细节处理

不同 Spring MVC 参数校验失败抛出不同异常，这里有不同的处理策略：

| 异常 | 触发场景 | 错误消息 |
|------|----------|----------|
| MissingServletRequestParameterException | `@RequestParam` 必填参数缺失 | `"请求参数缺失: xxx"` |
| MethodArgumentTypeMismatchException | 参数类型转换失败 | `"请求参数类型错误: xxx"` |
| MethodArgumentNotValidException | `@Valid/@Validated` 校验失败 | `"请求参数不正确: " + fieldError` |
| BindException | 数据绑定失败 | `"请求参数不正确: " + fieldError` |
| ConstraintViolationException | `@RequestParam` 上的 Bean Validation | `"请求参数不正确: " + message` |
| HttpMessageNotReadableException | JSON 解析失败 / 请求体缺失 | `"请求参数类型错误: "` |

### 2. Filter 层的异常处理

Filter 在 Spring MVC 的 `@ExceptionHandler` 范围之外，所以不能通过 `@ExceptionHandler` 处理。框架提供 `allExceptionHandler` 公共方法：
- `TokenAuthenticationFilter` 中 catch 所有异常，调用此方法
- 方法内部通过 `if-else` 链实现与 `@ExceptionHandler` 相同的处理逻辑
- 是**代码复用**的体现，而不是通过继承/切面实现

### 3. 表不存在检测的局限性

表名前缀匹配是通过 `String.contains()` 实现的，存在误判风险：
- 如果某个业务表名恰好包含 `report_`、`bpm_` 等前缀，会被误判
- 表名前缀列表需要手动维护，新增模块时需要更新
- 只支持 "doesn't exist" 关键字（MySQL 错误消息），其他数据库的类似错误可能检测不到

### 4. 异步错误日志的可靠性

`createApiErrorLogAsync()` 是异步方法（通过 `@Async` 或 MQ），不会阻塞请求响应：
- 错误日志记录失败**不影响**主流程
- 如果 Feign 调用失败（网络问题），catch 中只记录一行日志，不会抛出异常
- 可能在服务大规模故障时丢失错误日志（因为 Feign 依赖下游服务）

### 5. 日志级别控制

- 参数校验异常（400）：`log.warn`，不是系统异常
- 业务异常（ServiceException）：`log.warn`，只打印第一行堆栈
- 权限不足（403）：`log.warn`，记录 userId 和 URL
- 未预期异常（500）：`log.error`，完整堆栈

这种分级策略避免了日志"过量"，同时保留了关键的异常上下文。

### 6. 监控集成

可以通过以下方式监控异常：
- **错误日志**: 所有 500 错误自动记录到 `infra_api_error_log` 表
- **TraceId**: 使用 `TracerUtils.getTraceId()` 关联链路追踪系统
- **应用名**: 通过 `applicationName` 字段区分不同微服务的异常

### 7. 服务启动时的异常处理

如果业务模块未启用（对应的表不存在），用户第一次访问时会收到友好的提示（"请参考文档开启 XXX 模块"），而不是 500 页面。

### 8. UncheckedExecutionException 解包

```java
@ExceptionHandler(value = UncheckedExecutionException.class)
public CommonResult<?> uncheckedExecutionExceptionHandler(HttpServletRequest req, UncheckedExecutionException ex) {
    return allExceptionHandler(req, ex.getCause());
}
```

Guava LoadingCache 在 `load()` 抛出异常时会包装为 `UncheckedExecutionException`。此方法解包后重新处理，让真正的异常类型进入对应的 handler。
