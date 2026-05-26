package com.develop.mvp.pk.module.iot.infrastructure.thingmodel.external;

import com.develop.mvp.pk.module.iot.application.thingmodel.port.outbound.IotThingModelModbusPointPort;
import com.develop.mvp.pk.module.iot.service.device.IotDeviceModbusPointService;
import org.springframework.stereotype.Component;

@Component
public class IotThingModelModbusPointAdapter implements IotThingModelModbusPointPort {

    private final IotDeviceModbusPointService deviceModbusPointService;

    public IotThingModelModbusPointAdapter(IotDeviceModbusPointService deviceModbusPointService) {
        this.deviceModbusPointService = deviceModbusPointService;
    }

    @Override
    public void updateByThingModel(Long thingModelId, String identifier, String name) {
        deviceModbusPointService.updateDeviceModbusPointByThingModel(thingModelId, identifier, name);
    }

}
