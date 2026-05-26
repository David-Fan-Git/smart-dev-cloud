package com.develop.mvp.pk.module.promotion.application.seckill;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.domain.seckill.SeckillActivity;
import com.develop.mvp.pk.module.promotion.domain.seckill.SeckillActivityFactory;
import com.develop.mvp.pk.module.promotion.domain.seckill.repository.SeckillActivityRepository;
import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillActivityId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeckillActivityApplicationServiceTest {

    @Test
    void createActivity_returnsPersistedId() {
        SeckillActivityApplicationService applicationService = new SeckillActivityApplicationService(
                new StubSeckillActivityRepository(), event -> {});

        Long id = applicationService.createActivity(null, 1L, "秒杀", 0, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, List.of(1L), 1, 1,
                List.of(Map.of("configIds", List.of(1L), "spuId", 1L, "skuId", 2L,
                        "seckillPrice", 100, "stock", 10)));

        assertEquals(100L, id);
    }

    private static final class StubSeckillActivityRepository implements SeckillActivityRepository {
        @Override
        public SeckillActivity save(SeckillActivity activity) {
            return SeckillActivityFactory.reconstitute(100L, activity.spuId(), activity.name(), activity.status(),
                    activity.remark(), activity.startTime(), activity.endTime(), activity.sort(), activity.configIds(),
                    activity.totalLimitCount(), activity.singleLimitCount(), activity.stock(), activity.totalStock(),
                    activity.products());
        }

        @Override public void delete(SeckillActivityId id) {}
        @Override public SeckillActivity findById(SeckillActivityId id) { return null; }
        @Override public List<SeckillActivity> findByStatus(Integer status) { return List.of(); }
        @Override public List<SeckillActivity> findActiveActivities() { return List.of(); }
        @Override public PageResult<SeckillActivity> findPage(String name, Integer status, Long spuId, Integer pageNo, Integer pageSize) { return PageResult.empty(); }
        @Override public long count() { return 0; }
    }
}
