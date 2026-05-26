package com.develop.mvp.pk.module.promotion.domain.coupon;

// Skill: AggregateRoot_CouponTemplate_Validation_Skill

import com.develop.mvp.pk.module.promotion.domain.coupon.valueobject.CouponTemplateId;
import java.time.LocalDateTime;

public final class CouponTemplateFactory {
    private CouponTemplateFactory() {}

    public static CouponTemplate create(Long id, String name, String description, Integer type, Integer status,
                                         Integer totalCount, Integer limitCount,
                                         Integer discountType, Integer discountPercent, Integer discountPrice,
                                         Integer minimumPrice, Integer maximumPrice,
                                         LocalDateTime validStartTime, LocalDateTime validEndTime) {
        return new CouponTemplate(id != null ? CouponTemplateId.of(id) : null, name, description, type, status,
                totalCount, limitCount, 0, 0, discountType, discountPercent, discountPrice,
                minimumPrice, maximumPrice, validStartTime, validEndTime);
    }

    public static CouponTemplate reconstitute(Long id, String name, String description, Integer type, Integer status,
                                               Integer totalCount, Integer limitCount,
                                               Integer distributeCount, Integer useCount,
                                               Integer discountType, Integer discountPercent, Integer discountPrice,
                                               Integer minimumPrice, Integer maximumPrice,
                                               LocalDateTime validStartTime, LocalDateTime validEndTime) {
        return new CouponTemplate(CouponTemplateId.of(id), name, description, type, status,
                totalCount, limitCount, distributeCount, useCount,
                discountType, discountPercent, discountPrice, minimumPrice, maximumPrice,
                validStartTime, validEndTime);
    }
}
