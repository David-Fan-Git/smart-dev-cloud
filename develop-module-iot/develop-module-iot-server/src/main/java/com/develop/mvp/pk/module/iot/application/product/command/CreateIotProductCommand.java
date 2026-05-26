package com.develop.mvp.pk.module.iot.application.product.command;

public record CreateIotProductCommand(String name, String productKey, Boolean registerEnabled,
                                      Long categoryId, String icon, String picUrl, String description,
                                      Integer deviceType, Integer netType, String protocolType,
                                      String serializeType) {
}
