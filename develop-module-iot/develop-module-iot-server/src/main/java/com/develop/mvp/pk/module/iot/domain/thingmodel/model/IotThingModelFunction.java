package com.develop.mvp.pk.module.iot.domain.thingmodel.model;

import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelEventDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelPropertyDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelServiceDefinition;

public record IotThingModelFunction(IotThingModelPropertyDefinition property,
                                    IotThingModelEventDefinition event,
                                    IotThingModelServiceDefinition service) {

    public static IotThingModelFunction of(IotThingModelPropertyDefinition property,
                                           IotThingModelEventDefinition event,
                                           IotThingModelServiceDefinition service) {
        return new IotThingModelFunction(property, event, service);
    }

}
