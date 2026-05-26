package com.develop.mvp.pk.module.iot.core.topic.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IotDeviceServiceInvokeReqDTOTest {

    @Test
    void commandMetadataIsCarriedWithServiceInvokePayload() {
        IotDeviceServiceInvokeReqDTO dto = new IotDeviceServiceInvokeReqDTO(
                "switch", Map.of("power", true), "cmd-1", "trace-1", 1);

        assertEquals("switch", dto.getIdentifier());
        assertEquals(Map.of("power", true), dto.getInputParams());
        assertEquals("cmd-1", dto.getCommandId());
        assertEquals("trace-1", dto.getTraceId());
        assertEquals(1, dto.getSchemaVersion());
    }

}
