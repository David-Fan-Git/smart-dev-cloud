package com.develop.mvp.pk.module.promotion.infrastructure.seckill;

import com.develop.mvp.pk.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import com.develop.mvp.pk.module.promotion.dal.dataobject.seckill.SeckillProductDO;
import com.develop.mvp.pk.module.promotion.dal.mysql.seckill.seckillactivity.SeckillActivityMapper;
import com.develop.mvp.pk.module.promotion.dal.mysql.seckill.seckillactivity.SeckillProductMapper;
import com.develop.mvp.pk.module.promotion.domain.seckill.SeckillActivity;
import com.develop.mvp.pk.module.promotion.domain.seckill.SeckillActivityFactory;
import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillProduct;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeckillActivityRepositoryImplTest {

    @Test
    void save_deletesProductsRemovedFromAggregate() {
        SeckillActivityMapper activityMapper = mock(SeckillActivityMapper.class);
        SeckillProductMapper productMapper = mock(SeckillProductMapper.class);
        SeckillActivityRepositoryImpl repository = new SeckillActivityRepositoryImpl(activityMapper, productMapper);
        SeckillActivityDO activityDO = new SeckillActivityDO();
        activityDO.setId(100L);
        when(activityMapper.selectById(100L)).thenReturn(activityDO);
        SeckillProductDO keptProduct = new SeckillProductDO();
        keptProduct.setId(1L);
        SeckillProductDO removedProduct = new SeckillProductDO();
        removedProduct.setId(2L);
        when(productMapper.selectListByActivityId(100L)).thenReturn(List.of(keptProduct, removedProduct));
        when(productMapper.selectById(1L)).thenReturn(keptProduct);
        SeckillActivity activity = SeckillActivityFactory.reconstitute(100L, 1L, "秒杀", 0, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, List.of(1L), 1, 1, 10, 10,
                List.of(new SeckillProduct(1L, List.of(1L), 1L, 2L, 100, 10)));

        repository.save(activity);

        verify(productMapper).deleteById(2L);
    }
}
