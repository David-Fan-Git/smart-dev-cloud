package com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject;

public record IotThingModelPropertyDefinition(Object value) {

    public static IotThingModelPropertyDefinition of(Object value) {
        return new IotThingModelPropertyDefinition(value);
    }

    public static IotThingModelPropertyDefinition empty() {
        return of(null);
    }

}
