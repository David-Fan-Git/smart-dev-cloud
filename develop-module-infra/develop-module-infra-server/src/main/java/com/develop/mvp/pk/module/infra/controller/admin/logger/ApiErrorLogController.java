package com.develop.mvp.pk.module.infra.controller.admin.logger;

import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.infra.application.logger.port.inbound.ApiErrorLogUseCase;
import com.develop.mvp.pk.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogPageReqVO;
import com.develop.mvp.pk.module.infra.controller.admin.logger.vo.apierrorlog.ApiErrorLogRespVO;
import com.develop.mvp.pk.module.infra.dal.dataobject.logger.ApiErrorLogDO;
import com.develop.mvp.pk.module.infra.domain.logger.ApiErrorLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - API 错误日志")
@RestController
@RequestMapping("/infra/api-error-log")
@Validated
public class ApiErrorLogController {

    @Resource
    private ApiErrorLogUseCase apiErrorLogApplicationService;

    @PutMapping("/update-status")
    @Operation(summary = "更新 API 错误日志的状态")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true, example = "1024"),
            @Parameter(name = "processStatus", description = "处理状态", required = true, example = "1")
    })
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:update-status')")
    public CommonResult<Boolean> updateApiErrorLogProcess(@RequestParam("id") Long id,
                                                          @RequestParam("processStatus") Integer processStatus) {
        apiErrorLogApplicationService.processApiErrorLog(id, processStatus, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 API 错误日志")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:query')")
    public CommonResult<ApiErrorLogRespVO> getApiErrorLog(@RequestParam("id") Long id) {
        ApiErrorLog apiErrorLog = apiErrorLogApplicationService.getApiErrorLog(id);
        return success(BeanUtils.toBean(apiErrorLog, ApiErrorLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 API 错误日志分页")
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:query')")
    public CommonResult<PageResult<ApiErrorLogRespVO>> getApiErrorLogPage(@Valid ApiErrorLogPageReqVO pageReqVO) {
        com.develop.mvp.pk.module.infra.domain.logger.repository.ApiErrorLogPageQuery query =
                new com.develop.mvp.pk.module.infra.domain.logger.repository.ApiErrorLogPageQuery(
                        pageReqVO.getUserId(), pageReqVO.getUserType(), pageReqVO.getApplicationName(),
                        pageReqVO.getRequestUrl(), pageReqVO.getExceptionTime(), null, null,
                        pageReqVO.getProcessStatus(),
                        pageReqVO.getPageNo(), pageReqVO.getPageSize());
        PageResult<ApiErrorLog> pageResult = apiErrorLogApplicationService.getApiErrorLogPage(query);
        return success(BeanUtils.toBean(pageResult, ApiErrorLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出 API 错误日志 Excel")
    @PreAuthorize("@ss.hasPermission('infra:api-error-log:export')")
    @com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog(operateType = EXPORT)
    public void exportApiErrorLogExcel(@Valid ApiErrorLogPageReqVO exportReqVO,
                                       HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        com.develop.mvp.pk.module.infra.domain.logger.repository.ApiErrorLogPageQuery query =
                new com.develop.mvp.pk.module.infra.domain.logger.repository.ApiErrorLogPageQuery(
                        exportReqVO.getUserId(), exportReqVO.getUserType(), exportReqVO.getApplicationName(),
                        exportReqVO.getRequestUrl(), exportReqVO.getExceptionTime(), null, null,
                        exportReqVO.getProcessStatus(),
                        exportReqVO.getPageNo(), exportReqVO.getPageSize());
        PageResult<ApiErrorLog> pageResult = apiErrorLogApplicationService.getApiErrorLogPage(query);
        List<ApiErrorLogRespVO> list = pageResult.getList().stream()
                .map(log -> BeanUtils.toBean(log, ApiErrorLogRespVO.class))
                .collect(Collectors.toList());
        ExcelUtils.write(response, "API 错误日志.xls", "数据", ApiErrorLogRespVO.class, list);
    }
}
