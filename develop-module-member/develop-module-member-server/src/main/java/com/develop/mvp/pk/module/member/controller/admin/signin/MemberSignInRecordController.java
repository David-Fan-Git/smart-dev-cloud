package com.develop.mvp.pk.module.member.controller.admin.signin;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.member.application.signin.MemberSignInRecordApplicationService;
import com.develop.mvp.pk.module.member.controller.admin.signin.vo.record.MemberSignInRecordPageReqVO;
import com.develop.mvp.pk.module.member.controller.admin.signin.vo.record.MemberSignInRecordRespVO;
import com.develop.mvp.pk.module.member.convert.signin.MemberSignInRecordConvert;
import com.develop.mvp.pk.module.member.dal.dataobject.user.MemberUserDO;
import com.develop.mvp.pk.module.member.dal.mysql.user.MemberUserMapper;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInRecord;
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
@RequestMapping("/member/sign-in/record")
@Validated
public class MemberSignInRecordController {

    @Resource
    private MemberSignInRecordApplicationService signInRecordApplicationService;
    @Resource
    private MemberUserMapper memberUserMapper;

    @GetMapping("/page")
    @Operation(summary = "获得签到记录分页")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult<PageResult<MemberSignInRecordRespVO>> getSignInRecordPage(@Valid MemberSignInRecordPageReqVO pageVO) {
        PageResult<MemberSignInRecord> pageResult = signInRecordApplicationService.getSignInRecordPage(
                pageVO.getNickname(), pageVO.getUserId(), pageVO.getDay(),
                null, null, pageVO.getPageNo(), pageVO.getPageSize());
        if (CollectionUtils.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        List<MemberUserDO> users = memberUserMapper.selectByIds(
                convertSet(pageResult.getList(), MemberSignInRecord::userId));
        return success(MemberSignInRecordConvert.INSTANCE.convertPageFromDomain(pageResult, users));
    }

}
