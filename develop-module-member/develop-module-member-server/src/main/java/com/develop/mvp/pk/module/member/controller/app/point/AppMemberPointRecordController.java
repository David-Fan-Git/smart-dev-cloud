package com.develop.mvp.pk.module.member.controller.app.point;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.application.point.MemberPointRecordApplicationService;
import com.develop.mvp.pk.module.member.controller.app.point.vo.AppMemberPointRecordPageReqVO;
import com.develop.mvp.pk.module.member.controller.app.point.vo.AppMemberPointRecordRespVO;
import com.develop.mvp.pk.module.member.domain.point.MemberPointRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 签到记录")
@RestController
@RequestMapping("/member/point/record")
@Validated
public class AppMemberPointRecordController {

    @Resource
    private MemberPointRecordApplicationService pointRecordApplicationService;

    @GetMapping("/page")
    @Operation(summary = "获得用户积分记录分页")
    public CommonResult<PageResult<AppMemberPointRecordRespVO>> getPointRecordPage(
            @Valid AppMemberPointRecordPageReqVO pageReqVO) {
        PageResult<MemberPointRecord> pageResult = pointRecordApplicationService.getPointRecordPage(
                getLoginUserId(),
                pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length > 0 ? pageReqVO.getCreateTime()[0] : null,
                pageReqVO.getCreateTime() != null && pageReqVO.getCreateTime().length > 1 ? pageReqVO.getCreateTime()[1] : null,
                pageReqVO.getAddStatus(), pageReqVO.getPageNo(), pageReqVO.getPageSize());
        List<AppMemberPointRecordRespVO> list = pageResult.getList().stream().map(r -> {
            AppMemberPointRecordRespVO vo = new AppMemberPointRecordRespVO();
            vo.setId(r.id());
            vo.setUserId(r.userId());
            vo.setBizId(r.bizId());
            vo.setBizType(r.bizType());
            vo.setTitle(r.title());
            vo.setDescription(r.description());
            vo.setPoint(r.point());
            vo.setTotalPoint(r.totalPoint());
            return vo;
        }).collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

}
