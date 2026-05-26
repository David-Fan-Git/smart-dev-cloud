package com.develop.mvp.pk.module.pay.controller.admin.wallet;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletApplicationService;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.wallet.PayWalletPageReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.wallet.PayWalletRespVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.wallet.PayWalletUpdateBalanceReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.wallet.PayWalletUserReqVO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import com.develop.mvp.pk.module.pay.enums.wallet.PayWalletBizTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.develop.mvp.pk.framework.common.enums.UserTypeEnum.MEMBER;
import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_NOT_FOUND;

@Tag(name = "管理后台 - 用户钱包")
@RestController
@RequestMapping("/pay/wallet")
@Validated
@Slf4j
public class PayWalletController {

    @Resource
    private PayWalletApplicationService walletApplicationService;

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('pay:wallet:query')")
    @Operation(summary = "获得用户钱包明细")
    public CommonResult<PayWalletRespVO> getWallet(PayWalletUserReqVO reqVO) {
        PayWallet wallet = walletApplicationService.getOrCreate(reqVO.getUserId(), MEMBER.getValue());
        return success(BeanUtils.toBean(wallet, PayWalletRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员钱包分页")
    @PreAuthorize("@ss.hasPermission('pay:wallet:query')")
    public CommonResult<PageResult<PayWalletRespVO>> getWalletPage(@Valid PayWalletPageReqVO pageVO) {
        PageResult<PayWallet> pageResult = walletApplicationService.getPage(
                pageVO.getUserId(), pageVO.getUserType(), pageVO.getPageNo(), pageVO.getPageSize());
        return success(BeanUtils.toBean(pageResult, PayWalletRespVO.class));
    }

    @PutMapping("/update-balance")
    @Operation(summary = "更新会员用户余额")
    @PreAuthorize("@ss.hasPermission('pay:wallet:update-balance')")
    public CommonResult<Boolean> updateWalletBalance(@Valid @RequestBody PayWalletUpdateBalanceReqVO updateReqVO) {
        // 获得用户钱包
        PayWallet wallet = walletApplicationService.getOrCreate(updateReqVO.getUserId(), MEMBER.getValue());
        if (wallet == null) {
            log.error("[updateWalletBalance]，updateReqVO({}) 用户钱包不存在.", updateReqVO);
            throw exception(WALLET_NOT_FOUND);
        }

        // 更新钱包余额
        walletApplicationService.addBalance(wallet.getId(), String.valueOf(updateReqVO.getUserId()),
                PayWalletBizTypeEnum.UPDATE_BALANCE.getType(), updateReqVO.getBalance(), "后台余额调整");
        return success(true);
    }

}
