package com.develop.mvp.pk.module.pay.convert.wallet;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.pay.api.wallet.dto.PayWalletRespDTO;
import com.develop.mvp.pk.module.pay.controller.admin.wallet.vo.wallet.PayWalletRespVO;
import com.develop.mvp.pk.module.pay.controller.app.wallet.vo.wallet.AppPayWalletRespVO;
import com.develop.mvp.pk.module.pay.dal.dataobject.wallet.PayWalletDO;
import com.develop.mvp.pk.module.pay.domain.wallet.PayWallet;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayWalletConvert {

    PayWalletConvert INSTANCE = Mappers.getMapper(PayWalletConvert.class);

    AppPayWalletRespVO convert(PayWalletDO bean);

    AppPayWalletRespVO convert(PayWallet wallet);

    PayWalletRespDTO convertToApi(PayWallet wallet);

    PayWalletRespVO convert02(PayWalletDO bean);

    PageResult<PayWalletRespVO> convertPage(PageResult<PayWalletDO> page);

}
