package com.develop.mvp.pk.module.iot.domain.thingmodel;

import com.develop.mvp.pk.module.iot.domain.thingmodel.model.IotThingModel;
import com.develop.mvp.pk.module.iot.domain.thingmodel.service.IotThingModelPolicy;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelEventDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelIdentifier;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelName;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelPropertyDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelServiceDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IotThingModelTest {

    @Test
    void createPropertyThingModelMarksPropertyType() {
        IotThingModel thingModel = IotThingModel.create(1L, "pk001", IotThingModelIdentifier.of("temperature"),
                IotThingModelName.of("温度"), "温度属性", IotThingModelType.property(),
                IotThingModelPropertyDefinition.empty(), IotThingModelEventDefinition.empty(),
                IotThingModelServiceDefinition.empty());

        assertTrue(thingModel.isProperty());
        assertFalse(thingModel.isEvent());
        assertFalse(thingModel.isService());
    }

    @Test
    void reservedIdentifierIsRejectedByPolicy() {
        IotThingModelPolicy policy = new IotThingModelPolicy();

        assertFalse(policy.isValidIdentifier(IotThingModelIdentifier.of("set")));
        assertTrue(policy.isValidIdentifier(IotThingModelIdentifier.of("temperature")));
    }

}
