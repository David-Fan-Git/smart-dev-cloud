package com.develop.mvp.pk.module.iot.infrastructure.property.cache;

import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import com.develop.mvp.pk.module.iot.dal.redis.device.DevicePropertyRedisDAO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IotDeviceLatestPropertyCacheAdapter {

    private final DevicePropertyRedisDAO devicePropertyRedisDAO;

    public IotDeviceLatestPropertyCacheAdapter(DevicePropertyRedisDAO devicePropertyRedisDAO) {
        this.devicePropertyRedisDAO = devicePropertyRedisDAO;
    }

    public Map<String, IotDevicePropertyDO> get(Long deviceId) {
        return devicePropertyRedisDAO.get(deviceId);
    }

}
