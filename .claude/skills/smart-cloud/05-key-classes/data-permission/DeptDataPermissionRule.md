---
name: DeptDataPermissionRule
description: Data permission rule that filters SQL by department ID and user ID based on user's data scope
type: project
---

# DeptDataPermissionRule

## 功能定位

DeptDataPermissionRule 是部门级别的数据权限规则实现，位于 `develop-spring-boot-starter-biz-data-permission` 的 `core.rule.dept` 包下。它实现 `DataPermissionRule` 接口，**在 SQL 执行时动态追加 `dept_id IN (...)` 或 `user_id = ?` 过滤条件**。

核心职责：
- **部门过滤**：限制用户只能看到指定部门的数据
- **本人数据**：支持"仅查看自己"的权限模式
- **权限结果缓存**：首次计算后缓存到 `LoginUser.context`，同一次请求中避免重复远程调用
- **表或字段自定义**：通过编程式配置，不同表可以有不同的部门/用户字段名

支持的三种数据权限范围：
1. **全部数据**（ALL）：不追加任何条件，超级管理员可见全部
2. **指定部门及子部门**（DEPT_ONLY/DEPT_AND_CHILD）：追加 `dept_id IN (部门1, 部门2, ...)`
3. **仅本人数据**（SELF）：追加 `user_id = 当前用户ID`

## 设计模式

| 模式 | 说明 | 代码体现 |
|------|------|----------|
| **Strategy** | 实现 `DataPermissionRule` 接口，作为数据权限规则的一种策略 | `extends DataPermissionRule` |
| **Specification** | 根据 `DeptDataPermissionRespDTO` 组装不同的 SQL Expression | all/deptIds/self 三种情况 |
| **Cache-Aside** | 权限结果首次计算后缓存到 LoginUser.context | `getContext()` / `setContext()` |
| **Builder** | 构建 JSQLParser 表达式 | `buildDeptExpression()`, `buildUserExpression()` |
| **Plugin / SPI** | 通过 `DataPermissionRuleFactory` SPI 注册 | `DeptDataPermissionRuleCustomizer` |

## 核心逻辑流程

### getExpression 主流程

```
getExpression(tableName, tableAlias)
  |
  +-- 前置校验:
  |    +-- SecurityFrameworkUtils.getLoginUser() == null -> return null
  |    +-- loginUser.getUserType() != ADMIN -> return null
  |    (数据权限目前只对管理员生效)
  |
  +-- 从 LoginUser.context 获取缓存:
  |    loginUser.getContext(CONTEXT_KEY, DeptDataPermissionRespDTO.class)
  |    |
  |    +-- [CACHE HIT] -> 直接使用
  |    +-- [CACHE MISS] -> permissionApi.getDeptDataPermission(loginUser.getId())
  |         -> Feign 调用 system-server
  |         -> 获取用户的部门数据权限配置
  |         -> loginUser.setContext(CONTEXT_KEY, deptDataPermission)
  |
  +-- 根据 deptDataPermission 中的三个标志位组装表达式:
       |
       +-- 情况 A: deptDataPermission.getAll() == true
       |    超级管理员 / 角色配置了"查看全部"
       |    -> return null (不追加任何 SQL 条件)
       |
       +-- 情况 B: deptIds 为空 && self == false
       |    既不能查看部门数据，又不能查看本人数据
       |    实际上 100% 无权限
       |    -> return new EqualsTo(null, null)  (WHERE NULL=NULL -> 空结果)
       |
       +-- 情况 C: 需要拼接条件
            |
            +-- buildDeptExpression():
            |    +-- deptColumns 中配置了该表的部门字段名
            |    +-- deptIds 非空
            |    -> return dept_id IN (id1, id2, id3)
            |    +-- 不满足条件 -> return null
            |
            +-- buildUserExpression():
            |    +-- self == true (可查看本人)
            |    +-- userColumns 中配置了该表的用户字段名
            |    -> return user_id = currentUserId
            |    +-- 不满足条件 -> return null
            |
            +-- 组合:
                 +-- 两者都非空: (dept_id IN (...) OR user_id = ?)
                 +-- 仅部门: dept_id IN (...)
                 +-- 仅本人: user_id = ?
                 +-- 两者都空: NULL = NULL (空结果兜底)
```

### 生成 JSQLParser 表达式

```
buildDeptExpression(tableName, tableAlias, deptIds):
  -> MyBatisUtils.buildColumn(tableName, tableAlias, columnName)
     -> 生成: `table_name`.`dept_id`  (column reference)
  -> new ExpressionList(LongValue...)
     -> 生成: (1, 2, 3)
  -> new InExpression(column, list)
     -> 生成: `table_name`.`dept_id` IN (1, 2, 3)

buildUserExpression(tableName, tableAlias, self, userId):
  -> MyBatisUtils.buildColumn(tableName, tableAlias, columnName)
     -> 生成: `table_name`.`user_id`
  -> new EqualsTo(column, new LongValue(userId))
     -> 生成: `table_name`.`user_id` = 123
```

## 关键代码剖析

```java
@AllArgsConstructor
@Slf4j
public class DeptDataPermissionRule implements DataPermissionRule {

    protected static final String CONTEXT_KEY = DeptDataPermissionRule.class.getSimpleName();
    private static final String DEPT_COLUMN_NAME = "dept_id";
    private static final String USER_COLUMN_NAME = "user_id";

    private final PermissionCommonApi permissionApi;

    // 表配置: 表名 -> 字段名
    private final Map<String, String> deptColumns = new HashMap<>();
    private final Map<String, String> userColumns = new HashMap<>();
    private final Set<String> TABLE_NAMES = new HashSet<>();

    @Override
    public Expression getExpression(String tableName, Alias tableAlias) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) return null;
        if (ObjectUtil.notEqual(loginUser.getUserType(), UserTypeEnum.ADMIN.getValue())) return null;

        // 从 context 缓存获取 (每请求只计算一次)
        DeptDataPermissionRespDTO deptDataPermission =
                loginUser.getContext(CONTEXT_KEY, DeptDataPermissionRespDTO.class);
        if (deptDataPermission == null) {
            deptDataPermission = permissionApi.getDeptDataPermission(loginUser.getId()).getCheckedData();
            if (deptDataPermission == null) {
                log.error("[getExpression][LoginUser({}) 获取数据权限为 null]", JsonUtils.toJsonString(loginUser));
                throw new NullPointerException("数据权限未返回");
            }
            loginUser.setContext(CONTEXT_KEY, deptDataPermission);
        }

        // 情况 A: 全部可见
        if (deptDataPermission.getAll()) return null;

        // 情况 B: 完全无权限
        if (CollUtil.isEmpty(deptDataPermission.getDeptIds())
                && Boolean.FALSE.equals(deptDataPermission.getSelf())) {
            return new EqualsTo(null, null);
        }

        // 情况 C: 拼接条件
        Expression deptExpression = buildDeptExpression(tableName, tableAlias, deptDataPermission.getDeptIds());
        Expression userExpression = buildUserExpression(tableName, tableAlias, deptDataPermission.getSelf(), loginUser.getId());

        if (deptExpression == null && userExpression == null) {
            return new EqualsTo(null, null);
        }
        if (deptExpression == null) return userExpression;
        if (userExpression == null) return deptExpression;
        return new ParenthesedExpressionList(new OrExpression(deptExpression, userExpression));
    }
}
```

### 表配置机制

通过 `DeptDataPermissionRuleCustomizer` 函数式接口，各模块可以注册自己的表：

```java
// 配置示例: 在模块的自动配置中
@Bean
public DeptDataPermissionRuleCustomizer sysDeptDataPermissionRuleCustomizer() {
    return rule -> {
        rule.addDeptColumn(AdminUserDO.class);            // system_user 使用默认 dept_id
        rule.addDeptColumn(DeptDO.class);                  // system_dept 使用默认 dept_id
        rule.addUserColumn(OrderDO.class, "creator_id");   // 订单表使用 creator_id 关联用户
    };
}
```

### OR 逻辑的理解

部门权限和本人数据使用 **OR** 连接，意味着：
- 如果用户有部门 A 的权限，且开启了"查看本人"
- 那么 SQL 条件为: `WHERE (dept_id IN (A) OR user_id = 当前用户)`
- 用户可以看到：**部门 A 下的所有数据** + **自己创建的数据**（无论是否在部门 A 中）

这是大多数业务系统的常见需求：用户既需要看到部门数据，也需要看到自己创建但不在本部门的数据。

## 调用链

```
[Upstream - DataPermissionRuleHandler]
  MybatisPlusInterceptor
    -> DataPermissionRuleHandler.getSqlSegment()
       -> DeptDataPermissionRule.getExpression() (本类)
          |
          +-- [获取用户] SecurityFrameworkUtils.getLoginUser()
          +-- [远程调用] PermissionCommonApi.getDeptDataPermission()
          |    -> Feign -> PermissionApiImpl
          |    -> PermissionService.getDeptDataPermission()
          |    -> 查询 role -> 查询 role 的 dataScope -> 计算 deptIds
          |
          +-- [缓存] loginUser.setContext(CONTEXT_KEY, permission)
          |
          +-- [表达式] MyBatisUtils.buildColumn() + JSQLParser API

[Downstream - 实际执行的 SQL]
  原始 SQL:
    SELECT * FROM system_xxx WHERE status = 1
  
  修改后:
    SELECT * FROM system_xxx
    WHERE status = 1
      AND (`system_xxx`.`dept_id` IN (1, 2) OR `system_xxx`.`user_id` = 123)
```

## 配置与条件

| 标志位 | 类型 | 说明 |
|--------|------|------|
| `all` | Boolean | 是否可查看全部数据（超级管理员） |
| `deptIds` | Set<Long> | 可查看的部门编号集合 |
| `self` | Boolean | 是否可查看本人数据 |

| 方法 | 参数 | 说明 |
|------|------|------|
| `addDeptColumn(Class, columnName)` | 实体类 + 字段名 | 配置某实体的部门字段 |
| `addDeptColumn(Class)` | 实体类 | 使用默认 `dept_id` 字段 |
| `addUserColumn(Class, columnName)` | 实体类 + 字段名 | 配置某实体的用户字段 |
| `addUserColumn(Class)` | 实体类 | 使用默认 `user_id` 字段 |

### 数据范围枚举

| 数据范围 | all | deptIds | self | 效果 |
|----------|-----|---------|------|------|
| 全部数据权限 | true | - | - | 不加任何过滤 |
| 指定部门 | false | [1,2,3] | false | WHERE dept_id IN (1,2,3) |
| 本部门 | false | [当前部门] | false | WHERE dept_id = 当前部门 |
| 本部门及以下 | false | [当前部门 + 子部门] | false | WHERE dept_id IN (1,2,3,4) |
| 仅本人 | false | [] | true | WHERE user_id = 当前用户 |
| 自定义部门 | false | [自定义] | true | WHERE (dept_id IN (...) OR user_id = 当前用户) |

## 生产级关注点

### 1. 请求级缓存

`LoginUser.context` 是请求级别的缓存，key 为 `DeptDataPermissionRule.class.getSimpleName()`。这意味着：
- 同一请求内的多次 SQL 查询只需要一次远程调用
- 不同请求之间不共享（LoginUser 是每次请求新创建的）
- 缓存数据随请求结束自动被 GC

### 2. NULL=NULL 兜底

当表达式无法构建时，返回 `new EqualsTo(null, null)` 生成 `WHERE NULL = NULL`（MySQL 中永不成立），保证查询结果为空。这比返回全部数据安全，也比抛异常友好。

### 3. 性能影响

- **无数据权限的表没有额外开销**：如果某表没有在 `TABLE_NAMES` 中注册，规则直接跳过
- **缓存命中时无额外开销**：后续 SQL 直接使用已经计算好的表达式
- **JSQLParser 表达式构建非常轻量**：纯内存操作，微秒级

### 4. 数据权限的局限

- 只支持 `dept_id` 和 `user_id` 两个维度的过滤
- 不支持更复杂的业务规则（如"金额大于 1000 的订单"）
- 如果需要更复杂的规则，需要实现自定义的 `DataPermissionRule`

### 5. 使用提示

- 如果某个查询不需要数据权限限制，可以使用 `@DataPermission(enable = false)` 注解
- 通过 `DataPermissionUtils.executeIgnore()` 可在代码块中临时关闭
- 如果用户在修改部门后看不到之前的数据，这是设计如此——`dept_id` 不会随用户部门变更而更新

### 6. 扩展自定义数据权限规则

如果需要实现自己的数据权限规则：
1. 实现 `DataPermissionRule` 接口（主要是 `getTableNames()` 和 `getExpression()`）
2. 通过 `DataPermissionRuleFactory` 注册
3. 框架会自动在 SQL 执行时调用
