package com.develop.mvp.pk.module.pay.infrastructure.channel;

import com.develop.mvp.pk.framework.common.enums.CommonStatusEnum;
import com.develop.mvp.pk.framework.test.core.ut.BaseDbUnitTest;
import com.develop.mvp.pk.module.pay.dal.dataobject.channel.PayChannelDO;
import com.develop.mvp.pk.module.pay.dal.mysql.channel.PayChannelMapper;
import com.develop.mvp.pk.module.pay.domain.channel.PayChannel;
import com.develop.mvp.pk.module.pay.framework.pay.core.client.impl.NonePayClientConfig;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(PayChannelRepositoryImpl.class)
class PayChannelRepositoryImplTest extends BaseDbUnitTest {

    @Resource
    private PayChannelRepositoryImpl repository;
    @Resource
    private PayChannelMapper mapper;

    @Test
    void findById_mapsTenantIdForCallbackTenantSwitching() {
        PayChannelDO channel = new PayChannelDO()
                .setCode("mock_none")
                .setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setFeeRate(0.5D)
                .setRemark("remark")
                .setAppId(1L)
                .setConfig(new NonePayClientConfig());
        channel.setTenantId(123L);
        mapper.insert(channel);

        PayChannel result = repository.findById(channel.getId());

        assertEquals(123L, result.tenantId());
    }
}
