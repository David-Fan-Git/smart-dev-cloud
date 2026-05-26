package com.develop.mvp.pk.module.member.controller.app.signin;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.application.signin.MemberSignInConfigApplicationService;
import com.develop.mvp.pk.module.member.controller.app.signin.vo.config.AppMemberSignInConfigRespVO;
import com.develop.mvp.pk.module.member.convert.signin.MemberSignInConfigConvert;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 签到规则")
@RestController
@RequestMapping("/member/sign-in/config")
@Validated
public class AppMemberSignInConfigController {

    @Resource
    private MemberSignInConfigApplicationService signInConfigApplicationService;

    @GetMapping("/list")
    @Operation(summary = "获得签到规则列表")
    @PermitAll
    public CommonResult<List<AppMemberSignInConfigRespVO>> getSignInConfigList() {
        List<MemberSignInConfig> pageResult = signInConfigApplicationService.getListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(MemberSignInConfigConvert.INSTANCE.convertList02FromDomain(pageResult));
    }

}
