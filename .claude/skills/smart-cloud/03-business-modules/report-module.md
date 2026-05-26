---
name: report-module
description: Data visualization and reporting -- JiMuReport 2.3.2 integration, GoView dashboard builder, SQL/HTTP data sources, JmReportTokenServiceI custom auth, report permission integration
type: project
---

# develop-module-report

## 概述

报表模块。集成积木报表 (JiMuReport) 2.3.2 引擎与大屏可视化 GoView，提供数据可视化报表能力。

- **包路径**: `com.develop.mvp.pk.module.report`
- **服务名**: `report-server`
- **错误码区间**: [1-003-000-000 ~ 1-004-000-000)
- **数据库表前缀**: `report_` + 自动建表 `jimu_*` 系列
- **多租户**: 否（Entity 继承 `BaseDO`，报表配置不隔离租户）

## 模块结构

```
develop-module-report/
  pom.xml                                    # 聚合 pom
  develop-module-report-api/                 # Feign 接口、DTO、枚举
  develop-module-report-server/              # 实现层：Controller + Service + DAL
```

## 核心功能

### 1. 积木报表 (JiMuReport)

集成 `jimureport-spring-boot3-starter` 2.3.2，自动注册报表引擎的所有 Controller，无需手动编写。

**数据源配置**: 支持 MySQL、Oracle、PostgreSQL 等多数据源，通过在线设计器配置。

**报表模板**: 拖拽式设计器，支持：
- 表格报表、图表报表、交叉报表
- 参数查询、动态 SQL
- 报表预览、导出（PDF/Excel/Word/图片）

**定时报表**: 定时生成并推送报表。

**平台安全集成**: 通过自定义 `JmReportTokenServiceImpl` 实现积木报表与平台认证体系对接：
- Token 校验：对接平台 OAuth2 Token 认证
- 角色/权限映射：平台管理员自动映射为积木报表 `admin` 角色
- 租户上下文透传：积木报表请求自动继承平台租户信息
- API 数据集 Token 传递：自定义 Header 传递，参考 `customApiHeader()` 实现

```java
// 关键认证流程
// JmReportTokenServiceImpl implements JmReportTokenServiceI
// - verifyToken(token): 校验平台 Token 有效性
// - getUsername(token): 返回用户编号
// - getRoles(token): 管理员返回 ["admin"]，非管理员返回 null
// - getPermissions(token): 管理员返回所有仪表盘权限指令
// - getTenantId(): 返回当前租户编号
// - customApiHeader(): 透传 X-Access-Token 到平台认证头
```

**仪表盘集成**: 通过自定义 `JmOnlDragExternalServiceImpl` 实现积木仪表盘 (JimuBi) 的字典查询、日志记录等扩展点。

### 2. GoView 大屏 (可视化看板)

GoView 是一个基于 Vue3 的大屏可视化设计器，本模块提供后端数据支撑。

### 3. 报表权限

- 报表菜单集成平台权限体系，通过 `@PreAuthorize` 注解控制
- GoView 项目按用户隔离（`getMyProjectPage` 仅返回当前用户创建的项目）
- 积木报表路由 `/jmreport/**`、`/drag/**`、`/jimubi/**` 在 Security 配置中放行（permitAll），认证由 `JmReportTokenServiceImpl` 内部处理

## Controller 清单

### GoView 项目管理

| Controller | 路由 | 方法 |
|---|---|---|
| `GoViewProjectController` | `/report/go-view/project` | `/create`、`/update`、`/delete`、`/get`、`/my-page` |

**权限**: `report:go-view-project:create`, `report:go-view-project:update`, `report:go-view-project:delete`, `report:go-view-project:query`

### GoView 数据查询

| Controller | 路由 | 方法 |
|---|---|---|
| `GoViewDataController` | `/report/go-view/data` | `/get-by-sql`(SQL 查询)、`/get-by-http`(HTTP 示例) |

**权限**: `report:go-view-data:get-by-sql`

### JiMuReport 控制器

积木报表控制器由 `jimureport-spring-boot3-starter` 自动注册，路由前缀包括：
- `/jmreport/**` — 报表设计器及 API
- `/drag/**` — 仪表盘/大屏设计器 API
- `/jimubi/**` — 积木 BI 相关

## 关键类

### Framework 配置

| 类 | 职责 |
|---|---|
| `JmReportConfiguration` | 积木报表配置，`@ComponentScan` 扫描 `org.jeecg.modules.jmreport` 包，注册 Token 校验 Bean 和仪表盘扩展 Bean |
| `JmReportTokenServiceImpl` | 积木报表 Token 校验、用户/角色/权限/租户信息查询，实现 `JmReportTokenServiceI` |
| `JmOnlDragExternalServiceImpl` | 积木仪表盘外部扩展（字典、日志），实现 `IOnlDragExternalService` |
| `SecurityConfiguration` | Report 模块 Security 配置，放行 `/jmreport/**`、`/drag/**`、`/jimubi/**` 路由 |
| `RpcConfiguration` | 开启 Feign 客户端 `@EnableFeignClients` |

### Controllers

| Controller | 路由前缀 | 说明 |
|---|---|---|
| `GoViewProjectController` | `/report/go-view/project` | GoView 大屏项目 CRUD + 我的项目分页 |
| `GoViewDataController` | `/report/go-view/data` | GoView 数据查询（SQL 和 HTTP） |

### 关键 Services

| Service | 职责 |
|---|---|
| `GoViewProjectService` / `GoViewProjectServiceImpl` | GoView 项目管理（CRUD、用户隔离的分页查询） |
| `GoViewDataService` / `GoViewDataServiceImpl` | GoView 数据查询，使用 `JdbcTemplate` 执行 SQL 查询并返回列名+行数据 |

### 关键 Entities

- `GoViewProjectDO extends BaseDO` — GoView 项目，字段：`id`、`name`(项目名称)、`picUrl`(预览图片)、`content`(JSON 配置)、`status`(发布状态 0-已发布/1-未发布)、`remark`(备注)

### 关键 Mapper

- `GoViewProjectMapper extends BaseMapperX<GoViewProjectDO>` — 含 `selectPage(PageParam, Long userId)` 按用户分页查询

## 数据库表

| 表 | 说明 |
|---|---|
| `report_go_view_project` | GoView 大屏项目 |

JiMuReport 自动建表（`jimu_*` 系列）：
| 表 | 说明 |
|---|---|
| `jimu_report` | 报表模板 |
| `jimu_report_data_source` | 报表数据源 |
| `jimu_report_link` | 报表关联 |
| `jimu_report_share` | 报表分享 |
| `jimu_report_screenshot` | 报表截图 |

## 错误码

```java
// ErrorCodeConstants.java (report 系统，使用 1-003-000-000 段)
ErrorCode GO_VIEW_PROJECT_NOT_EXISTS = new ErrorCode(1_003_000_000, "GoView 项目不存在");
```

## 依赖的 Starter

- `develop-spring-boot-starter-web` — REST API
- `develop-spring-boot-starter-security` — 认证授权（放行积木报表路由）
- `develop-spring-boot-starter-mybatis` — 数据访问
- `develop-spring-boot-starter-redis` — 缓存
- `jimureport-spring-boot3-starter` 2.3.2 — 积木报表引擎
- GoView 前端资源（静态资源集成）

## 对外 API

报表模块主要面向管理后台，**无独立的 Feign API 暴露给其他业务模块**。

## 关键点

- 积木报表的路由 `/jmreport/**`、`/drag/**`、`/jimubi/**` 在 Security 配置中全部 `permitAll()`，认证由 `JmReportTokenServiceImpl` 内部完成
- `GoViewProjectServiceImpl` 创建项目时默认状态为 `CommonStatusEnum.DISABLE`（未发布）
- `GoViewDataServiceImpl` 使用 `JdbcTemplate` 执行 SQL，支持任意 SELECT 查询，返回列名 + 行数据
- 报表模块的 Entity 继承 `BaseDO`（非 `TenantBaseDO`），报表配置不隔离租户
- 仪表盘扩展 `JmOnlDragExternalServiceImpl` 当前使用默认实现（`IOnlDragExternalService.super.*`），可按需覆盖字典和日志逻辑
