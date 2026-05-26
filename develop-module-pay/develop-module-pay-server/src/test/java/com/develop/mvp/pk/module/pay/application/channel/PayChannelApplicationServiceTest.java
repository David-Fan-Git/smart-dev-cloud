package com.develop.mvp.pk.module.pay.application.channel;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.framework.common.util.json.JsonUtils;
import com.develop.mvp.pk.module.pay.domain.channel.PayChannel;
import com.develop.mvp.pk.module.pay.domain.channel.repository.PayChannelRepository;
import com.develop.mvp.pk.module.pay.enums.PayChannelEnum;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.PayClient;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.PayClientFactory;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.PayClientConfig;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.CHANNEL_EXIST_SAME_CHANNEL_ERROR;
import static com.develop.mvp.pk.module.pay.enums.ErrorCodeConstants.CHANNEL_IS_DISABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class PayChannelApplicationServiceTest {

    @Test
    void create_rejectsDuplicateAppChannel() {
        RecordingChannelRepository repository = new RecordingChannelRepository();
        repository.byAppIdAndCode = new PayChannel(100L, PayChannelEnum.ALIPAY_APP.getCode(), 1L)
                .status(CommonStatusEnum.ENABLE.getStatus());
        PayChannelApplicationService service = newService(repository, null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.create(
                PayChannelEnum.ALIPAY_APP.getCode(), 1L, CommonStatusEnum.ENABLE.getStatus(), 0.5D,
                "remark", "{}"));

        assertEquals(CHANNEL_EXIST_SAME_CHANNEL_ERROR.getCode(), exception.getCode());
    }

    @Test
    void create_parsesConfigAndSavesChannel() {
        RecordingChannelRepository repository = new RecordingChannelRepository();
        PayChannelApplicationService service = newService(repository, null);
        AlipayPayClientConfig config = new AlipayPayClientConfig()
                .setServerUrl("https://openapi.alipay.com/gateway.do")
                .setAppId("app-id").setPrivateKey("private-key")
                .setMode(AlipayPayClientConfig.MODE_CERTIFICATE)
                .setAppCertContent("app-cert").setAlipayPublicCertContent("alipay-cert")
                .setRootCertContent("root-cert");

        PayChannel channel = service.create(PayChannelEnum.ALIPAY_APP.getCode(), 1L,
                CommonStatusEnum.ENABLE.getStatus(), 0.5D, "remark", JsonUtils.toJsonString(config));

        assertEquals(1L, repository.saved.appId());
        assertEquals(PayChannelEnum.ALIPAY_APP.getCode(), repository.saved.code());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), repository.saved.status());
        assertEquals(0.5D, repository.saved.feeRate());
        assertEquals("remark", repository.saved.remark());
        assertEquals(AlipayPayClientConfig.class, repository.saved.config().getClass());
        assertEquals(100L, channel.id());
    }

    @Test
    void valid_rejectsDisabledChannel() {
        RecordingChannelRepository repository = new RecordingChannelRepository();
        repository.byId = new PayChannel(100L, PayChannelEnum.ALIPAY_APP.getCode(), 1L)
                .status(CommonStatusEnum.DISABLE.getStatus());
        PayChannelApplicationService service = newService(repository, null);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.valid(100L));

        assertEquals(CHANNEL_IS_DISABLE.getCode(), exception.getCode());
    }

    @Test
    void getPayClient_validatesChannelAndCreatesClient() {
        RecordingChannelRepository repository = new RecordingChannelRepository();
        PayClient<?> payClient = mock(PayClient.class);
        RecordingPayClientFactory payClientFactory = new RecordingPayClientFactory(payClient);
        AlipayPayClientConfig config = new AlipayPayClientConfig();
        repository.byId = new PayChannel(100L, PayChannelEnum.ALIPAY_APP.getCode(), 1L)
                .status(CommonStatusEnum.ENABLE.getStatus()).config(config);
        PayChannelApplicationService service = newService(repository, payClientFactory);

        PayClient<?> result = service.getPayClient(100L);

        assertSame(payClient, result);
        assertEquals(100L, payClientFactory.id);
        assertEquals(PayChannelEnum.ALIPAY_APP.getCode(), payClientFactory.code);
        assertSame(config, payClientFactory.config);
    }

    private static PayChannelApplicationService newService(RecordingChannelRepository repository,
                                                           PayClientFactory payClientFactory) {
        return new PayChannelApplicationService(repository, payClientFactory, mock(Validator.class));
    }

    private static final class RecordingChannelRepository implements PayChannelRepository {
        private PayChannel byAppIdAndCode;
        private PayChannel byId;
        private PayChannel saved;

        @Override
        public PayChannel save(PayChannel channel) {
            saved = channel;
            return new PayChannel(100L, channel.code(), channel.appId()).status(channel.status())
                    .feeRate(channel.feeRate()).remark(channel.remark()).config(channel.config());
        }

        @Override
        public PayChannel findById(Long id) {
            return byId;
        }

        @Override
        public Optional<PayChannel> findByAppIdAndCode(Long appId, String code) {
            return Optional.ofNullable(byAppIdAndCode);
        }

        @Override
        public List<PayChannel> findByAppIds(Collection<Long> appIds) {
            return List.of();
        }

        @Override
        public List<PayChannel> findEnabledByAppId(Long appId) {
            return List.of();
        }

        @Override
        public void deleteById(Long id) { }
    }

    private static final class RecordingPayClientFactory implements PayClientFactory {
        private final PayClient<?> payClient;
        private Long id;
        private String code;
        private PayClientConfig config;

        private RecordingPayClientFactory(PayClient<?> payClient) {
            this.payClient = payClient;
        }

        @Override
        public PayClient<?> getPayClient(Long channelId) {
            return null;
        }

        @Override
        public <Config extends PayClientConfig> PayClient<?> createOrUpdatePayClient(Long channelId, String channelCode,
                                                                                     Config config) {
            id = channelId;
            code = channelCode;
            this.config = config;
            return payClient;
        }
    }
}
