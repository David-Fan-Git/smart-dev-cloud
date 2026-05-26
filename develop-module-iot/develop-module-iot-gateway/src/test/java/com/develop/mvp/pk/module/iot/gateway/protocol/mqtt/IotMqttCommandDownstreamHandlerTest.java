package com.develop.mvp.pk.module.iot.gateway.protocol.mqtt;

import com.develop.mvp.pk.module.iot.core.enums.IotDeviceMessageMethodEnum;
import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.core.topic.service.IotDeviceServiceInvokeReqDTO;
import com.develop.mvp.pk.module.iot.gateway.protocol.mqtt.handler.downstream.IotMqttDownstreamHandler;
import com.develop.mvp.pk.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import com.develop.mvp.pk.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IotMqttCommandDownstreamHandlerTest {

    @Test
    void serviceInvokeCommandIsPublishedToDeviceTopic() {
        IotDeviceMessageService deviceMessageService = mock(IotDeviceMessageService.class);
        IotMqttConnectionManager connectionManager = mock(IotMqttConnectionManager.class);
        IotMqttDownstreamHandler handler = new IotMqttDownstreamHandler(deviceMessageService, connectionManager);
        IotMqttConnectionManager.ConnectionInfo connectionInfo = new IotMqttConnectionManager.ConnectionInfo();
        connectionInfo.setDeviceId(1L);
        connectionInfo.setProductKey("pk-1");
        connectionInfo.setDeviceName("device-1");
        IotDeviceMessage message = IotDeviceMessage.requestOf("req-1", IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod(),
                new IotDeviceServiceInvokeReqDTO("switch", Map.of("power", (Object) true), "cmd-1", "trace-1", 1))
                .setId("msg-1")
                .setDeviceId(1L)
                .setServerId("mqtt-1");
        byte[] payload = "payload".getBytes();
        when(connectionManager.getConnectionInfoByDeviceId(1L)).thenReturn(connectionInfo);
        when(deviceMessageService.serializeDeviceMessage(message, "pk-1", "device-1")).thenReturn(payload);
        when(connectionManager.sendToDevice(1L, "/sys/pk-1/device-1/thing/service/invoke", payload,
                MqttQoS.AT_LEAST_ONCE.value(), false)).thenReturn(true);

        handler.handle(message);

        verify(connectionManager).sendToDevice(1L, "/sys/pk-1/device-1/thing/service/invoke", payload,
                MqttQoS.AT_LEAST_ONCE.value(), false);
    }

}
