package com.develop.mvp.pk.module.pay.controller.admin.transfer.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayTransferPageReqVOTest {

    @Test
    void merchantTransferId_canBeCarriedByPageRequest() {
        PayTransferPageReqVO reqVO = new PayTransferPageReqVO();

        reqVO.setMerchantTransferId("MT202605230001");

        assertEquals("MT202605230001", reqVO.getMerchantTransferId());
    }
}
