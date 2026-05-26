package com.develop.mvp.pk.module.ai.controller.admin.model;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.ai.application.model.AiModelApplicationService;
import com.develop.mvp.pk.module.ai.controller.admin.model.vo.model.AiModelPageReqVO;
import com.develop.mvp.pk.module.ai.controller.admin.model.vo.model.AiModelRespVO;
import com.develop.mvp.pk.module.ai.controller.admin.model.vo.model.AiModelSaveReqVO;
import com.develop.mvp.pk.module.ai.domain.model.AiModel;
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

/**
 * Skill: AggregateRoot_Ai_Model_Skill — Controller 层（接口适配）
 * 职责：接收 HTTP 请求，委托 AiModelApplicationService 处理，将领域对象转换为 VO 返回
 */
@Tag(name = "管理后台 - AI 模型")
@RestController
@RequestMapping("/ai/model")
@Validated
public class AiModelController {

    @Resource
    private AiModelApplicationService modelApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建模型")
    @PreAuthorize("@ss.hasPermission('ai:model:create')")
    public CommonResult<Long> createModel(@Valid @RequestBody AiModelSaveReqVO createReqVO) {
        // Skill R01: 平台校验由 @InEnum(AiPlatformEnum.class) 在 VO 层完成
        // Skill R02: API Key 校验由应用层编排 apiKeyService.validateApiKey
        return success(modelApplicationService.createModel(
                createReqVO.getName(), createReqVO.getModel(), createReqVO.getPlatform(),
                createReqVO.getType(), createReqVO.getKeyId(), createReqVO.getSort(),
                createReqVO.getStatus(), createReqVO.getTemperature(),
                createReqVO.getMaxTokens(), createReqVO.getMaxContexts()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新模型")
    @PreAuthorize("@ss.hasPermission('ai:model:update')")
    public CommonResult<Boolean> updateModel(@Valid @RequestBody AiModelSaveReqVO updateReqVO) {
        // Skill R03: 存在性校验在应用层 findExistingModel 中执行
        // Skill R04: 平台+API Key 重新校验
        modelApplicationService.updateModel(
                updateReqVO.getId(), updateReqVO.getKeyId(), updateReqVO.getName(),
                updateReqVO.getModel(), updateReqVO.getPlatform(),
                updateReqVO.getType(), updateReqVO.getSort(), updateReqVO.getStatus(),
                updateReqVO.getTemperature(), updateReqVO.getMaxTokens(),
                updateReqVO.getMaxContexts());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:model:delete')")
    public CommonResult<Boolean> deleteModel(@RequestParam("id") Long id) {
        // Skill R05: 校验存在后删除
        modelApplicationService.deleteModel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:model:query')")
    public CommonResult<AiModelRespVO> getModel(@RequestParam("id") Long id) {
        AiModel model = modelApplicationService.getModel(id);
        return success(toRespVO(model));
    }

    @GetMapping("/page")
    @Operation(summary = "获得模型分页")
    @PreAuthorize("@ss.hasPermission('ai:model:query')")
    public CommonResult<PageResult<AiModelRespVO>> getModelPage(@Valid AiModelPageReqVO pageReqVO) {
        // Skill R09: 模型按 sort ASC 排列（仓储实现中保证）
        PageResult<AiModel> page = modelApplicationService.getModelPage(
                pageReqVO.getName(), pageReqVO.getModel(), pageReqVO.getPlatform(),
                pageReqVO.getType(), pageReqVO.getStatus(), pageReqVO.getPageNo(), pageReqVO.getPageSize());
        List<AiModelRespVO> voList = page.getList().stream()
                .map(this::toRespVO).collect(Collectors.toList());
        return success(new PageResult<>(voList, page.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得模型列表")
    @Parameter(name = "type", description = "类型", required = true, example = "1")
    @Parameter(name = "platform", description = "平台", example = "midjourney")
    public CommonResult<List<AiModelRespVO>> getModelSimpleList(
            @RequestParam("type") Integer type,
            @RequestParam(value = "platform", required = false) String platform) {
        List<AiModel> list = modelApplicationService.getModelListByStatusAndType(
                CommonStatusEnum.ENABLE.getStatus(), type, platform);
        return success(list.stream().map(model -> new AiModelRespVO()
                .setId(model.id()).setName(model.name())
                .setModel(model.model()).setPlatform(model.platform())).toList());
    }

    // ── Skill AC08: 领域对象 → VO 转换方法 ──

    /**
     * Skill: I02 — status 从 AiModelStatus 值对象获取 code
     */
    private AiModelRespVO toRespVO(AiModel model) {
        if (model == null) return null;
        return new AiModelRespVO()
                .setId(model.id())
                .setKeyId(model.keyId())
                .setName(model.name())
                .setModel(model.model())
                .setPlatform(model.platform())
                .setType(model.type())
                .setSort(model.sort())
                .setStatus(model.status() != null ? model.status().code() : null)
                .setTemperature(model.temperature())
                .setMaxTokens(model.maxTokens())
                .setMaxContexts(model.maxContexts());
    }

}