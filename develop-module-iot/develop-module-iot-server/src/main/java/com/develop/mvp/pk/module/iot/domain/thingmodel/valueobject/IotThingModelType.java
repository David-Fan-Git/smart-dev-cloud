package com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject;

import com.develop.mvp.pk.module.iot.enums.thingmodel.IotThingModelTypeEnum;

public record IotThingModelType(Integer code) {

    public static IotThingModelType of(Integer code) {
        return new IotThingModelType(code);
    }

    public static IotThingModelType property() {
        return of(IotThingModelTypeEnum.PROPERTY.getType());
    }

    public static IotThingModelType service() {
        return of(IotThingModelTypeEnum.SERVICE.getType());
    }

    public static IotThingModelType event() {
        return of(IotThingModelTypeEnum.EVENT.getType());
    }

    public boolean isProperty() {
        return IotThingModelTypeEnum.PROPERTY.getType().equals(code);
    }

    public boolean isService() {
        return IotThingModelTypeEnum.SERVICE.getType().equals(code);
    }

    public boolean isEvent() {
        return IotThingModelTypeEnum.EVENT.getType().equals(code);
    }

}
