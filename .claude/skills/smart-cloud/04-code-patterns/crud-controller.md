---
name: crud-controller
description: Standard CRUD controller patterns including tree, batch ops, export/import, simple-list endpoints
type: project
---

# CRUD Controller 模式

## 概述

为标准业务模块生成 REST 端点。所有请求返回 `CommonResult<T>`。Controller 只负责编排，不写业务逻辑。适用于管理后台（admin）接口。

## 标准 CRUD 模板（5 个核心端点）

```java
package com.develop.mvp.pk.module.{module}.controller.admin.{domain};

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo.{Domain}PageReqVO;
import com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo.{Domain}RespVO;
import com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo.{Domain}SaveReqVO;
import com.develop.mvp.pk.module.{module}.dal.dataobject.{domain}.{Domain}DO;
import com.develop.mvp.pk.module.{module}.service.{domain}.{Domain}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - {领域名}")
@RestController
@RequestMapping("/{module}/{domain}")
@Validated
public class {Domain}Controller {

    @Resource
    private {Domain}Service {domain}Service;

    @PostMapping("/create")
    @Operation(summary = "创建{领域名}")
    @PreAuthorize("@ss.hasPermission('{module}:{domain}:create')")
    public CommonResult<Long> create{Domain}(@Valid @RequestBody {Domain}SaveReqVO createReqVO) {
        return success({domain}Service.create{Domain}(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新{领域名}")
    @PreAuthorize("@ss.hasPermission('{module}:{domain}:update')")
    public CommonResult<Boolean> update{Domain}(@Valid @RequestBody {Domain}SaveReqVO updateReqVO) {
        {domain}Service.update{Domain}(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除{领域名}")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('{module}:{domain}:delete')")
    public CommonResult<Boolean> delete{Domain}(@RequestParam("id") Long id) {
        {domain}Service.delete{Domain}(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除{领域名}")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('{module}:{domain}:delete')")
    public CommonResult<Boolean> delete{Domain}List(@RequestParam("ids") List<Long> ids) {
        {domain}Service.delete{Domain}List(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得{领域名}")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('{module}:{domain}:query')")
    public CommonResult<{Domain}RespVO> get{Domain}(@RequestParam("id") Long id) {
        {Domain}DO domainDO = {domain}Service.get{Domain}(id);
        return success(BeanUtils.toBean(domainDO, {Domain}RespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得{领域名}分页")
    @PreAuthorize("@ss.hasPermission('{module}:{domain}:query')")
    public CommonResult<PageResult<{Domain}RespVO>> get{Domain}Page(@Valid {Domain}PageReqVO pageReqVO) {
        PageResult<{Domain}DO> pageResult = {domain}Service.get{Domain}Page(pageReqVO);
        return success(BeanUtils.toBean(pageResult, {Domain}RespVO.class));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获取{领域名}精简信息列表", description = "用于前端的下拉选项")
    public CommonResult<List<{Domain}SimpleRespVO>> getSimple{Domain}List() {
        List<{Domain}DO> list = {domain}Service.get{Domain}List();
        return success(BeanUtils.toBean(list, {Domain}SimpleRespVO.class));
    }
}
```

## 树形 Controller 模板（扩展）

适用于部门、菜单、分类等树形结构：

```java
// ========== 树形结构扩展 ==========

@GetMapping("/list")
@Operation(summary = "获取{领域名}列表", description = "用于【{领域名}管理】界面 - 树形展示")
@PreAuthorize("@ss.hasPermission('{module}:{domain}:query')")
public CommonResult<List<{Domain}RespVO>> get{ Domain}List({Domain}ListReqVO reqVO) {
    List<{Domain}DO> list = {domain}Service.get{ Domain}List(reqVO);
    list.sort(Comparator.comparing({Domain}DO::getSort));
    return success(BeanUtils.toBean(list, {Domain}RespVO.class));
}
```

## 导入导出端点

### Excel 导出

```java
@GetMapping("/export-excel")
@Operation(summary = "导出{领域名} Excel")
@ApiAccessLog(operateType = EXPORT) // 记录操作日志类型为"导出"
@PreAuthorize("@ss.hasPermission('{module}:{domain}:export')")
public void export{Domain}Excel(@Validated {Domain}PageReqVO exportReqVO,
                                HttpServletResponse response) throws IOException {
    exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE); // 不分页，查询全部
    List<{Domain}DO> list = {domain}Service.get{ Domain}Page(exportReqVO).getList();
    List<{Domain}ExcelVO> data = BeanUtils.toBean(list, {Domain}ExcelVO.class);
    ExcelUtils.write(response, "{领域名}.xls", "数据", {Domain}ExcelVO.class, data);
}
```

### Excel 导入 + 模板下载

```java
@PostMapping("/import")
@Operation(summary = "导入{领域名}")
@PreAuthorize("@ss.hasPermission('{module}:{domain}:import')")
public CommonResult<Boolean> import{Domain}(@RequestParam("file") MultipartFile file) throws IOException {
    List<{Domain}ExcelVO> data = ExcelUtils.read(file, {Domain}ExcelVO.class);
    {domain}Service.import{ Domain}(data);
    return success(true);
}

@PostMapping("/import-template")
@Operation(summary = "下载{领域名}导入模板")
@PreAuthorize("@ss.hasPermission('{module}:{domain}:import')")
public void importTemplate(HttpServletResponse response) throws IOException {
    ExcelUtils.write(response, "{领域名}导入模板.xls", "数据", {Domain}ExcelVO.class, null);
}
```

## VO 文件命名规范

| 文件 | 用途 |
|---|---|
| `{Domain}SaveReqVO` | 创建和更新共用请求体（id 为空表示创建，非空表示更新） |
| `{Domain}PageReqVO` | 分页查询请求（extends `PageParam`） |
| `{Domain}RespVO` | 详情/列表响应体 |
| `{Domain}SimpleRespVO` | 精简响应体（用于下拉选择等场景） |
| `{Domain}ExcelVO` | Excel 导入导出 VO |
| `{Domain}ListReqVO` | 列表查询请求（非分页，用于树形等结构） |

## 关键点

1. **`@Tag(name = "管理后台 - XXX")`** — Swagger 分组标签，管理后台用 `管理后台 - `，用户端用 `用户 APP - `
2. **`@Validated`** 类级别 + **`@Valid`** 在 `@RequestBody` 参数上
3. **`@PreAuthorize("@ss.hasPermission(...)")`** — action 可选：`create`, `update`, `delete`, `query`, `export`, `import`
4. **批量删除**使用 `@DeleteMapping("/delete-list")` + `@RequestParam("ids") List<Long> ids`
5. **精简列表**（下拉选项）使用 `@GetMapping({"/list-all-simple", "/simple-list"})` — 两个路径兼容
6. **导出使用 `@ApiAccessLog(operateType = EXPORT)`** — 记录操作日志
7. **`BeanUtils.toBean()`** — DO→VO 转换，支持单个、List、`PageResult`
8. **`@Resource`** 注入 Service（Jakarta EE 标准）
9. **`@RequestParam("id")`** 显式指定参数名，避免编译时丢失

## 常见错误

- 直接在 Controller 中写业务逻辑 — Controller 只负责编排，所有业务逻辑在 Service 层
- 直接返回 DO 而非 VO — 必须通过 `BeanUtils.toBean` 转换
- 使用 `@Autowired` 而非 `@Resource` — 项目统一使用 `@Resource`
- 忘记在 DELETE 方法上加 `@Parameter` 描述 Swagger 参数
- `@Valid` 和 `@Validated` 混用位置不对
- 导出方法未设置 `pageSize = PageParam.PAGE_SIZE_NONE` — 导致只导出第一页数据
- 导出方法未加 `@ApiAccessLog(operateType = EXPORT)` — 操作日志中未记录导出行为
