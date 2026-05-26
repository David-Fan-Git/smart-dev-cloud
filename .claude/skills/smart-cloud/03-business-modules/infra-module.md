---
name: infra-module
description: Infrastructure module — code generator, file storage (local/S3/DB/FTP/SFTP), API access/error logging, config center, Redis monitor, demo CRUD examples
type: project
---

# develop-module-infra

## 概述

基础设施模块。提供平台运行所必需的基础支撑功能：代码生成器、文件存储、API 日志、配置中心、Redis 监控、数据源配置、Demo CRUD 示例。

- **包路径**: `com.develop.mvp.pk.module.infra`
- **服务名**: `infra-server`
- **错误码区间**: [1-001-000-000 ~ 1-002-000-000)
- **数据库表前缀**: `infra_`

## 核心功能与 Controller 清单

### 1. 代码生成器 (Codegen)
| Controller | 路由 | 方法 |
|---|---|---|
| `CodegenController` | `/infra/codegen` | `/create`、`/update`、`/delete`、`/page`、`/get`、`/import`(导入表)、`/preview`(预览)、`/download`(生成下载) |

基于表结构反向生成全栈代码。支持单表、树表、主子表。VO 支持三种模式：普通 VO、Excel VO、Tree VO。

### 2. 文件存储
| Controller | 路由 | 方法 |
|---|---|---|
| `FileController` | `/infra/file` | `/create`(上传)、`/delete`、`/page`、`/get` |
| `FileConfigController` | `/infra/file-config` | 存储配置管理 CRUD |
| `AppFileController` | `/infra/file`(app) | 移动端文件上传 |

**存储客户端工厂** (`FileClientFactory`)：
- `LocalFileClient` — 本地磁盘
- `DBFileClient` — 数据库 BLOB
- `S3FileClient` — S3 协议（MinIO、阿里云 OSS、腾讯云 COS、华为云 OBS）
- `FtpFileClient` — FTP
- `SftpFileClient` — SFTP

### 3. API 日志
| Controller | 路由 | 方法 |
|---|---|---|
| `ApiAccessLogController` | `/infra/api-access-log` | `/page`、`/get` — 请求日志 |
| `ApiErrorLogController` | `/infra/api-error-log` | `/page`、`/get`、`/update-status`(处理) — 错误日志 |

日志由 `develop-spring-boot-starter-web` 的过滤器采集，异步写入。

### 4. 配置中心
| Controller | 路由 | 方法 |
|---|---|---|
| `ConfigController` | `/infra/config` | CRUD + `/page`、`/get-config-value`(根据 key 获取) |

支持配置分组（系统配置、模块配置）。

### 5. 数据库源 + 文档
| Controller | 路由 | 方法 |
|---|---|---|
| `DataSourceConfigController` | `/infra/data-source-config` | 数据源配置 CRUD + `/page` |

### 6. Redis 监控
| Controller | 路由 | 方法 |
|---|---|---|
| `RedisController` | `/infra/redis` | `/get-monitor-info`(监控数据) |

### 7. Demo CRUD 示例
| Controller | 模式 | 说明 |
|---|---|---|
| `Demo01ContactController` | 单表 | 简单 CRUD 示例 |
| `Demo02CategoryController` | 树表 | 树形 CRUD |
| `Demo03StudentNormalController` | 主子表 | 标准风格 |
| `Demo03StudentErpController` | 主子表 | ERP 风格 |
| `Demo03StudentInnerController` | 主子表 | 内联风格 |

## 数据库表

| 表 | 说明 |
|---|---|
| `infra_codegen_table` | 代码生成表定义 |
| `infra_codegen_column` | 代码生成字段定义 |
| `infra_file` | 文件记录 |
| `infra_file_config` | 文件存储配置 |
| `infra_config` | 配置项 |
| `infra_api_access_log` | API 访问日志 |
| `infra_api_error_log` | API 错误日志 |
| `infra_data_source_config` | 数据源配置 |
| `infra_demo01_contact` | 示例-联系人(单表) |
| `infra_demo02_category` | 示例-分类(树表) |
| `infra_demo03_student` | 示例-学生(主表) |
| `infra_demo03_contact` | 示例-学生联系人(子表) |

## 关键 Classes

### Controllers
| Controller | 路由 | 说明 |
|---|---|---|
| `CodegenController` | `/infra/codegen` | 代码生成器 |
| `FileController` | `/infra/file` | 文件管理 |
| `FileConfigController` | `/infra/file-config` | 文件配置 |
| `AppFileController` | `/infra/file`(app) | 移动端文件 |
| `ConfigController` | `/infra/config` | 配置管理 |
| `ApiAccessLogController` | `/infra/api-access-log` | API 访问日志 |
| `ApiErrorLogController` | `/infra/api-error-log` | API 错误日志 |
| `DataSourceConfigController` | `/infra/data-source-config` | 数据源配置 |
| `RedisController` | `/infra/redis` | Redis 监控 |

### 关键 Services
- `FileService` / `FileServiceImpl` — 文件存储业务
- `FileConfigService` / `FileConfigServiceImpl` — 文件配置管理
- `CodegenService` / `CodegenServiceImpl` — 代码生成业务
- `CodegenBuilder` — 代码生成器构造器
- `ApiAccessLogService` — API 访问日志
- `ApiErrorLogService` — API 错误日志
- `FileClientFactory` — 文件客户端工厂
- `ConfigService` / `ConfigServiceImpl` — 配置项管理

### 关键 Entities
- `CodegenTableDO extends BaseDO` — 代码生成表
- `CodegenColumnDO extends BaseDO` — 代码生成字段
- `FileDO extends BaseDO` — 文件记录
- `FileConfigDO extends BaseDO` — 文件配置
- `ConfigDO extends BaseDO` — 配置项
- `ApiAccessLogDO extends BaseDO` — API 访问日志
- `ApiErrorLogDO extends BaseDO` — API 错误日志
- `DataSourceConfigDO extends BaseDO` — 数据源配置

## Feign API 清单

| API | 方法 | 用途 |
|---|---|---|
| `FileApi` | `createFile`, `getFile`, `getFileList` | 文件读写 |
| `ConfigApi` | `getConfig`, `getConfigMap` | 配置查询 |
| `WebSocketSenderApi` | `send`, `sendObject` | WebSocket 跨模块推送 |

## 依赖的 Starter

- `develop-spring-boot-starter-web`
- `develop-spring-boot-starter-security`
- `develop-spring-boot-starter-mybatis`
- `develop-spring-boot-starter-redis`
- `develop-spring-boot-starter-excel` (代码生成导出)

## 关键点

- 所有 Entity 继承 `BaseDO`（非 `TenantBaseDO`）— 基础设施表不隔离租户
- 代码生成器支持多数据库（MySQL、Oracle、PostgreSQL、SQL Server）
- 文件客户端通过 `FileClientFactory` 工厂模式创建，配置可动态切换
- `infra-server` 服务名 = `"infra-server"`（`RpcConstants.INFRA_NAME`）
- 错误码段 [1-001-000-000 ~ 1-002-000-000)
