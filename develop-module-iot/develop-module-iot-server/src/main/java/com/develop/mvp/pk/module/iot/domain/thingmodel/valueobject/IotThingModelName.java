package com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject;

public record IotThingModelName(String value) {

    public static IotThingModelName of(String value) {
        return new IotThingModelName(value);
    }

}
