package com.develop.mvp.pk.module.iot.domain.product.valueobject;

public record IotProductId(Long value) {

    public static IotProductId of(Long value) {
        return new IotProductId(value);
    }

}
