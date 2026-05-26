package com.develop.mvp.pk.module.bpm.controller.admin.definition;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.application.listener.BpmProcessListenerApplicationService;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.listener.BpmProcessListenerPageReqVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.listener.BpmProcessListenerRespVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.listener.BpmProcessListenerSaveReqVO;
import com.develop.mvp.pk.module.bpm.domain.listener.BpmProcessListener;
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

@Tag(name = "管理后台 - BPM 流程监听器")
@RestController
@RequestMapping("/bpm/process-listener")
@Validated
public class BpmProcessListenerController {

    @Resource
    private BpmProcessListenerApplicationService processListenerApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建流程监听器")
    @PreAuthorize("@ss.hasPermission('bpm:process-listener:create')")
    public CommonResult<Long> createProcessListener(@Valid @RequestBody BpmProcessListenerSaveReqVO createReqVO) {
        return success(processListenerApplicationService.create(
                createReqVO.getName(), createReqVO.getStatus(), createReqVO.getType(),
                createReqVO.getEvent(), createReqVO.getValueType(), createReqVO.getValue()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新流程监听器")
    @PreAuthorize("@ss.hasPermission('bpm:process-listener:update')")
    public CommonResult<Boolean> updateProcessListener(@Valid @RequestBody BpmProcessListenerSaveReqVO updateReqVO) {
        processListenerApplicationService.update(
                updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getStatus(),
                updateReqVO.getType(), updateReqVO.getEvent(),
                updateReqVO.getValueType(), updateReqVO.getValue());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除流程监听器")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:process-listener:delete')")
    public CommonResult<Boolean> deleteProcessListener(@RequestParam("id") Long id) {
        processListenerApplicationService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得流程监听器")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:process-listener:query')")
    public CommonResult<BpmProcessListenerRespVO> getProcessListener(@RequestParam("id") Long id) {
        BpmProcessListener listener = processListenerApplicationService.get(id);
        if (listener == null) return success(null);
        return success(toRespVO(listener));
    }

    @GetMapping("/page")
    @Operation(summary = "获得流程监听器分页")
    @PreAuthorize("@ss.hasPermission('bpm:process-listener:query')")
    public CommonResult<PageResult<BpmProcessListenerRespVO>> getProcessListenerPage(
            @Valid BpmProcessListenerPageReqVO pageReqVO) {
        PageResult<BpmProcessListener> pageResult = processListenerApplicationService.getPage(
                pageReqVO.getName(), pageReqVO.getType(), pageReqVO.getEvent(), pageReqVO.getStatus(),
                pageReqVO.getPageNo(), pageReqVO.getPageSize());
        PageResult<BpmProcessListenerRespVO> voPage = new PageResult<>(
                pageResult.getList().stream().map(this::toRespVO).collect(Collectors.toList()),
                pageResult.getTotal());
        return success(voPage);
    }

    private BpmProcessListenerRespVO toRespVO(BpmProcessListener l) {
        return new BpmProcessListenerRespVO().setId(l.id().value()).setName(l.name().value())
                .setStatus(l.status().code()).setType(l.type()).setEvent(l.event())
                .setValueType(l.valueType()).setValue(l.value());
    }
}
