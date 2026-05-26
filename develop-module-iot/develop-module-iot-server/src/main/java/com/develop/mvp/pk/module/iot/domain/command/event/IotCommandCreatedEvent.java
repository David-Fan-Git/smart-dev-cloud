package com.develop.mvp.pk.module.iot.domain.command.event;

public record IotCommandCreatedEvent(Long deviceId, String requestId, String method) {
}
