---
name: starter-monitor
description: Monitoring and tracing — SkyWalking APM integration, Micrometer metrics, Actuator endpoints, TraceID propagation, and custom metric registration
type: project
---

# develop-spring-boot-starter-monitor

## Overview

应用监控与链路追踪模块。集成 **SkyWalking**（分布式追踪）、**Micrometer**（指标采集）和 **Spring Boot Actuator**（运维端点）。为微服务集群提供可观测性基础能力。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **2 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.tracer`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-monitor AutoConfiguration.imports
com.develop.mvp.pk.framework.tracer.config.DevelopTracerAutoConfiguration
com.develop.mvp.pk.framework.tracer.config.DevelopMetricsAutoConfiguration
```

## Core Components

### 1. SkyWalking Integration via DevelopTracerAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.tracer.config.DevelopTracerAutoConfiguration`

- **Conditional**: `@ConditionalOnClass(name = "org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer")` AND `@ConditionalOnProperty(prefix = "develop.tracer", value = "enable", matchIfMissing = true)`
- **Note:** The configuration prefix is `develop.tracer`, **NOT** `develop.monitor`

Provides:
- **TraceFilter**: `@Order(WebFilterOrderEnum.TRACE_FILTER)` — 2nd filter in chain (after CORS)
  - Generates traceId for each request
  - Sets traceId in response header for client-side debugging
  - Integrates with SkyWalking agent if present

- **apm-toolkit integration** (compile-time dependencies, agent required at runtime):
  - `apm-toolkit-trace`: `@Trace` annotation, `ActiveSpan` API for custom span creation
  - `apm-toolkit-logback`: injects `%traceId` into MDC for log correlation
  - `apm-toolkit-opentracing`: compatible OpenTracing API bridge

**Important Design Note:**
The `DevelopTracerAutoConfiguration` has commented-out OpenTracing integration (`GlobalTracer.register()`) due to compatibility issues — SkyWalking does not fully support the latest OpenTracing API versions. The code comment indicates a planned migration to OpenTelemetry.

### 2. DevelopMetricsAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.tracer.config.DevelopMetricsAutoConfiguration`

- **MeterRegistryCustomizer**: registers common tags
  ```java
  @Bean
  public MeterRegistryCustomizer<MeterRegistry> metricsCommonTag() {
      return registry -> registry.config().commonTags(
          "application", applicationName,
          "instance", instanceId
      );
  }
  ```

- Default Micrometer metrics collected:
  - JVM: memory (heap/non-heap), GC (count/pause time), threads, classes loaded
  - CPU usage
  - File descriptor usage
  - Tomcat requests (count, timing, error rate)
  - Data source connection pool (active, idle, pending)
  - Logback: log event counters per level
  - Cache: Redis cache hit/miss ratio

- Integration points:
  - Prometheus: `micrometer-registry-prometheus` (optional dependency)
  - JMX: `micrometer-registry-jmx` (optional dependency)

### 3. Actuator Endpoints

```yaml
management:
  endpoints:
    web:
      base-path: /actuator                # Actuator base path
      exposure:
        include: '*'                      # Expose all endpoints (restrict in production)
  endpoint:
    health:
      show-details: when-authorized       # Health details visibility
      show-components: when-authorized
  info:
    env:
      enabled: true                       # Expose environment info
```

**Key endpoints:**
| Endpoint | Purpose | Production Safety |
|---|---|---|
| `/actuator/health` | Health check (liveness + readiness) | Safe to expose |
| `/actuator/info` | Application info (version, build) | Safe to expose |
| `/actuator/metrics` | Micrometer metrics (JVM, Tomcat, DB) | Restrict to internal |
| `/actuator/prometheus` | Prometheus scrape endpoint | Restrict to prometheus IP |
| `/actuator/env` | Environment properties (may contain secrets) | Restrict to admin |
| `/actuator/logfile` | Log file content | Restrict to admin |
| `/actuator/configprops` | Configuration properties | Restrict to admin |

### 4. TracerUtils

**Location:** `com.develop.mvp.pk.framework.common.util.monitor.TracerUtils` (in develop-common)

Provides access to current TraceId without requiring runtime SkyWalking agent:

```java
public class TracerUtils {
    // Get SkyWalking traceId (returns null if no agent present)
    public static String getTraceId() {
        return TraceContext.traceId();  // From apm-toolkit-trace (compile-only dependency)
    }
    // Get spanId
    public static String getSpanId() {
        return TraceContext.spanId();
    }
}
```

- Located in `develop-common` module (not in this starter) so all modules can access it
- Compile-time dependency on `apm-toolkit-trace` with `provided` scope
- Returns `null` when no SkyWalking agent is loaded — callers must handle null safely

### 5. Logging with TraceId

In `logback-spring.xml`, the traceId pattern is configured:

```xml
<encoder>
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%traceId] - %msg%n</pattern>
</encoder>
```

- `[%traceId]` is populated by `apm-toolkit-logback` when SkyWalking agent is present
- Without agent: prints `[TID:N/A]`
- Example output:
  ```
  2026-05-23 10:30:00.123 [http-nio-48080-exec-1] INFO  [TID.1a2b3c4d5e6f] - Order created successfully
  ```

## Configuration Properties

```yaml
# SkyWalking Agent JVM arguments (must be set at JVM startup, not in application.yaml):
# -javaagent:/path/to/skywalking-agent/skywalking-agent.jar
# -DSW_AGENT_NAME=develop-system-server
# -DSW_AGENT_COLLECTOR_BACKEND_SERVICES=127.0.0.1:11800

develop:
  tracer:
    enable: true                    # Enable TraceFilter and tracer integration (default: true)
```

## Code Examples

```java
// Custom tracing
@Trace(operationName = "OrderService.pay")
public void pay(Long orderId, Integer amount) {
    ActiveSpan.tag("order_id", orderId.toString());
    ActiveSpan.tag("amount", amount.toString());
    // business logic
}

// Log traceId in business code
String traceId = TracerUtils.getTraceId();
log.warn("处理异常, traceId={}", Optional.ofNullable(traceId).orElse("N/A"));

// Access Actuator API
// GET /actuator/health         — Health check
// GET /actuator/info           — Application info
// GET /actuator/metrics         — Metric list
// GET /actuator/prometheus     — Prometheus format metrics

// Custom Micrometer metric
@Autowired
private MeterRegistry meterRegistry;

public void recordOrderCreate() {
    meterRegistry.counter("order.create.total").increment();
    meterRegistry.timer("order.create.time").record(() -> {
        // timed operation
    });
}
```

## Performance Considerations

| Component | Overhead | Notes |
|---|---|---|
| TraceFilter | < 0.1ms per request | Just sets response header, negligible |
| SkyWalking Agent | ~5-10% CPU overhead | Sampling rate can be configured (default: 100%) |
| `@Trace` annotation | ~0.5ms per span | Use sparingly on hot paths |
| Micrometer JVM metrics | Collection at fixed intervals (default: 60s) | No per-request overhead |
| Actuator endpoints | Only when accessed | Disable unnecessary endpoints in production |

## 注意事项

- SkyWalking Agent **必须**在 JVM 启动参数中通过 `-javaagent` 加载，不可仅依赖 Maven 依赖实现插桩。`apm-toolkit` 依赖仅提供手动 API 和日志 MDC 注入，实际插桩由 Agent 在类加载时完成
- `TracerUtils.getTraceId()` 在无 SkyWalking Agent 环境下返回 null，业务代码中调用时需做 null 判断或提供兜底值
- Actuator 配置前缀为 `develop.tracer`（**非** `develop.monitor`），`TracerProperties` 类使用 `@ConfigurationProperties(prefix = "develop.tracer")`
- 生产环境 Actuator 端点建议仅开放 `health` 和 `info`；`prometheus`、`env`、`configprops` 等端点需配合安全认证（IP 白名单或 Spring Security）
- Micrometer 与 Prometheus 集成时，需确保 `/actuator/prometheus` 端点在安全网关后暴露，避免未经授权的指标采集
- SkyWalking `TraceFilter` 为所有请求生成 Trace 上下文，对监控无关的静态资源请求（如健康检查），可通过配置忽略路径减少不必要的追踪开销
- 项目中 OpenTracing 集成因兼容性问题被注释，官方已计划迁移至 OpenTelemetry——新开发的追踪相关代码建议直接使用 OpenTelemetry API
- `TracerUtils` 位于 `develop-common` 模块（非 `develop-spring-boot-starter-monitor`），编译期依赖 `apm-toolkit`（provided scope），确保所有模块都能调用但无运行时强制依赖
