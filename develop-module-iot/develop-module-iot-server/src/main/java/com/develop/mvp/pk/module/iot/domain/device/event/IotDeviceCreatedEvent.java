package com.develop.mvp.pk.module.iot.domain.device.event;

import java.time.LocalDateTime;

public record IotDeviceCreatedEvent(Long deviceId, String deviceName, String productKey, LocalDateTime occurredAt) implements DomainEvent {

    public IotDeviceCreatedEvent(Long deviceId, String deviceName, String productKey) {
        this(deviceId, deviceName, productKey, LocalDateTime.now());
    }
}
