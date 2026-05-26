package com.develop.mvp.pk.module.iot.infrastructure.device.persistence;

import com.develop.mvp.pk.module.iot.dal.mysql.device.IotDeviceMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class IotDeviceRepositoryImpl extends com.develop.mvp.pk.module.iot.infrastructure.device.IotDeviceRepositoryImpl {

    public IotDeviceRepositoryImpl(IotDeviceMapper iotDeviceMapper) {
        super(iotDeviceMapper);
    }

}
