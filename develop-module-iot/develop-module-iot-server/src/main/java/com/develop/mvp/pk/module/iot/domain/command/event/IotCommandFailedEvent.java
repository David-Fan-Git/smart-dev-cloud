package com.develop.mvp.pk.module.iot.domain.command.event;

public record IotCommandFailedEvent(Long deviceId, String requestId, Integer code, String msg) {
}
