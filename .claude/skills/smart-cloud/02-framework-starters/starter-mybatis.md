---
name: starter-mybatis
description: MyBatis Plus data access layer — dynamic datasource (Druid), auto field filling, pagination, BaseMapperX/LambdaQueryWrapperX, multi-dialect ID generator, and easy-trans integration
type: project
---

# develop-spring-boot-starter-mybatis

## Overview

MyBatis Plus 数据访问层基础配置。提供多数据源（baomidou dynamic-datasource + Druid 连接池）、自动字段填充（审计字段）、分页插件、通用 BaseMapperX/BaseDO、LambdaQueryWrapperX 条件包装器、多方言 ID 生成器及 easy-trans 翻译集成。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **3 个自动配置类**，并通过 `spring.factories` 注册 1 个 `EnvironmentPostProcessor`。

**Package base:** `com.develop.mvp.pk.framework.mybatis|datasource|translate`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-mybatis AutoConfiguration.imports
com.develop.mvp.pk.framework.datasource.config.DevelopDataSourceAutoConfiguration
com.develop.mvp.pk.framework.mybatis.config.DevelopMybatisAutoConfiguration
com.develop.mvp.pk.framework.translate.config.DevelopTranslateAutoConfiguration

# spring.factories
org.springframework.boot.env.EnvironmentPostProcessor=\
com.develop.mvp.pk.framework.mybatis.config.IdTypeEnvironmentPostProcessor
```

## Core Components

### 1. DevelopMybatisAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.mybatis.config.DevelopMybatisAutoConfiguration`
**Note:** class name is `DevelopMybatisAutoConfiguration` (lowercase 'b'), not `DevelopMyBatis...`

- `@MapperScan(value = "${develop.info.base-package}", annotationClass = Mapper.class)` — scans whole project base package for `@Mapper` interfaces
- **MybatisPlusInterceptor**: registers core plugins:
  - `PaginationInnerInterceptor` — auto-dialect pagination
  - Additional interceptors at pos=0: `TenantLineInnerInterceptor` (tenant), `DataPermissionRuleHandler` (data permission) — injected by their respective starters
- **DefaultDBFieldHandler**: implements `MetaObjectHandler`, auto-fills:
  - `createTime`, `updateTime` (LocalDateTime, INSERT/INSERT_UPDATE)
  - `creator`, `updater` (String, from `SecurityFrameworkUtils.getLoginUserId()`)
  - `deleted` (Boolean, logic delete, default 0)
- **JacksonTypeHandler**: registers as `IJsonTypeHandler` for JSON column support
- **JsqlParserGlobal cache**: Caffeine cache, max 1024 entries, 5-second expiry — optimizes SQL parsing for tenant/data-permission interceptors
- Multi-dialect `IKeyGenerator` beans (conditional on `id-type=INPUT`):
  - PostgreSQL: `PostgreKeyGenerator`
  - Oracle: `OracleKeyGenerator`
  - H2: `H2KeyGenerator`
  - Kingbase: `KingbaseKeyGenerator`
  - DM: `DmKeyGenerator`

### 2. DevelopDataSourceAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.datasource.config`

- `@EnableTransactionManagement` — enables declarative transactions
- **DruidAdRemoveFilter**: removes Druid Monitor page footer ad
- Registers DruidStatViewServlet and DruidWebStatFilter

### 3. DevelopTranslateAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.translate.config`

- Initializes `easy-trans` `TranslateUtils`
- easy-trans provides `@Trans` annotation for field-level dictionary/foreign-key translation (e.g., `@Trans(type = TransType.SIMPLE, target = DeptDO.class, fields = "name")`)
- Auto-detects and registers all translation data sources

### 4. IdTypeEnvironmentPostProcessor

**Location:** `com.develop.mvp.pk.framework.mybatis.config`

Detects the primary datasource URL dialect and sets `mybatis-plus.global-config.db-config.id-type` accordingly:
- MySQL/MariaDB: `AUTO` (auto-increment)
- Oracle/PostgreSQL/Kingbase/DM/DB2/H2: `INPUT` (sequence-based, paired with `@KeySequence`)
- Fallback: `ASSIGN_ID` (Snowflake)

## Base Entity Classes

**BaseDO** (`com.develop.mvp.pk.framework.mybatis.core.dataobject`):
```java
public abstract class BaseDO implements Serializable, TransPojo {
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private String creator;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updater;
    @TableLogic
    private Boolean deleted;                 // 0=active, 1=deleted

    public void clean() { /* clear audit fields */ }
}
```

**TenantBaseDO** (in `develop-spring-boot-starter-biz-tenant` module, package `com.develop.mvp.pk.framework.tenant.core.db`):
```java
public abstract class TenantBaseDO extends BaseDO {
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
```

## BaseMapperX<T>

**Location:** `com.develop.mvp.pk.framework.mybatis.core.mapper`

Extends `BaseMapper<T>` with convenience methods:

```java
default T selectOne(SFunction<T, ?> field, Object value);                    // Single field EQ
default T selectOne(T entity);                                               // Multiple fields AND (non-null)
default List<T> selectList(SFunction<T, ?> field, Collection<?> values);     // IN query
default Long selectCount(SFunction<T, ?> field, Object value);               // Conditional count
default PageResult<T> selectPage(PageParam pageParam, Wrapper<T> queryWrapper); // Paginated + total count
default List<T> selectByIds(Collection<?> ids);                              // Batch ID query
```

## LambdaQueryWrapperX

**Location:** `com.develop.mvp.pk.framework.mybatis.core.query`

Extends `LambdaQueryWrapper<T>` with null-safe conditional methods:

```java
likeIfPresent(SFunction<T, ?> column, String val)         // LIKE %val% (skipped if val is null/empty)
eqIfPresent(SFunction<T, ?> column, Object val)           // = val (skipped if val is null)
inIfPresent(SFunction<T, ?> column, Collection<?> values) // IN values (skipped if values is empty)
betweenIfPresent(SFunction<T, ?> column, Object from, Object to) // BETWEEN (skipped if both null)
orderByIfPresent(SFunction<T, ?> column, boolean isAsc)   // ORDER BY (skipped if null)
```

## Dynamic DataSource

- Uses `baomidou dynamic-datasource-spring-boot-starter`
- Supports master-slave configuration
- Druid connection pool management
- `@DS("slave")` for read-only queries
- `@DSTransactional` for cross-datasource transactions (XA)

```java
@Service
public class UserServiceImpl implements UserService {
    @DS("slave")   // Routes to slave datasource
    public PageResult<UserDO> getUserPage(UserPageReqVO reqVO) {
        return userMapper.selectPage(reqVO);
    }

    @DS("master")  // Routes to master datasource (default, can omit)
    @Transactional
    public void createUser(UserSaveReqVO reqVO) {
        // ...
    }
}
```

## Configuration Properties

```yaml
spring:
  datasource:
    dynamic:
      primary: master                    # Default datasource
      strict: false                      # Whether to fail if datasource not found
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/smart_cloud
          driver-class-name: com.mysql.cj.jdbc.Driver
          username: root
          password: root
        slave:
          lazy: true                     # Lazy initialization
          url: jdbc:mysql://localhost:3306/smart_cloud_slave
          username: root
          password: root

mybatis-plus:
  global-config:
    db-config:
      id-type: NONE                      # Smart mode (auto-detected by IdTypeEnvironmentPostProcessor)
      logic-delete-value: 1              # Deleted flag value
      logic-not-delete-value: 0          # Active flag value
  configuration:
    default-enum-type-handler: com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
    map-underscore-to-camel-case: true
  type-aliases-package: ${develop.info.base-package}.module.*.dal.dataobject
```

## Code Examples

```java
// Entity
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_user")
@KeySequence("system_user_seq")  // For Oracle/PostgreSQL/Kingbase
public class UserDO extends TenantBaseDO {
    @TableId
    private Long id;
    private String username;
    private String nickname;
    private Integer status;
}

// Mapper
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {
    default UserDO selectByUsername(String username) {
        return selectOne(UserDO::getUsername, username);
    }
    default PageResult<UserDO> selectPage(UserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserDO>()
            .likeIfPresent(UserDO::getNickname, reqVO.getNickname())
            .eqIfPresent(UserDO::getStatus, reqVO.getStatus())
            .orderByDesc(UserDO::getId));
    }
}

// Service
@Service
public class UserServiceImpl implements UserService {
    @DS("slave")
    public PageResult<UserDO> getUserPage(UserPageReqVO reqVO) {
        return userMapper.selectPage(reqVO);
    }
}
```

## 注意事项

- 自动配置类名称为 `DevelopMybatisAutoConfiguration`（小写 'b'），与 MyBatis Plus 官方的 `MybatisPlusAutoConfiguration` 命名一致
- `BaseDO` 的逻辑删除（`deleted`）与唯一索引冲突：推荐方案是唯一的组合索引中包含 `deleted=0` 条件（部分索引），MySQL 5.7+ 或 PostgreSQL 支持；或在业务层做唯一性校验（Service validate 方法）
- 自动字段填充需注意 `insertStrategy=NEVER`/`UPDATE` 的字段不会被填充，确保审计字段不被覆盖
- `LambdaQueryWrapperX` 的 `*IfPresent` 方法在参数为 null 或空集合时自动跳过条件，确保 null-safety；但在 `val` 为 `""`（空串）时 `likeIfPresent` 也会跳过
- JSQL Parser 缓存默认 5 秒过期（硬编码在 `DevelopMybatisAutoConfiguration` 的 static 代码块中），对频繁变更的 SQL 拦截规则（如数据权限）需要注意传播延迟
- 多方言 ID 生成器仅在 `id-type=INPUT` 时激活；`ASSIGN_ID` 模式下统一走 Snowflake 算法；`AUTO` 模式下依赖数据库自增
- `@DS` 注解无法与 `@Transactional` 同时使用于同一方法（事务绑定到默认数据源），跨数据源事务需使用 `@DSTransactional` 或分布式事务方案
- easy-trans 的 `@Trans` 注解关联翻译会自动添加缓存，但首次查询会触发 N+1 查询（每个翻译字段会逐一查询），建议批量数据时使用 `@Trans` 的 `map` 模式进行批量翻译
