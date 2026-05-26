package com.develop.mvp.pk.module.system.controller.admin.logger;

import com.develop.mvp.pk.framework.apilog.core.annotation.ApiAccessLog;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.excel.core.util.ExcelUtils;
import com.develop.mvp.pk.module.system.application.logger.dto.LoginLogDTO;
import com.develop.mvp.pk.module.system.application.logger.port.inbound.LoggerUseCase;
import com.develop.mvp.pk.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import com.develop.mvp.pk.module.system.controller.admin.logger.vo.loginlog.LoginLogRespVO;
import com.develop.mvp.pk.module.system.convert.logger.LoggerConvert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static com.develop.mvp.pk.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * Login Log Controller 控制器。
 */
@Tag(name = "管理后台 - 登录日志")
@RestController
@RequestMapping("/system/login-log")
@Validated
public class LoginLogController {

    @Resource
    private LoggerUseCase loggerUseCase;

    /**
     * 查询 get Login Log 对应的数据。
     *
     * @param id id 参数
     * @return 处理结果
     */
    @GetMapping("/get")
    @Operation(summary = "获得登录日志")
    @PreAuthorize("@ss.hasPermission('system:login-log:query')")
    public CommonResult<LoginLogRespVO> getLoginLog(Long id) {
        LoginLogDTO loginLog = loggerUseCase.getLoginLog(id);
        return success(LoggerConvert.INSTANCE.convert(loginLog));
    }

    /**
     * 查询 get Login Log Page 对应的数据。
     *
     * @param pageReqVO pageReqVO 参数
     * @return 处理结果
     */
    @GetMapping("/page")
    @Operation(summary = "获得登录日志分页列表")
    @PreAuthorize("@ss.hasPermission('system:login-log:query')")
    public CommonResult<PageResult<LoginLogRespVO>> getLoginLogPage(@Valid LoginLogPageReqVO pageReqVO) {
        PageResult<LoginLogDTO> pageResult = loggerUseCase.getLoginLogPage(LoggerConvert.INSTANCE.convert(pageReqVO));
        return success(LoggerConvert.INSTANCE.convertLoginLogRespPage(pageResult));
    }

    /**
     * 执行 export Login Log 对应的业务操作。
     *
     * @param response response 参数
     * @param exportReqVO exportReqVO 参数
     */
    @GetMapping("/export-excel")
    @Operation(summary = "导出登录日志 Excel")
    @PreAuthorize("@ss.hasPermission('system:login-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLoginLog(HttpServletResponse response, @Valid LoginLogPageReqVO exportReqVO) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<LoginLogDTO> list = loggerUseCase.getLoginLogPage(LoggerConvert.INSTANCE.convert(exportReqVO)).getList();
        // 输出
        ExcelUtils.write(response, "登录日志.xls", "数据列表", LoginLogRespVO.class,
                LoggerConvert.INSTANCE.convertLoginLogRespList(list));
    }

}
