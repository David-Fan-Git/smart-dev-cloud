package com.develop.mvp.pk.module.product.domain.productspu.valueobject;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 值对象 ProductSpuId
// DDD 角色：商品 SPU 聚合根标识
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

public final class ProductSpuId {
    private final Long value;

    private ProductSpuId(Long value) {
        this.value = Objects.requireNonNull(value, "SPU编号不能为空");
    }

    public static ProductSpuId of(Long value) { return new ProductSpuId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSpuId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "ProductSpuId{" + value + '}'; }
}
