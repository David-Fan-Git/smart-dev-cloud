package com.develop.mvp.pk.module.promotion.application.coupon;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.domain.coupon.CouponTemplate;
import com.develop.mvp.pk.module.promotion.domain.coupon.CouponTemplateFactory;
import com.develop.mvp.pk.module.promotion.domain.coupon.repository.CouponTemplateRepository;
import com.develop.mvp.pk.module.promotion.domain.coupon.valueobject.CouponTemplateId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CouponTemplateApplicationServiceTest {

    @Test
    void createTemplate_returnsPersistedId() {
        CouponTemplateApplicationService applicationService = new CouponTemplateApplicationService(
                new StubCouponTemplateRepository(), event -> {});

        Long id = applicationService.createTemplate(null, "满减券", "description", 1, 0,
                100, 1, 1, null, 10, 100, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertEquals(100L, id);
    }

    private static final class StubCouponTemplateRepository implements CouponTemplateRepository {
        @Override
        public CouponTemplate save(CouponTemplate template) {
            return CouponTemplateFactory.reconstitute(100L, template.name(), template.description(), template.type(),
                    template.status(), template.totalCount(), template.limitCount(), template.distributeCount(),
                    template.useCount(), template.discountType(), template.discountPercent(), template.discountPrice(),
                    template.minimumPrice(), template.maximumPrice(), template.validStartTime(), template.validEndTime());
        }

        @Override public void delete(CouponTemplateId id) {}
        @Override public CouponTemplate findById(CouponTemplateId id) { return null; }
        @Override public List<CouponTemplate> findByStatus(Integer status) { return List.of(); }
        @Override public List<CouponTemplate> findAll() { return List.of(); }
        @Override public PageResult<CouponTemplate> findPage(String name, Integer status, Integer discountType, Integer pageNo, Integer pageSize) { return PageResult.empty(); }
        @Override public long count() { return 0; }
    }
}
