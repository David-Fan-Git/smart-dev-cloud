package com.develop.mvp.pk.module.member.controller.app.signin;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.application.signin.MemberSignInRecordApplicationService;
import com.develop.mvp.pk.module.member.controller.app.signin.vo.record.AppMemberSignInRecordRespVO;
import com.develop.mvp.pk.module.member.controller.app.signin.vo.record.AppMemberSignInRecordSummaryRespVO;
import com.develop.mvp.pk.module.member.convert.signin.MemberSignInRecordConvert;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 签到记录")
@RestController
@RequestMapping("/member/sign-in/record")
@Validated
public class AppMemberSignInRecordController {

    @Resource
    private MemberSignInRecordApplicationService signInRecordApplicationService;

    @GetMapping("/get-summary")
    @Operation(summary = "获得个人签到统计")
    public CommonResult<AppMemberSignInRecordSummaryRespVO> getSignInRecordSummary() {
        return success(signInRecordApplicationService.getSignInRecordSummary(getLoginUserId()));
    }

    @PostMapping("/create")
    @Operation(summary = "签到")
    public CommonResult<AppMemberSignInRecordRespVO> createSignInRecord() {
        MemberSignInRecord record = signInRecordApplicationService.createSignRecord(getLoginUserId());
        return success(MemberSignInRecordConvert.INSTANCE.coverRecordToAppRecordVo(record));
    }

    @GetMapping("/page")
    @Operation(summary = "获得签到记录分页")
    public CommonResult<PageResult<AppMemberSignInRecordRespVO>> getSignRecordPage(@Valid PageParam pageParam) {
        PageResult<MemberSignInRecord> pageResult = signInRecordApplicationService.getSignRecordPage(
                getLoginUserId(), pageParam.getPageNo(), pageParam.getPageSize());
        return success(MemberSignInRecordConvert.INSTANCE.convertPage02FromDomain(pageResult));
    }

}
