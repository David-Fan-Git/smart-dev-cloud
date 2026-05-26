package com.develop.mvp.pk.module.wms.infrastructure.inventory;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import com.develop.mvp.pk.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import com.develop.mvp.pk.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import com.develop.mvp.pk.module.wms.domain.inventory.WmsInventory;
import com.develop.mvp.pk.module.wms.domain.inventory.WmsInventoryFactory;
import com.develop.mvp.pk.module.wms.domain.inventory.repository.WmsInventoryPageQuery;
import com.develop.mvp.pk.module.wms.domain.inventory.repository.WmsInventoryRepository;
import com.develop.mvp.pk.module.wms.domain.inventory.valueobject.WmsInventoryId;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class WmsInventoryRepositoryImpl implements WmsInventoryRepository {

    private final WmsInventoryMapper wmsInventoryMapper;

    public WmsInventoryRepositoryImpl(WmsInventoryMapper wmsInventoryMapper) {
        this.wmsInventoryMapper = wmsInventoryMapper;
    }

    @Override
    public WmsInventory save(WmsInventory inventory) {
        WmsInventoryDO inventoryDO = toDataObject(inventory);
        if (inventory.id() != null && wmsInventoryMapper.selectById(inventory.id()) != null) {
            wmsInventoryMapper.updateById(inventoryDO);
            return inventory;
        }
        wmsInventoryMapper.insert(inventoryDO);
        return toDomain(inventoryDO);
    }

    @Override
    public WmsInventory findById(WmsInventoryId id) {
        WmsInventoryDO inventoryDO = wmsInventoryMapper.selectById(id.value());
        return inventoryDO != null ? toDomain(inventoryDO) : null;
    }

    @Override
    public Optional<WmsInventory> findBySkuIdAndWarehouseId(Long skuId, Long warehouseId) {
        WmsInventoryDO inventoryDO = wmsInventoryMapper.selectBySkuIdAndWarehouseId(skuId, warehouseId);
        return Optional.ofNullable(inventoryDO != null ? toDomain(inventoryDO) : null);
    }

    @Override
    public List<WmsInventory> findByWarehouseId(Long warehouseId) {
        return wmsInventoryMapper.selectList(
                new com.develop.mvp.pk.module.wms.controller.admin.inventory.vo.WmsInventoryListReqVO()
                        .setWarehouseId(warehouseId)
        ).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<WmsInventory> findByKeys(Collection<WmsInventory> keys) {
        if (CollUtil.isEmpty(keys)) return Collections.emptyList();
        List<WmsInventoryDO> keyDOs = new ArrayList<>(keys.size());
        for (WmsInventory key : keys) {
            keyDOs.add(new WmsInventoryDO()
                    .setSkuId(key.skuId())
                    .setWarehouseId(key.warehouseId()));
        }
        return wmsInventoryMapper.selectListByKeys(keyDOs).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<WmsInventory> findByIdsForUpdate(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        return wmsInventoryMapper.selectListByIdsForUpdate(ids).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<WmsInventory> findPage(WmsInventoryPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO();
        reqVO.setWarehouseId(query.warehouseId());
        reqVO.setSkuId(query.skuId());
        reqVO.setItemCode(query.itemCode());
        reqVO.setItemName(query.itemName());
        reqVO.setSkuCode(query.skuCode());
        reqVO.setSkuName(query.skuName());
        reqVO.setMinQuantity(query.minQuantity());
        reqVO.setOnlyPositiveQuantity(query.onlyPositiveQuantity());
        reqVO.setType(query.type() != null ? query.type() : WmsInventoryPageReqVO.TYPE_WAREHOUSE);
        if (query.pageNo() != null) reqVO.setPageNo(query.pageNo());
        if (query.pageSize() != null) reqVO.setPageSize(query.pageSize());

        PageResult<WmsInventoryDO> doPage = wmsInventoryMapper.selectPage(reqVO);
        List<WmsInventory> inventories = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(inventories, doPage.getTotal());
    }

    @Override
    public long countBySkuId(Long skuId) {
        return wmsInventoryMapper.selectCountBySkuId(skuId);
    }

    @Override
    public long countByWarehouseId(Long warehouseId) {
        return wmsInventoryMapper.selectCountByWarehouseId(warehouseId);
    }

    private WmsInventoryDO toDataObject(WmsInventory inventory) {
        WmsInventoryDO inventoryDO = new WmsInventoryDO();
        if (inventory.id() != null) inventoryDO.setId(inventory.id());
        inventoryDO.setSkuId(inventory.skuId());
        inventoryDO.setWarehouseId(inventory.warehouseId());
        inventoryDO.setQuantity(inventory.quantity());
        inventoryDO.setRemark(inventory.remark());
        return inventoryDO;
    }

    private WmsInventory toDomain(WmsInventoryDO inventoryDO) {
        return WmsInventoryFactory.reconstitute(
                inventoryDO.getId(), inventoryDO.getSkuId(), inventoryDO.getWarehouseId(),
                inventoryDO.getQuantity(), inventoryDO.getRemark()
        );
    }
}
