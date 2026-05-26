package com.develop.mvp.pk.module.promotion.domain.seckill;

// Skill: AggregateRoot_SeckillActivity_Validation_Skill — 工厂 SeckillActivityFactory

import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillActivityId;
import com.develop.mvp.pk.module.promotion.domain.seckill.valueobject.SeckillProduct;

import java.time.LocalDateTime;
import java.util.List;

public final class SeckillActivityFactory {

    private SeckillActivityFactory() {}

    public static SeckillActivity create(Long id, Long spuId, String name, Integer status, String remark,
                                          LocalDateTime startTime, LocalDateTime endTime, Integer sort,
                                          List<Long> configIds, Integer totalLimitCount, Integer singleLimitCount,
                                          List<SeckillProduct> products) {
        int totalStock = products != null ? products.stream().mapToInt(SeckillProduct::stock).sum() : 0;
        return new SeckillActivity(id != null ? SeckillActivityId.of(id) : null, spuId, name, status, remark,
                startTime, endTime, sort, configIds, totalLimitCount, singleLimitCount,
                totalStock, totalStock, products);
    }

    public static SeckillActivity reconstitute(Long id, Long spuId, String name, Integer status, String remark,
                                                LocalDateTime startTime, LocalDateTime endTime, Integer sort,
                                                List<Long> configIds, Integer totalLimitCount, Integer singleLimitCount,
                                                Integer stock, Integer totalStock, List<SeckillProduct> products) {
        return new SeckillActivity(SeckillActivityId.of(id), spuId, name, status, remark,
                startTime, endTime, sort, configIds, totalLimitCount, singleLimitCount,
                stock, totalStock, products);
    }
}
