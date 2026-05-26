package com.develop.mvp.pk.module.member.controller.admin.user;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.application.level.MemberLevelApplicationService;
import com.develop.mvp.pk.module.member.application.point.MemberPointRecordApplicationService;
import com.develop.mvp.pk.module.member.application.tag.MemberTagApplicationService;
import com.develop.mvp.pk.module.member.application.user.MemberUserApplicationService;
import com.develop.mvp.pk.module.member.controller.admin.user.vo.*;
import com.develop.mvp.pk.module.member.convert.user.MemberUserConvert;
import com.develop.mvp.pk.module.member.dal.dataobject.group.MemberGroupDO;
import com.develop.mvp.pk.module.member.dal.dataobject.level.MemberLevelDO;
import com.develop.mvp.pk.module.member.dal.dataobject.tag.MemberTagDO;
import com.develop.mvp.pk.module.member.domain.group.MemberGroup;
import com.develop.mvp.pk.module.member.domain.level.MemberLevel;
import com.develop.mvp.pk.module.member.domain.tag.MemberTag;
import com.develop.mvp.pk.module.member.domain.user.MemberUser;
import com.develop.mvp.pk.module.member.enums.point.MemberPointBizTypeEnum;
import com.develop.mvp.pk.module.member.service.group.MemberGroupService;
import com.develop.mvp.pk.module.member.service.level.MemberLevelService;
import com.develop.mvp.pk.module.member.service.tag.MemberTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;
import static com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 会员用户")
@RestController
@RequestMapping("/member/user")
@Validated
public class MemberUserController {

    @Resource
    private MemberUserApplicationService memberUserApplicationService;
    @Resource
    private MemberTagApplicationService tagApplicationService;
    @Resource
    private MemberLevelApplicationService levelApplicationService;
    @Resource
    private MemberLevelService memberLevelService; // 保留旧的用于 level DO 查询
    @Resource
    private MemberGroupService memberGroupService; // 保留旧的用于 group DO 查询
    @Resource
    private MemberTagService memberTagService; // 保留旧的用于 tag DO 查询
    @Resource
    private MemberPointRecordApplicationService pointRecordApplicationService;

    @PutMapping("/update")
    @Operation(summary = "更新会员用户")
    @PreAuthorize("@ss.hasPermission('member:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody MemberUserUpdateReqVO updateReqVO) {
        memberUserApplicationService.updateProfile(updateReqVO.getId(),
                updateReqVO.getNickname(), updateReqVO.getAvatar());
        return success(true);
    }

    @PutMapping("/update-level")
    @Operation(summary = "更新会员用户等级")
    @PreAuthorize("@ss.hasPermission('member:user:update-level')")
    public CommonResult<Boolean> updateUserLevel(@Valid @RequestBody MemberUserUpdateLevelReqVO updateReqVO) {
        levelApplicationService.updateUserLevel(updateReqVO.getId(), updateReqVO.getLevelId(), updateReqVO.getReason());
        return success(true);
    }

    @PutMapping("/update-point")
    @Operation(summary = "更新会员用户积分")
    @PreAuthorize("@ss.hasPermission('member:user:update-point')")
    public CommonResult<Boolean> updateUserPoint(@Valid @RequestBody MemberUserUpdatePointReqVO updateReqVO) {
        pointRecordApplicationService.createPointRecord(updateReqVO.getId(), updateReqVO.getPoint(),
                MemberPointBizTypeEnum.ADMIN, String.valueOf(getLoginUserId()));
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:user:query')")
    public CommonResult<MemberUserRespVO> getUser(@RequestParam("id") Long id) {
        MemberUser user = memberUserApplicationService.get(id);
        if (user == null) {
            return success(null);
        }
        MemberUserRespVO userVO = MemberUserConvert.INSTANCE.convert(user);
        if (user.levelId() != null) {
            MemberLevel level = levelApplicationService.get(user.levelId());
            if (level != null) {
                userVO.setLevelName(level.name());
            }
        }
        return success(userVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员用户分页")
    @PreAuthorize("@ss.hasPermission('member:user:query')")
    public CommonResult<PageResult<MemberUserRespVO>> getUserPage(@Valid MemberUserPageReqVO pageVO) {
        PageResult<MemberUser> pageResult = memberUserApplicationService.getPage(
                pageVO.getNickname(), pageVO.getMobile(), null,
                pageVO.getLevelId(), pageVO.getGroupId(), pageVO.getTagIds(),
                null, null, null, null,
                pageVO.getPageNo(), pageVO.getPageSize());
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 处理用户标签返显
        Set<Long> tagIds = pageResult.getList().stream()
                .map(MemberUser::tagIds)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        List<MemberTagDO> tags = memberTagService.getTagList(tagIds);
        // 处理用户级别返显
        List<MemberLevelDO> levels = memberLevelService.getLevelList(
                convertSet(pageResult.getList(), MemberUser::levelId));
        // 处理用户分组返显
        List<MemberGroupDO> groups = memberGroupService.getGroupList(
                convertSet(pageResult.getList(), MemberUser::groupId));
        return success(MemberUserConvert.INSTANCE.convertPageFromDomain(pageResult, tags, levels, groups));
    }

}
