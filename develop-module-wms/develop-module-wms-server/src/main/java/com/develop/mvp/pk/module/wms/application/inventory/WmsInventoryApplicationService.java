package com.develop.mvp.pk.module.wms.application.inventory;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.wms.domain.inventory.WmsInventory;
import com.develop.mvp.pk.module.wms.domain.inventory.WmsInventoryFactory;
import com.develop.mvp.pk.module.wms.domain.inventory.event.DomainEvent;
import com.develop.mvp.pk.module.wms.domain.inventory.event.DomainEventPublisher;
import com.develop.mvp.pk.module.wms.domain.inventory.repository.WmsInventoryPageQuery;
import com.develop.mvp.pk.module.wms.domain.inventory.repository.WmsInventoryRepository;
import com.develop.mvp.pk.module.wms.domain.inventory.valueobject.WmsInventoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Service
public class WmsInventoryApplicationService {

    private final WmsInventoryRepository wmsInventoryRepository;
    private final DomainEventPublisher eventPublisher;

    public WmsInventoryApplicationService(WmsInventoryRepository wmsInventoryRepository,
                                           DomainEventPublisher eventPublisher) {
        this.wmsInventoryRepository = wmsInventoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createInventory(Long skuId, Long warehouseId, BigDecimal quantity, String remark) {
        WmsInventory inventory = WmsInventoryFactory.create(skuId, warehouseId, quantity, remark);
        inventory = wmsInventoryRepository.save(inventory);
        inventory.markCreated();
        publishEvents(inventory);
        return inventory.id();
    }

    @Transactional
    public void addStock(Long id, BigDecimal amount) {
        WmsInventory inventory = findExistingInventory(WmsInventoryId.of(id));
        inventory.addStock(amount);
        wmsInventoryRepository.save(inventory);
        publishEvents(inventory);
    }

    @Transactional
    public void subtractStock(Long id, BigDecimal amount) {
        WmsInventory inventory = findExistingInventory(WmsInventoryId.of(id));
        inventory.subtractStock(amount);
        wmsInventoryRepository.save(inventory);
        publishEvents(inventory);
    }

    @Transactional
    public void setQuantity(Long id, BigDecimal quantity, String remark) {
        WmsInventory inventory = findExistingInventory(WmsInventoryId.of(id));
        inventory.setQuantity(quantity, remark);
        wmsInventoryRepository.save(inventory);
        publishEvents(inventory);
    }

    public WmsInventory getInventory(Long id) {
        return wmsInventoryRepository.findById(WmsInventoryId.of(id));
    }

    public Optional<WmsInventory> getBySkuIdAndWarehouseId(Long skuId, Long warehouseId) {
        return wmsInventoryRepository.findBySkuIdAndWarehouseId(skuId, warehouseId);
    }

    public List<WmsInventory> getInventoryListByWarehouseId(Long warehouseId) {
        return wmsInventoryRepository.findByWarehouseId(warehouseId);
    }

    public PageResult<WmsInventory> getInventoryPage(Long warehouseId, Long skuId,
                                                      String itemCode, String itemName,
                                                      String skuCode, String skuName,
                                                      BigDecimal minQuantity,
                                                      Boolean onlyPositiveQuantity,
                                                      String type, Integer pageNo,
                                                      Integer pageSize) {
        return wmsInventoryRepository.findPage(new WmsInventoryPageQuery(
                warehouseId, skuId, itemCode, itemName, skuCode, skuName,
                minQuantity, onlyPositiveQuantity, type, pageNo, pageSize));
    }

    public long getInventoryCountBySkuId(Long skuId) {
        return wmsInventoryRepository.countBySkuId(skuId);
    }

    public long getInventoryCountByWarehouseId(Long warehouseId) {
        return wmsInventoryRepository.countByWarehouseId(warehouseId);
    }

    private WmsInventory findExistingInventory(WmsInventoryId id) {
        WmsInventory inventory = wmsInventoryRepository.findById(id);
        if (inventory == null) {
            throw new IllegalArgumentException("库存记录不存在，id=" + id.value());
        }
        return inventory;
    }

    private void publishEvents(WmsInventory inventory) {
        for (DomainEvent event : inventory.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
