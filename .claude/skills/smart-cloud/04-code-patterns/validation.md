---
name: validation
description: Three-layer validation — Jakarta Bean Validation, @Validated/@Valid, custom InEnum/Mobile validators, Service business checks
type: project
---

# 校验模式

## 概述

三层校验体系：**VO 字段注解**（声明式约束）→ **Controller 触发**（`@Validated` + `@Valid`）→ **Service 业务校验**（唯一性、关联性等）。

框架自定义校验器路径：
- `@InEnum` / `InEnumValidator`: `com.develop.mvp.pk.framework.common.validation.InEnum`
- `@Mobile`: `com.develop.mvp.pk.framework.common.validation.Mobile`

## 第一层：VO 字段校验

```java
package com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.validation.InEnum;
import com.develop.mvp.pk.framework.common.validation.Mobile;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class {Domain}SaveReqVO {

    // create 时为 null，update 时必填
    private Long id;

    @NotBlank(message = "名称不能为空")
    @Size(min = 2, max = 50, message = "名称长度在 2-50 个字符之间")
    private String name;

    @NotBlank(message = "编码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "编码只能包含字母、数字和下划线")
    private String code;

    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序值最小为 0")
    @Max(value = 99999, message = "排序值最大为 99999")
    private Integer sort;

    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Mobile(message = "手机号格式不正确")
    private String mobile;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    @Future(message = "过期时间必须是未来时间")
    private LocalDateTime expireTime;
}
```

### 分页请求 VO

```java
package com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo;

import com.develop.mvp.pk.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class {Domain}PageReqVO extends PageParam {

    private String name;

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime[] createTime; // 数组形式：[startTime, endTime]
}
```

`PageParam` 内置校验：`pageNo`（最小 1）、`pageSize`（最小 1，最大 200，默认 10）。

## 第二层：Controller 触发校验

```java
@Tag(name = "管理后台 - {领域名}")
@RestController
@RequestMapping("/{module}/{domain}")
@Validated // 类级别：启用方法参数校验
public class {Domain}Controller {

    @PostMapping("/create")
    @Operation(summary = "创建{领域名}")
    public CommonResult<Long> create{Domain}(
            @Valid @RequestBody {Domain}SaveReqVO createReqVO) { // @Valid 触发字段校验
        return success({domain}Service.create{Domain}(createReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得{领域名}")
    public CommonResult<{Domain}RespVO> get{Domain}(
            @NotNull(message = "编号不能为空") // 参数级校验
            @RequestParam("id") Long id) {
        return success(BeanUtils.toBean({domain}Service.get{Domain}(id), {Domain}RespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得{领域名}分页")
    public CommonResult<PageResult<{Domain}RespVO>> get{Domain}Page(
            @Valid {Domain}PageReqVO pageReqVO) { // @Valid 触发分页参数校验
        return success(BeanUtils.toBean({domain}Service.get{Domain}Page(pageReqVO), {Domain}RespVO.class));
    }
}
```

## 第三层：Service 业务校验

```java
@Service
@Validated
public class {Domain}ServiceImpl implements {Domain}Service {

    @Resource
    private {Domain}Mapper {domain}Mapper;

    @Override
    public Long create{Domain}({Domain}SaveReqVO createReqVO) {
        // VO 字段校验已由 Jakarta Validation 完成
        // 业务规则校验
        validate{Domain}ForCreate(createReqVO);
        {Domain}DO entity = BeanUtils.toBean(createReqVO, {Domain}DO.class);
        {domain}Mapper.insert(entity);
        return entity.getId();
    }

    private void validate{Domain}ForCreate({Domain}SaveReqVO vo) {
        // 名称唯一性检查
        {Domain}DO exist = {domain}Mapper.selectByName(vo.getName());
        if (exist != null) {
            throw ServiceExceptionUtil.exception(
                    ErrorCodeConstants.{DOMAIN}_NAME_DUPLICATE, vo.getName());
        }
        // 父级存在性检查
        if (vo.getParentId() != null && vo.getParentId() > 0) {
            if ({domain}Mapper.selectById(vo.getParentId()) == null) {
                throw ServiceExceptionUtil.exception(
                        ErrorCodeConstants.{DOMAIN}_PARENT_NOT_EXISTS);
            }
        }
        // 枚举值合法性
        CommonStatusEnum.validate(vo.getStatus());
    }
}
```

## @InEnum 自定义校验器

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = InEnumValidator.class)
public @interface InEnum {

    /** 枚举类（必须实现 ArrayValuable 接口） */
    Class<? extends ArrayValuable<?>> value();

    String message() default "参数值不在合法范围内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

枚举需实现 `ArrayValuable` 接口：

```java
public interface ArrayValuable<T> {
    T[] array();
}

public enum CommonStatusEnum implements ArrayValuable<Integer> {
    ENABLE(0, "启用"),
    DISABLE(1, "禁用");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(CommonStatusEnum::getStatus).toArray(Integer[]::new);

    // getter + setter + array() 实现
}
```

## 自定义校验器创建指南

```java
// 1. 定义注解
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = XxxValidator.class)
public @interface Xxx {}

// 2. 实现 ConstraintValidator
public class XxxValidator implements ConstraintValidator<Xxx, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 自定义校验逻辑
        return value != null && value.matches("^...$");
    }
}
```

## 异常映射

| Controller 层异常 | HTTP 状态 | 响应体 code |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | 400 |
| `BindException` | 400 | 400 |
| `ConstraintViolationException` | 400 | 400 |
| `HttpMessageNotReadableException` | 400 | 400 |
| `AccessDeniedException` | 403 | 403 |
| `NoHandlerFoundException` | 404 | 404 |
| `ServiceException` | 200 | 业务错误码 |

## 关键点

1. **类级 `@Validated`** 启用 Spring Method Validation，使 `@RequestParam` 约束生效
2. **`@Valid`** 在 `@RequestBody` 参数上触发 DTO 字段级约束
3. **`@InEnum`** 要求枚举实现 `ArrayValuable` 接口
4. **`@Mobile`** 校验中国大陆手机号格式
5. **`@DateTimeFormat`** 配合 `LocalDateTime[]` 数组实现时间范围查询
6. **Service 层 `@Validated`** 使方法参数上的校验注解生效

## 常见错误

- 类上只加 `@Valid` 没加 `@Validated` — `@RequestParam` 约束不生效
- 自定义校验注解未指定 `validatedBy` — 校验器不执行
- 枚举未实现 `ArrayValuable` 就用 `@InEnum` — 运行时抛出 IllegalArgumentException
- 在 Controller 中做业务校验 — 移至 Service 层
- `message` 中使用 `{value}` 占位符但格式不正确
