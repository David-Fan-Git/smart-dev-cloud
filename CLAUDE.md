# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Summary

David develop-cloud (Smart Cloud) — a Spring Cloud Alibaba microservices rapid-development platform. This checkout contains the complete business module set, while the default Maven reactor and boot container only enable the lightweight system/infra subset. Derived from RuoYi-Vue-Pro.

**Tech stack:** Java 17, Spring Boot 3.5.x, Spring Cloud 2025.0.1, Spring Cloud Alibaba, Maven, MyBatis Plus, Redis/Redisson, Lombok + MapStruct. Exact dependency versions are centralized in `develop-dependencies/pom.xml`.

Dependency versions are centralized in `develop-dependencies/pom.xml`; the root `pom.xml` controls the default Maven reactor and compiler/plugin configuration. Prefer POM files over README badges/version prose when versions differ. There is no Maven wrapper in this checkout, so use a local `mvn` with Java 17.

## Build & Run

```bash
# Full build (skip tests)
mvn clean package -Dmaven.test.skip=true

# Full compile without packaging
mvn compile

# Compile a module and its dependencies
mvn compile -pl develop-module-system/develop-module-system-server -am

# Compile the boot server and required modules
mvn compile -pl develop-server -am

# Package the boot server and required modules
mvn clean package -pl develop-server -am -Dmaven.test.skip=true

# Package the gateway and required modules
mvn clean package -pl develop-gateway -am -Dmaven.test.skip=true

# Run tests for a single module
mvn test -pl develop-module-system/develop-module-system-server

# Run a single test class or method in a module
mvn test -pl develop-module-system/develop-module-system-server -Dtest=AdminUserServiceImplTest
mvn test -pl develop-module-system/develop-module-system-server -Dtest=AdminUserServiceImplTest#testCreateUser_success

# Run the server from the reactor
mvn spring-boot:run -pl develop-server -am

# Run the gateway from the reactor
mvn spring-boot:run -pl develop-gateway -am
```

There is no repository-wide lint command in this checkout; use Maven compile/tests for Java validation.

Configuration files: `develop-server/src/main/resources/application.yaml` (main) and per-profile variants (`application-local.yaml`, `application-dev.yaml`). `develop-gateway/src/main/resources/` has separate gateway configuration with the same profile pattern.

The project uses `maven-surefire-plugin` 3.x with JUnit 5. Module tests use `src/test/resources/application-unit-test.yaml` and related SQL fixtures. There is a `develop-spring-boot-starter-test` in the framework that provides test base classes and utilities — extend those for new tests.

The `develop-ui/` directory contains frontend checkouts, but no frontend `package.json` files were present during this initialization; do not assume a package manager or frontend command without checking the specific frontend project first.

No Cursor/Copilot instruction files were found during this initialization (`.cursorrules`, `.cursor/rules/*`, `.github/copilot-instructions.md`).

## Module Architecture

```
develop-dependencies/          # BOM — all dependency versions (single pom)
develop-framework/             # Shared starters (web, security, mybatis, redis, mq, rpc, job, excel, test…)
develop-gateway/               # Spring Cloud Gateway (standalone boot app)
develop-server/                # Monolithic boot app — aggregates develop-module-*-server as Maven dependencies
develop-module-{name}/         # Business modules, each split into api/ and server/ sub-modules
develop-ui/                    # Frontend projects (Vue3-element-plus, Vue3-vben, Vue2, uni-app)
sql/                           # Database init scripts for MySQL, Oracle, PostgreSQL, SQL Server, DM, Kingbase, OpenGauss
```

Business modules: system, infra, member, bpm, pay, report, mp, mall, crm, erp, iot, mes, wms, ai. Most business modules have `api/` and `server/` Maven submodules; the API module exposes inter-module contracts, while the server module contains controllers, services/domain code, DAL, jobs, MQ, and configuration.

The root `pom.xml` enables only the core backend reactor subset by default; optional business modules are present in the checkout but commented out in the reactor unless explicitly enabled. The boot application runs only the modules declared as dependencies of `develop-server/pom.xml`. `develop-server` is intentionally an empty container: it exposes REST APIs by depending on selected `develop-module-*-server` artifacts.

The platform runs in one of two modes controlled by which module dependencies are uncommented in `develop-server/pom.xml`:
- **Minimal:** only `develop-module-system-server` + `develop-module-infra-server` (fast compile)
- **Full:** uncomment additional modules (member, bpm, pay, report, mall, crm, erp, etc.)

Java package base: `com.develop.mvp.pk`

### Larger modules with sub-domains

`develop-module-mall` contains multiple sub-domains, each with their own api/server pair: product, promotion, trade, statistics. These appear as separate Maven modules (`develop-module-product`, `develop-module-promotion`, `develop-module-trade`, `develop-module-statistics`) under the mall directory.

## DDD Architecture (current, in-progress)

The project is undergoing a repository-wide DDD refactoring. The required end state is that all old three-layer business code is migrated to DDD domain architecture; `service` and `dal` are migration sources, not acceptable final homes for core business logic. The new layer layout under `com.develop.mvp.pk.module.{name}/`:

```
domain/{aggregate}/        # Aggregate root, value objects, repository interface, domain events, factory, domain services
  ├── AggregateRoot.java   # Pure Java class — no Spring/MyBatis annotations, constructor-injected value objects
  ├── repository/          # Repository interface (defined in domain, no infrastructure imports)
  ├── valueobject/         # Value objects (TenantId, TenantName, TenantStatus…)
  ├── event/               # Domain events
  └── service/             # Domain services (e.g., uniqueness checkers)

application/{aggregate}/   # Application services — use case orchestration, calls repositories via interfaces
infrastructure/{aggregate}/ # Repository implementations (MyBatis), external adapters — depends on domain + dal
convert/                   # Object mapping (domain ↔ DO ↔ DTO), uses MapStruct
```

**DDD skills** for existing aggregates live in `.claude/ddd-skills/`. Before modifying a domain aggregate or DDD skill, read both `.claude/ddd-skills/DDD_Skill_Production_Readiness_Standard.md` and `.claude/ddd-skills/Module_Structure_Standard.md`; skills that do not meet the production-readiness standard are drafts and must not be used for production refactoring without upgrading first. During DDD refactoring, do not ask the user clarifying questions or ask them to choose the execution path; use the existing skills and standards to decide independently, then report results after the full DDD migration work is complete. Before modifying a domain aggregate, read its skill document first. When creating a new aggregate, use this process before writing code:
1. Identify the domain intent, business responsibility, data boundary, and external dependencies.
2. Create `.claude/ddd-skills/AggregateRoot_<Name>_Skill.md` with skill name, applicable scenarios, DDD building blocks, responsibility boundaries, dependencies/collaboration, invariants/constraints, rollback conditions, and acceptance criteria.
3. Verify the skill against current code behavior, boundaries, dependencies, and invariants; revise the skill if anything does not match.
4. Refactor only that aggregate/context according to the verified skill.
5. Compile/test and check each acceptance criterion; if validation fails, revisit the analysis instead of broadening scope.

### Module structure standard

Before changing module structure, API contracts, runtime units, or DDD layer placement, read `.claude/ddd-skills/Module_Structure_Standard.md`.

The repository standard is:
- Maven structure is organized by runtime units. `server` and `gateway` may both exist when they have independent runtime responsibilities, but every runtime unit must follow the same internal structure rules.
- API modules use one stable contract with `local/remote` adapters for local module calls and remote Feign calls.
- `controller/job/mq/framework` may remain as entry and technical configuration layers. Core business logic belongs in `domain/application/infrastructure/convert`.
- `service/dal` are migration sources, not final homes for core business logic.
- Module and aggregate code must preserve high cohesion, low coupling, clear responsibility boundaries, single responsibility, necessary functional comments, maintainability, and extensibility.
- Use Java design patterns only for clear variation points or dependency isolation; do not add abstractions just to use a pattern.
- `iot` and `mall` are not permanent exceptions. They must converge to the same runtime-unit, API-contract, and DDD-layer standards.

### Existing layers (coexisting)

Some code still follows the pre-DDD three-layer pattern:

```
controller/    # REST endpoints (admin/app sub-directories for multi-terminal)
service/       # Business logic interfaces and implementations
dal/           # MyBatis Plus mapper interfaces + data objects (mysql/ and redis/ sub-directories)
framework/     # Module-local Spring configuration (auto-configurations, interceptors, etc.)
api/           # Internal API contracts for inter-module calls
mq/            # Message producers/consumers
job/           # XXL-Job scheduled tasks
```

## Framework Starters (develop-framework)

Key starters and their responsibilities:

| Starter | Purpose |
|---|---|
| `develop-spring-boot-starter-web` | REST API defaults, global exception handling, Jackson config |
| `develop-spring-boot-starter-security` | Spring Security authn/authz + operation logging |
| `develop-spring-boot-starter-mybatis` | MyBatis Plus, dynamic datasource, easy-trans |
| `develop-spring-boot-starter-redis` | Redis caching, Redisson distributed locks |
| `develop-spring-boot-starter-mq` | Message queue abstraction (Redis/RabbitMQ/Kafka/RocketMQ) |
| `develop-spring-boot-starter-rpc` | Feign-based inter-service RPC |
| `develop-spring-boot-starter-job` | XXL-Job integration |
| `develop-spring-boot-starter-biz-tenant` | Multi-tenant (SaaS) support |
| `develop-spring-boot-starter-biz-data-permission` | Row-level data permission filtering |
| `develop-spring-boot-starter-excel` | Excel import/export |
| `develop-spring-boot-starter-protection` | Sentinel rate limiting and circuit breaking |
| `develop-spring-boot-starter-monitor` | SkyWalking tracing and metrics |
| `develop-spring-boot-starter-websocket` | WebSocket support |
| `develop-spring-boot-starter-test` | Test base classes (extend for unit/integration tests) |

## API and Integration Patterns

- Business module API submodules expose internal contracts for inter-module calls. Server modules implement the business logic and can be aggregated into `develop-server`.
- `develop-spring-boot-starter-rpc` provides Feign-based RPC support; `develop-server` excludes OpenFeign from this starter so the monolithic boot mode can start without remote calls.
- Web/API documentation dependencies are managed through Springdoc OpenAPI 3 and Knife4j in `develop-dependencies/pom.xml`.

## Key Infrastructure Services

- **Nacos** — service registration + configuration center
- **Spring Cloud Gateway** (`develop-gateway`) — API gateway with token auth filter, gray routing, CORS, access logging
- **Sentinel** — rate limiting and circuit breaking (`develop-spring-boot-starter-protection`)
- **XXL-Job** — distributed scheduled tasks (`develop-spring-boot-starter-job`)
- **Flowable** — workflow engine (used in `develop-module-bpm`)
- **Spring Security + Token + Redis** — authentication, supports multi-terminal and SSO

## Configuration Conventions

- `application.yaml` — main config with Spring profile placeholders
- `application-local.yaml` — local development (use this for daily work)
- `application-dev.yaml` — dev environment
- `lombok.config` — project-level Lombok settings (chain accessors, callSuper on toString/equalsHashCode)
- `${develop.info.base-package}` property is set to `com.develop.mvp.pk` and used in `@SpringBootApplication` scanBasePackages

## Database

Multiple database dialects supported via DDL scripts in `sql/`: MySQL, Oracle, PostgreSQL, SQL Server, MariaDB, DM (达梦), Kingbase (人大金仓), OpenGauss. The project uses MyBatis Plus dynamic-datasource for multi-database routing.
