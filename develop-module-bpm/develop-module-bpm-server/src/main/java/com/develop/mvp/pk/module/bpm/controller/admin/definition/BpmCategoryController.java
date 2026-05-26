package com.develop.mvp.pk.module.bpm.controller.admin.definition;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.application.definition.BpmCategoryApplicationService;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.category.BpmCategoryPageReqVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.category.BpmCategoryRespVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.category.BpmCategorySaveReqVO;
import com.develop.mvp.pk.module.bpm.domain.definition.BpmCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - BPM 流程分类")
@RestController
@RequestMapping("/bpm/category")
@Validated
public class BpmCategoryController {

    @Resource
    private BpmCategoryApplicationService categoryApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建流程分类")
    @PreAuthorize("@ss.hasPermission('bpm:category:create')")
    public CommonResult<Long> createCategory(@Valid @RequestBody BpmCategorySaveReqVO createReqVO) {
        return success(categoryApplicationService.create(
                createReqVO.getName(), createReqVO.getCode(), createReqVO.getStatus(), createReqVO.getSort()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新流程分类")
    @PreAuthorize("@ss.hasPermission('bpm:category:update')")
    public CommonResult<Boolean> updateCategory(@Valid @RequestBody BpmCategorySaveReqVO updateReqVO) {
        categoryApplicationService.update(
                updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getCode(),
                updateReqVO.getStatus(), updateReqVO.getSort());
        return success(true);
    }

    @PutMapping("/update-sort-batch")
    @Operation(summary = "批量更新流程分类的排序")
    @Parameter(name = "ids", description = "分类编号列表", required = true, example = "1,2,3")
    @PreAuthorize("@ss.hasPermission('bpm:category:update')")
    public CommonResult<Boolean> updateCategorySortBatch(@RequestParam("ids") List<Long> ids) {
        categoryApplicationService.updateSortBatch(ids);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除流程分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:category:delete')")
    public CommonResult<Boolean> deleteCategory(@RequestParam("id") Long id) {
        categoryApplicationService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得流程分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:category:query')")
    public CommonResult<BpmCategoryRespVO> getCategory(@RequestParam("id") Long id) {
        BpmCategory category = categoryApplicationService.get(id);
        if (category == null) return success(null);
        return success(toRespVO(category));
    }

    @GetMapping("/page")
    @Operation(summary = "获得流程分类分页")
    @PreAuthorize("@ss.hasPermission('bpm:category:query')")
    public CommonResult<PageResult<BpmCategoryRespVO>> getCategoryPage(@Valid BpmCategoryPageReqVO pageReqVO) {
        PageResult<BpmCategory> pageResult = categoryApplicationService.getPage(
                pageReqVO.getName(), pageReqVO.getCode(), pageReqVO.getStatus(),
                pageReqVO.getPageNo(), pageReqVO.getPageSize());
        PageResult<BpmCategoryRespVO> voPage = new PageResult<>(
                pageResult.getList().stream().map(this::toRespVO).collect(Collectors.toList()),
                pageResult.getTotal());
        return success(voPage);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取流程分类的精简信息列表", description = "只包含被开启的分类，主要用于前端的下拉选项")
    public CommonResult<List<BpmCategoryRespVO>> getCategorySimpleList() {
        List<BpmCategory> list = categoryApplicationService.getByStatus(CommonStatusEnum.ENABLE.getStatus());
        list.sort(Comparator.comparingInt(BpmCategory::sort));
        return success(list.stream().map(c -> new BpmCategoryRespVO()
                .setId(c.id().value()).setName(c.name().value()).setCode(c.code().value()))
                .collect(Collectors.toList()));
    }

    private BpmCategoryRespVO toRespVO(BpmCategory c) {
        return new BpmCategoryRespVO()
                .setId(c.id().value()).setName(c.name().value()).setCode(c.code().value())
                .setStatus(c.status().code()).setSort(c.sort());
    }
}
