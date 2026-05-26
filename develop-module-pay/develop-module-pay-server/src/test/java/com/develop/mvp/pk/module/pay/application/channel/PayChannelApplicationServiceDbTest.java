package com.develop.mvp.pk.module.pay.application.channel;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.common.util.json.JsonUtils;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.pay.dal.dataobject.channel.PayChannelDO;
import com.develop.mvp.pk.module.pay.dal.mysql.channel.PayChannelMapper;
import com.develop.mvp.pk.module.pay.domain.channel.PayChannel;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.PayClientFactory;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.NonePayClientConfig;
import com.develop.mvp.pk.module.pay.infrastructure.channel.PayChannelRepositoryImpl;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({PayChannelApplicationService.class, PayChannelRepositoryImpl.class})
class PayChannelApplicationServiceDbTest extends BaseDbUnitTest {

    @Resource
    private PayChannelApplicationService channelApplicationService;
    @Resource
    private PayChannelMapper channelMapper;

    @MockitoBean
    private PayClientFactory payClientFactory;
    @MockitoBean
    private Validator validator;

    @Test
    void create_returnsGeneratedIdAndPersistsStatusAndRemark() {
        String config = JsonUtils.toJsonString(new NonePayClientConfig());

        PayChannel channel = channelApplicationService.create("mock_none", 1L,
                CommonStatusEnum.DISABLE.getStatus(), 0.5D, "remark", config);

        assertNotNull(channel.id());
        PayChannelDO saved = channelMapper.selectById(channel.id());
        assertEquals("mock_none", saved.getCode());
        assertEquals(1L, saved.getAppId());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), saved.getStatus());
        assertEquals(0.5D, saved.getFeeRate());
        assertEquals("remark", saved.getRemark());
        assertEquals(NonePayClientConfig.class, saved.getConfig().getClass());
    }
}
