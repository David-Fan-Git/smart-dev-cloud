package com.develop.mvp.pk.module.iot.application.property.result;

import java.time.LocalDateTime;

public record IotDeviceLatestPropertyResult(String identifier, Object value, LocalDateTime updateTime) {
}
