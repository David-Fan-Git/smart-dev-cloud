package com.develop.mvp.pk.module.pay.application.channel;
// DDD 角色：支付渠道应用服务 - AggregateRoot_Pay_Skill
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.util.json.JsonUtils;
import com.develop.mvp.pk.module.pay.domain.channel.PayChannel;
import com.develop.mvp.pk.module.pay.domain.channel.PayChannelFactory;
import com.develop.mvp.pk.module.pay.domain.channel.repository.PayChannelRepository;
import com.develop.mvp.pk.module.pay.enums.PayChannelEnum;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.PayClient;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.PayClientConfig;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.PayClientFactory;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.NonePayClientConfig;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.weixin.WxPayClientConfig;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;
import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.*;
@Service
@RequiredArgsConstructor
public class PayChannelApplicationService {
    private final PayChannelRepository repo;
    private final PayClientFactory payClientFactory;
    private final Validator validator;
    @Transactional public PayChannel create(String code, Long appId, Double feeRate, Object config) {
        return create(code, appId, CommonStatusEnum.ENABLE.getStatus(), feeRate, null, config);
    }
    @Transactional public PayChannel create(String code, Long appId, Integer status, Double feeRate, String remark, Object config) {
        if (repo.findByAppIdAndCode(appId, code).isPresent()) throw exception(CHANNEL_EXIST_SAME_CHANNEL_ERROR);
        PayChannel channel = PayChannelFactory.create(null, code, appId, feeRate, parseConfig(code, config));
        channel.status(status).remark(remark);
        return repo.save(channel);
    }
    @Transactional public void update(Long id, Double feeRate, String remark, Object config) {
        PayChannel channel = requireExisting(id);
        channel.feeRate(feeRate).remark(remark).config(parseConfig(channel.code(), config));
        repo.save(channel);
    }
    @Transactional public void update(Long id, Integer status, Double feeRate, String remark, Object config) {
        PayChannel channel = requireExisting(id);
        channel.status(status).feeRate(feeRate).remark(remark).config(parseConfig(channel.code(), config));
        repo.save(channel);
    }
    @Transactional public void delete(Long id) { requireExisting(id); repo.deleteById(id); }
    public PayChannel get(Long id) { return repo.findById(id); }
    public PayChannel getByAppIdAndCode(Long appId, String code) {
        return repo.findByAppIdAndCode(appId, code).orElse(null);
    }
    public PayChannel valid(Long id) { PayChannel channel = repo.findById(id); valid(channel); return channel; }
    public PayChannel valid(Long appId, String code) {
        PayChannel channel = repo.findByAppIdAndCode(appId, code).orElse(null); valid(channel); return channel;
    }
    public List<PayChannel> getListByAppIds(Collection<Long> appIds) { return repo.findByAppIds(appIds); }
    public List<PayChannel> getEnabledList(Long appId) { return repo.findEnabledByAppId(appId); }
    public PayClient<?> getPayClient(Long id) {
        PayChannel channel = valid(id);
        return payClientFactory.createOrUpdatePayClient(id, channel.code(), (PayClientConfig) channel.config());
    }
    private PayChannel requireExisting(Long id) {
        PayChannel channel = repo.findById(id);
        if (channel == null) throw exception(CHANNEL_NOT_FOUND);
        return channel;
    }
    private void valid(PayChannel channel) {
        if (channel == null) throw exception(CHANNEL_NOT_FOUND);
        if (CommonStatusEnum.DISABLE.getStatus().equals(channel.status())) throw exception(CHANNEL_IS_DISABLE);
    }
    private PayClientConfig parseConfig(String code, Object config) {
        if (config instanceof PayClientConfig payClientConfig) {
            payClientConfig.validate(validator);
            return payClientConfig;
        }
        Class<? extends PayClientConfig> payClass = PayChannelEnum.isAlipay(code) ? AlipayPayClientConfig.class
                : PayChannelEnum.isWeixin(code) ? WxPayClientConfig.class : NonePayClientConfig.class;
        if (ObjectUtil.isNull(payClass)) throw exception(CHANNEL_NOT_FOUND);
        PayClientConfig payClientConfig = JsonUtils.parseObject2(String.valueOf(config), payClass);
        Assert.notNull(payClientConfig);
        payClientConfig.validate(validator);
        return payClientConfig;
    }
}
