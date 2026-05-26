package com.develop.mvp.pk.module.pay.convert.wallet;

import java.util.*;

import com.develop.mvp.pk.framework.common.pojo.PageResult;

import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageCreateReqVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageRespVO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.rechargepackage.WalletRechargePackageUpdateReqVO;
import com.develop.mvp.pk.module.pay.dal.dataobject.wallet.PayWalletRechargePackageDO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletRechargePackage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayWalletRechargePackageConvert {

    PayWalletRechargePackageConvert INSTANCE = Mappers.getMapper(PayWalletRechargePackageConvert.class);

    PayWalletRechargePackageDO convert(WalletRechargePackageCreateReqVO bean);

    PayWalletRechargePackageDO convert(WalletRechargePackageUpdateReqVO bean);

    WalletRechargePackageRespVO convert(PayWalletRechargePackageDO bean);

    WalletRechargePackageRespVO convert(PayWalletRechargePackage bean);

    List<WalletRechargePackageRespVO> convertList(List<PayWalletRechargePackageDO> list);

    PageResult<WalletRechargePackageRespVO> convertPage(PageResult<PayWalletRechargePackageDO> page);

    PageResult<WalletRechargePackageRespVO> convertDomainPage(PageResult<PayWalletRechargePackage> page);

}
