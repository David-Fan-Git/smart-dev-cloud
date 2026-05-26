package com.develop.mvp.pk.module.iot.domain.product.valueobject;

public record IotProductKey(String value) {

    public static IotProductKey of(String value) {
        return new IotProductKey(value);
    }

}
