package com.develop.mvp.pk.module.product.domain.productcategory.valueobject;

// Skill: AggregateRoot_ProductCategory_Validation_Skill — 值对象 ProductCategoryId
// DDD 角色：商品分类聚合根标识
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

public final class ProductCategoryId {
    private final Long value;

    private ProductCategoryId(Long value) {
        this.value = Objects.requireNonNull(value, "分类编号不能为空");
    }

    public static ProductCategoryId of(Long value) { return new ProductCategoryId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCategoryId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "ProductCategoryId{" + value + '}'; }
}
