---
name: exception-handling
description: Error code definition by ServiceErrorCodeRange, ServiceExceptionUtil usage, GlobalExceptionHandler flow, best practices
type: project
---

# 异常处理模式

## 概述

三段式流程：**定义错误码**（ErrorCodeConstants）→ **Service 抛出**（`ServiceExceptionUtil.exception()`）→ **全局处理器捕获**（`GlobalExceptionHandler`）。

## 错误码编码规则

错误码定义在 `com.develop.mvp.pk.framework.common.exception.enums.ServiceErrorCodeRange`，共 10 位分四段：

```
1 - 002 - 001 - 000
│    │     │     └─ 序列 (000~999)，自增
│    │     └─────── 领域 (3位)，如 001=菜单, 002=角色, 003=用户
│    └───────────── 模块 (3位)，如 002=system, 003=report, 004=member
└────────────────── 类型 (1位)，1=业务异常
```

### 各模块错误码区间

| 模块 | 区间 |
|---|---|
| infra | [1-001-000-000 ~ 1-002-000-000) |
| **system** | [**1-002-000-000 ~ 1-003-000-000)** |
| report | [1-003-000-000 ~ 1-004-000-000) |
| member | [1-004-000-000 ~ 1-005-000-000) |
| mp | [1-006-000-000 ~ 1-007-000-000) |
| pay | [1-007-000-000 ~ 1-008-000-000) |
| product | [1-008-000-000 ~ 1-009-000-000) |
| bpm | [1-009-000-000 ~ 1-010-000-000) |
| trade | [1-011-000-000 ~ 1-012-000-000) |
| promotion | [1-013-000-000 ~ 1-014-000-000) |
| crm | [1-020-000-000 ~ 1-021-000-000) |
| ai | [1-022-000-000 ~ 1-023-000-000) |

## 定义错误码

```java
package com.develop.mvp.pk.module.{module}.enums;

import com.develop.mvp.pk.framework.common.exception.ErrorCode;

/**
 * {模块} 错误码常量
 *
 * 区间：{errorCodeRange}
 */
public interface ErrorCodeConstants {

    // ========== {领域} {codePrefix} ==========
    ErrorCode {DOMAIN}_NOT_EXISTS = new ErrorCode({codePrefix}_001, "{领域}不存在");
    ErrorCode {DOMAIN}_NAME_DUPLICATE = new ErrorCode({codePrefix}_002, "{领域}名称「{}」已存在");
    ErrorCode {DOMAIN}_EXISTS_CHILDREN = new ErrorCode({codePrefix}_004, "{领域}存在子节点，无法删除");
    ErrorCode {DOMAIN}_PARENT_NOT_EXISTS = new ErrorCode({codePrefix}_005, "父{领域}不存在");
}
```

**实际示例**（system 模块菜单）：

```java
// ========== 菜单模块 1-002-001-000 ==========
ErrorCode MENU_NAME_DUPLICATE = new ErrorCode(1_002_001_000, "已经存在该名字的菜单");
ErrorCode MENU_PARENT_NOT_EXISTS = new ErrorCode(1_002_001_001, "父菜单不存在");
ErrorCode MENU_NOT_EXISTS = new ErrorCode(1_002_001_003, "菜单不存在");
ErrorCode MENU_EXISTS_CHILDREN = new ErrorCode(1_002_001_004, "存在子菜单，无法删除");
```

## Service 中抛出异常

```java
import static com.develop.mvp.pk.module.{module}.enums.ErrorCodeConstants.*;

@Service
@Validated
public class {Domain}ServiceImpl implements {Domain}Service {

    @Override
    public Long create{Domain}({Domain}SaveReqVO createReqVO) {
        // 带参数的错误：{} 占位符自动替换
        if ({domain}Mapper.selectByName(createReqVO.getName()) != null) {
            throw ServiceExceptionUtil.exception(
                    ErrorCodeConstants.{DOMAIN}_NAME_DUPLICATE, createReqVO.getName());
        }
        // ... 正常逻辑
    }

    @Override
    public void delete{Domain}(Long id) {
        if ({domain}Mapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception({DOMAIN}_NOT_EXISTS);
        }
        if ({domain}Mapper.selectCount({Domain}DO::getParentId, id) > 0) {
            throw ServiceExceptionUtil.exception({DOMAIN}_EXISTS_CHILDREN);
        }
    }
}
```

### ServiceExceptionUtil 方法

```java
// 标准错误（无参数）
ServiceExceptionUtil.exception(errorCode);

// 带参数错误（{} 占位符按顺序替换）
ServiceExceptionUtil.exception(errorCode, arg1, arg2);

// 参数无效快捷方法
ServiceExceptionUtil.invalidParamException("参数[id]不合法：{}", id);
```

## 全局异常处理器（框架层）

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 业务异常 → 返回对应的错误码
    @ExceptionHandler(ServiceException.class)
    public CommonResult<?> handleServiceException(ServiceException ex) {
        return CommonResult.error(ex.getCode(), ex.getMessage());
    }

    // 安全异常 → 403
    @ExceptionHandler(AccessDeniedException.class)
    public CommonResult<?> handleAccessDeniedException(HttpServletRequest req,
                                                        AccessDeniedException ex) {
        return CommonResult.error(403, "没有访问权限");
    }

    // 校验异常 → 400
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<?> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage()).collect(Collectors.joining("; "));
        return CommonResult.error(400, msg);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage()).collect(Collectors.joining("; "));
        return CommonResult.error(400, msg);
    }

    // 最终兜底 → 500 + 异步记录 error 日志
    @ExceptionHandler(Exception.class)
    public CommonResult<?> handleException(Exception ex) {
        log.error("[handleException]", ex);
        apiErrorLogApi.createApiErrorLog(createErrorLog(ex));
        return CommonResult.error(500, "系统异常，请联系管理员");
    }
}
```

## 异常响应格式

```json
{
    "code": 1_002_001_003,
    "msg": "菜单不存在",
    "data": null
}
```

| 字段 | 说明 |
|---|---|
| `code` | 成功为 0，错误为业务错误码 |
| `msg` | 成功为空串，错误为错误消息 |
| `data` | 成功为业务数据，错误为 null |

## 关键点

1. **错误码编码严格遵循 `ServiceErrorCodeRange`** — 各模块有独立区间，不可跨段使用
2. **`ErrorCodeConstants` 使用 `1_002_000_000` 下划线格式** — Java 数值字面量下划线可读性
3. **`ServiceExceptionUtil.exception()` 是唯一抛出入口** — 不手动 `new ServiceException()`
4. **`{}` 占位符** — ErrorCode message 中的 `{}` 按顺序替换（非 `String.format` 的 `%s`）
5. **`invalidParamException()`** — 参数校验快捷方法
6. **`GlobalExceptionHandler` 框架提供** — 开发者只需定义 ErrorCode 并抛出
7. **500 异常自动入库** — `ApiErrorLogApi.createApiErrorLog()` 异步写入 `infra_api_error_log` 表

## 常见错误

- 在 Controller 中捕获异常并手动 `CommonResult.error()` — 应直接抛出
- 错误码不遵循编码规则（如使用 0、-1、随机数字）
- 错误消息用 `String.format` 或拼接而非 `{}` 占位符
- 在 Service 中打印 ERROR 日志并吞掉异常 — 直接 throw，全局处理器负责
- 在循环中反复 throw — 循环前一次性校验所有数据
- 两个模块使用了相同的错误码编码 — 违反 `ServiceErrorCodeRange`
