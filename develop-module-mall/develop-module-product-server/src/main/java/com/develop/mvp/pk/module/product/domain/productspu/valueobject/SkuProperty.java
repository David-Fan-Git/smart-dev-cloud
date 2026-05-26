package com.develop.mvp.pk.module.product.domain.productspu.valueobject;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 值对象 SkuProperty
// DDD 角色：SKU 属性值对象，封装属性键值对
// 验收标准 AC04：final 字段，无 setter

import java.util.Objects;

public final class SkuProperty {
    private final Long propertyId;
    private final String propertyName;
    private final Long valueId;
    private final String valueName;

    public SkuProperty(Long propertyId, String propertyName, Long valueId, String valueName) {
        this.propertyId = Objects.requireNonNull(propertyId, "属性编号不能为空");
        this.propertyName = Objects.requireNonNull(propertyName, "属性名称不能为空");
        this.valueId = Objects.requireNonNull(valueId, "属性值编号不能为空");
        this.valueName = Objects.requireNonNull(valueName, "属性值名称不能为空");
    }

    public Long propertyId() { return propertyId; }
    public String propertyName() { return propertyName; }
    public Long valueId() { return valueId; }
    public String valueName() { return valueName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkuProperty that)) return false;
        return propertyId.equals(that.propertyId) && valueId.equals(that.valueId);
    }

    @Override
    public int hashCode() { return Objects.hash(propertyId, valueId); }

    @Override
    public String toString() {
        return propertyName + ": " + valueName;
    }
}
