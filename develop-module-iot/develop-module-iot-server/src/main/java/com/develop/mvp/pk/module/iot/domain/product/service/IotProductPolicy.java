package com.develop.mvp.pk.module.iot.domain.product.service;

import com.develop.mvp.pk.module.iot.domain.product.model.IotProduct;

public class IotProductPolicy {

    public boolean canDelete(IotProduct product) {
        return product != null && !product.isPublished();
    }

}
