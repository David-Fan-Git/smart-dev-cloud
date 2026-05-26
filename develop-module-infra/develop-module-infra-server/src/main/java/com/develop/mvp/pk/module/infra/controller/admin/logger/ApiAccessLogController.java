package com.develop.mvp.pk.module.infra.controller.admin.logger;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.infra.application.logger.port.inbound.ApiAccessLogUseCase;
import com.develop.mvp.pk.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogPageReqVO;
import com.develop.mvp.pk.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogRespVO;
import com.develop.mvp.pk.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import com.develop.mvp.pk.module.infra.domain.logger.ApiAccessLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - API 访问日志")
@RestController
@RequestMapping("/infra/api-access-log")
@Validated
public class ApiAccessLogController {

    @Resource
    private ApiAccessLogUseCase apiAccessLogApplicationService;

    @GetMapping("/get")
    @Operation(summary = "获得 API 访问日志")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:query')")
    public CommonResult<ApiAccessLogRespVO> getApiAccessLog(@RequestParam("id") Long id) {
        ApiAccessLog apiAccessLog = apiAccessLogApplicationService.getApiAccessLog(id);
        return success(BeanUtils.toBean(apiAccessLog, ApiAccessLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得API 访问日志分页")
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:query')")
    public CommonResult<PageResult<ApiAccessLogRespVO>> getApiAccessLogPage(@Valid ApiAccessLogPageReqVO pageReqVO) {
        com.develop.mvp.pk.module.infra.domain.logger.repository.ApiAccessLogPageQuery query =
                new com.develop.mvp.pk.module.infra.domain.logger.repository.ApiAccessLogPageQuery(
                        pageReqVO.getUserId(), pageReqVO.getUserType(), pageReqVO.getApplicationName(),
                        pageReqVO.getRequestUrl(), pageReqVO.getBeginTime(), pageReqVO.getDuration(),
                        pageReqVO.getResultCode(), pageReqVO.getPageNo(), pageReqVO.getPageSize());
        PageResult<ApiAccessLog> pageResult = apiAccessLogApplicationService.getApiAccessLogPage(query);
        return success(BeanUtils.toBean(pageResult, ApiAccessLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出API 访问日志 Excel")
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:export')")
    @com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog(operateType = EXPORT)
    public void exportApiAccessLogExcel(@Valid ApiAccessLogPageReqVO exportReqVO,
                                        HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        com.develop.mvp.pk.module.infra.domain.logger.repository.ApiAccessLogPageQuery query =
                new com.develop.mvp.pk.module.infra.domain.logger.repository.ApiAccessLogPageQuery(
                        exportReqVO.getUserId(), exportReqVO.getUserType(), exportReqVO.getApplicationName(),
                        exportReqVO.getRequestUrl(), exportReqVO.getBeginTime(), exportReqVO.getDuration(),
                        exportReqVO.getResultCode(), exportReqVO.getPageNo(), exportReqVO.getPageSize());
        PageResult<ApiAccessLog> pageResult = apiAccessLogApplicationService.getApiAccessLogPage(query);
        List<ApiAccessLogRespVO> list = pageResult.getList().stream()
                .map(log -> BeanUtils.toBean(log, ApiAccessLogRespVO.class))
                .collect(Collectors.toList());
        ExcelUtils.write(response, "API 访问日志.xls", "数据", ApiAccessLogRespVO.class, list);
    }
}
