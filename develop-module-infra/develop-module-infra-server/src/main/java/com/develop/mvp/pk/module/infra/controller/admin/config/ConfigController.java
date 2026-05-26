package com.develop.mvp.pk.module.infra.controller.admin.config;

import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.infra.application.config.port.inbound.ConfigUseCase;
import com.develop.mvp.pk.module.infra.controller.admin.config.vo.ConfigPageReqVO;
import com.develop.mvp.pk.module.infra.controller.admin.config.vo.ConfigRespVO;
import com.develop.mvp.pk.module.infra.controller.admin.config.vo.ConfigSaveReqVO;
import com.develop.mvp.pk.module.infra.domain.config.Config;
import com.develop.mvp.pk.module.infra.domain.config.valueobject.ConfigType;
import com.develop.mvp.pk.module.infra.enums.ErrorCodeConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 参数配置")
@RestController
@RequestMapping("/infra/config")
@Validated
public class ConfigController {

    @Resource
    private ConfigUseCase configApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:create')")
    public CommonResult<Long> createConfig(@Valid @RequestBody ConfigSaveReqVO createReqVO) {
        return success(configApplicationService.createConfig(
                createReqVO.getKey(), createReqVO.getValue(), createReqVO.getName(),
                createReqVO.getCategory(), null, createReqVO.getVisible(), createReqVO.getRemark()));
    }

    @PutMapping("/update")
    @Operation(summary = "修改参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody ConfigSaveReqVO updateReqVO) {
        configApplicationService.updateConfig(
                updateReqVO.getId(), updateReqVO.getKey(), updateReqVO.getValue(),
                updateReqVO.getName(), updateReqVO.getCategory(),
                null, updateReqVO.getVisible(), updateReqVO.getRemark());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除参数配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:config:delete')")
    public CommonResult<Boolean> deleteConfig(@RequestParam("id") Long id) {
        configApplicationService.deleteConfig(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除参数配置")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('infra:config:delete')")
    public CommonResult<Boolean> deleteConfigList(@RequestParam("ids") List<Long> ids) {
        configApplicationService.deleteConfigList(ids);
        return success(true);
    }

    @GetMapping(value = "/get")
    @Operation(summary = "获得参数配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:config:query')")
    public CommonResult<ConfigRespVO> getConfig(@RequestParam("id") Long id) {
        Config config = configApplicationService.getConfig(id);
        return success(toConfigRespVO(config));
    }

    @GetMapping(value = "/get-value-by-key")
    @Operation(summary = "根据参数键名查询参数值", description = "不可见的配置，不允许返回给前端")
    @Parameter(name = "key", description = "参数键", required = true, example = "yunai.biz.username")
    public CommonResult<String> getConfigKey(@RequestParam("key") String key) {
        try {
            String value = configApplicationService.getConfigValueByKey(key);
            return success(value);
        } catch (Exception e) {
            throw exception(ErrorCodeConstants.CONFIG_GET_VALUE_ERROR_IF_VISIBLE);
        }
    }

    @GetMapping("/page")
    @Operation(summary = "获取参数配置分页")
    @PreAuthorize("@ss.hasPermission('infra:config:query')")
    public CommonResult<PageResult<ConfigRespVO>> getConfigPage(@Valid ConfigPageReqVO pageReqVO) {
        PageResult<Config> page = configApplicationService.getConfigPage(
                pageReqVO.getName(), pageReqVO.getKey(), pageReqVO.getType(),
                pageReqVO.getCreateTime(), pageReqVO.getPageNo(), pageReqVO.getPageSize());
        PageResult<ConfigRespVO> voPage = new PageResult<>(
                page.getList().stream().map(this::toConfigRespVO).collect(Collectors.toList()),
                page.getTotal());
        return success(voPage);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出参数配置")
    @PreAuthorize("@ss.hasPermission('infra:config:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportConfig(ConfigPageReqVO exportReqVO,
                             HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<Config> page = configApplicationService.getConfigPage(
                exportReqVO.getName(), exportReqVO.getKey(), exportReqVO.getType(),
                exportReqVO.getCreateTime(), exportReqVO.getPageNo(), exportReqVO.getPageSize());
        List<ConfigRespVO> list = page.getList().stream()
                .map(this::toConfigRespVO).collect(Collectors.toList());
        ExcelUtils.write(response, "参数配置.xls", "数据", ConfigRespVO.class, list);
    }

    // ── 转换方法 ──

    private ConfigRespVO toConfigRespVO(Config config) {
        if (config == null) return null;
        ConfigRespVO vo = new ConfigRespVO();
        vo.setId(config.id().value());
        vo.setKey(config.key().value());
        vo.setValue(config.value());
        vo.setName(config.name());
        vo.setCategory(config.category());
        vo.setType(config.type().code());
        vo.setVisible(config.visible().value());
        vo.setRemark(config.remark());
        return vo;
    }
}
