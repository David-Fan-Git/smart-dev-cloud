package com.develop.mvp.pk.module.bpm.controller.admin.definition;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.application.form.BpmFormApplicationService;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.form.BpmFormPageReqVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.form.BpmFormRespVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.form.BpmFormSaveReqVO;
import com.develop.mvp.pk.module.bpm.domain.form.BpmForm;
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

@Tag(name = "管理后台 - 动态表单")
@RestController
@RequestMapping("/bpm/form")
@Validated
public class BpmFormController {

    @Resource
    private BpmFormApplicationService formApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建动态表单")
    @PreAuthorize("@ss.hasPermission('bpm:form:create')")
    public CommonResult<Long> createForm(@Valid @RequestBody BpmFormSaveReqVO createReqVO) {
        return success(formApplicationService.create(
                createReqVO.getName(), createReqVO.getStatus(), createReqVO.getConf(),
                createReqVO.getFields(), createReqVO.getRemark()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新动态表单")
    @PreAuthorize("@ss.hasPermission('bpm:form:update')")
    public CommonResult<Boolean> updateForm(@Valid @RequestBody BpmFormSaveReqVO updateReqVO) {
        formApplicationService.update(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getStatus(),
                updateReqVO.getConf(), updateReqVO.getFields(), updateReqVO.getRemark());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除动态表单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:form:delete')")
    public CommonResult<Boolean> deleteForm(@RequestParam("id") Long id) {
        formApplicationService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得动态表单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:form:query')")
    public CommonResult<BpmFormRespVO> getForm(@RequestParam("id") Long id) {
        BpmForm form = formApplicationService.get(id);
        if (form == null) return success(null);
        return success(toRespVO(form));
    }

    @GetMapping({"/list-all-simple", "/simple-list"})
    @Operation(summary = "获得动态表单的精简列表", description = "用于表单下拉框")
    public CommonResult<List<BpmFormRespVO>> getFormSimpleList() {
        return success(formApplicationService.getAll().stream()
                .map(f -> new BpmFormRespVO().setId(f.id().value()).setName(f.name().value()))
                .collect(Collectors.toList()));
    }

    @GetMapping("/page")
    @Operation(summary = "获得动态表单分页")
    @PreAuthorize("@ss.hasPermission('bpm:form:query')")
    public CommonResult<PageResult<BpmFormRespVO>> getFormPage(@Valid BpmFormPageReqVO pageVO) {
        PageResult<BpmForm> pageResult = formApplicationService.getPage(
                pageVO.getName(), pageVO.getPageNo(), pageVO.getPageSize());
        PageResult<BpmFormRespVO> voPage = new PageResult<>(
                pageResult.getList().stream().map(this::toRespVO).collect(Collectors.toList()),
                pageResult.getTotal());
        return success(voPage);
    }

    private BpmFormRespVO toRespVO(BpmForm f) {
        return new BpmFormRespVO().setId(f.id().value()).setName(f.name().value())
                .setStatus(f.status().code()).setConf(f.conf()).setFields(f.fields()).setRemark(f.remark());
    }
}
