package com.develop.mvp.pk.module.system.controller.admin.ip;

import com.develop.mvp.pk.framework.common.pojo.CommonResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AreaControllerTest {

    private final AreaController areaController = new AreaController();

    @Test
    void getAreaByIpWithInvalidIpReturnsUnknown() {
        CommonResult<String> result = areaController.getAreaByIp("ct20260527141555");

        assertEquals(0, result.getCode());
        assertEquals("未知", result.getData());
    }
}
