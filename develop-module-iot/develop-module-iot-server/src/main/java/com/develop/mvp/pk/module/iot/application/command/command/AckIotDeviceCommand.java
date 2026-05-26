package com.develop.mvp.pk.module.iot.application.command.command;

public record AckIotDeviceCommand(Long deviceId, String requestId, Object data, Integer code, String msg) {
}
