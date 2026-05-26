package com.develop.mvp.pk.module.pay.controller.app.wallet;

import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletTransactionApplicationService;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionSummaryRespVO;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionPageReqVO;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionRespVO;
import com.develop.mvp.pk.module.pay.convert.wallet.PayWalletTransactionConvert;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static com.develop.mvp.pk.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 钱包余额明细")
@RestController
@RequestMapping("/pay/wallet-transaction")
@Validated
@Slf4j
public class AppPayWalletTransactionController {

    @Resource
    private PayWalletTransactionApplicationService transactionApplicationService;

    @GetMapping("/page")
    @Operation(summary = "获得钱包流水分页")
    public CommonResult<PageResult<AppPayWalletTransactionRespVO>> getWalletTransactionPage(
            @Valid AppPayWalletTransactionPageReqVO pageReqVO) {
        PageResult<PayWalletTransaction> pageResult = transactionApplicationService.getAppPage(
                getLoginUserId(), UserTypeEnum.MEMBER.getValue(), pageReqVO.getType(), pageReqVO.getCreateTime(),
                pageReqVO.getPageNo(), pageReqVO.getPageSize());
        return success(PayWalletTransactionConvert.INSTANCE.convertAppDomainPage(pageResult));
    }

    @GetMapping("/get-summary")
    @Operation(summary = "获得钱包流水统计")
    @Parameter(name = "times", description = "时间段", required = true)
    public CommonResult<AppPayWalletTransactionSummaryRespVO> getWalletTransactionSummary(
            @RequestParam("createTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime[] createTime) {
        AppPayWalletTransactionSummaryRespVO summary = transactionApplicationService.getSummary(
                getLoginUserId(), UserTypeEnum.MEMBER.getValue(), createTime);
        return success(summary);
    }

}
