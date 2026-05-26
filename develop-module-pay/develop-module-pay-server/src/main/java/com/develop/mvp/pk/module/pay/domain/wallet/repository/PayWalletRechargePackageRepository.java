package com.develop.mvp.pk.module.pay.domain.wallet.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;

import java.time.LocalDateTime;
import java.util.List;

public interface PayWalletRechargePackageRepository {

    PayWalletRechargePackage save(PayWalletRechargePackage rechargePackage);

    PayWalletRechargePackage findById(Long id);

    PayWalletRechargePackage findByName(String name);

    void deleteById(Long id);

    PageResult<PayWalletRechargePackage> findPage(String name, Integer status, LocalDateTime[] createTime,
                                                  Integer pageNo, Integer pageSize);

    List<PayWalletRechargePackage> findListByStatus(Integer status);
}
