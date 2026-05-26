---
name: dto-vo-conversion
description: Object conversion — BeanUtils (Hutool-based) for simple, MapStruct for complex, @Builder, nested, @Context usage
type: project
---

# DTO / VO 对象转换模式

## 概述

三层对象：**VO**（Controller 展示）、**DO**（DAL 持久化）、**DTO**（跨模块 RPC）。转换使用两套方案：`BeanUtils.toBean()`（简单转换，基于 Hutool BeanUtil）和 MapStruct `Convert`（复杂/批量转换，编译期生成）。

## 转换方案对比

| 方案 | 适用场景 | 原理 |
|---|---|---|
| `BeanUtils.toBean()` | 字段名一致的单对象/列表/分页 | Hutool BeanUtil 反射 |
| MapStruct `Convert` | 字段名不同、聚合逻辑、大批量 | 编译期生成代码，零反射 |

## 方案一：BeanUtils.toBean()（优先使用）

```java
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;

// DO → VO（Controller 层）
{Domain}RespVO respVO = BeanUtils.toBean(entity, {Domain}RespVO.class);

// VO → DO（Service 层）
{Domain}DO entity = BeanUtils.toBean(createReqVO, {Domain}DO.class);

// 列表转换
List<{Domain}RespVO> voList = BeanUtils.toBean(doList, {Domain}RespVO.class);

// 分页转换
PageResult<{Domain}RespVO> pageResult = BeanUtils.toBean(doPageResult, {Domain}RespVO.class);

// 带 peek 回调的转换（转换后额外处理）
BeanUtils.toBean(doList, {Domain}RespVO.class, vo -> vo.setExtraInfo("xxx"));
```

`BeanUtils` 基于 `cn.hutool.core.bean.BeanUtil`，支持：
- 同名同类型字段自动映射
- 支持 `List<T>` 和 `PageResult<T>` 批量转换
- 带 `Consumer<T> peek` 回调的扩展方法

## 方案二：MapStruct Convert（复杂转换）

### 1. 定义 Convert 接口

```java
package com.develop.mvp.pk.module.{module}.convert.{domain};

import com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo.{Domain}RespVO;
import com.develop.mvp.pk.module.{module}.dal.dataobject.{domain}.{Domain}DO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface {Domain}Convert {

    // 单例模式（非 Spring 注入）
    {Domain}Convert INSTANCE = Mappers.getMapper({Domain}Convert.class);

    // ===== 基础映射（字段名一致，自动匹配） =====
    {Domain}RespVO convert({Domain}DO source);

    // ===== 列表映射 =====
    List<{Domain}RespVO> convertList(List<{Domain}DO> list);

    // ===== 自定义字段映射 =====
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "statusText", expression = "java(convertStatus(source.getStatus()))")
    {Domain}RespVO convertWithExtra({Domain}DO source);

    // ===== 接口默认方法 =====
    default String convertStatus(Integer status) {
        if (status == null) return "未知";
        return status.equals(0) ? "启用" : "禁用";
    }
}
```

### 2. @Builder 兼容

Lombok `@Builder` 与 MapStruct 兼容，需在 `maven-compiler-plugin` 中配置：

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-mapstruct-binding</artifactId>
    </path>
</annotationProcessorPaths>
```

注意顺序：Lombok 必须在 MapStruct 之前。

### 3. @Context 传递上下文

```java
import org.mapstruct.Context;

@Mapper
public interface {Domain}Convert {
    @Mapping(target = "createdByName", source = "creator")
    {Domain}RespVO convert(@MappingTarget {Domain}RespVO vo, {Domain}DO source,
                           @Context UserApi userApi);

    default void afterMapping(@Context UserApi userApi, @MappingTarget {Domain}RespVO vo) {
        // 通过上下文做额外处理
    }
}
```

### 4. 嵌套对象转换

```java
@Mapping(target = "deptName", source = "dept.name")           // 一级嵌套
@Mapping(target = "parentCategoryName", source = "parent.category.name") // 多级嵌套
{Domain}RespVO convert({Domain}DO source);
```

### 5. 更新已有对象

```java
@Mapper
public interface {Domain}Convert {
    // void 方法，更新已有目标对象
    @Mapping(target = "id", ignore = true) // 忽略主键
    void update(@MappingTarget {Domain}DO target, {Domain}SaveReqVO source);
}
```

## Maven 依赖

```xml
<!-- BOM 已统一管理版本，子模块只需声明 -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok-mapstruct-binding</artifactId>
</dependency>
```

## 使用场景选择

| 场景 | 方案 |
|---|---|
| VO/DO/DTO 字段名相同 | `BeanUtils.toBean()` |
| 少量数据（< 1000 条）的列表转换 | `BeanUtils.toBean()` |
| 分页结果转换 | `BeanUtils.toBean()` |
| 字段名不同，需要 `@Mapping` | MapStruct |
| 从关联对象聚合字段 | MapStruct |
| 大批量数据（> 1000 条）转换 | MapStruct |
| 类型转换（Integer→String） | MapStruct `expression` |
| 更新已有对象 | MapStruct `@MappingTarget` |

## 常见错误

- 在循环中使用 `BeanUtils.toBean()` 大批量转换 — 用 MapStruct 或在外部批量转换
- 未添加 `lombok-mapstruct-binding` — 生成的映射代码为空
- MapStruct 使用 `componentModel = "spring"` — 项目统一用 `Mappers.getMapper()` 模式
- VO 和 DTO 混用 — VO 是 Controller 返回，DTO 是跨模块 RPC 契约
- `@Mapping` 的 `source`/`target` 字段名拼写错误 — 编译期报错
- MapStruct annotationProcessor 顺序在 Lombok 之前导致编译失败
