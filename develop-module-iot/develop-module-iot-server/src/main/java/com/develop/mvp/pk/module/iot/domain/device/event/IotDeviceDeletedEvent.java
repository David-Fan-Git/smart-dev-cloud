package com.develop.mvp.pk.module.iot.domain.device.event;

import java.time.LocalDateTime;

public record IotDeviceDeletedEvent(Long deviceId, String deviceName, String productKey, LocalDateTime occurredAt) implements DomainEvent {

    public IotDeviceDeletedEvent(Long deviceId, String deviceName, String productKey) {
        this(deviceId, deviceName, productKey, LocalDateTime.now());
    }
}
