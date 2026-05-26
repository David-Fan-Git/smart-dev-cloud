package com.develop.mvp.pk.module.iot.core.topic.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IotDeviceServiceInvokeRespDTOTest {

    @Test
    void commandAckMetadataIsCarriedWithServiceInvokeResponse() {
        IotDeviceServiceInvokeRespDTO dto = new IotDeviceServiceInvokeRespDTO(
                "cmd-1", "trace-1", 1, Map.of("success", true));

        assertEquals("cmd-1", dto.getCommandId());
        assertEquals("trace-1", dto.getTraceId());
        assertEquals(1, dto.getSchemaVersion());
        assertEquals(Map.of("success", true), dto.getOutputParams());
    }

}
