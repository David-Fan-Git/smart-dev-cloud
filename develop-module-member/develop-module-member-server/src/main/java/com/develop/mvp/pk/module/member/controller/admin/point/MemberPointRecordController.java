package com.develop.mvp.pk.module.member.controller.admin.point;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.application.point.MemberPointRecordApplicationService;
import com.develop.mvp.pk.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import com.develop.mvp.pk.module.member.controller.admin.point.vo.recrod.MemberPointRecordRespVO;
import com.develop.mvp.pk.module.member.convert.point.MemberPointRecordConvert;
import com.develop.mvp.pk.module.member.dal.dataobject.user.MemberUserDO;
import com.develop.mvp.pk.module.member.dal.mysql.user.MemberUserMapper;
import com.develop.mvp.pk.module.member.domain.point.MemberPointRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 签到记录")
@RestController
@RequestMapping("/member/point/record")
@Validated
public class MemberPointRecordController {

    @Resource
    private MemberPointRecordApplicationService pointRecordApplicationService;
    @Resource
    private MemberUserMapper memberUserMapper;

    @GetMapping("/page")
    @Operation(summary = "获得用户积分记录分页")
    @PreAuthorize("@ss.hasPermission('point:record:query')")
    public CommonResult<PageResult<MemberPointRecordRespVO>> getPointRecordPage(@Valid MemberPointRecordPageReqVO pageVO) {
        PageResult<MemberPointRecord> pageResult = pointRecordApplicationService.getPointRecordPage(
                pageVO.getNickname(), pageVO.getUserId(), pageVO.getBizType(), pageVO.getTitle(),
                pageVO.getPageNo(), pageVO.getPageSize());
        if (CollectionUtils.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        List<MemberUserDO> users = memberUserMapper.selectByIds(
                convertSet(pageResult.getList(), MemberPointRecord::userId));
        return success(MemberPointRecordConvert.INSTANCE.convertPageFromDomain(pageResult, users));
    }

}
