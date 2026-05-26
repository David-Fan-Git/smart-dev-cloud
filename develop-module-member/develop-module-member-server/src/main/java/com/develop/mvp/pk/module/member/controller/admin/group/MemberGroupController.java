package com.develop.mvp.pk.module.member.controller.admin.group;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.application.group.MemberGroupApplicationService;
import com.develop.mvp.pk.module.member.controller.admin.group.vo.*;
import com.develop.mvp.pk.module.member.convert.group.MemberGroupConvert;
import com.develop.mvp.pk.module.member.domain.group.MemberGroup;
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


@Tag(name = "管理后台 - 用户分组")
@RestController
@RequestMapping("/member/group")
@Validated
public class MemberGroupController {

    @Resource
    private MemberGroupApplicationService groupApplicationService;

    @Resource
    private com.develop.mvp.pk.module.member.application.user.MemberUserApplicationService memberUserApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建用户分组")
    @PreAuthorize("@ss.hasPermission('member:group:create')")
    public CommonResult<Long> createGroup(@Valid @RequestBody MemberGroupCreateReqVO createReqVO) {
        return success(groupApplicationService.createGroup(createReqVO.getName(), createReqVO.getRemark(), createReqVO.getStatus()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户分组")
    @PreAuthorize("@ss.hasPermission('member:group:update')")
    public CommonResult<Boolean> updateGroup(@Valid @RequestBody MemberGroupUpdateReqVO updateReqVO) {
        groupApplicationService.updateGroup(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getRemark(), updateReqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户分组")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:group:delete')")
    public CommonResult<Boolean> deleteGroup(@RequestParam("id") Long id) {
        groupApplicationService.setUserCountByGroupId(memberUserApplicationService::countByGroupId);
        groupApplicationService.deleteGroup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户分组")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:group:query')")
    public CommonResult<MemberGroupRespVO> getGroup(@RequestParam("id") Long id) {
        MemberGroup group = groupApplicationService.get(id);
        return success(MemberGroupConvert.INSTANCE.convert(group));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取会员分组精简信息列表", description = "只包含被开启的会员分组，主要用于前端的下拉选项")
    public CommonResult<List<MemberGroupSimpleRespVO>> getSimpleGroupList() {
        List<MemberGroup> list = groupApplicationService.getEnableList();
        return success(MemberGroupConvert.INSTANCE.convertSimpleListFromDomain(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户分组分页")
    @PreAuthorize("@ss.hasPermission('member:group:query')")
    public CommonResult<PageResult<MemberGroupRespVO>> getGroupPage(@Valid MemberGroupPageReqVO pageVO) {
        PageResult<MemberGroup> pageResult = groupApplicationService.getPage(
                pageVO.getName(), pageVO.getStatus(), null, null, pageVO.getPageNo(), pageVO.getPageSize());
        return success(MemberGroupConvert.INSTANCE.convertPageFromDomain(pageResult));
    }

}
