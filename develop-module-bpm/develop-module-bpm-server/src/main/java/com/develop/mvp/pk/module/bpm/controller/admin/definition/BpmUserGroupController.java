package com.develop.mvp.pk.module.bpm.controller.admin.definition;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.bpm.application.usergroup.BpmUserGroupApplicationService;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.group.BpmUserGroupPageReqVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.group.BpmUserGroupRespVO;
import com.develop.mvp.pk.module.bpm.controller.admin.definition.vo.group.BpmUserGroupSaveReqVO;
import com.develop.mvp.pk.module.bpm.domain.usergroup.BpmUserGroup;
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

@Tag(name = "管理后台 - 用户组")
@RestController
@RequestMapping("/bpm/user-group")
@Validated
public class BpmUserGroupController {

    @Resource
    private BpmUserGroupApplicationService userGroupApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建用户组")
    @PreAuthorize("@ss.hasPermission('bpm:user-group:create')")
    public CommonResult<Long> createUserGroup(@Valid @RequestBody BpmUserGroupSaveReqVO createReqVO) {
        return success(userGroupApplicationService.create(
                createReqVO.getName(), createReqVO.getDescription(), createReqVO.getStatus(), createReqVO.getUserIds()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户组")
    @PreAuthorize("@ss.hasPermission('bpm:user-group:update')")
    public CommonResult<Boolean> updateUserGroup(@Valid @RequestBody BpmUserGroupSaveReqVO updateReqVO) {
        userGroupApplicationService.update(updateReqVO.getId(), updateReqVO.getName(),
                updateReqVO.getDescription(), updateReqVO.getStatus(), updateReqVO.getUserIds());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户组")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:user-group:delete')")
    public CommonResult<Boolean> deleteUserGroup(@RequestParam("id") Long id) {
        userGroupApplicationService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户组")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:user-group:query')")
    public CommonResult<BpmUserGroupRespVO> getUserGroup(@RequestParam("id") Long id) {
        BpmUserGroup group = userGroupApplicationService.get(id);
        if (group == null) return success(null);
        return success(toRespVO(group));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户组分页")
    @PreAuthorize("@ss.hasPermission('bpm:user-group:query')")
    public CommonResult<PageResult<BpmUserGroupRespVO>> getUserGroupPage(@Valid BpmUserGroupPageReqVO pageVO) {
        PageResult<BpmUserGroup> pageResult = userGroupApplicationService.getPage(
                pageVO.getName(), pageVO.getStatus(), pageVO.getPageNo(), pageVO.getPageSize());
        PageResult<BpmUserGroupRespVO> voPage = new PageResult<>(
                pageResult.getList().stream().map(this::toRespVO).collect(Collectors.toList()),
                pageResult.getTotal());
        return success(voPage);
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取用户组精简信息列表", description = "只包含被开启的用户组，主要用于前端的下拉选项")
    public CommonResult<List<BpmUserGroupRespVO>> getUserGroupSimpleList() {
        return success(userGroupApplicationService.getByStatus(CommonStatusEnum.ENABLE.getStatus())
                .stream().map(g -> new BpmUserGroupRespVO().setId(g.id().value()).setName(g.name().value()))
                .collect(Collectors.toList()));
    }

    private BpmUserGroupRespVO toRespVO(BpmUserGroup g) {
        return new BpmUserGroupRespVO().setId(g.id().value()).setName(g.name().value())
                .setDescription(g.description()).setStatus(g.status().code()).setUserIds(g.userIds());
    }
}
