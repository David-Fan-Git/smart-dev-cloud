package com.develop.mvp.pk.module.iot.application.command.result;

public record IotDeviceCommandResult(Long id, Long deviceId, String requestId, String method, Object params,
                                     Integer status, String messageId, String serverId, Object data,
                                     Integer code, String msg) {
}
