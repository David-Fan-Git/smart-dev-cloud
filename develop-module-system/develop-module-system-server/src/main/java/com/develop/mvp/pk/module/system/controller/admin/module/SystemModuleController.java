package com.develop.mvp.pk.module.system.controller.admin.module;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.system.application.module.port.inbound.SystemModuleUseCase;
import com.develop.mvp.pk.module.system.controller.admin.module.vo.SystemModuleRegisterReqVO;
import com.develop.mvp.pk.module.system.controller.admin.module.vo.SystemModuleRespVO;
import com.develop.mvp.pk.module.system.convert.module.SystemModuleConvert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 系统模块")
@RestController
@RequestMapping("/system/module")
public class SystemModuleController {

    private final SystemModuleUseCase systemModuleUseCase;
    private final SystemModuleConvert convert = SystemModuleConvert.INSTANCE;

    public SystemModuleController(SystemModuleUseCase systemModuleUseCase) {
        this.systemModuleUseCase = systemModuleUseCase;
    }

    @GetMapping("/list")
    @Operation(summary = "获得系统模块列表")
    @PreAuthorize("@ss.hasPermission('system:module:query')")
    public CommonResult<List<SystemModuleRespVO>> listModules() {
        return success(systemModuleUseCase.listModules().stream().map(convert::toRespVO).toList());
    }

    @GetMapping("/get")
    @Operation(summary = "获得系统模块详情")
    @Parameter(name = "code", description = "模块编码", required = true)
    @PreAuthorize("@ss.hasPermission('system:module:query')")
    public CommonResult<SystemModuleRespVO> getModule(@RequestParam("code") String code) {
        return success(convert.toRespVO(systemModuleUseCase.getModule(code)));
    }

    @PostMapping("/register")
    @Operation(summary = "注册系统模块")
    @PreAuthorize("@ss.hasPermission('system:module:create')")
    public CommonResult<Boolean> register(@Valid @RequestBody SystemModuleRegisterReqVO reqVO) {
        systemModuleUseCase.register(convert.toCommand(reqVO));
        return success(true);
    }

    @PostMapping("/start")
    @Operation(summary = "启动系统模块")
    @PreAuthorize("@ss.hasPermission('system:module:update')")
    public CommonResult<Boolean> startModule(@RequestParam("code") String code) {
        systemModuleUseCase.startModule(code);
        return success(true);
    }

    @PostMapping("/stop")
    @Operation(summary = "停止系统模块")
    @PreAuthorize("@ss.hasPermission('system:module:update')")
    public CommonResult<Boolean> stopModule(@RequestParam("code") String code) {
        systemModuleUseCase.stopModule(code);
        return success(true);
    }
}
