package com.develop.mvp.pk.module.promotion.domain.seckill.valueobject;

// Skill: AggregateRoot_SeckillActivity_Validation_Skill — 值对象 SeckillActivityId

import java.util.Objects;

public final class SeckillActivityId {
    private final Long value;

    private SeckillActivityId(Long value) {
        this.value = Objects.requireNonNull(value, "秒杀活动编号不能为空");
    }

    public static SeckillActivityId of(Long value) { return new SeckillActivityId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeckillActivityId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "SeckillActivityId{" + value + '}'; }
}
