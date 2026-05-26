package com.develop.mvp.pk.module.promotion.domain.coupon.valueobject;

// Skill: AggregateRoot_CouponTemplate_Validation_Skill

import java.util.Objects;

public final class CouponId {
    private final Long value;

    private CouponId(Long value) {
        this.value = Objects.requireNonNull(value, "优惠券编号不能为空");
    }

    public static CouponId of(Long value) { return new CouponId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CouponId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "CouponId{" + value + '}'; }
}
