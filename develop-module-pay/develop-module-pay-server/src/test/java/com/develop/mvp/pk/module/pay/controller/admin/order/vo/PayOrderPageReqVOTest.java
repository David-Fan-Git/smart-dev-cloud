package com.develop.mvp.pk.module.pay.controller.admin.order.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayOrderPageReqVOTest {

    @Test
    void channelId_canBeCarriedByPageRequest() {
        PayOrderPageReqVO reqVO = new PayOrderPageReqVO();

        reqVO.setChannelId(10L);

        assertEquals(10L, reqVO.getChannelId());
    }
}
