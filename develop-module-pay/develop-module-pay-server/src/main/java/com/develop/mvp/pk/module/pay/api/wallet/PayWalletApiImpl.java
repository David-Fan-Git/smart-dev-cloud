package com.develop.mvp.pk.module.pay.api.wallet;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.develop.mvp.pk.module.pay.api.wallet.dto.PayWalletRespDTO;
import com.develop.mvp.pk.module.pay.application.wallet.PayWalletApplicationService;
import com.develop.mvp.pk.module.pay.convert.wallet.PayWalletConvert;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.*;

/**
 * 钱包 API 实现类
 *
 * @author David
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class PayWalletApiImpl implements PayWalletApi {

    @Resource
    private PayWalletApplicationService payWalletApplicationService;

    @Override
    public CommonResult<Boolean> addWalletBalance(PayWalletAddBalanceReqDTO reqDTO) {
        PayWallet wallet = payWalletApplicationService.getOrCreate(reqDTO.getUserId(), reqDTO.getUserType());
        payWalletApplicationService.addBalance(wallet.id(), reqDTO.getBizId(), reqDTO.getBizType(), reqDTO.getPrice(), null);
        return success(true);
    }

    @Override
    public CommonResult<PayWalletRespDTO> getOrCreateWallet(Long userId, Integer userType) {
        PayWallet wallet = payWalletApplicationService.getOrCreate(userId, userType);
        return success(PayWalletConvert.INSTANCE.convertToApi(wallet));
    }

}
