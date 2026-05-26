---
name: starter-excel
description: FastExcel-based import/export with dictionary conversion, auto column width, dropdown lists, annotation-driven converters (dict, area, JSON, money), and DictFrameworkUtils
type: project
---

# develop-spring-boot-starter-excel

## Overview

Excel 导入导出基础模块。基于 FastExcel 1.3.0（Alibaba EasyExcel 的增强 Fork），提供便捷的 `ExcelUtils` 封装、字典/行政区域/JSON/金额等注解驱动的数据转换器。字典框架和 Excel 功能整合在同一 starter 中。所有 Bean 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 **2 个自动配置类**。

**Package base:** `com.develop.mvp.pk.framework.dict` | `com.develop.mvp.pk.framework.excel`

## AutoConfiguration Registration

```
# develop-spring-boot-starter-excel AutoConfiguration.imports
com.develop.mvp.pk.framework.dict.config.DevelopDictRpcAutoConfiguration
com.develop.mvp.pk.framework.dict.config.DevelopDictAutoConfiguration
```

Note: The dict auto-configurations are part of `develop-spring-boot-starter-excel` module, not a separate module. Dictionary framework and Excel features are co-located.

## Core Components

### 1. ExcelUtils Utility Class

**Location:** `com.develop.mvp.pk.framework.excel.core.util.ExcelUtils`

```java
// Export to HTTP response
ExcelUtils.write(HttpServletResponse response, String filename,
                 String sheetName, Class<T> head, List<T> data) throws IOException;

// Import (read)
List<T> list = ExcelUtils.read(MultipartFile file, Class<T> head) throws IOException;
```

Features:
- **Auto-width**: `ColumnWidthMatchStyleStrategy` — intelligent column width based on header content
- **Dropdown list**: `SelectSheetWriteHandler` — generates Excel dropdown lists via `@ExcelSelected` annotation
- **Long text truncation**: `LongStringConverter` — auto-truncates cells exceeding 32767 characters
- **Error handling**: import failures report row numbers and column names

### 2. Dictionary Conversion

**@DictFormat("dict_type")**: marks a field with its dictionary type:
- Export: value -> label (e.g., `0` -> `"正常"`)
- Import: label -> value (e.g., `"正常"` -> `0`)

**DictConvert**: implements `Converter` interface:
- Uses `DictFrameworkUtils` with 1-minute Guava cache for dictionary data
- Auto-registered as Excel converter for any field annotated with `@DictFormat`

### 3. DictFrameworkUtils

**Location:** `com.develop.mvp.pk.framework.dict.core.DictFrameworkUtils`

```java
String parseDictDataLabel(String dictType, Integer value);     // value -> label
String parseDictDataValue(String dictType, String label);      // label -> value
void clearCache();                                              // Clear 1-min cache
```

- Initialized by `DevelopDictAutoConfiguration` via `DictDataCommonApi` Feign client
- Cache TTL: 1 minute (Guava Cache)
- Used by: `DictConvert` (Excel), `DictDataCommonApi` consumers

### 4. DevelopDictAutoConfiguration

**Location:** `com.develop.mvp.pk.framework.dict.config`

- Initializes `DictFrameworkUtils` with `DictDataCommonApi` Feign client
- Conditional: not explicitly property-gated (default enabled)

### 5. DevelopDictRpcAutoConfiguration

- Feign-based dict data RPC support
- Excluded in monolithic mode via `spring.autoconfigure.exclude`

### 6. Additional Converters

| Converter | Description | Package |
|---|---|---|
| `AreaConvert` | Administrative area code <-> name | `com.develop.mvp.pk.framework.excel.core.convert` |
| `JsonConvert` | Java object <-> JSON string | `com.develop.mvp.pk.framework.excel.core.convert` |
| `MoneyConvert` | Yuan <-> Fen (amount conversion) | `com.develop.mvp.pk.framework.excel.core.convert` |

### 7. @ExcelSelected Annotation

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelSelected {
    String[] value() default {};         // Fixed dropdown values
    Class<? extends DynamicSelect> source() default DynamicSelect.class; // Dynamic data source
}
```

- Generates dropdown list in exported Excel
- Supports fixed values or dynamic data source classes

## Error Handling and Validation

**Import validation flow:**
1. Parse Excel rows via FastExcel
2. Apply Hibernate Validator annotations on VO fields
3. Convert dictionary labels to values (via DictConvert)
4. Return list of validated POJOs
5. Service layer performs business validation

**Export error handling:**
- Response flush after header write — if an error occurs mid-export, the client receives a partial file
- Export filename should be URL-encoded (`URLEncoder.encode(filename, "UTF-8")`) for Chinese character support
- Large datasets should be streamed with `OutputStream` to avoid OOM

## Configuration Properties

```yaml
develop:
  excel:
    auto-column-width: true            # Auto-fit column width (default: true)
    max-import-count: 5000             # Max rows per import (default: 5000)
```

Note: These are custom properties parsed by the ExcelUtils directly, not via a `@ConfigurationProperties` class.

## Code Examples

```java
// Export
@GetMapping("/export-excel")
public void exportExcel(HttpServletResponse response,
                        @Validated UserExportReqVO reqVO) throws IOException {
    List<UserDO> list = userService.getList(reqVO);
    List<UserExcelVO> data = UserConvert.INSTANCE.convert(list);
    ExcelUtils.write(response, "用户数据.xlsx", "用户列表", UserExcelVO.class, data);
}

// Import
@PostMapping("/import-excel")
public CommonResult<Integer> importExcel(MultipartFile file) throws IOException {
    List<UserExcelVO> list = ExcelUtils.read(file, UserExcelVO.class);
    return success(userService.importUserList(list));
}

// Dictionary conversion entity
@Data
public class UserExcelVO {
    @ExcelProperty("用户名")
    private String username;

    @DictFormat("system_user_status")   // export: 0->"正常", import: "正常"->0
    @ExcelProperty("状态")
    private Integer status;

    @ExcelSelected(value = {"正常", "停用"})
    @ExcelProperty("状态（下拉）")
    private String statusText;
}
```

## Performance Guidance

| Scenario | Recommended Approach |
|---|---|
| < 5000 rows import | Direct `ExcelUtils.read()` + batch insert |
| 5000-50000 rows import | Split into batches of 1000 + `@Transactional` per batch |
| > 50000 rows import | Consider async import with progress tracking |
| < 10000 rows export | Direct `ExcelUtils.write()` with in-memory list |
| > 10000 rows export | Use streaming write with `OutputStream` + page-based query |
| Frequent dictionary conversion | Use `DictFrameworkUtils.clearCache()` after dict data changes |

## 注意事项

- FastExcel 1.3.0 兼容 EasyExcel 3.x API，但 Maven 坐标需使用 `com.alibaba:fastexcel` 而非 `com.alibaba:easyexcel`
- 字典缓存 1 分钟过期，新增字典数据后需等待缓存刷新或手动调用 `DictFrameworkUtils.clearCache()` 立即生效
- 自动列宽对合并单元格支持有限，复杂表头建议手动预设宽度
- `SelectSheetWriteHandler` 的下拉列表最多显示 255 字符，超出的内容会被截断
- 单次导入行数建议不超过 `develop.excel.max-import-count`（默认 5000），超限建议分批导入 + 前端分批上传
- 导出文件名为中文时需要 URL 编码（`URLEncoder.encode(filename, "UTF-8")`），否则浏览器或 WPS 可能显示乱码
- 字典相关的自动配置（`DevelopDictAutoConfiguration`）位于 Excel starter 模块中，而非独立的 starter；依赖 `DictDataCommonApi` Feign 客户端
- 金额转换器（`MoneyConvert`）将数据库存储的"分"（整数）转换为 Excel 展示的"元"（小数），注意数值精度
