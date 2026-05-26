package com.develop.mvp.pk.module.iot.application.product.command;

public record UpdateIotProductCommand(Long id, String name, Boolean registerEnabled,
                                      Long categoryId, String icon, String picUrl, String description,
                                      Integer deviceType, Integer netType, String protocolType,
                                      String serializeType) {
}
