package com.develop.mvp.pk.module.pay.controller.app.wallet;

import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletApplicationService;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.wallet.AppPayWalletRespVO;
import com.develop.mvp.pk.module.pay.convert.wallet.PayWalletConvert;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @author David
 */
@Tag(name = "用户 APP - 钱包")
@RestController
@RequestMapping("/pay/wallet")
@Validated
@Slf4j
public class AppPayWalletController {

    @Resource
    private PayWalletApplicationService payWalletApplicationService;

    @GetMapping("/get")
    @Operation(summary = "获取钱包")
    public CommonResult<AppPayWalletRespVO> getPayWallet() {
        PayWallet wallet = payWalletApplicationService.getOrCreate(getLoginUserId(), UserTypeEnum.MEMBER.getValue());
        return success(PayWalletConvert.INSTANCE.convert(wallet));
    }

}
