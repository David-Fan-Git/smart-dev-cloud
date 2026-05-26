package com.develop.mvp.pk.module.member.controller.app.level;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.application.level.MemberLevelApplicationService;
import com.develop.mvp.pk.module.member.controller.app.level.vo.level.AppMemberLevelRespVO;
import com.develop.mvp.pk.module.member.convert.level.MemberLevelConvert;
import com.develop.mvp.pk.module.member.domain.level.MemberLevel;
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

@Tag(name = "用户 App - 会员等级")
@RestController
@RequestMapping("/member/level")
@Validated
public class AppMemberLevelController {

    @Resource
    private MemberLevelApplicationService levelApplicationService;

    @GetMapping("/list")
    @Operation(summary = "获得会员等级列表")
    @PermitAll
    public CommonResult<List<AppMemberLevelRespVO>> getLevelList() {
        List<MemberLevel> result = levelApplicationService.getEnableList();
        return success(MemberLevelConvert.INSTANCE.convertList02FromDomain(result));
    }

}
