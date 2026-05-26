package com.develop.mvp.pk.module.iot.domain.product.valueobject;

public record IotProductSecret(String value) {

    public static IotProductSecret of(String value) {
        return new IotProductSecret(value);
    }

}
