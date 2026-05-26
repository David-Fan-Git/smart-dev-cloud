package com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject;

public record IotThingModelId(Long value) {

    public static IotThingModelId of(Long value) {
        return new IotThingModelId(value);
    }

}
