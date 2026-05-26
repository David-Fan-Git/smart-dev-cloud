package com.develop.mvp.pk.module.iot.domain.device;

import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceId;
import com.develop.mvp.pk.module.iot.domain.device.valueobject.IotDeviceState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class IotDeviceFactory {

    public static IotDevice create(String deviceName, String nickname, String serialNumber,
                                    String picUrl, Set<Long> groupIds, Long productId,
                                    String productKey, Integer deviceType, Long gatewayId,
                                    String deviceSecret, String config,
                                    BigDecimal latitude, BigDecimal longitude) {
        return new IotDevice(null, deviceName, nickname, serialNumber, picUrl, groupIds,
                productId, productKey, deviceType, gatewayId, IotDeviceState.INACTIVE,
                null, null, null, null, deviceSecret, config, latitude, longitude);
    }

    public static IotDevice reconstitute(Long id, String deviceName, String nickname,
                                          String serialNumber, String picUrl, Set<Long> groupIds,
                                          Long productId, String productKey, Integer deviceType,
                                          Long gatewayId, Integer state, LocalDateTime onlineTime,
                                          LocalDateTime offlineTime, LocalDateTime activeTime,
                                          Long firmwareId, String deviceSecret, String config,
                                          BigDecimal latitude, BigDecimal longitude) {
        return new IotDevice(id, deviceName, nickname, serialNumber, picUrl, groupIds,
                productId, productKey, deviceType, gatewayId,
                state != null ? IotDeviceState.of(state) : IotDeviceState.INACTIVE,
                onlineTime, offlineTime, activeTime, firmwareId, deviceSecret, config,
                latitude, longitude);
    }
}
