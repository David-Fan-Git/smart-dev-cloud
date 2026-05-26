package com.develop.mvp.pk.module.pay.controller.admin.wallet;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageCreateReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackagePageReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageRespVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageUpdateReqVO;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletRechargePackageApplicationService;
import com.develop.mvp.pk.module.pay.convert.wallet.PayWalletRechargePackageConvert;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;


@Tag(name = "管理后台 - 钱包充值套餐")
@RestController
@RequestMapping("/pay/wallet-recharge-package")
@Validated
public class PayWalletRechargePackageController {

    @Resource
    private PayWalletRechargePackageApplicationService rechargePackageApplicationService;

    @PostMapping("/create")
    @Operation(summary = "创建钱包充值套餐")
    @PreAuthorize("@ss.hasPermission('pay:wallet-recharge-package:create')")
    public CommonResult<Long> createWalletRechargePackage(@Valid @RequestBody WalletRechargePackageCreateReqVO createReqVO) {
        return success(rechargePackageApplicationService.create(createReqVO.getName(), createReqVO.getPayPrice(),
                createReqVO.getBonusPrice(), Integer.valueOf(createReqVO.getStatus())));
    }

    @PutMapping("/update")
    @Operation(summary = "更新钱包充值套餐")
    @PreAuthorize("@ss.hasPermission('pay:wallet-recharge-package:update')")
    public CommonResult<Boolean> updateWalletRechargePackage(@Valid @RequestBody WalletRechargePackageUpdateReqVO updateReqVO) {
        rechargePackageApplicationService.update(updateReqVO.getId(), updateReqVO.getName(), updateReqVO.getPayPrice(),
                updateReqVO.getBonusPrice(), Integer.valueOf(updateReqVO.getStatus()));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除钱包充值套餐")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('pay:wallet-recharge-package:delete')")
    public CommonResult<Boolean> deleteWalletRechargePackage(@RequestParam("id") Long id) {
        rechargePackageApplicationService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得钱包充值套餐")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:wallet-recharge-package:query')")
    public CommonResult<WalletRechargePackageRespVO> getWalletRechargePackage(@RequestParam("id") Long id) {
        PayWalletRechargePackage walletRechargePackage = rechargePackageApplicationService.get(id);
        return success(PayWalletRechargePackageConvert.INSTANCE.convert(walletRechargePackage));
    }

    @GetMapping("/page")
    @Operation(summary = "获得钱包充值套餐分页")
    @PreAuthorize("@ss.hasPermission('pay:wallet-recharge-package:query')")
    public CommonResult<PageResult<WalletRechargePackageRespVO>> getWalletRechargePackagePage(@Valid WalletRechargePackagePageReqVO pageVO) {
        PageResult<PayWalletRechargePackage> pageResult = rechargePackageApplicationService.getPage(pageVO.getName(),
                pageVO.getStatus(), pageVO.getCreateTime(), pageVO.getPageNo(), pageVO.getPageSize());
        return success(PayWalletRechargePackageConvert.INSTANCE.convertDomainPage(pageResult));
    }

}
