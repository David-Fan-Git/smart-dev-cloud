package com.develop.mvp.pk.module.promotion.domain.seckill;

import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillProduct;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

class SeckillActivityTest {

    @Test
    void create_allowsTransientId() {
        SeckillActivity activity = SeckillActivityFactory.create(null, 1L, "秒杀", 0, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, List.of(1L), 1, 1,
                List.of(new SeckillProduct(null, List.of(1L), 1L, 2L, 100, 10)));

        assertNull(activity.id());
    }
}
