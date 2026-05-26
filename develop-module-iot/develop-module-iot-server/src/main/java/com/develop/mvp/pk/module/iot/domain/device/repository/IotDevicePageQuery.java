package com.develop.mvp.pk.module.iot.domain.device.repository;

import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceState;

public record IotDevicePageQuery(
        String deviceName,
        String nickname,
        Long productId,
        Integer deviceType,
        IotDeviceState state,
        Long groupId,
        Long gatewayId,
        Integer pageNo,
        Integer pageSize
) {}
