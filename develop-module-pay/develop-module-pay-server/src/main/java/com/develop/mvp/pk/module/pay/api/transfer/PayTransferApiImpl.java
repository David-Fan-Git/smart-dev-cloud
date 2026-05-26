package com.develop.mvp.pk.module.pay.api.transfer;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import com.develop.mvp.pk.framework.common.util.object.BeanUtils;
import com.develop.mvp.pk.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import com.develop.mvp.pk.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import com.develop.mvp.pk.module.pay.api.transfer.dto.PayTransferRespDTO;
import com.develop.mvp.pk.module.pay.application.channel.PayChannelApplicationService;
import com.develop.mvp.pk.module.pay.dal.dataobject.transfer.PayTransferDO;
import com.develop.mvp.pk.module.pay.domain.channel.PayChannel;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.weixin.WxPayClientConfig;
import com.develop.mvp.pk.module.pay.service.transfer.PayTransferService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static com.develop.mvp.pk.framework.common.pojo.CommonResult.success;

/**
 * 转账单 API 实现类
 *
 * @author David
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class PayTransferApiImpl implements PayTransferApi {

    @Resource
    private PayTransferService payTransferService;
    @Resource
    private PayChannelApplicationService channelApplicationService;

    @Override
    public CommonResult<PayTransferCreateRespDTO> createTransfer(PayTransferCreateReqDTO reqDTO) {
        return success(payTransferService.createTransfer(reqDTO));
    }

    @Override
    public CommonResult<PayTransferRespDTO> getTransfer(Long id) {
        PayTransferDO transfer = payTransferService.getTransfer(id);
        if (transfer == null) {
            return null;
        }
        PayChannel channel = channelApplicationService.get(transfer.getChannelId());
        String mchId = null;
        if (channel != null && channel.config() instanceof WxPayClientConfig config) {
            mchId = config.getMchId();
        }
        return success(BeanUtils.toBean(transfer, PayTransferRespDTO.class).setChannelMchId(mchId));
    }

}
