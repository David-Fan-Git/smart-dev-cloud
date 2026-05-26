package com.develop.mvp.pk.module.iot.domain.command;

import com.develop.mvp.pk.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.develop.mvp.pk.module.iot.domain.command.model.IotDeviceCommand;
import com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IotDeviceCommandTest {

    @Test
    void commandCanBeAckedAfterSent() {
        IotDeviceCommand command = IotDeviceCommand.create(1L, "req-1", "thing.service.invoke", Map.of("switch", true));

        command.markPending();
        command.markSent("msg-1", "server-1");
        command.ack(Map.of("success", true), GlobalErrorCodeConstants.SUCCESS.getCode(), GlobalErrorCodeConstants.SUCCESS.getMsg());

        assertEquals(IotCommandStatus.acked(), command.status());
        assertTrue(command.isFinalState());
        assertEquals("req-1", command.requestId().value());
        assertEquals("thing.service.invoke", command.method());
        assertEquals(Map.of("success", true), command.data());
    }

    @Test
    void commandCanFailAfterSent() {
        IotDeviceCommand command = IotDeviceCommand.create(1L, "req-2", "thing.service.invoke", Map.of("switch", true));

        command.markPending();
        command.markSent("msg-2", "server-1");
        command.fail(500, "device error");

        assertEquals(IotCommandStatus.failed(), command.status());
        assertTrue(command.isFinalState());
        assertEquals(500, command.code());
        assertEquals("device error", command.msg());
    }

    @Test
    void commandCanTimeoutAfterSent() {
        IotDeviceCommand command = IotDeviceCommand.create(1L, "req-3", "thing.service.invoke", Map.of("switch", true));

        command.markPending();
        command.markSent("msg-3", "server-1");
        command.timeout();

        assertEquals(IotCommandStatus.timeout(), command.status());
        assertTrue(command.isFinalState());
    }

    @Test
    void duplicateAckKeepsAckedStateStable() {
        IotDeviceCommand command = IotDeviceCommand.create(1L, "req-4", "thing.service.invoke", Map.of("switch", true));

        command.markPending();
        command.markSent("msg-4", "server-1");
        command.ack(Map.of("success", true), GlobalErrorCodeConstants.SUCCESS.getCode(), GlobalErrorCodeConstants.SUCCESS.getMsg());
        command.ack(Map.of("success", false), 500, "duplicate failure");

        assertEquals(IotCommandStatus.acked(), command.status());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), command.code());
        assertEquals(GlobalErrorCodeConstants.SUCCESS.getMsg(), command.msg());
        assertEquals(Map.of("success", true), command.data());
    }

    @Test
    void ackDoesNotOverrideFailedFinalState() {
        IotDeviceCommand command = IotDeviceCommand.create(1L, "req-5", "thing.service.invoke", Map.of("switch", true));

        command.markPending();
        command.markSent("msg-5", "server-1");
        command.fail(500, "device error");
        command.ack(Map.of("success", true), GlobalErrorCodeConstants.SUCCESS.getCode(), GlobalErrorCodeConstants.SUCCESS.getMsg());

        assertEquals(IotCommandStatus.failed(), command.status());
        assertEquals(500, command.code());
        assertEquals("device error", command.msg());
    }

}
