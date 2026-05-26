package com.develop.mvp.pk.module.wms.domain.inventory.repository;

import java.math.BigDecimal;

public record WmsInventoryPageQuery(
        Long warehouseId,
        Long skuId,
        String itemCode,
        String itemName,
        String skuCode,
        String skuName,
        BigDecimal minQuantity,
        Boolean onlyPositiveQuantity,
        String type,
        Integer pageNo,
        Integer pageSize
) {}
