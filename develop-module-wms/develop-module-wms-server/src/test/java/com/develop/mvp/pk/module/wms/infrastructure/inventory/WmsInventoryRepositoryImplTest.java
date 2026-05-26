package com.develop.mvp.pk.module.wms.infrastructure.inventory;

import com.develop.mvp.pk.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import com.develop.mvp.pk.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import com.develop.mvp.pk.module.wms.domain.inventory.WmsInventory;
import com.develop.mvp.pk.module.wms.domain.inventory.WmsInventoryFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class WmsInventoryRepositoryImplTest {

    @Test
    void save_returnsPersistedAggregateAfterInsert() {
        WmsInventoryMapper mapper = mock(WmsInventoryMapper.class);
        doAnswer(invocation -> {
            WmsInventoryDO inventoryDO = invocation.getArgument(0);
            inventoryDO.setId(1L);
            return 1;
        }).when(mapper).insert(any(WmsInventoryDO.class));
        WmsInventoryRepositoryImpl repository = new WmsInventoryRepositoryImpl(mapper);

        WmsInventory saved = repository.save(WmsInventoryFactory.create(1L, 100L, new BigDecimal("5.00"), "初始化"));

        assertEquals(1L, saved.id());
    }
}
