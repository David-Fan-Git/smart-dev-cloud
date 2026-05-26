package com.develop.mvp.pk.module.promotion.domain.seckill.valueobject;

// Skill: AggregateRoot_SeckillActivity_Validation_Skill — 值对象 SeckillProduct

import java.util.List;
import java.util.Objects;

public final class SeckillProduct {
    private final Long id;
    private final List<Long> configIds;
    private final Long spuId;
    private final Long skuId;
    private final Integer seckillPrice;
    private final Integer stock;

    public SeckillProduct(Long id, List<Long> configIds, Long spuId, Long skuId,
                          Integer seckillPrice, Integer stock) {
        this.id = id;
        this.configIds = configIds;
        this.spuId = Objects.requireNonNull(spuId, "SPU编号不能为空");
        this.skuId = Objects.requireNonNull(skuId, "SKU编号不能为空");
        this.seckillPrice = Objects.requireNonNull(seckillPrice, "秒杀金额不能为空");
        this.stock = Objects.requireNonNull(stock, "库存不能为空");
    }

    public Long id() { return id; }
    public List<Long> configIds() { return configIds; }
    public Long spuId() { return spuId; }
    public Long skuId() { return skuId; }
    public Integer seckillPrice() { return seckillPrice; }
    public Integer stock() { return stock; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeckillProduct that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
