package com.develop.mvp.pk.module.iot.application.command.query;

public record IotDeviceCommandQuery(Long deviceId, String requestId, Integer status, Integer pageNo, Integer pageSize) {
}
