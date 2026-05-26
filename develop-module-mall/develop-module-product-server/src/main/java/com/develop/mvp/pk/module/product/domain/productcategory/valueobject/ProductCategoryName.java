package com.develop.mvp.pk.module.product.domain.productcategory.valueobject;

// Skill: AggregateRoot_ProductCategory_Validation_Skill — 值对象 ProductCategoryName
// DDD 角色：商品分类名称值对象，封装分类名称校验逻辑
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

public final class ProductCategoryName {
    private final String value;

    private ProductCategoryName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("分类名称不能超过100个字符");
        }
        this.value = value;
    }

    public static ProductCategoryName of(String value) { return new ProductCategoryName(value); }

    public String value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCategoryName that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
