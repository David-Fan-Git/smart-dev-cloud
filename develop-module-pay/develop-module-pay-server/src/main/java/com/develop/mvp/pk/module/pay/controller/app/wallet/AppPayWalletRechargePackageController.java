package com.develop.mvp.pk.module.pay.controller.app.wallet;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletRechargePackageApplicationService;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.recharge.AppPayWalletPackageRespVO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 钱包充值套餐")
@RestController
@RequestMapping("/pay/wallet-recharge-package")
@Validated
@Slf4j
public class AppPayWalletRechargePackageController {

    @Resource
    private PayWalletRechargePackageApplicationService rechargePackageApplicationService;

    @GetMapping("/list")
    @Operation(summary = "获得钱包充值套餐列表")
    public CommonResult<List<AppPayWalletPackageRespVO>> getWalletRechargePackageList() {
        List<PayWalletRechargePackage> list = new ArrayList<>(
                rechargePackageApplicationService.getList(CommonStatusEnum.ENABLE.getStatus()));
        list.sort(Comparator.comparingInt(PayWalletRechargePackage::payPrice));
        return success(BeanUtils.toBean(list, AppPayWalletPackageRespVO.class));
    }

}
