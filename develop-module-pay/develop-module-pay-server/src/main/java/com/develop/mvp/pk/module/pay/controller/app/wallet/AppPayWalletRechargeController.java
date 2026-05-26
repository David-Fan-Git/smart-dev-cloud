package com.develop.mvp.pk.module.pay.controller.app.wallet;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.enums.UserTypeEnum;
import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.pojo.PageParam;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletRechargeApplicationService;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.recharge.AppPayWalletRechargeCreateReqVO;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.recharge.AppPayWalletRechargeCreateRespVO;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.recharge.AppPayWalletRechargeRespVO;
import com.develop.mvp.pk.module.pay.convert.wallet.PayWalletRechargeConvert;
import com.develop.mvp.pk.module.pay.dal.dataobject.order.PayOrderDO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRecharge;
import com.develop.mvp.pk.module.pay.service.order.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.util.List;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;
import static com.develop.mvp.pk.framework.common.util.collection.CollectionUtils.convertList;
import static com.develop.mvp.pk.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.develop.mvp.pk.framework.web.core.util.WebFrameworkUtils.getLoginUserType;

@Tag(name = "用户 APP - 钱包充值")
@RestController
@RequestMapping("/pay/wallet-recharge")
@Validated
@Slf4j
public class AppPayWalletRechargeController {

    @Resource
    private PayWalletRechargeApplicationService walletRechargeApplicationService;
    @Resource
    private PayOrderService payOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建钱包充值记录（发起充值）")
    public CommonResult<AppPayWalletRechargeCreateRespVO> createWalletRecharge(
            @Valid @RequestBody  AppPayWalletRechargeCreateReqVO reqVO) {
        PayWalletRecharge walletRecharge = walletRechargeApplicationService.create(
                getLoginUserId(), getLoginUserType(), getClientIP(), reqVO.getPayPrice(), reqVO.getPackageId());
        return success(PayWalletRechargeConvert.INSTANCE.convert(walletRecharge));
    }

    @GetMapping("/page")
    @Operation(summary = "获得钱包充值记录分页")
    public CommonResult<PageResult<AppPayWalletRechargeRespVO>> getWalletRechargePage(@Valid PageParam pageReqVO) {
        PageResult<PayWalletRecharge> pageResult = walletRechargeApplicationService.getPage(
                getLoginUserId(), UserTypeEnum.MEMBER.getValue(), pageReqVO.getPageNo(), pageReqVO.getPageSize(), true);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 拼接数据
        List<PayOrderDO> payOrderList = payOrderService.getOrderList(
                convertList(pageResult.getList(), PayWalletRecharge::payOrderId));
        return success(PayWalletRechargeConvert.INSTANCE.convertDomainPage(pageResult, payOrderList));
    }

}
