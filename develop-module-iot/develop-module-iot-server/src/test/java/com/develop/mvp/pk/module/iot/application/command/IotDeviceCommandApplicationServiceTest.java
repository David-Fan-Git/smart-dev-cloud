package com.develop.mvp.pk.module.iot.application.command;

import com.develop.mvp.pk.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.develop.mvp.pk.module.iot.application.command.command.AckIotDeviceCommand;
import com.develop.mvp.pk.module.iot.application.command.port.outbound.IotDeviceCommandMessagePort;
import com.develop.mvp.pk.module.iot.application.command.service.IotDeviceCommandApplicationService;
import com.develop.mvp.pk.module.iot.domain.command.model.IotDeviceCommand;
import com.develop.mvp.pk.module.iot.domain.command.repository.IotDeviceCommandRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IotDeviceCommandApplicationServiceTest {

    @Test
    void duplicateAckDoesNotRecordReplyTwice() {
        IotDeviceCommand command = IotDeviceCommand.create(1L, "req-1", "thing.service.invoke", Map.of("switch", true));
        command.markPending();
        command.markSent("msg-1", "server-1");
        command.ack(Map.of("success", true), GlobalErrorCodeConstants.SUCCESS.getCode(), GlobalErrorCodeConstants.SUCCESS.getMsg());
        IotDeviceCommandRepository repository = mock(IotDeviceCommandRepository.class);
        IotDeviceCommandMessagePort messagePort = mock(IotDeviceCommandMessagePort.class);
        when(repository.findByRequestId(1L, "req-1")).thenReturn(command);
        IotDeviceCommandApplicationService service = new IotDeviceCommandApplicationService(repository, messagePort);

        service.handleAck(new AckIotDeviceCommand(1L, "req-1", Map.of("success", false), 500, "duplicate failure"));

        verify(repository, never()).save(command);
        verify(messagePort, never()).recordReply(1L, "req-1", "thing.service.invoke",
                Map.of("success", false), 500, "duplicate failure");
    }

}
