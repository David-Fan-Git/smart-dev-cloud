package com.develop.mvp.pk.module.iot.application.property;

import com.develop.mvp.pk.module.iot.application.property.command.PostIotDevicePropertyCommand;
import com.develop.mvp.pk.module.iot.application.property.port.outbound.IotDevicePropertyStoragePort;
import com.develop.mvp.pk.module.iot.application.property.service.IotDevicePropertyApplicationService;
import com.develop.mvp.pk.module.iot.core.enums.IotDeviceMessageMethodEnum;
import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceDO;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IotDevicePropertyApplicationServiceTest {

    @Test
    void duplicateMessageIdDoesNotSavePropertyTwice() {
        FakePropertyStoragePort storagePort = new FakePropertyStoragePort();
        IotDevicePropertyApplicationService service = new IotDevicePropertyApplicationService(storagePort);
        IotDeviceDO device = IotDeviceDO.builder().id(1L).productId(2L).build();
        IotDeviceMessage message = new IotDeviceMessage()
                .setId("msg-1")
                .setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                .setParams(Map.of("temperature", 26.5))
                .setReportTime(LocalDateTime.now());
        PostIotDevicePropertyCommand command = new PostIotDevicePropertyCommand(device, message);

        service.postProperty(command);
        service.postProperty(command);

        assertEquals(1, storagePort.saveCount);
    }

    private static final class FakePropertyStoragePort implements IotDevicePropertyStoragePort {

        private final Set<String> processedMessages = ConcurrentHashMap.newKeySet();
        private int saveCount;

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
            saveCount++;
        }

        @Override
        public Map<String, IotDevicePropertyDO> getLatestDeviceProperties(Long deviceId) {
            return Map.of();
        }

        private String buildKey(String messageId, Long productId, Long deviceId) {
            return productId + ":" + deviceId + ":" + messageId;
        }

    }

}
