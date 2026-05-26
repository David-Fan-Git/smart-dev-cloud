---
name: starter-job
description: XXL-Job distributed scheduling with executor auto-configuration, @Async TTL thread pool, and multi-tenant job execution support
type: project
---

# develop-spring-boot-starter-job

## Overview

分布式定时任务模块。整合 XXL-Job 2.4.0，提供执行器自动配置、`@XxlJob` 注解支持、异步任务线程池（TTL 传播、`@Async` 支持），以及与多租户模块配合的 `@TenantJob`/`@TenantIgnore` 注解。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **2 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.quartz`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-job AutoConfiguration.imports
com.develop.mvp.pk.framework.quartz.config.DevelopXxlJobAutoConfiguration
com.develop.mvp.pk.framework.quartz.config.DevelopAsyncAutoConfiguration
```

## Core Components

### 1. DevelopXxlJobAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.quartz.config`

- **XxlJobSpringExecutor**: XXL-Job executor Bean
  - Conditional: `xxl.job.enabled=true` (disabled by default for monolithic/local dev)
  - Registers `@XxlJob` annotated methods with the XXL-Job Admin
  - Properties bound via `XxlJobProperties`

- **XxlJobProperties**: `@ConfigurationProperties(prefix = "xxl.job")`

### 2. DevelopAsyncAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.quartz.config`

- `@EnableAsync` — enables Spring's `@Async` method execution
- **TtlRunnable/TtlCallable wrapping**: configures the task executor to wrap submitted tasks with `TtlRunnable`/`TtlCallable` from `transmittable-thread-local`
  - Ensures `TransmittableThreadLocal` context (tenant ID, login user, etc.) is propagated to async threads
  - Covers: `@Async` annotated methods, `ThreadPoolTaskExecutor`, and explicit `CompletableFuture` usage

### 3. Job Annotations

- **@XxlJob("jobName")**: XXL-Job native annotation, placed on `@Component` class methods
  ```java
  @Component
  public class DemoJob {
      @XxlJob("demoJob")
      public void execute() {
          String param = XxlJobHelper.getJobParam();
          // job logic
          XxlJobHelper.handleSuccess("completed");
      }
  }
  ```

- **@TenantJob** (from starter-tenant): iterates over all active tenants, executes the job once per tenant
- **@TenantIgnore** (from starter-tenant): skips tenant context for a single-execution job

### 4. Execution Modes

```java
@Component
public class DemoJob {
    @XxlJob("demoJob")
    public void execute() throws Exception {
        String param = XxlJobHelper.getJobParam();
        System.out.println("Executing job with param: " + param);
        XxlJobHelper.handleSuccess("执行完成");
    }
}
```

## Configuration Properties

```yaml
xxl:
  job:
    enabled: false                          # Default: disabled (enable in production)
    admin-addresses: http://localhost:9080/xxl-job-admin
    app-name: ${spring.application.name}    # Executor app name (registered in Admin)
    ip:                                     # Executor IP (auto-detect if empty)
    port: 9999                              # Executor port (must not conflict with app port)
    access-token: default_token             # Communication token
    log-path: /data/applogs/xxl-job/jobhandler  # Job log path
    log-retention-days: 30                  # Log retention
```

## Code Examples

```java
// Standard scheduled job
@Component
public class OrderTimeoutJob {
    @XxlJob("orderTimeout")
    public void execute() {
        String param = XxlJobHelper.getJobParam();
        int timeoutMinutes = Integer.parseInt(param);
        List<OrderDO> timeoutOrders = orderService.getTimeoutOrders(timeoutMinutes);
        for (OrderDO order : timeoutOrders) {
            orderService.cancelOrder(order.getId(), "超时未支付");
        }
        XxlJobHelper.handleSuccess("处理超时订单 " + timeoutOrders.size() + " 条");
    }
}

// Multi-tenant job
@Component
public class StatisticsJob {
    @TenantJob   // One execution per tenant
    @XxlJob("dailyStatistics")
    public void execute() {
        // TenantContextHolder is set for current tenant
        StatisticDO stat = statisticsService.collectDaily();
        statisticsService.save(stat);
        XxlJobHelper.handleSuccess("租户 " + TenantContextHolder.getTenantId() + " 统计完成");
    }
}

// Async method with TTL propagation
@Async
public CompletableFuture<Void> sendNotificationAsync(Long userId) {
    // TTL context (tenant ID, login user) auto-propagated
    notificationService.send(userId);
    return CompletableFuture.completedFuture(null);
}
```

## Error Handling and Retry

| Failure Type | XXL-Job Behavior | Recommendation |
|---|---|---|
| Business exception (invalid data) | Job marked FAIL | Call `XxlJobHelper.handleFail()` for explicit failure |
| Transient error (network timeout, DB deadlock) | Job marked FAIL, can be retried manually | Consider splitting into smaller batches |
| Long timeout (job running > max timeout) | Admin kills the job | Report intermediate progress with `XxlJobHelper.handleSuccess()` |
| @TenantJob fails on tenant #5/100 | Remaining tenants still execute | Log per-tenant result with tenant ID |

## Performance Considerations

- **@TenantJob serial execution**: tenants run one-by-one; for 100+ tenants, consider parallel processing with a custom ExecutorService (but be mindful of DB connection pool exhaustion)
- **Batch processing**: for large datasets, process in pages (e.g., 1000 records per page) and report progress after each page
- **Async queue monitoring**: monitor `ThreadPoolTaskExecutor` queue depth and reject count; configure `max-queue-size` and rejection policy appropriate to workload
- **Job log size**: `xxl.job.log-retention-days` controls on-disk log cleanup; set according to disk capacity and compliance requirements

## 注意事项

- `@TenantJob` 的租户遍历顺序依赖 `TenantCommonApi.getTenantList()`，新增租户后需等待缓存刷新（1分钟 Guava 缓存）或主动清除缓存
- XXL-Job 执行器端口（`xxl.job.port`）不能与应用端口冲突；多实例部署时需确保每实例端口唯一（可通过 `-Dxxl.job.port` 启动参数覆盖）
- 任务失败建议调用 `XxlJobHelper.handleFail()` 触发调度中心的告警策略；未显式调用 handleFail 的任务会被 XXL-Job 标记为成功
- 长时间运行的任务建议每批处理完成后调用 `XxlJobHelper.handleSuccess()` 报告中间进度，避免被调度中心判定为超时
- `@Async` 默认线程池需监控队列积压，建议自定义 `ThreadPoolTaskExecutor` 并配置 `maxQueueSize` 和拒绝策略（`CallerRunsPolicy` 或 `AbortPolicy`）
- `TtlRunnable` 包装仅对 `@Async` 和显式提交到线程池的任务生效，`new Thread()` 创建的线程不继承上下文——禁止在项目中使用 `new Thread()`
- XXL-Job Admin 与 Executor 之间的通信使用 access-token 认证，生产环境需设置强密码
