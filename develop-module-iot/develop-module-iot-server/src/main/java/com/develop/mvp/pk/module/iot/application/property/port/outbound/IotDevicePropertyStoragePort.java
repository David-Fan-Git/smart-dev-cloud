package com.develop.mvp.pk.module.iot.application.property.port.outbound;

import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceDO;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDevicePropertyDO;

import java.util.Map;

public interface IotDevicePropertyStoragePort {

    boolean hasProcessedMessage(String messageId, Long productId, Long deviceId);

    void markMessageProcessed(String messageId, Long productId, Long deviceId);

    void saveDeviceProperty(IotDeviceDO device, IotDeviceMessage message);

    Map<String, IotDevicePropertyDO> getLatestDeviceProperties(Long deviceId);

}
