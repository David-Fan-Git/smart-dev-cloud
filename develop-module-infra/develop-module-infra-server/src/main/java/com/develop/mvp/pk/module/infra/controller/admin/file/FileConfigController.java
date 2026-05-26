package com.develop.mvp.pk.module.infra.controller.admin.file;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.infra.application.file.port.inbound.FileConfigUseCase;
import com.develop.mvp.pk.module.infra.controller.admin.file.vo.config.FileConfigPageReqVO;
import com.develop.mvp.pk.module.infra.controller.admin.file.vo.config.FileConfigRespVO;
import com.develop.mvp.pk.module.infra.controller.admin.file.vo.config.FileConfigSaveReqVO;
import com.develop.mvp.pk.module.infra.domain.file.FileConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文件配置")
@RestController
@RequestMapping("/infra/file-config")
@Validated
public class FileConfigController {

    @Resource
    private FileConfigUseCase fileConfigApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建文件配置")
    @PreAuthorize("@ss.hasPermission('infra:file-config:create')")
    public CommonResult<Long> createFileConfig(@Valid @RequestBody FileConfigSaveReqVO createReqVO) {
        return success(fileConfigApplicationService.createFileConfig(
                createReqVO.getName(), createReqVO.getStorage(), false,
                createReqVO.getConfig(), createReqVO.getRemark()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文件配置")
    @PreAuthorize("@ss.hasPermission('infra:file-config:update')")
    public CommonResult<Boolean> updateFileConfig(@Valid @RequestBody FileConfigSaveReqVO updateReqVO) {
        fileConfigApplicationService.updateFileConfig(
                updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getStorage(),
                updateReqVO.getConfig(), updateReqVO.getRemark());
        return success(true);
    }

    @PutMapping("/update-master")
    @Operation(summary = "更新文件配置为 Master")
    @PreAuthorize("@ss.hasPermission('infra:file-config:update')")
    public CommonResult<Boolean> updateFileConfigMaster(@RequestParam("id") Long id) {
        fileConfigApplicationService.updateFileConfigMaster(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:file-config:delete')")
    public CommonResult<Boolean> deleteFileConfig(@RequestParam("id") Long id) {
        fileConfigApplicationService.deleteFileConfig(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除文件配置")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('infra:file-config:delete')")
    public CommonResult<Boolean> deleteFileConfigList(@RequestParam("ids") List<Long> ids) {
        fileConfigApplicationService.deleteFileConfigList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文件配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:file-config:query')")
    public CommonResult<FileConfigRespVO> getFileConfig(@RequestParam("id") Long id) {
        FileConfig config = fileConfigApplicationService.getFileConfig(id);
        return success(toFileConfigRespVO(config));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文件配置分页")
    @PreAuthorize("@ss.hasPermission('infra:file-config:query')")
    public CommonResult<PageResult<FileConfigRespVO>> getFileConfigPage(@Valid FileConfigPageReqVO pageVO) {
        PageResult<FileConfig> pageResult = fileConfigApplicationService.getFileConfigPage(
                pageVO.getName(), pageVO.getStorage(), pageVO.getCreateTime(),
                pageVO.getPageNo(), pageVO.getPageSize());
        PageResult<FileConfigRespVO> voPage = new PageResult<>(
                pageResult.getList().stream().map(this::toFileConfigRespVO).collect(Collectors.toList()),
                pageResult.getTotal());
        return success(voPage);
    }

    @GetMapping("/test")
    @Operation(summary = "测试文件配置是否正确")
    @PreAuthorize("@ss.hasPermission('infra:file-config:query')")
    public CommonResult<String> testFileConfig(@RequestParam("id") Long id) throws Exception {
        String result = fileConfigApplicationService.testFileConfig(id);
        return success(result);
    }

    // ── 转换方法 ──

    private FileConfigRespVO toFileConfigRespVO(FileConfig config) {
        if (config == null) return null;
        FileConfigRespVO vo = new FileConfigRespVO();
        vo.setId(config.id().value());
        vo.setName(config.name().value());
        vo.setStorage(config.storage());
        vo.setMaster(config.master());
        vo.setConfig(config.config());
        vo.setRemark(config.remark());
        return vo;
    }
}
