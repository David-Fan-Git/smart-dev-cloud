package com.develop.mvp.pk.module.iot.application.thingmodel.command;

public record UpdateIotThingModelCommand(Long id, Long productId, String productKey, String identifier,
                                         String name, String description, Integer type, Object property,
                                         Object event, Object service) {
}
