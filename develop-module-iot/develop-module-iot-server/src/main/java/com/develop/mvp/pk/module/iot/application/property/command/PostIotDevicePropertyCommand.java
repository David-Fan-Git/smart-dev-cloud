package com.develop.mvp.pk.module.iot.application.property.command;

import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceDO;

public record PostIotDevicePropertyCommand(IotDeviceDO device, IotDeviceMessage message) {
}
