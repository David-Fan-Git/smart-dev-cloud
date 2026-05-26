---
name: CodegenService
description: Code generation service that reads DB metadata and generates Java/Vue3 code via templates
type: project
---

# CodegenService

## 功能定位

CodegenServiceImpl 是代码生成器的核心 Service，位于 `develop-module-infra` 的 `service.codegen` 包下。它通过读取数据库表的元信息（表结构、字段、注释），自动生成完整的 CRUD 代码。

核心职责：
- **表导入**：从数据库读取表元信息，生成 CodegenTableDO + CodegenColumnDO 配置
- **代码生成**：使用 Velocity 模板引擎生成 Controller/Service/Mapper/Entity/Convert/VO/SQL/前端页面
- **主子表支持**：主表生成代码时自动包含子表（一对多关系）的完整 CRUD
- **表同步**：数据库表结构变更后，同步更新已有配置（只增删，不覆盖手动配置）
- **多数据源**：支持从不同数据源导入表

生成的内容覆盖：
- Java 后端：Controller, Service, ServiceImpl, Mapper, DO, Convert, VO (XReqVO, XRespVO, XPageReqVO)
- 前端 Vue3：API, Views (index.vue, form.vue)
- SQL：菜单权限 SQL
- 支持场景：单表、树表、主子表

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Template Method** | 代码生成遵循固定步骤 | 读取元数据 -> 构建表/列定义 -> 执行模板引擎 |
| **Builder** | 将数据库原始元数据转换为业务对象 | `CodegenBuilder.buildTable()`, `CodegenBuilder.buildColumns()` |
| **Strategy** | 根据场景选择不同模板 | `CodegenEngine` 区分管理后台/App/主子表模板 |
| **Memento** | 表/列定义持久化到 DB | CodegenTableDO/CodegenColumnDO 存储在 MySQL |
| **Diff Algorithm** | 同步时计算差异 | `syncCodegen0()` 中计算新增/修改/删除的字段 |

## 核心逻辑流程

### 创建代码生成配置 (createCodegenList)

```
createCodegenList(author, reqVO)
  |
  +-- 遍历 reqVO 中的表名列表
       |
       +-- 对每张表: createCodegen(author, dataSourceConfigId, tableName)
            |
            +-- 1. databaseTableService.getTable(dataSourceConfigId, tableName)
            |    通过 JDBC 读取表的元信息 (TableInfo)
            |    包含: 表名, 注释, 所有字段(名称,类型,注释,主键,可空等)
            |
            +-- 2. createCodegen0(author, dataSourceConfigId, tableInfo)
                 |
                 +-- 2.1 validateTableInfo(tableInfo)
                 |    校验: 表非空, 有注释, 字段非空, 每个字段有注释
                 |
                 +-- 2.2 校验: 该表是否已导入
                 |    codegenTableMapper.selectByTableNameAndDataSourceConfigId() != null
                 |    已存在 -> 抛异常 CODEGEN_TABLE_EXISTS
                 |
                 +-- 2.3 codegenBuilder.buildTable(tableInfo)
                 |    从 TableInfo 构建 CodegenTableDO
                 |    -> 解析表注释(业务名、功能名)
                 |    -> 生成类名、模块名、包路径等
                 |
                 +-- 2.4 设置: scene(默认管理后台), frontType, author
                 |
                 +-- 2.5 codegenTableMapper.insert(table)
                 |
                 +-- 2.6 codegenBuilder.buildColumns(tableId, fields)
                 |    从 TableInfo.Field 构建 List<CodegenColumnDO>
                 |    -> 设置字段的 Java 类型、HTML 类型、查询方式、字典类型等
                 |    -> 如果没有主键，使用第一个字段作为主键
                 |
                 +-- 2.7 codegenColumnMapper.insertBatch(columns)
```

### 同步表结构 (syncCodegenFromDB)

```
syncCodegenFromDB(tableId)
  |
  +-- 查询已有 table 配置
  +-- 重新从数据库读取表元信息
  +-- syncCodegen0(tableId, tableInfo)
       |
       +-- 1. 已有字段列表 codegenColumns (从 MySQL 查询)
       +-- 2. 最新字段列表 tableFields (从 DB 读取)
       |
       +-- 3. 计算差异:
       |    +-- modifyFieldNames: 字段存在但类型/注释/主键/可空/排序有变化
       |    +-- tableFieldNames: 最新的字段名集合
       |    +-- deleteColumnIds: 在表中已删除的字段 ID
       |
       +-- 4. 移除已存在的字段 (保留待新增的)
       |    tableFields.removeIf(column -> codegenColumnNames.contains(...) && !modify)
       |
       +-- 5. 如果无差异: 抛异常 CODEGEN_SYNC_NONE_CHANGE
       |
       +-- 6. codegenBuilder.buildColumns() -> insertBatch 新增字段
       +-- 7. codegenColumnMapper.deleteByIds() 删除不存在的字段
```

**重要的设计决策**: 同步只做新增和删除，**不会覆盖已有字段的手工配置**。也就是说，如果开发者在界面上修改了某个字段的显示类型（如从文本框改为下拉框），同步后这个配置不会丢失。

### 生成代码 (generationCodes)

```
generationCodes(tableId)
  |
  +-- 1. 查询 table + columns
  +-- 2. 主子表加载 (如果 templateType == MASTER):
  |    +-- 查询子表列表 subTables (templateType=SUB, masterTableId=tableId)
  |    +-- 查询子表的 column 列表
  |    +-- 校验子表关联字段存在
  |
  +-- 3. 获取数据源配置，确定数据库类型 (dbType)
  |    JdbcUtils.getDbType(dataSourceConfig.getUrl())
  |
  +-- 4. codegenEngine.execute(dbType, table, columns, subTables, subColumnsList)
       |
       +-- 4.1 遍历所有模板文件
       |    模板类型:
       |    - Java: DO, Mapper, MapperXML, Service, ServiceImpl, Controller, Convert
       |    - VO: XRespVO, XPageReqVO, XSaveReqVO (create/update 合并)
       |    - SQL: 菜单权限 SQL
       |    - Vue: index.vue, form.vue
       |    - API: api/*.ts
       |
       +-- 4.2 Velocity 模板渲染
       |    - 传递 Map<String, Object> 模板变量
       |    - basePackage, className, moduleName, columns 等
       |
       +-- 4.3 返回 Map<String, String>
            key = 文件路径 (如 "java/.../UserController.java")
            value = 生成的代码内容
```

## 关键代码剖析

### 表名解析与驼峰转换

`CodegenBuilder` 中完成了从数据库表名到 Java 类名的自动转换：
```
表名: system_user       -> 模块名: system, 业务名: user,  类名: User
表名: system_user_role  -> 模块名: system, 业务名: user_role, 类名: UserRole
```

### 字段类型映射

`CodegenBuilder.buildColumns` 包含从 JDBC 类型到 Java 类型的映射：
```
JDBC 类型         -> Java 类型
VARCHAR/CHAR      -> String
INTEGER/BIGINT    -> Long
TINYINT           -> Integer
DATE/DATETIME     -> LocalDateTime
DECIMAL           -> BigDecimal
TEXT/LONGTEXT     -> String
```

同时生成对应的 HTML 组件类型：
```
String  -> 文本框 (input)
Integer -> 下拉框/单选框 (select/radio)
Long    -> 下拉框 (select)
LocalDateTime -> 日期选择器 (datetime)
```

### 主子表代码生成

主表的模板会额外生成：
- 子表对应的 List<SubDO> 字段
- 子表 CRUD 的 Mapper 方法
- 子表的 VO 转换
- 前端的子表表格组件

## 调用链

```
[Upstream]
  CodegenController (管理后台 REST)
    -> CodegenServiceImpl (本类)
       |
       +-- DatabaseTableService.getTable()
       |    通过 JDBC 读取数据库元信息 (information_schema / sys.columns)
       |
       +-- DataSourceConfigService.getDataSourceConfig()
       |    获取数据源配置 (URL/用户名/密码)
       |
       +-- CodegenBuilder.buildTable() / buildColumns()
       |    转换元数据为业务对象
       |
       +-- CodegenEngine.execute()
       |    Velocity 模板渲染引擎
       |
       +-- CodegenTableMapper / CodegenColumnMapper
            MySQL 持久化表/列配置
```

## 配置与条件

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `codegen.front-type` | 前端类型 | 跟随框架配置 |
| `codegen.author` | 代码作者 | 可配置 |
| 模板变量 | 所有模板可用的变量 | package, moduleName, businessName, className, columns 等 |

支持的数据源：MySQL, Oracle, PostgreSQL, SQL Server, MariaDB, DM 等（通过 `JdbcUtils.getDbType` 识别）。

## 生产级关注点

### 1. 表结构校验

导入时强制要求：
- 表必须有注释（`tableInfo.getComment()` 不能为空）
- 每个字段必须有注释（`field.getComment()` 不能为空）

这是为了确保生成的代码有完整的注释，避免生成的代码中出现 "未知字段" 这种无意义的注释。

### 2. 同步不覆盖原则

`syncCodegenFromDB` 的设计原则是"增量同步"：
- 新增的字段 -> 插入新的 CodegenColumnDO
- 已删除的字段 -> 删除对应的 CodegenColumnDO
- 已有的字段 -> **不修改**

这意味着开发者在代码生成界面手工调整的配置（字段显示类型、查询方式、字典类型等）不会因为表结构同步而丢失。

### 3. 主子表约束

- 主子表必须先生成子表的代码生成配置
- 子表通过 `masterTableId` 关联主表
- 主表生成代码时，强制要求子表已配置且关联字段存在
- 子表没有独立的代码生成入口

### 4. 性能考虑

- `createCodegen` 对每张表执行一次 `databaseTableService.getTable()`（JDBC 查询表结构）
- 批量创建时遍历执行，效率较低但可接受（代码生成不是高频操作）
- `generationCodes` 涉及较重的 Velocity 模板渲染，单次生成耗时 ~200-500ms

### 5. 安全提示

生成的代码中包含 SQL 文件和菜单权限 SQL，其中包含硬编码的 `menu_id`。这些 ID 在部署时需要根据实际的数据库序列进行调整。

### 6. 模板自定义

模板文件位于 `CodegenEngine` 中，使用：
- Java 模板：基于 Velocity 模板字符串（硬编码在 Java 中）
- 前端模板：Vue 组件基于 Velocity 模板字符串
- 未来可扩展：支持从文件系统加载自定义模板
