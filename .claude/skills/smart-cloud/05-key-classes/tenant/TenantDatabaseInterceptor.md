---
name: TenantDatabaseInterceptor
description: MyBatis Plus tenant line handler that auto-applies tenant_id filtering to SQL queries
type: project
---

# TenantDatabaseInterceptor

## 功能定位

TenantDatabaseInterceptor 是多租户（SaaS）架构在数据库层的核心实现，位于 `develop-spring-boot-starter-biz-tenant` 的 `core.db` 包下。它基于 MyBatis Plus 的 `TenantLineHandler` 接口，**在 SQL 编译阶段通过 JSQLParser 修改 AST（抽象语法树）**，自动为所有 SQL 查询追加 `AND tenant_id = ?` 条件。

它是平台多租户数据隔离的"守门人"，确保租户 A 的操作永远不会影响到租户 B 的数据。

核心职责：
- **自动 SQL 过滤**：所有 SQL 自动追加租户条件，无需手动拼写 WHERE tenant_id
- **智能忽略**：根据表类型、注解、配置等多级策略判断哪些表需要忽略
- **全局开关**：支持编程式完全关闭租户过滤（如登录、租户管理模块）
- **大小写兼容**：同时处理表名的大小写（适配不同数据库的习惯）

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Interceptor** | MyBatis Plus 插件机制，拦截 SQL 执行 | 实现 `TenantLineHandler` 接口 |
| **Template Method** | 实现接口的 `getTenantId()` 和 `ignoreTable()` 模板方法 | MP 固定的方法签名 |
| **Strategy** | 多级判断策略决定表是否忽略 | 全局忽略 -> 缓存 -> 计算忽略 |
| **Memoization** | 计算结果缓存，避免重复反射 | `ignoreTables` HashMap 缓存 |
| **AST Manipulation** | JSQLParser 修改 SQL 语法树 | Expression 和 LongValue 的使用 |

## 核心逻辑流程

### ignoreTable 判断流程

```
ignoreTable(tableName)
  |
  +-- 优先级 1: 全局忽略
  |    TenantContextHolder.isIgnore() == true
  |    -> 所有表都不加租户过滤
  |    场景: 登录接口、租户管理、系统初始化
  |
  +-- 优先级 2: 缓存查询
  |    tableName = SqlParserUtils.removeWrapperSymbol(tableName)  (去除引号包裹)
  |    ignore = ignoreTables.get(tableName.toLowerCase())
  |    |
  |    +-- [缓存 HIT] -> return ignore
  |    +-- [缓存 MISS] -> computeIgnoreTable(tableName) -> 写入缓存 -> return ignore
  |
  +-- computeIgnoreTable(tableName) 详细计算逻辑:
       |
       +-- 2.1 非 MyBatis Plus 实体表
       |    TableInfoHelper.getTableInfo(tableName) == null
       |    -> 不是 MP 管理的实体类，无法判定是否继承 TenantBaseDO
       |    -> return true (忽略，不加租户过滤)
       |    例如: flyway_schema_history, ACT_* (Flowable 工作流表)
       |
       +-- 2.2 继承 TenantBaseDO 基类
       |    TenantBaseDO.class.isAssignableFrom(entityType) == true
       |    -> 显式继承了租户基类，需要租户过滤
       |    -> return false (不忽略)
       |
       +-- 2.3 标注 @TenantIgnore 注解
       |    entityType.getAnnotation(TenantIgnore.class) != null
       |    -> 手动标记忽略租户
       |    -> return true (忽略)
       |
       +-- 2.4 默认情况
            -> return true (忽略)
```

### getTenantId

```java
@Override
public Expression getTenantId() {
    return new LongValue(TenantContextHolder.getRequiredTenantId());
}
```

从 `TenantContextHolder` 获取当前线程的租户 ID，构建为 JSQLParser 的 `LongValue` 表达式。如果租户 ID 为空，抛出 NullPointerException。

### MyBatis Plus 中 SQL 的修改

当 MyBatis Plus 的 `MybatisPlusInterceptor` 拦截到一条 SQL 时：

```sql
-- 原始 SQL
SELECT * FROM system_user WHERE username = 'admin'

-- 经过 TenantDatabaseInterceptor 处理后:
SELECT * FROM system_user 
WHERE username = 'admin' 
  AND tenant_id = 1  -- 自动追加
```

如果表有别名:
```sql
SELECT u.* FROM system_user u
WHERE u.username = 'admin'

-- 处理后:
SELECT u.* FROM system_user u
WHERE u.username = 'admin' 
  AND u.tenant_id = 1  -- 别名自动匹配
```

这背后的机制是 JSQLParser 解析 SQL 为 AST -> 在 WHERE 子句追加 AND 条件 -> 输出修改后的 SQL。

## 关键代码剖析

```java
public class TenantDatabaseInterceptor implements TenantLineHandler {

    // 忽略表缓存: key=表名(大小写都存), value=是否忽略
    private final Map<String, Boolean> ignoreTables = new HashMap<>();

    public TenantDatabaseInterceptor(TenantProperties properties) {
        // 从配置加载忽略表列表 (如 flyway_schema_history, ACT_GE_PROPERTY 等)
        properties.getIgnoreTables().forEach(table -> addIgnoreTable(table, true));
        // Oracle 用 DUAL 表查询序列，加租户条件会报错
        addIgnoreTable("DUAL", true);
    }

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContextHolder.getRequiredTenantId());
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 全局忽略
        if (TenantContextHolder.isIgnore()) {
            return true;
        }
        // 去除 SQL 中的引号 (如 `table_name` 或 "table_name")
        tableName = SqlParserUtils.removeWrapperSymbol(tableName);
        // 从缓存查找 (统一小写)
        Boolean ignore = ignoreTables.get(tableName.toLowerCase());
        if (ignore == null) {
            // 缓存 MISS -> 计算
            ignore = computeIgnoreTable(tableName);
            // 同步写入缓存 (双大小写)
            synchronized (ignoreTables) {
                addIgnoreTable(tableName, ignore);
            }
        }
        return ignore;
    }

    private boolean computeIgnoreTable(String tableName) {
        // 不是 MyBatis Plus 实体表 -> 忽略
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        if (tableInfo == null) {
            return true;
        }
        // 继承 TenantBaseDO -> 需要租户过滤
        if (TenantBaseDO.class.isAssignableFrom(tableInfo.getEntityType())) {
            return false;
        }
        // 有 @TenantIgnore 注解 -> 忽略
        TenantIgnore tenantIgnore = tableInfo.getEntityType().getAnnotation(TenantIgnore.class);
        return tenantIgnore != null;
    }
}
```

## 调用链

```
[Upstream]
  MyBatisPlusInterceptor (MyBatis Plus 插件)
    -> TenantDatabaseInterceptor (本类, 作为 TenantLineHandler)
       |
       +-- [获取租户] TenantContextHolder.getRequiredTenantId()
       |    -> TransmittableThreadLocal<Long>
       |
       +-- [判断忽略] TableInfoHelper.getTableInfo()
       |    -> MyBatis Plus 实体元数据缓存
       |
       +-- [配置加载] TenantProperties.getIgnoreTables()
            -> application.yaml: develop.tenant.ignore-tables

[Downstream]
  生成的 SQL 片段:
    AND tenant_id = 123
```

## 配置与条件

| 配置/注解 | 位置 | 说明 |
|-----------|------|------|
| `develop.tenant.ignore-tables` | application.yaml | 需要忽略租户过滤的表名列表 |
| `@TenantIgnore` | 实体类上 | 标记该实体忽略租户过滤 |
| `TenantContextHolder.setIgnore(true)` | 编程式 | 全局关闭租户过滤 |
| `TenantBaseDO` | 实体继承 | 继承此基类的实体自动启用租户过滤 |

### 配置示例

```yaml
develop:
  tenant:
    ignore-tables:
      - flyway_schema_history
      - ACT_GE_PROPERTY
      - ACT_GE_BYTEARRAY
      - ACT_RE_DEPLOYMENT
      # ... 其他 Flowable 表
```

### Bean 注册

```java
@Bean
public TenantLineHandler tenantLineHandler(TenantProperties properties) {
    return new TenantDatabaseInterceptor(properties);
}
```

注册到 `MybatisPlusInterceptor` 时必须放在拦截器列表的**第一位**：
```java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor(TenantLineHandler tenantLineHandler) {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));  // 第一位!
    interceptor.addInnerInterceptor(new DataPermissionInterceptor(handler));             // 第二位
    // ...
}
```

## 生产级关注点

### 1. 性能考虑

- **缓存优化**: `ignoreTables` HashMap 缓存计算结果，避免每次 SQL 都执行反射查询 `TableInfoHelper`
- **大小写双缓存**: 同时存储 `tableName.toLowerCase()` 和 `tableName.toUpperCase()`，避免大小写转换开销
- **反射开销**: `computeIgnoreTable` 中的 `TableInfoHelper.getTableInfo()` 和 `isAssignableFrom()` 涉及反射，但只在首次访问该表时执行一次

### 2. synchronized 锁范围

```java
synchronized (ignoreTables) {
    addIgnoreTable(tableName, ignore);
}
```

锁的范围很小：只在缓存 miss 时，且只加在写入时。缓存查询在锁外执行，不影响并发读性能。

### 3. 已知的忽略表

必须忽略的表包括：
- **系统表**: DUAL (Oracle), flyway_schema_history
- **Flowable 工作流表**: ACT_* (Flowable 的多租户通过 tenant_id 字段在 Flowable 层面处理)
- **跨租户共享表**: 如系统配置表、字典表等需要跨租户共享的数据

### 4. 租户字段要求

启用租户过滤的表，其对应实体类必须：
1. 继承 `TenantBaseDO`（该基类包含 `tenantId` 字段）
2. 或手动在实体中添加 `tenantId` 字段并确保表中有对应列

### 5. DUAL 表特殊处理

```java
addIgnoreTable("DUAL", true);
```

Oracle 中 `SELECT SEQ.NEXTVAL FROM DUAL` 用于生成序列值。如果不忽略 DUAL 表，会在 DUAL 表上追加 `AND tenant_id = ?`，导致 Oracle 报错。

### 6. 全局忽略场景

`TenantContextHolder.setIgnore(true)` 在以下场景被调用：
- 登录接口（用户尚未确定归属租户）
- 租户管理（超级管理员管理租户列表）
- 注册接口（新用户注册，尚未分配到租户）

### 7. 限制与风险

- 子查询中的表也可能被追加租户条件，如果子查询用于统计或其他目的，可能导致结果不准确
- `@TenantIgnore` 仅标注在实体类上生效，不能控制到方法级别
- 如果表通过 `@TableName("table_name")` 映射且配置了 schema，表名匹配可能失效

### 8. 排查提示

如果出现了"查不到数据"的问题，可以考虑：
- 确认 `TenantContextHolder.getTenantId()` 是否正确设置
- 确认表是否继承 `TenantBaseDO`
- 在 SQL 日志中查看是否自动追加了 `tenant_id` 条件
- 检查 `@TenantIgnore` 配置是否意外遗漏
