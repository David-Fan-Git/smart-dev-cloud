package com.develop.mvp.pk.module.wms.domain.inventory;

import java.math.BigDecimal;

public class WmsInventoryFactory {

    public static WmsInventory create(Long skuId, Long warehouseId) {
        return new WmsInventory(null, skuId, warehouseId, BigDecimal.ZERO, null);
    }

    public static WmsInventory create(Long skuId, Long warehouseId, BigDecimal quantity, String remark) {
        return new WmsInventory(null, skuId, warehouseId, quantity, remark);
    }

    public static WmsInventory reconstitute(Long id, Long skuId, Long warehouseId,
                                             BigDecimal quantity, String remark) {
        return new WmsInventory(id, skuId, warehouseId, quantity, remark);
    }
}
