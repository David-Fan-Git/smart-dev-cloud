package com.develop.mvp.pk.module.promotion.domain.coupon.repository;

// Skill: AggregateRoot_CouponTemplate_Validation_Skill

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.promotion.domain.coupon.CouponTemplate;
import com.develop.mvp.pk.module.promotion.domain.coupon.valueobject.CouponTemplateId;

import java.util.List;

public interface CouponTemplateRepository {
    CouponTemplate save(CouponTemplate template);
    void delete(CouponTemplateId id);
    CouponTemplate findById(CouponTemplateId id);
    List<CouponTemplate> findByStatus(Integer status);
    List<CouponTemplate> findAll();
    PageResult<CouponTemplate> findPage(String name, Integer status, Integer discountType, Integer pageNo, Integer pageSize);
    long count();
}
