---
name: code-generation
description: Built-in code generator — database-to-CRUD, template customization, post-generation hooks, tree/master-detail support
type: project
---

# 代码生成模式

## 概述

`develop-module-infra` 提供内置代码生成器（`CodegenController`），基于数据库表结构反向生成完整 CRUD 代码。输入：数据库表 → 输出：Controller、Service、ServiceImpl、DO、Mapper、Convert、VO、DTO、Menu SQL、Vue 页面。

## 功能架构

```
数据库表 → CodegenBuilder → 模板引擎 → 代码文件
   ↓            ↓                ↓
 表结构     类型映射(NV)        Controller.java
 字段信息    命名转换           Service.java
 索引        配置增强           DO.java / Mapper.java
 注释                           VO (SaveReqVO, PageReqVO, RespVO)
                                Convert.java
                                DTO (ReqDTO, RespDTO)
                                Menu SQL
                                前端 Vue3 / uni-app
```

## 使用步骤

### 1. 访问页面
管理后台 → 基础设施 → 代码生成（`/infra/codegen`）

### 2. 导入表
选择数据库 → 选择表 → 确认导入

### 3. 配置生成参数

| 配置项 | 说明 | 示例 |
|---|---|---|
| **作者** | Java `@author` | develop |
| **包名** | 基础包 | `com.develop.mvp.pk` |
| **模块名** | 所属模块 | `system` |
| **业务名** | 业务名称（类名用） | `user` |
| **表前缀** | 自动去除 | `system_` |
| **功能名** | 中文名 | 用户 |
| **上级菜单** | 父菜单 ID | 10 |

### 4. 字段配置

| 配置项 | 说明 |
|---|---|
| **Java 类型** | Long / String / Integer / LocalDateTime 等 |
| **字典类型** | 关联的字典类型 |
| **是否列表** | 在列表中显示 |
| **是否查询** | 作为查询条件 |
| **查询方式** | `=` / `BETWEEN` / `LIKE` |
| **显示类型** | 文本框 / 下拉框 / 日期等 |

### 5. 生成代码结构

```
develop-module-{name}-api/
  api/{Domain}Api.java                    # Feign 接口
  api/{domain}/dto/{Domain}ReqDTO.java    # 请求 DTO
  api/{domain}/dto/{Domain}RespDTO.java   # 响应 DTO

develop-module-{name}-server/
  controller/admin/{domain}/{Domain}Controller.java
  controller/admin/{domain}/vo/{Domain}SaveReqVO.java
  controller/admin/{domain}/vo/{Domain}PageReqVO.java
  controller/admin/{domain}/vo/{Domain}RespVO.java
  convert/{domain}/{Domain}Convert.java
  dal/dataobject/{domain}/{Domain}DO.java
  dal/mysql/{domain}/{Domain}Mapper.java
  service/{domain}/{Domain}Service.java
  service/{domain}/impl/{Domain}ServiceImpl.java

sql/{module}_{entity}.sql                 # 菜单权限 SQL
```

### 6. 生成菜单 SQL

```sql
INSERT INTO system_menu (name, permission, type, sort, parent_id, status)
VALUES ('{领域名}管理', '', 1, 1000, @parentId, 0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, status)
VALUES ('{领域名}查询', '{module}:{domain}:query', 2, 1, @parentId, 0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, status)
VALUES ('{领域名}创建', '{module}:{domain}:create', 2, 2, @parentId, 0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, status)
VALUES ('{领域名}更新', '{module}:{domain}:update', 2, 3, @parentId, 0);

INSERT INTO system_menu (name, permission, type, sort, parent_id, status)
VALUES ('{领域名}删除', '{module}:{domain}:delete', 2, 4, @parentId, 0);
```

## 生成后处理

1. **菜单 SQL 必须导入数据库** — 否则前端菜单不显示，接口返回 403
2. **多租户表检查** — 确认继承 `TenantBaseDO` 还是 `BaseDO`
3. **字典类型配置** — 在 `DictTypeConstants` 中注册字典 key
4. **权限点注册** — 确保菜单 SQL 中的 permission 与 `@PreAuthorize` 一致
5. **`ErrorCodeConstants`** — 手动合并新增的错误码常量（生成器不会自动合并）

## 自定义模板

代码生成器使用模板引擎，模板在代码生成器中定义。如需修改生成逻辑，调整 `CodegenBuilder` 中的模板字符串或 `CodegenServiceImpl` 中的处理逻辑。

## 可选功能

| 功能 | 说明 |
|---|---|
| **树表** | 生成 `parentId` 树形结构 |
| **主子表** | 主表 + 子表关联生成 |
| **Excel 导出** | 添加 `@ExcelProperty` + `/export` 端点 |
| **Excel 导入** | 添加 `/import` + 模板下载 |
| **批量删除** | 生成 `/delete-list` 端点 |

## 关键点

1. **本质是"数据库表 → CRUD 代码"的管道**
2. **导入后必须检查字段配置** — 默认类型映射可能不准确
3. **生成的代码是起点** — 复杂业务需手动修改
4. **菜单 SQL 必须导入** — 否则权限不生效
5. **生成的代码规范与手写一致**
6. **自动识别 `deleted`、`createTime`、`updateTime`** — 映射到 BaseDO

## 常见错误

- 导入表后未配置字段类型直接生成 — Java 类型不匹配
- 生成的 SQL 未导入 — 403 无权限
- 重新生成覆盖了手动修改 — 生成前检查文件
- 表缺少 `create_time`/`deleted` 字段 — BaseDO 必需的字段
- 多租户表错误配置为 `BaseDO` — 应用 `TenantBaseDO`
- `ErrorCodeConstants` 需手动合并 — 生成器不自动合并
