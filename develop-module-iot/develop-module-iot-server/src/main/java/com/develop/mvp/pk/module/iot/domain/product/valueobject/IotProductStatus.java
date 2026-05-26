package com.develop.mvp.pk.module.iot.domain.product.valueobject;

import com.develop.mvp.pk.module.iot.enums.product.IotProductStatusEnum;

public record IotProductStatus(Integer code) {

    public static IotProductStatus of(Integer code) {
        return new IotProductStatus(code);
    }

    public static IotProductStatus unpublished() {
        return of(IotProductStatusEnum.UNPUBLISHED.getStatus());
    }

    public static IotProductStatus published() {
        return of(IotProductStatusEnum.PUBLISHED.getStatus());
    }

    public boolean isPublished() {
        return IotProductStatusEnum.PUBLISHED.getStatus().equals(code);
    }

}
