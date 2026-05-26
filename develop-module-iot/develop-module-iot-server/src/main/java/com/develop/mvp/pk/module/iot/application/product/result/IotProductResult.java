package com.develop.mvp.pk.module.iot.application.product.result;

public record IotProductResult(Long id, String name, String productKey, String productSecret,
                               Boolean registerEnabled, Long categoryId, String icon, String picUrl,
                               String description, Integer status, Integer deviceType, Integer netType,
                               String protocolType, String serializeType) {
}
