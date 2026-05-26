package com.develop.mvp.pk.module.product.domain.productbrand.valueobject;

// Skill: AggregateRoot_ProductBrand_Validation_Skill — 值对象 ProductBrandId
// DDD 角色：商品品牌聚合根标识
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

public final class ProductBrandId {
    private final Long value;

    private ProductBrandId(Long value) {
        this.value = Objects.requireNonNull(value, "品牌编号不能为空");
    }

    public static ProductBrandId of(Long value) { return new ProductBrandId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductBrandId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "ProductBrandId{" + value + '}'; }
}
