package com.develop.mvp.pk.module.bpm.controller.admin.definition;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.application.expression.BpmProcessExpressionApplicationService;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionPageReqVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionRespVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.expression.BpmProcessExpressionSaveReqVO;
import com.develop.mvp.pk.module.bpm.domain.expression.BpmProcessExpression;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - BPM 流程表达式")
@RestController
@RequestMapping("/bpm/process-expression")
@Validated
public class BpmProcessExpressionController {

    @Resource
    private BpmProcessExpressionApplicationService processExpressionApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建流程表达式")
    @PreAuthorize("@ss.hasPermission('bpm:process-expression:create')")
    public CommonResult<Long> createProcessExpression(@Valid @RequestBody BpmProcessExpressionSaveReqVO createReqVO) {
        return success(processExpressionApplicationService.create(
                createReqVO.getName(), createReqVO.getStatus(), createReqVO.getExpression()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新流程表达式")
    @PreAuthorize("@ss.hasPermission('bpm:process-expression:update')")
    public CommonResult<Boolean> updateProcessExpression(@Valid @RequestBody BpmProcessExpressionSaveReqVO updateReqVO) {
        processExpressionApplicationService.update(
                updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getStatus(), updateReqVO.getExpression());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除流程表达式")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:process-expression:delete')")
    public CommonResult<Boolean> deleteProcessExpression(@RequestParam("id") Long id) {
        processExpressionApplicationService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得流程表达式")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:process-expression:query')")
    public CommonResult<BpmProcessExpressionRespVO> getProcessExpression(@RequestParam("id") Long id) {
        BpmProcessExpression expression = processExpressionApplicationService.get(id);
        if (expression == null) return success(null);
        return success(toRespVO(expression));
    }

    @GetMapping("/page")
    @Operation(summary = "获得流程表达式分页")
    @PreAuthorize("@ss.hasPermission('bpm:process-expression:query')")
    public CommonResult<PageResult<BpmProcessExpressionRespVO>> getProcessExpressionPage(
            @Valid BpmProcessExpressionPageReqVO pageReqVO) {
        PageResult<BpmProcessExpression> pageResult = processExpressionApplicationService.getPage(
                pageReqVO.getName(), pageReqVO.getStatus(), pageReqVO.getPageNo(), pageReqVO.getPageSize());
        PageResult<BpmProcessExpressionRespVO> voPage = new PageResult<>(
                pageResult.getList().stream().map(this::toRespVO).collect(Collectors.toList()),
                pageResult.getTotal());
        return success(voPage);
    }

    private BpmProcessExpressionRespVO toRespVO(BpmProcessExpression e) {
        return new BpmProcessExpressionRespVO().setId(e.id().value()).setName(e.name().value())
                .setStatus(e.status().code()).setExpression(e.expression());
    }
}
