package com.develop.mvp.pk.module.system.controller.admin.logger;

import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.framework.translate.core.TranslateUtils;
import com.develop.mvp.pk.module.system.application.logger.dto.OperateLogDTO;
import com.develop.mvp.pk.module.system.application.logger.port.inbound.LoggerUseCase;
import com.develop.mvp.pk.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.logger.vo.operatelog.OperateLogRespVO;
import com.develop.mvp.pk.module.system.convert.logger.LoggerConvert;
import com.fhs.core.trans.anno.TransMethodResult;
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

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Operate Log Controller 控制器。
 */
@Tag(name = "管理后台 - 操作日志")
@RestController
@RequestMapping("/system/operate-log")
@Validated
public class OperateLogController {

    @Resource
    private LoggerUseCase loggerUseCase;

    /**
     * 查询 get Operate Log 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "查看操作日志")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:operate-log:query')")
    public CommonResult<OperateLogRespVO> getOperateLog(@RequestParam("id") Long id) {
        OperateLogDTO operateLog = loggerUseCase.getOperateLog(id);
        return success(LoggerConvert.INSTANCE.convert(operateLog));
    }

    /**
     * 查询 page Operate Log 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    @GetMapping("/page")
    @Operation(summary = "查看操作日志分页列表")
    @PreAuthorize("@ss.hasPermission('system:operate-log:query')")
    @TransMethodResult
    public CommonResult<PageResult<OperateLogRespVO>> pageOperateLog(@Valid OperateLogPageReqVO pageReqVO) {
        PageResult<OperateLogDTO> pageResult = loggerUseCase.getOperateLogPage(LoggerConvert.INSTANCE.convert(pageReqVO));
        return success(LoggerConvert.INSTANCE.convertOperateLogRespPage(pageResult));
    }

    /**
     * 执行 export Operate Log 对应的业务操作。
     *
     * @param response response 参数
     * @param exportReqVO exportReqVO 参数
     */
    @Operation(summary = "导出操作日志")
    @GetMapping("/export-excel")
    @PreAuthorize("@ss.hasPermission('system:operate-log:export')")
    @TransMethodResult
    @ApiAccessLog(operateType = EXPORT)
    public void exportOperateLog(HttpServletResponse response, @Valid OperateLogPageReqVO exportReqVO) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OperateLogDTO> list = loggerUseCase.getOperateLogPage(LoggerConvert.INSTANCE.convert(exportReqVO)).getList();
        ExcelUtils.write(response, "操作日志.xls", "数据列表", OperateLogRespVO.class,
                TranslateUtils.translate(LoggerConvert.INSTANCE.convertOperateLogRespList(list)));
    }

}
