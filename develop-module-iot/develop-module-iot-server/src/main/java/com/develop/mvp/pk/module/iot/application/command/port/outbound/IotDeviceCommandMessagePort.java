package com.develop.mvp.pk.module.iot.application.command.port.outbound;

import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;

public interface IotDeviceCommandMessagePort {

    IotDeviceMessage sendToDevice(Long deviceId, String method, Object params, String requestId);

    void recordReply(Long deviceId, String requestId, String method, Object data, Integer code, String msg);

}
