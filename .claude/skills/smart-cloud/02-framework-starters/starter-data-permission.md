---
name: starter-data-permission
description: Row-level data filtering via JSQLParser — department-level and self-only data scoping with @DataPermission annotation, rule customization, and easy-trans auto-exclusion
type: project
---

# develop-spring-boot-starter-biz-data-permission

## Overview

行级数据权限模块。通过 JSQLParser 在 SQL 执行前动态注入数据过滤条件，实现部门级别、个人级别的数据隔离。配合 `@DataPermission` 注解精细控制每个 Mapper 方法的数据范围。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **3 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.datapermission`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-biz-data-permission AutoConfiguration.imports
com.develop.mvp.pk.framework.datapermission.config.DevelopDataPermissionAutoConfiguration
com.develop.mvp.pk.framework.datapermission.config.DevelopDeptDataPermissionAutoConfiguration
com.develop.mvp.pk.framework.datapermission.config.DevelopDataPermissionRpcAutoConfiguration
```

## Core Architecture

### 1. DataPermissionRule Interface

```java
public interface DataPermissionRule {
    Set<String> getTableNames();                              // Tables affected by this rule
    Expression getExpression(String tableName, String alias); // JSQLParser WHERE expression
}
```

All contributed `DataPermissionRule` beans are discovered and combined by `DataPermissionRuleFactoryImpl`.

### 2. DataPermissionRuleHandler

- Implements `MultiDataPermissionHandler` (MyBatis Plus 3.5+ API)
- AND-combines expressions from multiple rules
- Excludes rules specified in `@DataPermission(excludeRules = ...)`
- Inserts into `MybatisPlusInterceptor` at **pos=0** (same position as tenant interceptor, both are AND-combined)

### 3. DataPermissionRuleFactoryImpl

- Collects all `DataPermissionRule` beans from Spring context
- Parses `@DataPermission` annotations on the current Mapper method:
  - `enable=true/false` — enable/disable data permissions
  - `includeRules` — whitelist of rules to apply
  - `excludeRules` — blacklist of rules to skip
- Detects easy-trans translation calls via stack trace analysis and auto-disables data permissions during translation

### 4. @DataPermission Annotation

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {
    boolean enable() default true;           // Enable/disable data permission filtering
    Class<?>[] includeRules() default {};    // Only apply specified rule types
    Class<?>[] excludeRules() default {};    // Exclude specified rule types
}
```

### 5. DataPermissionContextHolder

- ThreadLocal storage for current Mapper execution context
- Allows programmatic temporary data permission adjustment
- Useful for batch operations or export scenarios where permission scope needs dynamic adjustment

### 6. DeptDataPermissionRule (Built-in Implementation)

Built-in department-level data permission with 4 scope modes:

| Mode | SQL Condition | Business Meaning |
|---|---|---|
| `ALL` | No filter appended | Can see all data (admin/supervisor) |
| `DEPT_LEVEL` | `dept_id IN (managed dept IDs)` | Can see own department and sub-departments data |
| `SELF_ONLY` | `user_id = ?` | Can only see own data |
| `DEPT_AND_SELF` | `(dept_id IN ... OR user_id = ?)` | Combination of dept and self scope |

Data scope is determined by `LoginUser.context` containing `DeptDataPermissionRespDTO`, loaded from `PermissionCommonApi` at login time and cached.

### 7. DeptDataPermissionRuleCustomizer

`@FunctionalInterface`, allows each module to customize table-column mapping:

```java
@FunctionalInterface
public interface DeptDataPermissionRuleCustomizer {
    void customize(DeptDataPermissionRule rule);
}
```

Usage:
```java
@Component
public class OrderDataPermissionCustomizer implements DeptDataPermissionRuleCustomizer {
    @Override
    public void customize(DeptDataPermissionRule rule) {
        rule.addDeptColumn(OrderDO.class);              // Filter by dept_id column
        rule.addUserColumn(OrderDO::getUserId);         // Filter by user_id column
    }
}
```

### 8. Easy-trans Auto-Exclusion

- Stack trace detection: if `TranslationInvoker` is in the call stack, data permissions are auto-disabled
- Prevents translation queries (for dictionary label, dept name, etc.) from being incorrectly filtered by data permission rules
- This is transparent to developers — no additional annotation or configuration needed

## Data Flow

```
LoginUser.context
  contains DeptDataPermissionRespDTO
    (loaded from PermissionCommonApi at login)
         |
         v
DeptDataPermissionRule.buildExpression()
  converts scope + dept IDs → JSQLParser Expression
         |
         v
DataPermissionRuleFactoryImpl.getExpression()
  AND-combines all active rules
         |
         v
DataPermissionRuleHandler.intercept()
  appends WHERE clause to SQL in MybatisPlusInterceptor
         |
         v
Final SQL executed by MyBatis
```

## Configuration Properties

```yaml
develop:
  data-permission:
    enabled: true             # Global switch (default: true)
```

## Code Examples

```java
// Customize department data permission for an entity
@Component
public class OrderDataPermissionCustomizer implements DeptDataPermissionRuleCustomizer {
    @Override
    public void customize(DeptDataPermissionRule rule) {
        rule.addDeptColumn(OrderDO.class);              // By dept_id
        rule.addUserColumn(OrderDO::getUserId);         // By user_id
    }
}

// Disable data permission for a specific method
@DataPermission(enable = false)
@GetMapping("/export-all")
public void exportAll(HttpServletResponse response) { ... }

// Exclude specific rules
@DataPermission(excludeRules = DeptDataPermissionRule.class)
@GetMapping("/dept-stat")
public CommonResult<List<DeptStatVO>> deptStat() { ... }

// Only include specific rules
@DataPermission(includeRules = DeptDataPermissionRule.class)
@GetMapping("/self-orders")
public CommonResult<List<OrderVO>> selfOrders() { ... }
```

## Performance Considerations

- JSQLParser parses SQL into AST — adds ~1-5ms per query
- Caffeine cache (1024 entries, 5s TTL) in `DevelopMybatisAutoConfiguration` reduces overhead for repeated queries
- High-QPS interfaces: consider caching results rather than hitting DB with dynamic permission filters
- Batch queries (e.g., `selectBatchIds`) are filtered individually by the interceptor, no special batching optimization exists

## 注意事项

- `DeptDataPermissionRule` 的数据范围由 `LoginUser.context` 中的缓存权限决定，用户重新授权后需**重新登录**以刷新缓存
- JSQLParser AST 解析有性能开销（约 1-5ms/查询），高并发接口建议使用查询结果缓存（`@Cacheable`）减少拦截次数
- easy-trans 翻译排除通过异常堆栈检测实现，注意不要在异步线程的翻译调用中包裹数据权限逻辑——堆栈检测可能失效
- `@DataPermission(enable = false)` 对整个 Mapper 方法生效，无法在同方法内的部分查询上选择性跳过；如需部分过滤，考虑拆分方法或使用 `DataPermissionContextHolder`
- `DataPermissionRuleHandler` 和 `TenantLineInnerInterceptor` 都在 pos=0，它们通过 AND 组合，互不冲突
- 三个自动配置类分别负责：通用规则引擎、部门数据权限具体实现、Feign RPC 支持
- `DevelopDeptDataPermissionAutoConfiguration` 是部门数据权限的具体实现配置，会在检测到 `DeptDataPermissionRuleCustomizer` Bean 时生效
