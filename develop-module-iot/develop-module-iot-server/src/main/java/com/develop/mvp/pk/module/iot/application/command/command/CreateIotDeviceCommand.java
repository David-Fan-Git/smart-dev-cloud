package com.develop.mvp.pk.module.iot.application.command.command;

public record CreateIotDeviceCommand(Long deviceId, String requestId, String method, Object params) {
}
