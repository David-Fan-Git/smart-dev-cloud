package com.develop.mvp.pk.module.iot.application.thingmodel.port.outbound;

public interface IotThingModelModbusPointPort {

    void updateByThingModel(Long thingModelId, String identifier, String name);

}
