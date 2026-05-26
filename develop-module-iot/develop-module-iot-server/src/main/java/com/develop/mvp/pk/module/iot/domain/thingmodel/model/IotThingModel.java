package com.develop.mvp.pk.module.iot.domain.thingmodel.model;

import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelEventDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelIdentifier;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelName;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelPropertyDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelServiceDefinition;
import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelType;

public final class IotThingModel {

    private Long id;
    private Long productId;
    private String productKey;
    private IotThingModelIdentifier identifier;
    private IotThingModelName name;
    private String description;
    private IotThingModelType type;
    private IotThingModelPropertyDefinition property;
    private IotThingModelEventDefinition event;
    private IotThingModelServiceDefinition service;

    private IotThingModel() {
    }

    public static IotThingModel create(Long productId, String productKey, IotThingModelIdentifier identifier,
                                       IotThingModelName name, String description, IotThingModelType type,
                                       IotThingModelPropertyDefinition property,
                                       IotThingModelEventDefinition event,
                                       IotThingModelServiceDefinition service) {
        IotThingModel thingModel = new IotThingModel();
        thingModel.productId = productId;
        thingModel.productKey = productKey;
        thingModel.identifier = identifier;
        thingModel.name = name;
        thingModel.description = description;
        thingModel.type = type;
        thingModel.property = property;
        thingModel.event = event;
        thingModel.service = service;
        return thingModel;
    }

    public static IotThingModel reconstitute(Long id, Long productId, String productKey, String identifier,
                                             String name, String description, Integer type, Object property,
                                             Object event, Object service) {
        IotThingModel thingModel = create(productId, productKey, IotThingModelIdentifier.of(identifier),
                IotThingModelName.of(name), description, IotThingModelType.of(type),
                IotThingModelPropertyDefinition.of(property), IotThingModelEventDefinition.of(event),
                IotThingModelServiceDefinition.of(service));
        thingModel.id = id;
        return thingModel;
    }

    public void update(IotThingModelIdentifier identifier, IotThingModelName name, String description,
                       IotThingModelType type, IotThingModelPropertyDefinition property,
                       IotThingModelEventDefinition event, IotThingModelServiceDefinition service) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.type = type;
        this.property = property;
        this.event = event;
        this.service = service;
    }

    public boolean isProperty() {
        return type != null && type.isProperty();
    }

    public boolean isEvent() {
        return type != null && type.isEvent();
    }

    public boolean isService() {
        return type != null && type.isService();
    }

    public Long id() {
        return id;
    }

    public Long productId() {
        return productId;
    }

    public String productKey() {
        return productKey;
    }

    public IotThingModelIdentifier identifier() {
        return identifier;
    }

    public IotThingModelName name() {
        return name;
    }

    public String description() {
        return description;
    }

    public IotThingModelType type() {
        return type;
    }

    public IotThingModelPropertyDefinition property() {
        return property;
    }

    public IotThingModelEventDefinition event() {
        return event;
    }

    public IotThingModelServiceDefinition service() {
        return service;
    }

}
