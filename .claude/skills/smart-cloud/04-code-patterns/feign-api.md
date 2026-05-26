---
name: feign-api
description: Feign RPC interface pattern — per-module service names, AutoTransable, timeout config, fallback, exception handling
type: project
---

# Feign RPC 模式

## 概述

跨模块 RPC 调用。每个业务模块的 `{module}-api` 模块定义 `{Domain}Api` Feign 接口，`{module}-server` 模块提供 `{Domain}ApiImpl` REST 实现。单体模式下通过 `@Primary` 去掉 HTTP 转为本地调用。

**关键区别**：每个模块的 `ApiConstants.NAME` 是独立的服务名（如 `"system-server"`、`"infra-server"`），不是统一的 `"develop-server"`。

## ApiConstants 定义（每个模块一个）

```java
// develop-module-system/develop-module-system-api 中定义
package com.develop.mvp.pk.module.system.enums;

import com.develop.mvp.pk.framework.common.enums.RpcConstants;

public class ApiConstants {
    /** 服务名，必须与 spring.application.name 一致 */
    public static final String NAME = "system-server";

    /** RPC 前缀 */
    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/system";

    public static final String VERSION = "1.0.0";
}
```

## 步骤 1：API 接口（定义在 `-api` 模块）

```java
package com.develop.mvp.pk.module.{module}.api.{domain};

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.{module}.api.{domain}.dto.{Domain}ReqDTO;
import com.develop.mvp.pk.module.{module}.api.{domain}.dto.{Domain}RespDTO;
import com.develop.mvp.pk.module.{module}.enums.ApiConstants;
import com.fhs.core.trans.anno.AutoTrans;
import com.fhs.trans.service.AutoTransable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.develop.mvp.pk.module.{module}.api.{domain}.{Domain}Api.PREFIX;

@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - {领域}")
@AutoTrans(namespace = PREFIX, fields = {"name"}) // Easy-Trans 自动翻译
public interface {Domain}Api {

    String PREFIX = ApiConstants.PREFIX + "/{domain}";

    @GetMapping(PREFIX + "/get")
    @Operation(summary = "获得{领域}")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    CommonResult<{Domain}RespDTO> get{Domain}(@RequestParam("id") Long id);

    @GetMapping(PREFIX + "/list")
    @Operation(summary = "获得{领域}列表")
    @Parameter(name = "ids", description = "编号列表", required = true)
    CommonResult<List<{Domain}RespDTO>> get{Domain}List(@RequestParam("ids") Collection<Long> ids);

    @PostMapping(PREFIX + "/create")
    @Operation(summary = "创建{领域}")
    CommonResult<Long> create{Domain}(@RequestBody {Domain}ReqDTO reqDTO);

    @GetMapping(PREFIX + "/valid")
    @Operation(summary = "校验{领域}是否有效")
    CommonResult<Boolean> validate{Domain}List(@RequestParam("ids") Collection<Long> ids);

    /** default 方法提供便捷调用 */
    default Map<Long, {Domain}RespDTO> get{Domain}Map(Collection<Long> ids) {
        List<{Domain}RespDTO> list = get{Domain}List(ids).getCheckedData();
        // return CollectionUtils.convertMap(list, {Domain}RespDTO::getId);
        return null;
    }
}
```

### DTO（定义在 `-api` 模块）

```java
package com.develop.mvp.pk.module.{module}.api.{domain}.dto;

import lombok.Data;

@Data
public class {Domain}RespDTO {
    private Long id;
    private String name;
    private Integer status;
}
```

## 步骤 2：API 实现（定义在 `-server` 模块）

```java
package com.develop.mvp.pk.module.{module}.api.{domain};

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.{module}.api.{domain}.dto.{Domain}ReqDTO;
import com.develop.mvp.pk.module.{module}.api.{domain}.dto.{Domain}RespDTO;
import com.develop.mvp.pk.module.{module}.dal.dataobject.{domain}.{Domain}DO;
import com.develop.mvp.pk.module.{module}.service.{domain}.{Domain}Service;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@RestController
@Validated
@Primary // 单体模式下覆盖 Feign 代理
public class {Domain}ApiImpl implements {Domain}Api {

    @Resource
    private {Domain}Service {domain}Service;

    @Override
    public CommonResult<{Domain}RespDTO> get{Domain}(Long id) {
        {Domain}DO entity = {domain}Service.get{Domain}(id);
        return success(BeanUtils.toBean(entity, {Domain}RespDTO.class));
    }

    @Override
    public CommonResult<List<{Domain}RespDTO>> get{Domain}List(Collection<Long> ids) {
        List<{Domain}DO> list = {domain}Service.get{Domain}List(ids);
        return success(BeanUtils.toBean(list, {Domain}RespDTO.class));
    }

    @Override
    public CommonResult<Long> create{Domain}({Domain}ReqDTO reqDTO) {
        return success({domain}Service.create{Domain}(reqDTO));
    }

    @Override
    public CommonResult<Boolean> validate{Domain}List(Collection<Long> ids) {
        {domain}Service.validate{Domain}List(ids);
        return success(true);
    }
}
```

## 步骤 3：Feign 客户端注册

```java
package com.develop.mvp.pk.module.{other}.framework.rpc;

import com.develop.mvp.pk.module.{module}.api.{domain}.{Domain}Api;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "{module}RpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {Domain}Api.class)
public class {Module}RpcConfiguration {

}
```

## 步骤 4：Consumer 使用

```java
@Service
@Validated
public class {Other}ServiceImpl implements {Other}Service {

    @Resource
    private {Domain}Api {domain}Api;

    @Override
    public void someBusinessMethod(Long domainId) {
        // 方式一：手动检查
        CommonResult<{Domain}RespDTO> result = {domain}Api.get{Domain}(domainId);
        if (result.isSuccess()) {
            {Domain}RespDTO dto = result.getData();
        }

        // 方式二：使用 getCheckedData() 自动抛出异常
        {Domain}RespDTO dto = {domain}Api.get{Domain}(domainId).getCheckedData();
    }
}
```

## 单体模式 vs 微服务模式

| 模式 | Feign | 说明 |
|---|---|---|
| 微服务 | 启用 | Feign 通过 Nacos 发现 `system-server` 等独立服务 |
| 单体 | 排除 | `@Primary` 让 Impl Bean 直接注入 |

单体模式在 `develop-server/pom.xml` 中排除 openfeign，`application-local.yaml` 中排除 `*RpcAutoConfiguration`。

## RpcConstants 全局定义

```java
// com.develop.mvp.pk.framework.common.enums.RpcConstants
public interface RpcConstants {
    String RPC_API_PREFIX = "/rpc-api";

    String SYSTEM_NAME = "system-server";
    String SYSTEM_PREFIX = RPC_API_PREFIX + "/system";

    String INFRA_NAME = "infra-server";
    String INFRA_PREFIX = RPC_API_PREFIX + "/infra";
}
```

## 关键点

1. **每个模块有独立的 `ApiConstants.NAME`**，如 `"system-server"`、`"infra-server"`，不是统一的 `"develop-server"`
2. **`@FeignClient(name = ApiConstants.NAME)`** 引用本模块的常量
3. **DTO 必须放在 `-api` 模块**，VO 放在 `-server` 模块的 `controller/vo/` 包下
4. **`@RestController` + `@Validated` + `@Primary`** — API 实现上的三个必要注解
5. **`getCheckedData()`** — 自动判断 code 并抛出 ServiceException
6. **`@AutoTrans`** — Easy-Trans 注解，支持自动翻译（需实现 `AutoTransable`）
7. **Feign 参数必须显式绑定** — `@RequestParam("id")`、`@RequestBody`
8. **`RpcConfiguration`** 使用精确注册 — 避免扫描整个包

## 常见错误

- DTO 定义在 `-server` 模块导致循环依赖
- Feign 方法参数缺少 `@RequestParam` 或 `@RequestBody` — 参数绑定异常
- `ApiConstants.NAME` 误写为 `"develop-server"` — 应该用模块独立的服务名
- 单体模式下未配置 `@Primary` — 注入的是 Feign 代理而非 Impl，启动报错
- 使用 `@PathVariable` 而非 `@RequestParam` — 项目统一使用 `@RequestParam`
