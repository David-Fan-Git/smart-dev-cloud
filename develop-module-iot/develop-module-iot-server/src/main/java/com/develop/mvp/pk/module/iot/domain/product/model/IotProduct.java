package com.develop.mvp.pk.module.iot.domain.product.model;

import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductKey;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductSecret;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductStatus;

public final class IotProduct {

    private Long id;
    private String name;
    private IotProductKey productKey;
    private IotProductSecret productSecret;
    private Boolean registerEnabled;
    private Long categoryId;
    private String icon;
    private String picUrl;
    private String description;
    private IotProductStatus status;
    private Integer deviceType;
    private Integer netType;
    private String protocolType;
    private String serializeType;

    private IotProduct() {
    }

    public static IotProduct create(String name, IotProductKey productKey, IotProductSecret productSecret,
                                    Boolean registerEnabled, Long categoryId, String icon, String picUrl,
                                    String description, Integer deviceType, Integer netType,
                                    String protocolType, String serializeType) {
        IotProduct product = new IotProduct();
        product.name = name;
        product.productKey = productKey;
        product.productSecret = productSecret;
        product.registerEnabled = registerEnabled;
        product.categoryId = categoryId;
        product.icon = icon;
        product.picUrl = picUrl;
        product.description = description;
        product.status = IotProductStatus.unpublished();
        product.deviceType = deviceType;
        product.netType = netType;
        product.protocolType = protocolType;
        product.serializeType = serializeType;
        return product;
    }

    public static IotProduct reconstitute(Long id, String name, String productKey, String productSecret,
                                          Boolean registerEnabled, Long categoryId, String icon, String picUrl,
                                          String description, Integer status, Integer deviceType, Integer netType,
                                          String protocolType, String serializeType) {
        IotProduct product = create(name, IotProductKey.of(productKey), IotProductSecret.of(productSecret),
                registerEnabled, categoryId, icon, picUrl, description, deviceType, netType, protocolType, serializeType);
        product.id = id;
        product.status = IotProductStatus.of(status);
        return product;
    }

    public void updateProfile(String name, Boolean registerEnabled, Long categoryId, String icon,
                              String picUrl, String description, Integer deviceType,
                              Integer netType, String protocolType, String serializeType) {
        this.name = name;
        this.registerEnabled = registerEnabled;
        this.categoryId = categoryId;
        this.icon = icon;
        this.picUrl = picUrl;
        this.description = description;
        this.deviceType = deviceType;
        this.netType = netType;
        this.protocolType = protocolType;
        this.serializeType = serializeType;
    }

    public void changeStatus(IotProductStatus status) {
        this.status = status;
    }

    public boolean isPublished() {
        return status != null && status.isPublished();
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public IotProductKey productKey() {
        return productKey;
    }

    public IotProductSecret productSecret() {
        return productSecret;
    }

    public Boolean registerEnabled() {
        return registerEnabled;
    }

    public Long categoryId() {
        return categoryId;
    }

    public String icon() {
        return icon;
    }

    public String picUrl() {
        return picUrl;
    }

    public String description() {
        return description;
    }

    public IotProductStatus status() {
        return status;
    }

    public Integer deviceType() {
        return deviceType;
    }

    public Integer netType() {
        return netType;
    }

    public String protocolType() {
        return protocolType;
    }

    public String serializeType() {
        return serializeType;
    }

}
