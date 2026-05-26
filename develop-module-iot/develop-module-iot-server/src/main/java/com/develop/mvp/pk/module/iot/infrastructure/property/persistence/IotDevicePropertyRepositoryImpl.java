package com.develop.mvp.pk.module.iot.infrastructure.property.persistence;

import com.develop.mvp.pk.module.iot.application.property.port.outbound.IotDevicePropertyStoragePort;
import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceDO;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import com.develop.mvp.pk.module.iot.service.device.property.IotDevicePropertyService;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class IotDevicePropertyRepositoryImpl implements IotDevicePropertyStoragePort {

    private final IotDevicePropertyService devicePropertyService;
    private final Set<String> processedMessages = ConcurrentHashMap.newKeySet();

    public IotDevicePropertyRepositoryImpl(IotDevicePropertyService devicePropertyService) {
        this.devicePropertyService = devicePropertyService;
    }

    @Override
    public boolean hasProcessedMessage(String messageId, Long productId, Long deviceId) {
        return processedMessages.contains(buildKey(messageId, productId, deviceId));
    }

    @Override
    public void markMessageProcessed(String messageId, Long productId, Long deviceId) {
        processedMessages.add(buildKey(messageId, productId, deviceId));
    }

    @Override
    public void saveDeviceProperty(IotDeviceDO device, IotDeviceMessage message) {
        devicePropertyService.saveDeviceProperty(device, message);
    }

    @Override
    public Map<String, IotDevicePropertyDO> getLatestDeviceProperties(Long deviceId) {
        return devicePropertyService.getLatestDeviceProperties(deviceId);
    }

    private String buildKey(String messageId, Long productId, Long deviceId) {
        return productId + ":" + deviceId + ":" + messageId;
    }

}
