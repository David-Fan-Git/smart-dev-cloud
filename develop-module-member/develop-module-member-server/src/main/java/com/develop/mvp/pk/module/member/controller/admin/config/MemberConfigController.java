package com.develop.mvp.pk.module.member.controller.admin.config;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.member.application.config.MemberConfigApplicationService;
import com.develop.mvp.pk.module.member.controller.admin.config.vo.MemberConfigRespVO;
import com.develop.mvp.pk.module.member.controller.admin.config.vo.MemberConfigSaveReqVO;
import com.develop.mvp.pk.module.member.convert.config.MemberConfigConvert;
import com.develop.mvp.pk.module.member.domain.config.MemberConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员设置")
@RestController
@RequestMapping("/member/config")
@Validated
public class MemberConfigController {

    @Resource
    private MemberConfigApplicationService configApplicationService;

    @PutMapping("/save")
    @Operation(summary = "保存会员配置")
    @PreAuthorize("@ss.hasPermission('member:config:save')")
    public CommonResult<Boolean> saveConfig(@Valid @RequestBody MemberConfigSaveReqVO saveReqVO) {
        configApplicationService.saveConfig(saveReqVO.getPointTradeDeductEnable(),
                saveReqVO.getPointTradeDeductUnitPrice(), saveReqVO.getPointTradeDeductMaxPrice(),
                saveReqVO.getPointTradeGivePoint());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员配置")
    @PreAuthorize("@ss.hasPermission('member:config:query')")
    public CommonResult<MemberConfigRespVO> getConfig() {
        MemberConfig config = configApplicationService.getConfig();
        return success(MemberConfigConvert.INSTANCE.convert(config));
    }

}
