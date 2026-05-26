package com.develop.mvp.pk.module.member.controller.admin.level;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.application.level.MemberLevelApplicationService;
import com.develop.mvp.pk.module.member.controller.admin.level.vo.level.*;
import com.develop.mvp.pk.module.member.convert.level.MemberLevelConvert;
import com.develop.mvp.pk.module.member.domain.level.MemberLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员等级")
@RestController
@RequestMapping("/member/level")
@Validated
public class MemberLevelController {

    @Resource
    private MemberLevelApplicationService levelApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建会员等级")
    @PreAuthorize("@ss.hasPermission('member:level:create')")
    public CommonResult<Long> createLevel(@Valid @RequestBody MemberLevelCreateReqVO createReqVO) {
        return success(levelApplicationService.createLevel(createReqVO.getName(), createReqVO.getLevel(),
                createReqVO.getExperience(), createReqVO.getDiscountPercent(), createReqVO.getIcon(),
                createReqVO.getBackgroundUrl(), createReqVO.getStatus()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新会员等级")
    @PreAuthorize("@ss.hasPermission('member:level:update')")
    public CommonResult<Boolean> updateLevel(@Valid @RequestBody MemberLevelUpdateReqVO updateReqVO) {
        levelApplicationService.updateLevel(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getLevel(),
                updateReqVO.getExperience(), updateReqVO.getDiscountPercent(), updateReqVO.getIcon(),
                updateReqVO.getBackgroundUrl(), updateReqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除会员等级")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:level:delete')")
    public CommonResult<Boolean> deleteLevel(@RequestParam("id") Long id) {
        levelApplicationService.deleteLevel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员等级")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:level:query')")
    public CommonResult<MemberLevelRespVO> getLevel(@RequestParam("id") Long id) {
        MemberLevel level = levelApplicationService.get(id);
        return success(MemberLevelConvert.INSTANCE.convert(level));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取会员等级精简信息列表", description = "只包含被开启的会员等级，主要用于前端的下拉选项")
    public CommonResult<List<MemberLevelSimpleRespVO>> getSimpleLevelList() {
        List<MemberLevel> list = levelApplicationService.getEnableList();
        return success(MemberLevelConvert.INSTANCE.convertSimpleListFromDomain(list));
    }

    @GetMapping("/list")
    @Operation(summary = "获得会员等级列表")
    @PreAuthorize("@ss.hasPermission('member:level:query')")
    public CommonResult<List<MemberLevelRespVO>> getLevelList(@Valid MemberLevelListReqVO listReqVO) {
        List<MemberLevel> result = levelApplicationService.getList(listReqVO.getName(), listReqVO.getStatus());
        return success(MemberLevelConvert.INSTANCE.convertListFromDomain(result));
    }

}
