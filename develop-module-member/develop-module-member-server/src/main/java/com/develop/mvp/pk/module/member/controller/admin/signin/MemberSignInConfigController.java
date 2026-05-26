package com.develop.mvp.pk.module.member.controller.admin.signin;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.application.signin.MemberSignInConfigApplicationService;
import com.develop.mvp.pk.module.member.controller.admin.signin.vo.config.MemberSignInConfigCreateReqVO;
import com.develop.mvp.pk.module.member.controller.admin.signin.vo.config.MemberSignInConfigRespVO;
import com.develop.mvp.pk.module.member.controller.admin.signin.vo.config.MemberSignInConfigUpdateReqVO;
import com.develop.mvp.pk.module.member.convert.signin.MemberSignInConfigConvert;
import com.develop.mvp.pk.module.member.domain.signin.MemberSignInConfig;
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

@Tag(name = "管理后台 - 签到规则")
@RestController
@RequestMapping("/member/sign-in/config")
@Validated
public class MemberSignInConfigController {

    @Resource
    private MemberSignInConfigApplicationService signInConfigApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建签到规则")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:create')")
    public CommonResult<Long> createSignInConfig(@Valid @RequestBody MemberSignInConfigCreateReqVO createReqVO) {
        return success(signInConfigApplicationService.createSignInConfig(createReqVO.getDay(),
                createReqVO.getPoint(), createReqVO.getExperience(), createReqVO.getStatus()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新签到规则")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:update')")
    public CommonResult<Boolean> updateSignInConfig(@Valid @RequestBody MemberSignInConfigUpdateReqVO updateReqVO) {
        signInConfigApplicationService.updateSignInConfig(updateReqVO.getId(), updateReqVO.getDay(),
                updateReqVO.getPoint(), updateReqVO.getExperience(), updateReqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除签到规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:delete')")
    public CommonResult<Boolean> deleteSignInConfig(@RequestParam("id") Long id) {
        signInConfigApplicationService.deleteSignInConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得签到规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:query')")
    public CommonResult<MemberSignInConfigRespVO> getSignInConfig(@RequestParam("id") Long id) {
        MemberSignInConfig signInConfig = signInConfigApplicationService.get(id);
        return success(MemberSignInConfigConvert.INSTANCE.convert(signInConfig));
    }

    @GetMapping("/list")
    @Operation(summary = "获得签到规则列表")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:query')")
    public CommonResult<List<MemberSignInConfigRespVO>> getSignInConfigList() {
        List<MemberSignInConfig> list = signInConfigApplicationService.getList();
        return success(MemberSignInConfigConvert.INSTANCE.convertListFromDomain(list));
    }

}
