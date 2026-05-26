---
name: excel-import-export
description: Excel import/export using @DictFormat, @ExcelProperty, ExcelUtils with error handling, progress tracking, large file handling
type: project
---

# Excel 导入导出模式

## 概述

使用 `develop-spring-boot-starter-excel`（基于 FastExcel，EasyExcel 的活跃维护 fork）实现 Excel 导入导出。`@DictFormat` 标注字典字段自动字典标签/值转换，`@ExcelProperty` 定义列头，`ExcelUtils` 工具类完成读写。

## 1. 定义 Excel VO

```java
package com.develop.mvp.pk.module.{module}.controller.admin.{domain}.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.develop.mvp.pk.framework.excel.core.annotations.DictFormat;
import com.develop.mvp.pk.framework.excel.core.convert.DictConvert;
import com.develop.mvp.pk.framework.excel.core.convert.MoneyConvert;
import com.develop.mvp.pk.framework.excel.core.convert.JsonConvert;
import com.develop.mvp.pk.framework.excel.core.convert.AreaConvert;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ExcelIgnoreUnannotated // 未标注 @ExcelProperty 的字段自动忽略（避免敏感字段导出）
public class {Domain}ExcelVO {

    @ExcelProperty("编号")
    private Long id;

    @ExcelProperty("名称")
    private String name;

    @DictFormat("system_user_status") // 字典类型 key
    @ExcelProperty(value = "状态", converter = DictConvert.class) // 自动转换 label ↔ value
    private Integer status;
    // Excel 显示 "启用" / "禁用" → Java 接收 0 / 1

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty(value = "金额（元）", converter = MoneyConvert.class)
    private BigDecimal amount;

    @ExcelProperty(value = "地区", converter = AreaConvert.class)
    private String area;

    @ExcelProperty(value = "扩展信息", converter = JsonConvert.class)
    private String extraInfo;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
```

## 2. 导出端点

```java
@GetMapping("/export-excel")
@Operation(summary = "导出{领域名} Excel")
@ApiAccessLog(operateType = EXPORT) // 重要：记录操作日志类型为导出
@PreAuthorize("@ss.hasPermission('{module}:{domain}:export')")
public void export{Domain}Excel(@Validated {Domain}PageReqVO reqVO,
                               HttpServletResponse response) throws IOException {
    // 不分页，查全部数据
    reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    List<{Domain}DO> list = {domain}Service.get{ Domain}Page(reqVO).getList();
    // DO → Excel VO
    List<{Domain}ExcelVO> data = BeanUtils.toBean(list, {Domain}ExcelVO.class);
    // 写入响应流
    ExcelUtils.write(response, "{领域名}.xls", "数据", {Domain}ExcelVO.class, data);
}
```

## 3. 导入端点 + 模板下载

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

## 4. Service 层导入处理

```java
@Override
@Transactional(rollbackFor = Exception.class) // 原子性导入
public void import{Domain}(List<{Domain}ExcelVO> importList) {
    // 1. 批量前置校验（一次性收集所有错误）
    List<String> errorMessages = new ArrayList<>();
    for (int i = 0; i < importList.size(); i++) {
        try {
            validateImportRow(importList.get(i));
        } catch (ServiceException e) {
            errorMessages.add("第" + (i + 2) + "行：" + e.getMessage());
        }
    }
    if (!errorMessages.isEmpty()) {
        throw ServiceExceptionUtil.exception(
                ErrorCodeConstants.{DOMAIN}_IMPORT_ERROR,
                String.join("; ", errorMessages));
    }

    // 2. 批量转换并插入
    List<{Domain}DO> createList = BeanUtils.toBean(importList, {Domain}DO.class);
    {domain}Mapper.insertBatch(createList);
}

private void validateImportRow({Domain}ExcelVO row) {
    if (StrUtil.isBlank(row.getName())) {
        throw ServiceExceptionUtil.exception({DOMAIN}_IMPORT_NAME_EMPTY);
    }
    if ({domain}Mapper.selectByName(row.getName()) != null) {
        throw ServiceExceptionUtil.exception({DOMAIN}_NAME_DUPLICATE, row.getName());
    }
}
```

## 5. 内置转换器

| 转换器 | 注解 | 功能 |
|---|---|---|
| `DictConvert` | `@DictFormat("dict_type")` | 字典标签 ↔ 字典值 |
| `AreaConvert` | 无 | 地区编码 ↔ 地区名称 |
| `JsonConvert` | 无 | JSON 字符串 ↔ 格式化文本 |
| `MoneyConvert` | 无 | 元 ↔ 分（数据库存分为单位） |

## 6. 大文件/高性能处理

```java
// 分批读取大文件（超过 5000 行）
EasyExcel.read(inputStream, {Domain}ExcelVO.class, new AnalysisEventListener<{Domain}ExcelVO>() {
    private static final int BATCH_SIZE = 1000;
    private List<{Domain}ExcelVO> batch = new ArrayList<>();

    @Override
    public void invoke({Domain}ExcelVO data, AnalysisContext context) {
        batch.add(data);
        if (batch.size() >= BATCH_SIZE) {
            saveBatch(batch);
            batch.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!batch.isEmpty()) {
            saveBatch(batch);
        }
    }
}).sheet().doRead();
```

## 关键点

1. **`@DictFormat` + `converter = DictConvert.class`** — 必须同时使用
2. **`@ExcelIgnoreUnannotated`** — 未标注字段不导出，避免敏感字段泄露
3. **导入使用 `@Transactional(rollbackFor = Exception.class)`** — 原子性导入
4. **批量前置校验** — 收集所有错误行一次性返回，而非逐行中断
5. **导出加 `@ApiAccessLog(operateType = EXPORT)`** — 记录操作日志
6. **导出前设置 `pageSize = PageParam.PAGE_SIZE_NONE`** — 不分页查询全部
7. **大文件（>5000 行）使用 EasyExcel 监听器分批读取**
8. **模板下载 = data 传 null 的导出**

## 常见错误

- 未加 `@ExcelIgnoreUnannotated` — 敏感字段（如密码）被导出
- `@DictFormat` 和 `DictConvert` 只用了其中一个
- 导入未加 `@Transactional` — 部分入库失败无法回滚
- 批量导入大量数据未做分批处理 — 内存溢出或事务超时
- 导出方法未设置 `PAGE_SIZE_NONE` — 只导出了第一页
- 导出未使用 `@ApiAccessLog(operateType = EXPORT)` — 日志未记录导出行为
- 未做单行校验的异常捕获 — 一条数据错误导致整个导入中断
