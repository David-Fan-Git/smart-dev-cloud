package com.develop.mvp.pk.module.wms.domain.inventory.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.wms.domain.inventory.WmsInventory;
import com.develop.mvp.pk.module.wms.domain.inventory.valueobject.WmsInventoryId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WmsInventoryRepository {
    WmsInventory save(WmsInventory inventory);
    WmsInventory findById(WmsInventoryId id);
    Optional<WmsInventory> findBySkuIdAndWarehouseId(Long skuId, Long warehouseId);
    List<WmsInventory> findByWarehouseId(Long warehouseId);
    List<WmsInventory> findByKeys(Collection<WmsInventory> keys);
    List<WmsInventory> findByIdsForUpdate(Collection<Long> ids);
    PageResult<WmsInventory> findPage(WmsInventoryPageQuery query);
    long countBySkuId(Long skuId);
    long countByWarehouseId(Long warehouseId);
}
