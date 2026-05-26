package com.develop.mvp.pk.module.iot.domain.thingmodel.service;

import com.develop.mvp.pk.module.iot.domain.thingmodel.valueobject.IotThingModelIdentifier;

import java.util.Set;

public class IotThingModelPolicy {

    private static final Set<String> RESERVED_IDENTIFIERS = Set.of("set", "get", "post", "property", "event", "time", "value");

    public boolean isValidIdentifier(IotThingModelIdentifier identifier) {
        return identifier != null && !RESERVED_IDENTIFIERS.contains(identifier.value());
    }

}
