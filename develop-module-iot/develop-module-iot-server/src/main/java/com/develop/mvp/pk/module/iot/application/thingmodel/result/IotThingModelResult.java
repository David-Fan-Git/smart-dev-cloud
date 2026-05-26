package com.develop.mvp.pk.module.iot.application.thingmodel.result;

public record IotThingModelResult(Long id, Long productId, String productKey, String identifier,
                                  String name, String description, Integer type, Object property,
                                  Object event, Object service) {
}
