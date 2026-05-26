package com.develop.mvp.pk.module.iot.domain.device.event;

import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceState;

import java.time.LocalDateTime;

public record IotDeviceStateChangedEvent(Long deviceId, String deviceName, IotDeviceState oldState,
                                         IotDeviceState newState, LocalDateTime occurredAt) implements DomainEvent {

    public IotDeviceStateChangedEvent(Long deviceId, String deviceName, IotDeviceState oldState, IotDeviceState newState) {
        this(deviceId, deviceName, oldState, newState, LocalDateTime.now());
    }
}
