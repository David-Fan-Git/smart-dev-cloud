package com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject;

public record IotThingModelServiceDefinition(Object value) {

    public static IotThingModelServiceDefinition of(Object value) {
        return new IotThingModelServiceDefinition(value);
    }

    public static IotThingModelServiceDefinition empty() {
        return of(null);
    }

}
