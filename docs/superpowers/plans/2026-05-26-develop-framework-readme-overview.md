# Develop Framework README Overview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rewrite `develop-framework/README.md` into a professional top-level framework overview with clear architecture, responsibility boundaries, Mermaid diagrams, and build/maintenance guidance.

**Architecture:** This is documentation-only work. The README should summarize the `develop-framework` Maven aggregator and its child Spring Boot starters from existing POM/README facts, while avoiding implementation claims not verified from the repository. Diagrams should be embedded as Mermaid blocks so the documentation stays maintainable in Markdown.

**Tech Stack:** Markdown, Mermaid, Maven multi-module Java 17 / Spring Boot starter repository.

---

## File Structure

- Modify: `develop-framework/README.md`
  - Owns the top-level overview for the framework aggregator.
  - Must not duplicate every child starter README in detail.
  - Must include architecture diagram, flowchart, mind map, and sequence diagrams using Mermaid.
- Read-only facts to consult during execution:
  - `develop-framework/pom.xml`
  - `develop-framework/*/README.md`
  - `develop-framework/*/pom.xml`
  - root `README.md`
  - root `CLAUDE.md`

## Verified Source Facts

- `develop-framework` is a Maven `pom` aggregator.
- Child modules currently declared in `develop-framework/pom.xml`:
  - `develop-common`
  - `develop-spring-boot-starter-env`
  - `develop-spring-boot-starter-mybatis`
  - `develop-spring-boot-starter-redis`
  - `develop-spring-boot-starter-web`
  - `develop-spring-boot-starter-security`
  - `develop-spring-boot-starter-websocket`
  - `develop-spring-boot-starter-monitor`
  - `develop-spring-boot-starter-protection`
  - `develop-spring-boot-starter-job`
  - `develop-spring-boot-starter-mq`
  - `develop-spring-boot-starter-rpc`
  - `develop-spring-boot-starter-excel`
  - `develop-spring-boot-starter-test`
  - `develop-spring-boot-starter-biz-tenant`
  - `develop-spring-boot-starter-biz-data-permission`
  - `develop-spring-boot-starter-biz-ip`
- Existing top-level description states each technical component conceptually has:
  - `core` package for core encapsulation
  - `config` package for Spring configuration
- Child README/POM summary:
  - `develop-common`: base POJOs, enums, utility classes.
  - `develop-spring-boot-starter-env`: development/feature environment extension with Nacos discovery/config-related support.
  - `develop-spring-boot-starter-mybatis`: database connection pool, dynamic datasource, transaction, MyBatis extensions.
  - `develop-spring-boot-starter-redis`: Redis extension based on Spring cache and Redisson.
  - `develop-spring-boot-starter-web`: REST/web support, global exception handling, API logging, masking, error codes, OpenAPI/Knife4j.
  - `develop-spring-boot-starter-security`: authentication, authorization, and operation log support.
  - `develop-spring-boot-starter-websocket`: WebSocket framework with multi-node broadcast support.
  - `develop-spring-boot-starter-monitor`: tracing, log service, metrics, Spring Boot Admin client.
  - `develop-spring-boot-starter-protection`: distributed lock, idempotency, rate limiting, circuit breaking.
  - `develop-spring-boot-starter-job`: XXL-Job extension.
  - `develop-spring-boot-starter-mq`: MQ abstraction for Redis, RocketMQ, RabbitMQ, Kafka.
  - `develop-spring-boot-starter-rpc`: OpenFeign REST API calls, load balancing, Feign/OkHttp.
  - `develop-spring-boot-starter-excel`: Excel import/export and area conversion support.
  - `develop-spring-boot-starter-test`: test support with H2, Redis mock, POJO generation, ArchUnit.
  - `develop-spring-boot-starter-biz-tenant`: multi-tenancy support.
  - `develop-spring-boot-starter-biz-data-permission`: data permission support.
  - `develop-spring-boot-starter-biz-ip`: IP-to-city and city-code lookup based on ip2region and administrative division data.

---

### Task 1: Replace Top-Level README With Professional Overview

**Files:**
- Modify: `develop-framework/README.md`

- [ ] **Step 1: Re-read existing README and POM before editing**

Run/read:
```text
Read develop-framework/README.md
Read develop-framework/pom.xml
```
Expected: existing README has the current simple module positioning, submodule table, build commands, and maintenance suggestions.

- [ ] **Step 2: Replace README content with this structure**

Use the following section order exactly:

```markdown
# develop-framework

## 模块定位
## 阅读导航
## 设计目标
## 总体架构图
## Starter 能力分层图
## 框架能力思维导图
## 子模块职责矩阵
## 业务模块接入流程
## Spring Boot 自动配置时序
## 典型 Web 请求链路
## 依赖与边界规则
## 构建与验证
## 维护建议
```

- [ ] **Step 3: Write module positioning section**

Include these points in polished Chinese prose:

```markdown
`develop-framework` 是 Smart Cloud 的通用框架与 Spring Boot Starter 聚合层，负责把 Web、安全、数据访问、缓存、消息、RPC、任务、监控、租户、数据权限、Excel、测试等横向能力沉淀为可复用组件。

它不承载具体业务用例，也不反向依赖业务模块；业务模块通过 Maven 依赖按需引入 Starter，并在 Spring Boot 自动配置、拦截器、切面、工具类和基础服务的帮助下获得统一技术能力。
```

- [ ] **Step 4: Add Mermaid overall architecture diagram**

Add this diagram under `## 总体架构图`:

````markdown
```mermaid
flowchart TB
    subgraph Runtime[运行入口]
        Server[develop-server\n模块化单体启动容器]
        Gateway[develop-gateway\nAPI 网关]
    end

    subgraph Business[业务模块]
        System[develop-module-system]
        Infra[develop-module-infra]
        Optional[member / bpm / pay / mall / crm / erp / iot / mes / wms / ai]
    end

    subgraph Framework[develop-framework]
        Common[develop-common\n公共基础]
        Tech[技术 Starter\nweb / security / mybatis / redis / mq / rpc / job / monitor / protection]
        Biz[业务通用 Starter\nbiz-tenant / biz-data-permission / biz-ip]
        Tool[工具与支撑 Starter\nexcel / websocket / test / env]
    end

    subgraph Dependencies[develop-dependencies]
        Bom[BOM 版本治理\nSpring Boot / Spring Cloud / MyBatis / Redis / Flowable / 工具库]
    end

    Gateway --> Framework
    Server --> Business
    Business --> Framework
    Framework --> Bom
    Tech --> Common
    Biz --> Common
    Tool --> Common
```
````

- [ ] **Step 5: Add Starter capability layering diagram**

Add this diagram under `## Starter 能力分层图`:

````markdown
```mermaid
flowchart LR
    Common[develop-common\n基础对象 / 枚举 / 工具类]

    subgraph WebLayer[接入与接口层]
        Web[web\nREST / 异常 / 日志 / OpenAPI]
        Security[security\n认证 / 授权 / 操作日志]
        WebSocket[websocket\n连接 / 会话 / 多节点广播]
        Rpc[rpc\nOpenFeign / 负载均衡 / OkHttp]
    end

    subgraph DataLayer[数据与状态层]
        MyBatis[mybatis\n连接池 / 多数据源 / 事务 / ORM 扩展]
        Redis[redis\n缓存 / Redisson]
        MQ[mq\nRedis / RocketMQ / RabbitMQ / Kafka]
    end

    subgraph Governance[治理与可靠性]
        Monitor[monitor\n链路追踪 / 日志 / 指标 / Admin Client]
        Protection[protection\n锁 / 幂等 / 限流 / 熔断]
        Job[job\nXXL-Job]
    end

    subgraph BizSupport[业务通用能力]
        Tenant[biz-tenant\n多租户]
        DataPermission[biz-data-permission\n数据权限]
        BizIp[biz-ip\nIP 归属地 / 城市编码]
    end

    subgraph Tools[工具与测试]
        Excel[excel\n导入导出 / 地区转换]
        Test[test\nH2 / Redis Mock / ArchUnit]
        Env[env\n环境扩展]
    end

    Common --> WebLayer
    Common --> DataLayer
    Common --> Governance
    Common --> BizSupport
    Common --> Tools
    WebLayer --> BusinessModule[业务模块 server]
    DataLayer --> BusinessModule
    Governance --> BusinessModule
    BizSupport --> BusinessModule
    Tools --> BusinessModule
```
````

- [ ] **Step 6: Add Mermaid mind map**

Add this diagram under `## 框架能力思维导图`:

````markdown
```mermaid
mindmap
  root((develop-framework))
    基础层
      develop-common
        基础 POJO
        枚举
        工具类
    接入层
      web
        REST
        全局异常
        API 日志
        OpenAPI / Knife4j
      security
        认证
        授权
        操作日志
      rpc
        OpenFeign
        负载均衡
      websocket
        会话管理
        多节点广播
    数据层
      mybatis
        多数据源
        事务
        ORM 扩展
      redis
        缓存
        Redisson
      mq
        Redis MQ
        RocketMQ
        RabbitMQ
        Kafka
    治理层
      monitor
        链路追踪
        指标
        Spring Boot Admin
      protection
        分布式锁
        幂等
        限流
        熔断
      job
        XXL-Job
    业务通用能力
      biz-tenant
        多租户
      biz-data-permission
        数据权限
      biz-ip
        IP 归属地
        城市编码
    工具与测试
      excel
        导入导出
        地区转换
      test
        H2
        Redis Mock
        ArchUnit
      env
        环境扩展
```
````

- [ ] **Step 7: Add child module responsibility matrix**

Create a Markdown table with these columns:

```markdown
| 子模块 | 类型 | 核心职责 | 典型使用方 |
|---|---|---|---|
```

Include all 17 child modules from the verified source facts. Keep each responsibility one concise sentence. Do not claim configuration keys or APIs unless visible in child docs.

- [ ] **Step 8: Add business module onboarding flowchart**

Add this diagram under `## 业务模块接入流程`:

````markdown
```mermaid
flowchart TD
    Start[业务模块需要横向能力] --> Pick[选择对应 Starter]
    Pick --> Pom[在业务模块 pom.xml 引入依赖]
    Pom --> Config[按 profile 补充 application 配置]
    Config --> AutoConfig[Spring Boot 自动配置装配 Bean]
    AutoConfig --> Use[Controller / ApplicationService / Infrastructure 使用能力]
    Use --> Verify[运行模块编译或测试验证]

    Pick --> Boundary{是否涉及业务规则?}
    Boundary -- 是 --> Domain[业务规则留在业务模块 domain/application]
    Boundary -- 否 --> Framework[通用技术能力留在 develop-framework]
    Domain --> Pom
    Framework --> Pom
```
````

- [ ] **Step 9: Add Spring Boot auto-configuration sequence diagram**

Add this diagram under `## Spring Boot 自动配置时序`:

````markdown
```mermaid
sequenceDiagram
    participant App as 业务模块 Spring Boot 应用
    participant Maven as Maven 依赖
    participant Starter as Framework Starter
    participant AutoConfig as 自动配置 / framework 包
    participant Bean as Spring Bean
    participant UseCase as 业务代码

    App->>Maven: 引入 starter 依赖
    Maven->>Starter: 解析传递依赖与版本
    App->>AutoConfig: 启动时扫描自动配置
    AutoConfig->>Bean: 注册配置属性、拦截器、切面、客户端或工具 Bean
    UseCase->>Bean: 注入并使用横向能力
    Bean-->>UseCase: 返回统一封装后的技术能力
```
````

- [ ] **Step 10: Add typical Web request sequence diagram**

Add this diagram under `## 典型 Web 请求链路`:

````markdown
```mermaid
sequenceDiagram
    autonumber
    participant Client as 前端 / 调用方
    participant Web as web Starter\n过滤器 / 异常 / 日志
    participant Security as security Starter\n认证 / 授权
    participant Tenant as biz-tenant\n租户上下文
    participant Controller as 业务 Controller
    participant App as ApplicationService / Service
    participant Data as mybatis / redis / mq 等 Starter
    participant Monitor as monitor Starter

    Client->>Web: HTTP 请求
    Web->>Security: 进入安全链路
    Security->>Tenant: 解析登录用户与租户上下文
    Tenant->>Controller: 传递请求上下文
    Controller->>App: 调用业务用例
    App->>Data: 访问数据库、缓存或消息能力
    Data-->>App: 返回技术能力结果
    App-->>Controller: 返回业务结果
    Controller-->>Web: 返回统一响应
    Web->>Monitor: 记录日志、链路与指标
    Web-->>Client: HTTP 响应
```
````

- [ ] **Step 11: Add dependency and boundary rules**

Include these rules:

```markdown
- `develop-framework` 输出横向技术能力，不承载具体业务用例。
- Starter 可以依赖 `develop-common` 和必要的第三方库，版本由 `develop-dependencies` 统一治理。
- 业务模块可以依赖 Starter；Starter 不应反向依赖具体业务模块。
- 多租户、数据权限、IP 归属地等业务通用能力放在 `biz-*` Starter，避免散落到各业务模块重复实现。
- 业务规则、领域不变量和用例编排仍属于业务模块的 `domain` / `application` / `infrastructure`，不应下沉到 framework。
- 修改 Starter 时必须关注自动配置条件、默认 Bean、拦截器/切面顺序和对调用方的兼容影响。
```

- [ ] **Step 12: Add build and verification commands**

Use these commands:

```bash
# 编译 framework 聚合模块及其依赖
mvn compile -pl develop-framework -am

# 打包 framework 聚合模块及其依赖，跳过测试
mvn clean package -pl develop-framework -am -Dmaven.test.skip=true

# 修改某个 Starter 后，优先编译该 Starter 及依赖
mvn compile -pl develop-framework/develop-spring-boot-starter-web -am
```

- [ ] **Step 13: Save README**

Use `Write` for a complete rewrite or `Edit` if preserving parts is simpler. The final README must be in Chinese and should not reference this plan file.

- [ ] **Step 14: Verify Markdown content**

Run:
```bash
git diff --check -- develop-framework/README.md
```
Expected: no output and exit code 0.

- [ ] **Step 15: Review final diff**

Run:
```bash
git diff -- develop-framework/README.md
```
Expected: one documentation-only diff that rewrites the README and contains no application code changes.

- [ ] **Step 16: Optional compile check if user requests it**

This is a documentation-only change, so Maven compile is not required by default. If the user asks for build verification anyway, run:

```bash
mvn compile -pl develop-framework -am
```
Expected: Maven exits 0.

---

## Self-Review Checklist

- Spec coverage: The plan covers professional README structure, architecture diagram, flowchart, mind map, sequence diagrams, module matrix, boundaries, and verification.
- Placeholder scan: No TBD/TODO placeholders are present.
- Scope check: Only `develop-framework/README.md` is modified; no application code is touched.
- Fact discipline: Module responsibilities come from existing README/POM summaries and must be rechecked before writing.
