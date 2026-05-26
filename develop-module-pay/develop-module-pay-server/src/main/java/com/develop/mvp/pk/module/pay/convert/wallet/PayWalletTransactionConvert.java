package com.develop.mvp.pk.module.pay.convert.wallet;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.transaction.PayWalletTransactionRespVO;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.transaction.AppPayWalletTransactionRespVO;
import com.develop.mvp.pk.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWalletTransaction;
import com.develop.mvp.pk.module.pay.service.wallet.bo.WalletTransactionCreateReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayWalletTransactionConvert {

    PayWalletTransactionConvert INSTANCE = Mappers.getMapper(PayWalletTransactionConvert.class);

    PageResult<PayWalletTransactionRespVO> convertPage2(PageResult<PayWalletTransactionDO> page);

    PageResult<PayWalletTransactionRespVO> convertDomainPage(PageResult<PayWalletTransaction> page);

    PageResult<AppPayWalletTransactionRespVO> convertAppDomainPage(PageResult<PayWalletTransaction> page);

    PayWalletTransactionDO convert(WalletTransactionCreateReqBO bean);

}
