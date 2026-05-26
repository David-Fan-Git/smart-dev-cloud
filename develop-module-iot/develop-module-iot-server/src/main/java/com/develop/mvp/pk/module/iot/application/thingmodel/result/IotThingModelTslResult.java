package com.develop.mvp.pk.module.iot.application.thingmodel.result;

import java.util.List;

public record IotThingModelTslResult(Long productId, String productKey, List<Object> properties,
                                     List<Object> events, List<Object> services) {
}
