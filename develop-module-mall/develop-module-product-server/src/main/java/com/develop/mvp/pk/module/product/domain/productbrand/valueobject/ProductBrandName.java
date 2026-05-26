package com.develop.mvp.pk.module.product.domain.productbrand.valueobject;

// Skill: AggregateRoot_ProductBrand_Validation_Skill — 值对象 ProductBrandName
// DDD 角色：商品品牌名称值对象，封装品牌名称校验逻辑
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

public final class ProductBrandName {
    private final String value;

    private ProductBrandName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("品牌名称不能为空");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("品牌名称不能超过100个字符");
        }
        this.value = value;
    }

    public static ProductBrandName of(String value) { return new ProductBrandName(value); }

    public String value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductBrandName that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
