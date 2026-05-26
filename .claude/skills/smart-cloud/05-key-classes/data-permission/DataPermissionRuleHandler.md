---
name: DataPermissionRuleHandler
description: MyBatis Plus data permission handler that aggregates multiple DataPermissionRule expressions
type: project
---

# DataPermissionRuleHandler

## 功能定位

DataPermissionRuleHandler 是数据权限在 MyBatis Plus 层面的统一处理器，位于 `develop-spring-boot-starter-biz-data-permission` 的 `core.db` 包下。它实现 `MultiDataPermissionHandler` 接口，在 MyBatis Plus 执行 SQL 前**拦截并追加数据权限过滤条件**。

核心职责：
- **规则聚合**：通过 `DataPermissionRuleFactory` 获取所有匹配的数据权限规则
- **表达式组合**：将各规则生成的 WHERE 表达式通过 **AND** 组合为最终条件
- **跨租户跳过**：跨租户访问时全局跳过数据权限
- **MappedStatement 匹配**：根据 `@DataPermission` 注解配置控制哪些 Mapper 启用数据权限

它与 `DeptDataPermissionRule` 的关系：
- `DataPermissionRuleHandler` 是**调度者**（Handler），决定哪些规则被激活
- `DeptDataPermissionRule` 是**执行者**（Rule），生成具体的 SQL 条件
- 一个 Handler 可以组合多个 Rule

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Composite** | 组合多个 `DataPermissionRule` 的表达式结果 | `Expression allExpression` 通过 AND 组合 |
| **Chain of Responsibility** | 依次遍历每个规则 | `for (DataPermissionRule rule : rules)` |
| **Interceptor** | MyBatis Plus 插件机制，拦截 SQL 执行 | 实现 `MultiDataPermissionHandler` |
| **Mediator** | 协调多个 Rule 与 SQL 执行引擎 | `getSqlSegment()` 做规则调度 |
| **Guard Clause** | 前置条件快速返回 | `skipPermissionCheck()` -> null, `rules.isEmpty()` -> null |

## 核心逻辑流程

### 数据权限 SQL 生成

```
getSqlSegment(table, where, mappedStatementId)
  |
  +-- Guard 1: 跨租户访问
  |    SecurityFrameworkUtils.skipPermissionCheck() == true
  |    -> 返回 null (不加任何数据权限条件)
  |
  +-- 步骤 1: 获取 MappedStatement 匹配的数据权限规则
  |    ruleFactory.getDataPermissionRule(mappedStatementId)
  |    |
  |    +-- 基于 @DataPermission 注解判断:
  |    |    @DataPermission(enable = false) -> 返回空列表
  |    |    @DataPermission(enable = true)  -> 返回所有规则
  |    |
  |    +-- 基于 DataPermissionContextHolder 判断:
  |    |    include/exclude 过滤
  |    |
  |    +-- 返回 List<DataPermissionRule>
  |
  +-- Guard 2: 无匹配规则
  |    CollUtil.isEmpty(rules) -> 返回 null (无数据权限限制)
  |
  +-- 步骤 2: 遍历所有规则
  |    FOR EACH rule IN rules:
  |    |
  |    +-- 步骤 2.1: 匹配表名
  |    |    tableName = MyBatisUtils.getTableName(table)
  |    |    (去除 schema 前缀，获取纯表名)
  |    |    IF !rule.getTableNames().contains(tableName) -> continue
  |    |
  |    +-- 步骤 2.2: 获取该规则的表达式
  |    |    Expression oneExpress = rule.getExpression(tableName, table.getAlias())
  |    |    IF oneExpress == null -> continue (此规则对此 SQL 无约束)
  |    |
  |    +-- 步骤 2.3: 组合到总表达式
  |         allExpression = allExpression == null
  |             ? oneExpress
  |             : new AndExpression(allExpression, oneExpress)
  |
  +-- 步骤 3: 返回组合后的表达式
       return allExpression
```

### 最终 SQL 示例

```sql
-- 原始 SQL
SELECT * FROM system_leave
WHERE status = 'PENDING'

-- 经过 DataPermissionRuleHandler 处理后:
SELECT * FROM system_leave
WHERE status = 'PENDING'
  AND (`dept_id` IN (1, 2) OR `user_id` = 123)  -- 部门数据权限
  AND `tenant_id` = 1                             -- 多租户 (由 TenantDatabaseInterceptor 追加)
```

注意：多租户和数据权限是**两层独立的拦截器**，按配置顺序叠加。

## 关键代码剖析

```java
@RequiredArgsConstructor
public class DataPermissionRuleHandler implements MultiDataPermissionHandler {

    private final DataPermissionRuleFactory ruleFactory;

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        // 1. 跨租户跳过
        if (skipPermissionCheck()) {
            return null;
        }

        // 2. 获取匹配的规则
        List<DataPermissionRule> rules = ruleFactory.getDataPermissionRule(mappedStatementId);
        if (CollUtil.isEmpty(rules)) {
            return null;
        }

        // 3. 组合规则
        Expression allExpression = null;
        for (DataPermissionRule rule : rules) {
            String tableName = MyBatisUtils.getTableName(table);
            if (!rule.getTableNames().contains(tableName)) {
                continue;
            }
            Expression oneExpress = rule.getExpression(tableName, table.getAlias());
            if (oneExpress == null) {
                continue;
            }
            allExpression = (allExpression == null)
                    ? oneExpress
                    : new AndExpression(allExpression, oneExpress);
        }
        return allExpression;
    }
}
```

### DataPermissionRuleFactory 机制

`DataPermissionRuleFactory` 内部是如何决定哪些规则匹配一个 MappedStatement 的？

```java
public List<DataPermissionRule> getDataPermissionRule(String mappedStatementId) {
    // 1. 查找 MappedStatement 对应的方法是否标记了 @DataPermission(enable = false)
    //    如果标记了，返回空列表（不应用任何数据权限）
    
    // 2. 检查 DataPermissionContextHolder 中的 include/exclude 控制
    //    当前线程 enable/disable/include/exclude 操作

    // 3. 返回所有已注册的 DataPermissionRule
    return allRules;
}
```

@VisibleForTesting 注解的方法 `getDisableDataPermissionDisable` 展示了如何解析 `@DataPermission(enable = false)`。

### @DataPermission 注解

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DataPermission {
    boolean enable() default true;
}
```

使用场景：
- 标注在 Mapper 接口上，该 Mapper 的所有方法不应用数据权限
- 标注在 Service 方法上，某个查询方法不应用数据权限
- 标注在 Controller 方法上（如 `getPermissionInfo` 需要关闭数据权限）

### DataPermissionUtils 编程式控制

```java
public class DataPermissionUtils {

    // 1. 解析 @DataPermission(enable = false) 注解
    private static DataPermission DATA_PERMISSION_DISABLE;

    @DataPermission(enable = false)
    private static DataPermission getDisableDataPermissionDisable() {
        if (DATA_PERMISSION_DISABLE == null) {
            DATA_PERMISSION_DISABLE = DataPermissionUtils.class
                    .getDeclaredMethod("getDisableDataPermissionDisable")
                    .getAnnotation(DataPermission.class);
        }
        return DATA_PERMISSION_DISABLE;
    }

    // 2. 线程级控制
    public static void executeIgnore(Runnable runnable) {
        addDisableDataPermission();  // 向 DataPermissionContextHolder 添加禁用标记
        try {
            runnable.run();
        } finally {
            removeDataPermission();
        }
    }
}
```

使用 `DataPermissionUtils.executeIgnore()` 可以在不影响其他代码的前提下临时关闭数据权限。

## 调用链

```
[Upstream - SQL 执行拦截]
  MyBatisPlusInterceptor
    |
    +-- [优先级高] TenantLineInnerInterceptor (多租户, 先执行)
    |
    +-- DataPermissionInterceptor (数据权限, 后执行)
         |
         +-- DataPermissionRuleHandler.getSqlSegment() (本类)
              |
              +-- DataPermissionRuleFactory.getDataPermissionRule()
              |    +-- 检查 @DataPermission 注解
              |    +-- 检查 DataPermissionContextHolder
              |    +-- 返回匹配的规则列表
              |
              +-- [遍历] DeptDataPermissionRule.getExpression()
              +-- [遍历] 其他 DataPermissionRule
              |
              +-- JSQLParser 组合表达式
                   -> AND 条件追加到原始 SQL 的 WHERE 子句

[Downstream - 规则提供方]
  DataPermissionRule (接口)
    +-- DeptDataPermissionRule (部门数据权限)
    +-- 未来可扩展的自定义规则
```

## 配置与条件

| 控制方式 | 粒度 | 说明 |
|----------|------|------|
| `@DataPermission(enable = false)` | 类/方法 | 注解在 Mapper 或 Service 上关闭数据权限 |
| `DataPermissionUtils.executeIgnore()` | 代码块 | 编程式临时关闭，使用 try-finally 保证恢复 |
| `DataPermissionContextHolder.add/remove()` | 线程级 | 直接操作 ThreadLocal 控制 |
| `skipPermissionCheck()` | 全局 | 跨租户访问时自动跳过 |
| `DataPermissionRuleFactory` | SPI | 注册/取消注册数据权限规则 |

### 拦截器注册顺序

```java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));   // 1st: 多租户
    interceptor.addInnerInterceptor(new DataPermissionInterceptor(dataPermissionHandler)); // 2nd: 数据权限
    // 其他插件...
    return interceptor;
}
```

**顺序重要**: 多租户必须在数据权限之前。因为多租户的 `tenant_id` 是最基础的过滤条件，数据权限的 `dept_id` / `user_id` 是在租户内部进一步的过滤。如果数据权限先执行，生成的 SQL 中表别名可能不匹配。

## 生产级关注点

### 1. AND 组合语义

所有规则的表达式通过 AND 连接，意味着必须**同时满足所有规则**。例如：
- 租户规则: `tenant_id = 1`
- 部门规则: `dept_id IN (1, 2)`
- 最终: `WHERE tenant_id = 1 AND (dept_id IN (1, 2) OR user_id = 123)`

如果希望规则之间是 OR 关系，需要在规则内部实现（如 `DeptDataPermissionRule` 中 `dept` 和 `user` 是 OR 关系）。

### 2. 跨租户优先级最高

`skipPermissionCheck()` 的优先级高于所有规则：
```java
if (skipPermissionCheck()) {
    return null;
}
```

这是因为跨租户访问时，当前用户的权限数据（角色、部门数据权限）是针对其原始租户的，在当前目标租户中没有意义。

### 3. 性能影响

- **表名匹配**：`rule.getTableNames().contains(tableName)` 使用 HashSet 的 O(1) 查找
- **规则计算**：`rule.getExpression()` 中如果缓存命中（LoginUser.context）也无额外开销
- **JSQLParser 组合**：`AndExpression` 是纯内存操作

### 4. 常见问题排查

如果某条 SQL 没有按预期加上数据权限限制：
1. 检查 Mapper/Service 是否有 `@DataPermission(enable = false)`
2. 检查调用链中是否包含 `DataPermissionUtils.executeIgnore()`
3. 检查表名是否注册到规则的 `TABLE_NAMES` 中
4. 检查是否跨租户访问导致 `skipPermissionCheck()` 返回 true
5. 检查 `DataPermissionContextHolder` 是否被当前线程的上一个请求污染

### 5. @DataPermission 注解的传播

`@DataPermission` 使用 `@Inherited` 元注解，但仅对类继承有效（子类继承父类的注解），对方法重载无效。因此：
- 如果 Service 接口方法标记了 `@DataPermission(enable = false)`，实现类方法也需要标记（如果不标记，不会被继承）

### 6. 扩展自定义规则

如果需要添加自定义数据权限规则（如"只能查看本月数据"）：
1. 实现 `DataPermissionRule` 接口
2. 实现 `getTableNames()` 和 `getExpression()` 方法
3. 通过 `DataPermissionRuleFactory` SPI 机制注册

### 7. 多数据源支持

`DataPermissionRuleHandler` 支持多数据源场景，因为它在 MyBatis Plus 层面工作，而每个数据源的 `SqlSessionFactory` 都有自己独立的 `MybatisPlusInterceptor` 配置。在多数据源配置中需要对需要数据权限的数据源单独配置此拦截器。
