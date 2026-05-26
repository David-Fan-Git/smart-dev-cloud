package com.develop.mvp.pk.module.iot.application.thingmodel.query;

public record IotThingModelPageQuery(Long productId, String identifier, String name, Integer type,
                                     Integer pageNo, Integer pageSize) {
}
