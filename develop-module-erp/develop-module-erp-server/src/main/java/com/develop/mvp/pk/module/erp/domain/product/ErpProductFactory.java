package com.develop.mvp.pk.module.erp.domain.product;

import com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductId;
import com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductStatus;
import java.math.BigDecimal;

public final class ErpProductFactory {

    private ErpProductFactory() {}

    public static ErpProduct create(String name, String barCode, Long categoryId, Long unitId,
                                     Integer status, String standard, String remark,
                                     Integer expiryDay, BigDecimal weight,
                                     BigDecimal purchasePrice, BigDecimal salePrice,
                                     BigDecimal minPrice) {
        return new ErpProduct(
                (ErpProductId) null, name, barCode, categoryId, unitId,
                status != null ? ErpProductStatus.of(status) : ErpProductStatus.ENABLED,
                standard, remark, expiryDay, weight, purchasePrice, salePrice, minPrice
        );
    }

    public static ErpProduct reconstitute(Long id, String name, String barCode, Long categoryId,
                                           Long unitId, Integer status, String standard,
                                           String remark, Integer expiryDay, BigDecimal weight,
                                           BigDecimal purchasePrice, BigDecimal salePrice,
                                           BigDecimal minPrice) {
        return new ErpProduct(
                ErpProductId.of(id), name, barCode, categoryId, unitId,
                ErpProductStatus.of(status), standard, remark, expiryDay, weight,
                purchasePrice, salePrice, minPrice
        );
    }
}
