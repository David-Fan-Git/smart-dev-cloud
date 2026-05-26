package com.develop.mvp.pk.module.iot.application.property.service;

import com.develop.mvp.pk.module.iot.application.property.command.PostIotDevicePropertyCommand;
import com.develop.mvp.pk.module.iot.application.property.port.inbound.IotDevicePropertyUseCase;
import com.develop.mvp.pk.module.iot.application.property.port.outbound.IotDevicePropertyStoragePort;
import com.develop.mvp.pk.module.iot.application.property.query.IotDeviceLatestPropertyQuery;
import com.develop.mvp.pk.module.iot.application.property.result.IotDeviceLatestPropertyResult;
import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceDO;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class IotDevicePropertyApplicationService implements IotDevicePropertyUseCase {

    private final IotDevicePropertyStoragePort storagePort;

    public IotDevicePropertyApplicationService(IotDevicePropertyStoragePort storagePort) {
        this.storagePort = storagePort;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postProperty(PostIotDevicePropertyCommand command) {
        IotDeviceDO device = command.device();
        IotDeviceMessage message = command.message();
        if (storagePort.hasProcessedMessage(message.getId(), device.getProductId(), device.getId())) {
            return;
        }
        storagePort.saveDeviceProperty(device, message);
        storagePort.markMessageProcessed(message.getId(), device.getProductId(), device.getId());
    }

    @Override
    public List<IotDeviceLatestPropertyResult> getLatestProperties(IotDeviceLatestPropertyQuery query) {
        Map<String, IotDevicePropertyDO> properties = storagePort.getLatestDeviceProperties(query.deviceId());
        return properties.entrySet().stream()
                .map(entry -> new IotDeviceLatestPropertyResult(entry.getKey(), entry.getValue().getValue(),
                        entry.getValue().getUpdateTime()))
                .toList();
    }

}
