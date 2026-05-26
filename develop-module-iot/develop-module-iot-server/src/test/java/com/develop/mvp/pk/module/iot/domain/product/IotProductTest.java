package com.develop.mvp.pk.module.iot.domain.product;

import com.develop.mvp.pk.module.iot.domain.product.model.IotProduct;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductKey;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductSecret;
import com.develop.mvp.pk.module.iot.enums.product.IotProductStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IotProductTest {

    @Test
    void createProductDefaultsToUnpublished() {
        IotProduct product = IotProduct.create("温湿度", IotProductKey.of("abc123"), IotProductSecret.of("secret"),
                false, 1L, "icon", "pic", "desc", 0, 0, "mqtt", "json");

        assertEquals(IotProductStatusEnum.UNPUBLISHED.getStatus(), product.status().code());
    }

    @Test
    void changeStatusPublishesProduct() {
        IotProduct product = IotProduct.create("温湿度", IotProductKey.of("abc123"), IotProductSecret.of("secret"),
                false, 1L, "icon", "pic", "desc", 0, 0, "mqtt", "json");

        product.changeStatus(com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductStatus.published());

        assertTrue(product.isPublished());
    }

}
