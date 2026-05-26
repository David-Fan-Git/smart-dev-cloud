package com.develop.mvp.pk.module.promotion.domain.coupon.valueobject;

// Skill: AggregateRoot_CouponTemplate_Validation_Skill

import java.util.Objects;

public final class CouponTemplateId {
    private final Long value;

    private CouponTemplateId(Long value) {
        this.value = Objects.requireNonNull(value, "优惠券模板编号不能为空");
    }

    public static CouponTemplateId of(Long value) { return new CouponTemplateId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CouponTemplateId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "CouponTemplateId{" + value + '}'; }
}
