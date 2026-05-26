package com.develop.mvp.pk.module.iot.domain.command.event;

public record IotCommandAckedEvent(Long deviceId, String requestId, Object data, Integer code, String msg) {
}
