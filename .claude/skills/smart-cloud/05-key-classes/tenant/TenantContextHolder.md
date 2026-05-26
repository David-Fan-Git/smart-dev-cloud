---
name: TenantContextHolder
description: ThreadLocal holder for tenant ID that propagates across thread pools via TransmittableThreadLocal
type: project
---

# TenantContextHolder

## 功能定位

TenantContextHolder 是多租户上下文的持有器，位于 `develop-spring-boot-starter-biz-tenant` 的 `core.context` 包下。它使用 Alibaba 的 `TransmittableThreadLocal`（TTL）在线程间传递租户 ID 和忽略标识。

核心职责：
- **租户 ID 存储**：当前请求所属的租户编号
- **忽略标识**：标记当前请求是否需要跳过租户过滤
- **线程传播**：通过 TTL 在线程池中自动传递上下文

它是全平台租户信息的**单一数据源**，所有需要感知租户的组件都从此处获取当前租户编号：
- `TenantDatabaseInterceptor`：获取租户 ID 用于 SQL 过滤
- `TenantContextWebFilter`：从请求 Header 中提取租户 ID 并设置
- 业务 Service：获取当前租户信息用于业务逻辑
- `TenantUtils.execute()`：编程式切换租户

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **ThreadLocal Context Holder** | 通过 TTL 在线程维度持有租户上下文 | `TransmittableThreadLocal<T>` |
| **Singleton** | 静态方法和字段，全局唯一访问点 | 所有字段和方法都是 `static` 的 |
| **Propagation** | TTL 自动在线程池中传播上下文 | `TransmittableThreadLocal` 特性 |
| **Null Object / Guard** | 保护性异常 | `getRequiredTenantId()` 防止 null 传播 |

## 核心逻辑流程

### 数据生命周期

```
[请求入口]
  TenantContextWebFilter.doFilterInternal()
    |
    +-- 从 request header["tenant-id"] 获取租户编号
    |
    +-- TenantContextHolder.setTenantId(tenantId)
    |    -> TENANT_ID.set(tenantId)
    |    -> IGNORE.set(false)
    |
    +-- chain.doFilter(request, response)   [执行业务逻辑]
    |    |
    |    +-- 业务代码期间:
    |    |    TenantContextHolder.getTenantId() 获取租户
    |    |    TenantContextHolder.getRequiredTenantId() 获取租户(强制非空)
    |    |    TenantContextHolder.isIgnore() 判断是否跳过租户
    |    |
    |    +-- 跨租户操作:
    |    |    TenantUtils.execute(targetTenantId, () -> {
    |    |        // 临时切换到目标租户
    |    |        // 内部调用 setTenantId + clear
    |    |    })
    |    |
    |    +-- 临时跳过租户过滤:
    |         TenantContextHolder.setIgnore(true)
    |         // 操作不需要租户过滤的数据
    |         TenantContextHolder.setIgnore(false)
    |
    +-- finally:
         TenantContextHolder.clear()
         -> TENANT_ID.remove()
         -> IGNORE.remove()
```

### 线程池传播机制

```
[主线程]                              [子线程]
         |                                |
  setTenantId(123)                        |
         |                                |
  提交到线程池 --- TTL 自动复制 --->  getTenantId() = 123
         |                                |
  修改租户为 456                 getTenantId() 仍 = 123
         |                           (子线程独立副本)
  clear()                                |
         |                                |
  提交到线程池 --- TTL 自动复制 --->  getTenantId() = null  (已清理)
```

### 异步场景传播

```
异步 MQ 消费:
  MQ 消息体中携带 tenantId
    -> 消费者收到消息
    -> TenantContextHolder.setTenantId(message.getTenantId())
    -> 执行业务逻辑
    -> finally { TenantContextHolder.clear() }

异步定时任务:
  @XxlJob("demoJob")
    -> 从 JobHandler 参数或数据库中获取 tenantId
    -> TenantContextHolder.setTenantId(tenantId)
    -> 执行任务
    -> finally { TenantContextHolder.clear() }
```

## 关键代码剖析

```java
public class TenantContextHolder {

    /**
     * 当前租户编号
     */
    private static final ThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    /**
     * 是否忽略租户
     */
    private static final ThreadLocal<Boolean> IGNORE = new TransmittableThreadLocal<>();

    /**
     * 获取当前租户编号
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 获取当前租户编号，如果不存在则抛出 NullPointerException
     * 这比返回 null 更安全，因为 null 的租户 ID 进入 SQL 会导致 "tenant_id = NULL" 查不到数据
     * 而开发者在调试时看到 NPE 更容易定位问题
     */
    public static Long getRequiredTenantId() {
        Long tenantId = getTenantId();
        if (tenantId == null) {
            throw new NullPointerException("TenantContextHolder 不存在租户编号！可参考文档："
                + DocumentEnum.TENANT.getUrl());
        }
        return tenantId;
    }

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static void setIgnore(Boolean ignore) {
        IGNORE.set(ignore);
    }

    /**
     * 当前是否忽略租户
     * 使用 Boolean.TRUE.equals() 防止 NPE
     */
    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE.get());
    }

    /**
     * 清理上下文，必须在请求结束时调用！
     */
    public static void clear() {
        TENANT_ID.remove();
        IGNORE.remove();
    }
}
```

### TransmittableThreadLocal 的优势

相比 `ThreadLocal` 和 `InheritableThreadLocal`：

| 类型 | 父子线程传递 | 线程池复用 | 适用场景 |
|------|-------------|-----------|----------|
| ThreadLocal | 不传递 | 不传递 | 简单请求 |
| InheritableThreadLocal | 创建时传递 | 线程池复用时不传递 | 简单异步 |
| **TransmittableThreadLocal** | **创建时传递** | **线程池复用时也传递** | **线程池 + 异步任务** |

框架中大量使用异步任务（MQ 消费、定时任务、`@Async` 方法），`TransmittableThreadLocal` 能确保在这些场景中租户上下文正确传递。

## 调用链

```
[上游设置者]
  |-- TenantContextWebFilter
  |     HTTP 请求到达 -> header["tenant-id"] -> setTenantId()
  |
  |-- TenantUtils.execute(tenantId, runnable)
  |     编程式切换租户 -> 内部 setTenantId() -> 执行 -> 恢复
  |
  |-- MQ 消费者
  |     消息中携带 tenantId -> setTenantId()
  |
  |-- 定时任务 (XXL-Job)
  |     任务参数或数据库中获取 -> setTenantId()

[下游读取者]
  |-- TenantDatabaseInterceptor.getTenantId()
  |     -> getRequiredTenantId() -> SQL 过滤
  |
  |-- TenantDatabaseInterceptor.ignoreTable()
  |     -> isIgnore() -> 跳过过滤
  |
  |-- 业务 Service
  |     -> getTenantId() -> 业务逻辑中的租户判断
  |
  |-- TenantContextWebFilter
  |     -> clear() -> 请求结束后清理
```

## 配置与条件

| 方法 | 语义 | 典型调用者 |
|------|------|-----------|
| `setTenantId(null)` | 清空当前租户 | TenantContextWebFilter (header 无 tenant-id 时) |
| `setIgnore(true)` | 跳过租户过滤（所有 SQL 不加 WHERE tenant_id） | AdminAuthController (登录接口) |
| `getRequiredTenantId()` | 强制要求租户存在，否则抛 NPE | TenantDatabaseInterceptor |
| `clear()` | 清理上下文 | TenantContextWebFilter (finally 块) |

## 生产级关注点

### 1. 内存泄漏风险

ThreadLocal 的典型风险是**内存泄漏**。如果设置了租户 ID 但未在请求结束时调用 `clear()`，会导致：
- 当前线程归还线程池后，下一个请求复用该线程时仍然持有上一个请求的租户 ID
- 导致数据错乱（租户 A 的请求查到了租户 B 的数据）

**防护措施**: `TenantContextWebFilter` 在 `finally` 块中确保 `clear()` 被调用。

### 2. 子线程修改隔离

子线程通过 TTL 复制了父线程的值，但修改子线程的值不会影响父线程：
```java
// 父线程
TenantContextHolder.setTenantId(1L);
// 子线程修改
TenantContextHolder.setTenantId(2L); // 不影响父线程
```

### 3. TTL 的侵入性

TTL 要求使用 `TtlRunnable` / `TtlCallable` 包装提交到线程池的任务：
```java
executor.submit(TtlRunnable.get(() -> {
    // 这里可以获取到父线程的 TenantId
}));
```

或者通过 `TransmittableThreadLocal.TransmittableThreadLocalExecutor` 自动包装。

### 4. getRequiredTenantId() 的设计意图

为什么要有 `getRequiredTenantId()` 而不仅仅是 `getTenantId()`？

如果 `TenantDatabaseInterceptor` 拿到 `null` 的租户 ID，生成的 SQL 片段是 `AND tenant_id = NULL`，这在 SQL 中等于 `AND tenant_id IS NULL`（正确的语义应该是 `AND 1=0`），导致查不到任何数据。这种错误很难追踪。因此，当租户 ID 为空时直接抛 NPE，开发者能立即定位问题。

### 5. 跨租户操作

`TenantUtils.execute()` 的实现：
```java
public static void execute(Long tenantId, Runnable runnable) {
    Long oldTenantId = getTenantId();
    try {
        setTenantId(tenantId);
        runnable.run();
    } finally {
        setTenantId(oldTenantId);
    }
}
```

**重要**: 它用保存-恢复的方式，而不是 `clear()`，确保操作结束后不会影响外层的租户上下文。

### 6. 单元测试注意

测试多租户逻辑时，需要在每个测试方法前后手动设置和清理租户上下文：
```java
@BeforeEach
void setUp() {
    TenantContextHolder.setTenantId(1L);
}

@AfterEach
void tearDown() {
    TenantContextHolder.clear();
}
```
