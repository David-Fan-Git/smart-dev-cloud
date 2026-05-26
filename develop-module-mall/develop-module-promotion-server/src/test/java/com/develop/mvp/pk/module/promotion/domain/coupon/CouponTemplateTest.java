package com.develop.mvp.pk.module.promotion.domain.coupon;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;

class CouponTemplateTest {

    @Test
    void create_allowsTransientId() {
        CouponTemplate template = CouponTemplateFactory.create(null, "满减券", "description", 1, 0,
                100, 1, 1, null, 10, 100, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertNull(template.id());
    }
}
