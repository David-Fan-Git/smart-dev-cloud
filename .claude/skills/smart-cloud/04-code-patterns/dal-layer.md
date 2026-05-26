---
name: dal-layer
description: Data Access Layer — Entity (DO) and Mapper patterns with MyBatis Plus, including MPJ join, JSON fields, FOR UPDATE locking, multi-db sequences
type: project
---

# DAL 层模式

## 概述

DAL 层包含 Data Object（DO / Entity）和 Mapper 接口。项目使用 MyBatis Plus 3.5.16 + MyBatis Plus Join（mybatis-plus-join）。所有 DO 继承 `BaseDO` 或 `TenantBaseDO`，Mapper 继承 `BaseMapperX`。

## 框架基类路径

| 类 | 包路径 |
|---|---|
| `BaseDO` | `com.develop.mvp.pk.framework.mybatis.core.dataobject.BaseDO` |
| `TenantBaseDO` | `com.develop.mvp.pk.framework.tenant.core.db.TenantBaseDO` |
| `BaseMapperX` | `com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX` |
| `LambdaQueryWrapperX` | `com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX` |

## Entity (DO) 模板

```java
package com.develop.mvp.pk.module.{module}.dal.dataobject.{domain};

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.develop.mvp.pk.framework.mybatis.core.dataobject.BaseDO;
import com.develop.mvp.pk.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * {领域名} DO
 */
@TableName(value = "{module}_{entity}", autoResultMap = true) // autoResultMap 开启 JSON 字段处理
@KeySequence("{module}_{entity}_seq") // Oracle/PostgreSQL/Kingbase/DM 序列
@Data
@EqualsAndHashCode(callSuper = true)
public class {Domain}DO extends TenantBaseDO { // 多租户表；非租户表用 BaseDO

    @TableId
    private Long id;

    private String name;

    /** 状态 {@link CommonStatusEnum} */
    private Integer status; // 0=启用, 1=禁用

    private Integer sort;

    private Long parentId; // 树形结构父 ID

    /**
     * JSON 字段：使用 JacksonTypeHandler 自动序列化/反序列化
     * 注意：@TableName 必须开启 autoResultMap = true
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Long> dataScopeDeptIds;

    // 时间字段
    private LocalDateTime expireTime;
}
```

### BaseDO 公共字段

```java
// BaseDO (非租户表，或标记 @TenantIgnore 的实体)
public class BaseDO {
    @TableField(fill = FieldFill.INSERT)      private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)      private String creator;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updater;
    @TableLogic                              private Boolean deleted;
}

// TenantBaseDO extends BaseDO (多租户表)
public class TenantBaseDO extends BaseDO {
    private Long tenantId; // 由 TenantInterceptor 自动填充
}
```

## Mapper 模板

```java
package com.develop.mvp.pk.module.{module}.dal.mysql.{domain};

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.mybatis.core.mapper.BaseMapperX;
import com.develop.mvp.pk.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo.{Domain}PageReqVO;
import com.develop.mvp.pk.module.{module}.dal.dataobject.{domain}.{Domain}DO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface {Domain}Mapper extends BaseMapperX<{Domain}DO> {

    default {Domain}DO selectByName(String name) {
        return selectOne({Domain}DO::getName, name);
    }

    default List<{Domain}DO> selectListByStatus(Integer status) {
        return selectList({Domain}DO::getStatus, status);
    }

    default List<{Domain}DO> selectListByIds(List<Long> ids) {
        return selectList({Domain}DO::getId, ids);
    }

    default PageResult<{Domain}DO> selectPage({Domain}PageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<{Domain}DO>()
                .likeIfPresent({Domain}DO::getName, reqVO.getName())
                .eqIfPresent({Domain}DO::getStatus, reqVO.getStatus())
                .betweenIfPresent({Domain}DO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc({Domain}DO::getId));
    }

    default Long selectCountByStatus(Integer status) {
        return selectCount({Domain}DO::getStatus, status);
    }
}
```

## BaseMapperX 内置方法（extends MPJBaseMapper）

`BaseMapperX` 继承 `MPJBaseMapper`（mybatis-plus-join），提供以下内置方法：

### 基础查询

| 方法 | 说明 |
|---|---|
| `selectOne(field, value)` | 单字段等值查询 |
| `selectOne(field1, val1, field2, val2)` | 两字段等值查询 |
| `selectOne(field1, val1, field2, val2, field3, val3)` | 三字段等值查询 |
| `selectFirstOne(field, value)` | 获取满足条件的第 1 条（并发安全） |
| `selectList(field, value)` | 单字段查询列表 |
| `selectList(field, values)` | `IN` 查询列表 |
| `selectList(field1, val1, field2, val2)` | 两字段查询列表 |
| `selectCount()` | 全表计数 |
| `selectCount(field, value)` | 按字段计数 |

### FOR UPDATE 行级锁

| 方法 | 说明 |
|---|---|
| `selectOneForUpdate(queryWrapper)` | 带 `FOR UPDATE` 的行级锁查询 |
| `selectOneForUpdate(field, value)` | 单字段 FOR UPDATE |
| `selectOneForUpdate(f1, v1, f2, v2)` | 两字段 FOR UPDATE |

必须包装在 `@Transactional` 中，事务提交后释放锁。

### 分页查询

| 方法 | 说明 |
|---|---|
| `selectPage(pageParam, wrapper)` | 标准分页，返回 `PageResult` |
| `selectPage(sortablePageParam, wrapper)` | 支持排序字段的分页 |
| `selectJoinPage(pageParam, clazz, lambdaWrapper)` | 连表分页（MPJ） |

### 批量操作

| 方法 | 说明 |
|---|---|
| `insertBatch(Collection<T>)` | 批量插入（SQL Server 特殊处理） |
| `insertBatch(Collection<T>, int size)` | 指定批次大小 |
| `updateBatch(Collection<T>)` | 批量更新 |
| `updateBatch(Collection<T>, int size)` | 指定批次大小 |
| `delete(field, value)` | 按字段删除 |
| `deleteBatch(field, values)` | 批量删除 |

特殊：`insertBatch` 对 SQL Server 自动回退为逐条插入（因为无法获取自增 ID）。

## LambdaQueryWrapperX 条件方法

| 方法 | SQL 效果 |
|---|---|
| `likeIfPresent(fn, val)` | `LIKE %val%`（val 为空时忽略） |
| `eqIfPresent(fn, val)` | `= val` |
| `neIfPresent(fn, val)` | `!= val` |
| `inIfPresent(fn, vals)` | `IN (vals)` |
| `betweenIfPresent(fn, from, to)` | `BETWEEN from AND to` |
| `geIfPresent(fn, val)` | `>= val` |
| `leIfPresent(fn, val)` | `<= val` |
| `gtIfPresent(fn, val)` | `> val` |
| `ltIfPresent(fn, val)` | `< val` |
| `orderByAsc(fn)` | `ORDER BY fn ASC` |
| `orderByDesc(fn)` | `ORDER BY fn DESC` |

## MPJ 连表查询

```java
// 使用 MPJLambdaWrapper 进行连表查询
default PageResult<{Domain}RespVO> selectJoinPage(PageParam pageParam) {
    return selectJoinPage(pageParam, {Domain}RespVO.class,
            new MPJLambdaWrapper<{Domain}DO>()
                .selectAll({Domain}DO.class)
                .selectAs(AnotherDO::getName, {Domain}RespVO::getAnotherName)
                .leftJoin(AnotherDO.class, AnotherDO::getId, {Domain}DO::getAnotherId)
                .eq({Domain}DO::getStatus, 0));
}
```

## JSON 字段处理

使用 `JacksonTypeHandler` 存储 JSON 到数据库字段：

```java
@TableName(value = "system_role", autoResultMap = true) // 必须开启 autoResultMap！
@Data
public class RoleDO extends TenantBaseDO {
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Long> dataScopeDeptIds; // JSON 数组 → Java Set
}
```

数据库字段类型应为 `json`（MySQL 5.7+）或 `varchar`。

## 多数据库序列

```java
// @KeySequence 为 Oracle / PostgreSQL / Kingbase / DM 提供主键生成
// MySQL / MariaDB 下自动忽略（使用自增 ID）
@KeySequence("{module}_{entity}_seq")
```

## 常见错误

- 实体未继承 `BaseDO`，缺少 `createTime` / `deleted` 等必要字段
- 多租户表错误使用 `BaseDO` 而非 `TenantBaseDO`，导致租户数据泄露
- `@TableName` 未设置 `autoResultMap = true` 且使用了 `JacksonTypeHandler` — JSON 字段无法反序列化
- Mapper 中定义抽象方法而非 default 方法 — 导致需要额外的 Impl 类
- 手动编写 WHERE `deleted=0` — `@TableLogic` 自动处理
- 分页查询手动构造 `Page` 对象而非使用 `selectPage(pageReqVO, wrapper)` 模式
- 使用 `selectOne()` 但在并发场景下可能查到多条 — 使用 `selectFirstOne()` 替代
