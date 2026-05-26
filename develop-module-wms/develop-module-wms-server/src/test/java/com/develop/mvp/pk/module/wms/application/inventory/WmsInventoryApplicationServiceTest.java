package com.develop.mvp.pk.module.wms.application.inventory;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.wms.domain.inventory.WmsInventory;
import com.develop.mvp.pk.module.wms.domain.inventory.WmsInventoryFactory;
import com.develop.mvp.pk.module.wms.domain.inventory.event.DomainEvent;
import com.develop.mvp.pk.module.wms.domain.inventory.event.DomainEventPublisher;
import com.develop.mvp.pk.module.wms.domain.inventory.event.WmsInventoryCreatedEvent;
import com.develop.mvp.pk.module.wms.domain.inventory.repository.WmsInventoryPageQuery;
import com.develop.mvp.pk.module.wms.domain.inventory.repository.WmsInventoryRepository;
import com.develop.mvp.pk.module.wms.domain.inventory.valueobject.WmsInventoryId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class WmsInventoryApplicationServiceTest {

    @Test
    void createInventory_returnsPersistedIdAndPublishesEventWithPersistedId() {
        InMemoryWmsInventoryRepository repository = new InMemoryWmsInventoryRepository();
        CapturingDomainEventPublisher eventPublisher = new CapturingDomainEventPublisher();
        WmsInventoryApplicationService applicationService = new WmsInventoryApplicationService(repository, eventPublisher);

        Long id = applicationService.createInventory(1L, 100L, new BigDecimal("5.00"), "初始化");

        assertEquals(1L, id);
        WmsInventoryCreatedEvent event = assertInstanceOf(WmsInventoryCreatedEvent.class, eventPublisher.events.get(0));
        assertEquals(1L, event.inventoryId());
    }

    private static final class CapturingDomainEventPublisher implements DomainEventPublisher {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }
    }

    private static final class InMemoryWmsInventoryRepository implements WmsInventoryRepository {
        private long nextId = 1L;

        @Override
        public WmsInventory save(WmsInventory inventory) {
            if (inventory.id() != null) {
                return inventory;
            }
            return WmsInventoryFactory.reconstitute(nextId++, inventory.skuId(), inventory.warehouseId(),
                    inventory.quantity(), inventory.remark());
        }

        @Override public WmsInventory findById(WmsInventoryId id) { return null; }
        @Override public Optional<WmsInventory> findBySkuIdAndWarehouseId(Long skuId, Long warehouseId) { return Optional.empty(); }
        @Override public List<WmsInventory> findByWarehouseId(Long warehouseId) { return List.of(); }
        @Override public List<WmsInventory> findByKeys(java.util.Collection<WmsInventory> keys) { return List.of(); }
        @Override public List<WmsInventory> findByIdsForUpdate(java.util.Collection<Long> ids) { return List.of(); }
        @Override public PageResult<WmsInventory> findPage(WmsInventoryPageQuery query) { return new PageResult<>(List.of(), 0L); }
        @Override public long countBySkuId(Long skuId) { return 0; }
        @Override public long countByWarehouseId(Long warehouseId) { return 0; }
    }
}
