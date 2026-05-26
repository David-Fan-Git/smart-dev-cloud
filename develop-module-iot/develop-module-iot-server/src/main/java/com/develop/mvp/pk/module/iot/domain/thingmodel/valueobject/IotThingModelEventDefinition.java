package com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject;

public record IotThingModelEventDefinition(Object value) {

    public static IotThingModelEventDefinition of(Object value) {
        return new IotThingModelEventDefinition(value);
    }

    public static IotThingModelEventDefinition empty() {
        return of(null);
    }

}
