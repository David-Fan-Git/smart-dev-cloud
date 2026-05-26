---
name: deployment-modes
description: Smart Cloud microservices mode vs monolithic mode — Maven dependency management, configuration switching, deployment topology, and best practices
type: project
---

# Deployment Modes

## Overview

Smart Cloud 支持两种部署模式: **微服务模式**（各模块独立进程）和 **单体模式**（单一 JVM 聚合所有模块）。两种模式通过 Maven 依赖管理 + 运行时配置 `spring.autoconfigure.exclude` + Nacos 开关切换，代码本身无需修改。

## Mode Comparison

| Dimension | Microservices Mode | Monolithic Mode |
|---|---|---|
| Deployment Unit | Per-module independent JVM | Single JVM (develop-server) |
| Service Discovery | Nacos | Disabled |
| Config Center | Nacos | Disabled |
| Remote Call | Feign / OpenFeign | Excluded via exclusion |
| Service Port | Per-module independent port | 48080 |
| Entry Class | Each -server module's Application | No @SpringBootApplication in develop-server |
| API Gateway | Spring Cloud Gateway | Not needed |
| Starting Speed | Slow (Nacos + multi-process) | Fast (single process) |
| Debugging | Cross-process debugging needed | Single-process, easy debugging |

## Microservices Mode (微服务模式)

每个 `develop-module-{name}-server` 作为独立进程运行，拥有自己的 `@SpringBootApplication` 启动类。

**Architecture Topology:**

```
                     +-------------------+
                     |   Nacos Server    |
                     | (Registry+Config) |
                     +--------+----------+
                              |
            +-----------------+------------------+
            |                                    |
+-----------v-----------+            +-----------v-----------+
|   Spring Cloud Gateway|            |  Nacos Config Server  |
|   (develop-gateway)    |            |  (Configuration Mgmt) |
|   Port: 48080          |            +-----------------------+
+-----------+-----------+
            |
    +-------+--------+--------+--------+------+
    |                |        |        |       |
    v                v        v        v       v
system-server   infra-server  member-server  bpm-server  ...
(Port: 48081)   (Port: 48082)  (Port: 48083)  (Port: 48084)
```

**Startup Commands:**
```bash
# Gateway
cd develop-gateway && mvn spring-boot:run

# System service
cd develop-module-system/develop-module-system-server && mvn spring-boot:run

# Infra service
cd develop-module-infra/develop-module-infra-server && mvn spring-boot:run

# Other modules - same pattern
```

**Port Allocation Convention:**

| Module | Port |
|---|---|
| develop-gateway | 48080 |
| develop-module-system | 48081 |
| develop-module-infra | 48082 |
| develop-module-member | 48083 |
| develop-module-bpm | 48084 |
| develop-module-pay | 48085 |
| develop-module-report | 48086 |
| develop-module-mp | 48087 |
| develop-module-mall (product) | 48088 |
| develop-module-mall (promotion) | 48089 |
| develop-module-mall (trade) | 48090 |
| develop-module-crm | 48091 |
| develop-module-erp | 48092 |
| develop-module-iot | 48093 |
| develop-module-mes | 48094 |
| develop-module-wms | 48095 |
| develop-module-ai | 48096 |

**Key Nacos Configuration (per-module application-dev.yaml):**
```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: true
        server-addr: 127.0.0.1:8848
        namespace: develop
      config:
        enabled: true
        server-addr: 127.0.0.1:8848
        namespace: develop
        group: DEFAULT_GROUP
        file-extension: yaml
```

## Monolithic Mode (单体模式)

`develop-server` 作为单体聚合器，通过 Maven 依赖引入所有 `develop-module-*-server` 模块，运行在单一 JVM 中。

**Topology:**
```
                    +-----------+
                    |  Single   |
                    |   JVM     |
                    |  Port     |
                    |  48080    |
                    +-----+-----+
                          |
          +---------------+---------------+
          |               |               |
    system-server    infra-server    member-server ...
    (Maven dep)     (Maven dep)     (Maven dep)
```

**develop-server/pom.xml Dependency Management:**
```xml
<!-- Default: core modules (fast compile) -->
<dependency>
    <groupId>com.develop.mvp</groupId>
    <artifactId>develop-module-system-server</artifactId>
    <version>${revision}</version>
</dependency>
<dependency>
    <groupId>com.develop.mvp</groupId>
    <artifactId>develop-module-infra-server</artifactId>
    <version>${revision}</version>
</dependency>

<!-- Optional modules: uncomment to enable -->
<!--
<dependency>
    <groupId>com.develop.mvp</groupId>
    <artifactId>develop-module-member-server</artifactId>
    <version>${revision}</version>
</dependency>
<dependency>
    <groupId>com.develop.mvp</groupId>
    <artifactId>develop-module-bpm-server</artifactId>
    <version>${revision}</version>
</dependency>
-->
```

**Critical: Exclude OpenFeign in Monolithic Mode:**
```xml
<!-- In develop-server/pom.xml -->
<dependency>
    <groupId>com.develop.mvp</groupId>
    <artifactId>develop-spring-boot-starter-rpc</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

**application.yaml Defaults (Common Section):**
```yaml
spring:
  cloud:
    nacos:
      discovery:
        enabled: false  # Disable Nacos service discovery
      config:
        enabled: false  # Disable Nacos config center
  main:
    allow-circular-references: true       # Allow circular deps (3-layer architecture)
    allow-bean-definition-overriding: true # Allow bean override (Feign conflict)
```

**application-local.yaml — Exclude RPC AutoConfiguration:**
```yaml
spring:
  autoconfigure:
    exclude:
      # Disable all RPC-related auto-configs for monolithic local startup
      - com.develop.mvp.pk.framework.security.config.DevelopSecurityRpcAutoConfiguration
      - com.develop.mvp.pk.framework.operatelog.config.DevelopOperateLogRpcAutoConfiguration
      - com.develop.mvp.pk.framework.datapermission.config.DevelopDataPermissionRpcAutoConfiguration
      - com.develop.mvp.pk.framework.dict.config.DevelopDictRpcAutoConfiguration
      - com.develop.mvp.pk.framework.tenant.config.DevelopTenantRpcAutoConfiguration
      - com.develop.mvp.pk.framework.env.config.DevelopEnvRpcAutoConfiguration
      - com.develop.mvp.pk.framework.apilog.config.DevelopApiLogRpcAutoConfiguration
      # AI vector store auto-configs (not needed locally)
      - org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration
      - org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration
```

**Startup:**
```bash
cd develop-server && mvn spring-boot:run
# Or package and run
java -jar develop-server/target/develop-server.jar
```

## How to Add/Remove Business Modules

**Monolithic Mode:**
1. Edit `develop-server/pom.xml` — add/comment `<dependency>` on `develop-module-{name}-server`
2. Restart develop-server

```xml
<!-- Enable new module: uncomment -->
<dependency>
    <groupId>com.develop.mvp</groupId>
    <artifactId>develop-module-crm-server</artifactId>
    <version>${revision}</version>
</dependency>
```

**Microservices Mode:**
1. Ensure `develop-module-{name}` has a standalone `@SpringBootApplication` entry class
2. Add `application.yaml` (or `application-dev.yaml`) with independent port + Nacos config
3. Add new module to root `pom.xml` `<modules>` section
4. Start independently

## Production Deployment Guidelines

**Monolithic Mode — When to Use:**
- Development and debugging (fast startup, default profile)
- Small teams or single-app scale
- Resource-constrained environments (dev machines, demo environments)
- Low ops complexity needed

**Microservices Mode — When to Use:**
- Larger teams, independent module development and iteration
- Independent scaling needs (e.g., system module high-load multi-instance)
- Independent release cadence per module
- Container orchestration (K8s) deployment
- Production environments with high availability requirements

**Common Configuration Checklist for Production:**
- Database connection pool: adjust `max-active` based on QPS (formula: `max-active = peakTPS * peakRT / 1000`)
- Redis: enable password, configure `max-clients`
- XXL-Job: configure `log-retention-days`, `access-token`
- Nacos: use MySQL-backed cluster mode
- Gateway: configure rate limiting, configure CORS origin pattern restrictively
- Log: configure rolling policy, set appropriate log levels

## Infrastructure Checklist

| Component | Microservices | Monolithic |
|---|---|---|
| MySQL | Required | Required |
| Redis | Required | Required |
| Nacos | Required | Not required |
| XXL-Job Admin | Optional | Optional |
| SkyWalking | Optional | Optional |
| Sentinel Dashboard | Optional | Optional |
| Spring Boot Admin | Optional | Optional |
| RocketMQ/RabbitMQ/Kafka | Optional | Optional |

## 注意事项

- 单体模式下 `develop-server` 打包为 jar 但不含 spring-boot-maven-plugin 的 `repackage` 时，需确认各 server 模块的依赖处理
- 切换模式时需同步检查 `develop-server/pom.xml` 中的 RPC exclusion 配置 + `application-local.yaml` 中的 `spring.autoconfigure.exclude`
- 微服务模式下各模块需独立维护数据库连接池配置（`max-active`、`min-idle` 等）
- 单体模式下 `spring.main.allow-circular-references: true` 和 `allow-bean-definition-overriding: true` 必须开启
- 微服务模式下启动前需先启动 Nacos 和数据库等基础设施；单体模式下只需启动数据库和 Redis
- 局部切换（如本地微服务调试单个模块）可在 `application-local.yaml` 中单独开启该模块的 Nacos 发现
- AI 模块的 Qdrant/Milvus 向量库 AutoConfiguration 在本地单体模式也需排除（已在 application-local.yaml 中预设）
