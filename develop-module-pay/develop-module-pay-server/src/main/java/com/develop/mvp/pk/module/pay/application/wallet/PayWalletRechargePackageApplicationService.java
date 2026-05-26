package com.develop.mvp.pk.module.pay.application.wallet;

import cn.hutool.core.util.StrUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import com.develop.mvp.pk.module.pay.domain.wallet.repository.PayWalletRechargePackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_RECHARGE_PACKAGE_IS_DISABLE;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_RECHARGE_PACKAGE_NAME_EXISTS;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.WALLET_RECHARGE_PACKAGE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PayWalletRechargePackageApplicationService {

    private final PayWalletRechargePackageRepository rechargePackageRepository;

    public PayWalletRechargePackage get(Long id) {
        return rechargePackageRepository.findById(id);
    }

    public PayWalletRechargePackage valid(Long id) {
        PayWalletRechargePackage rechargePackage = rechargePackageRepository.findById(id);
        if (rechargePackage == null) {
            throw exception(WALLET_RECHARGE_PACKAGE_NOT_FOUND);
        }
        if (CommonStatusEnum.DISABLE.getStatus().equals(rechargePackage.status())) {
            throw exception(WALLET_RECHARGE_PACKAGE_IS_DISABLE);
        }
        return rechargePackage;
    }

    public Long create(String name, Integer payPrice, Integer bonusPrice, Integer status) {
        validateRechargePackageNameUnique(null, name);
        PayWalletRechargePackage rechargePackage = new PayWalletRechargePackage(null)
                .name(name).payPrice(payPrice).bonusPrice(bonusPrice).status(status);
        return rechargePackageRepository.save(rechargePackage).id();
    }

    public void update(Long id, String name, Integer payPrice, Integer bonusPrice, Integer status) {
        validateWalletRechargePackageExists(id);
        validateRechargePackageNameUnique(id, name);
        rechargePackageRepository.save(new PayWalletRechargePackage(id)
                .name(name).payPrice(payPrice).bonusPrice(bonusPrice).status(status));
    }

    public void delete(Long id) {
        validateWalletRechargePackageExists(id);
        rechargePackageRepository.deleteById(id);
    }

    public PageResult<PayWalletRechargePackage> getPage(String name, Integer status, LocalDateTime[] createTime,
                                                        Integer pageNo, Integer pageSize) {
        return rechargePackageRepository.findPage(name, status, createTime, pageNo, pageSize);
    }

    public List<PayWalletRechargePackage> getList(Integer status) {
        return rechargePackageRepository.findListByStatus(status);
    }

    private void validateRechargePackageNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        PayWalletRechargePackage rechargePackage = rechargePackageRepository.findByName(name);
        if (rechargePackage == null) {
            return;
        }
        if (id == null || !id.equals(rechargePackage.id())) {
            throw exception(WALLET_RECHARGE_PACKAGE_NAME_EXISTS);
        }
    }

    private void validateWalletRechargePackageExists(Long id) {
        if (rechargePackageRepository.findById(id) == null) {
            throw exception(WALLET_RECHARGE_PACKAGE_NOT_FOUND);
        }
    }
}
