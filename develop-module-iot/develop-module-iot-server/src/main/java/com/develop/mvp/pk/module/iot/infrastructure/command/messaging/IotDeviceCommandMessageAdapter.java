package com.develop.mvp.pk.module.iot.infrastructure.command.messaging;

import com.develop.mvp.pk.module.iot.application.command.port.outbound.IotDeviceCommandMessagePort;
import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceDO;
import com.develop.mvp.pk.module.iot.service.device.IotDeviceService;
import com.develop.mvp.pk.module.iot.service.device.message.IotDeviceMessageService;
import org.springframework.stereotype.Component;

@Component
public class IotDeviceCommandMessageAdapter implements IotDeviceCommandMessagePort {

    private final IotDeviceMessageService deviceMessageService;
    private final IotDeviceService deviceService;

    public IotDeviceCommandMessageAdapter(IotDeviceMessageService deviceMessageService,
                                          IotDeviceService deviceService) {
        this.deviceMessageService = deviceMessageService;
        this.deviceService = deviceService;
    }

    @Override
    public IotDeviceMessage sendToDevice(Long deviceId, String method, Object params, String requestId) {
        IotDeviceDO device = deviceService.validateDeviceExists(deviceId);
        return deviceMessageService.sendDeviceMessage(IotDeviceMessage.requestOf(requestId, method, params), device);
    }

    @Override
    public void recordReply(Long deviceId, String requestId, String method, Object data, Integer code, String msg) {
        IotDeviceDO device = deviceService.validateDeviceExists(deviceId);
        deviceMessageService.handleUpstreamDeviceMessage(IotDeviceMessage.replyOf(requestId, method, data, code, msg), device);
    }

}
