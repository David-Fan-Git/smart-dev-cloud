package com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject;

public record IotThingModelIdentifier(String value) {

    public static IotThingModelIdentifier of(String value) {
        return new IotThingModelIdentifier(value);
    }

}
