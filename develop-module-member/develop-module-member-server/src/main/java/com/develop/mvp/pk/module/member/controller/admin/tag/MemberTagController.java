package com.develop.mvp.pk.module.member.controller.admin.tag;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.application.tag.MemberTagApplicationService;
import com.develop.mvp.pk.module.member.controller.admin.tag.vo.MemberTagCreateReqVO;
import com.develop.mvp.pk.module.member.controller.admin.tag.vo.MemberTagPageReqVO;
import com.develop.mvp.pk.module.member.controller.admin.tag.vo.MemberTagRespVO;
import com.develop.mvp.pk.module.member.controller.admin.tag.vo.MemberTagUpdateReqVO;
import com.develop.mvp.pk.module.member.convert.tag.MemberTagConvert;
import com.develop.mvp.pk.module.member.domain.tag.MemberTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员标签")
@RestController
@RequestMapping("/member/tag")
@Validated
public class MemberTagController {

    @Resource
    private MemberTagApplicationService tagApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建会员标签")
    @PreAuthorize("@ss.hasPermission('member:tag:create')")
    public CommonResult<Long> createTag(@Valid @RequestBody MemberTagCreateReqVO createReqVO) {
        return success(tagApplicationService.createTag(createReqVO.getName()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新会员标签")
    @PreAuthorize("@ss.hasPermission('member:tag:update')")
    public CommonResult<Boolean> updateTag(@Valid @RequestBody MemberTagUpdateReqVO updateReqVO) {
        tagApplicationService.updateTag(updateReqVO.getId(), updateReqVO.getName());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除会员标签")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:tag:delete')")
    public CommonResult<Boolean> deleteTag(@RequestParam("id") Long id) {
        tagApplicationService.deleteTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员标签")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:tag:query')")
    public CommonResult<MemberTagRespVO> getMemberTag(@RequestParam("id") Long id) {
        MemberTag tag = tagApplicationService.get(id);
        return success(MemberTagConvert.INSTANCE.convert(tag));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取会员标签精简信息列表")
    public CommonResult<List<MemberTagRespVO>> getSimpleTagList() {
        List<MemberTag> list = tagApplicationService.getList();
        return success(MemberTagConvert.INSTANCE.convertListFromDomain(list));
    }

    @GetMapping("/list")
    @Operation(summary = "获得会员标签列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('member:tag:query')")
    public CommonResult<List<MemberTagRespVO>> getMemberTagList(@RequestParam("ids") Collection<Long> ids) {
        List<MemberTag> list = tagApplicationService.getList(ids);
        return success(MemberTagConvert.INSTANCE.convertListFromDomain(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员标签分页")
    @PreAuthorize("@ss.hasPermission('member:tag:query')")
    public CommonResult<PageResult<MemberTagRespVO>> getTagPage(@Valid MemberTagPageReqVO pageVO) {
        PageResult<MemberTag> pageResult = tagApplicationService.getPage(
                pageVO.getName(), null, null, pageVO.getPageNo(), pageVO.getPageSize());
        return success(MemberTagConvert.INSTANCE.convertPageFromDomain(pageResult));
    }

}
