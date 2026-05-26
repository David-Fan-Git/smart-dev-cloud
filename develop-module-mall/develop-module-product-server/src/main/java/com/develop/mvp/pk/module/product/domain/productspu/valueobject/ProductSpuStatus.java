package com.develop.mvp.pk.module.product.domain.productspu.valueobject;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 值对象 ProductSpuStatus
// DDD 角色：商品 SPU 状态值对象，封装状态流转规则
// 验收标准 AC04：final 字段，无 setter

import com.develop.mvp.pk.module.product.enums.spu.ProductSpuStatusEnum;

import java.util.Objects;

public final class ProductSpuStatus {

    public static final ProductSpuStatus ENABLED = new ProductSpuStatus(ProductSpuStatusEnum.ENABLE.getStatus());
    public static final ProductSpuStatus DISABLED = new ProductSpuStatus(ProductSpuStatusEnum.DISABLE.getStatus());
    public static final ProductSpuStatus RECYCLE = new ProductSpuStatus(ProductSpuStatusEnum.RECYCLE.getStatus());

    private final Integer code;

    private ProductSpuStatus(Integer code) {
        this.code = Objects.requireNonNull(code, "SPU状态不能为空");
    }

    public static ProductSpuStatus of(Integer code) {
        if (ProductSpuStatusEnum.ENABLE.getStatus().equals(code)) return ENABLED;
        if (ProductSpuStatusEnum.DISABLE.getStatus().equals(code)) return DISABLED;
        if (ProductSpuStatusEnum.RECYCLE.getStatus().equals(code)) return RECYCLE;
        throw new IllegalArgumentException("无效的SPU状态: " + code);
    }

    /** 上架 */
    public ProductSpuStatus enable() {
        if (isRecycle()) throw new IllegalStateException("回收站中的商品不能上架");
        return ENABLED;
    }

    /** 下架 */
    public ProductSpuStatus disable() { return DISABLED; }

    /** 放入回收站 */
    public ProductSpuStatus recycle() { return RECYCLE; }

    public boolean isEnabled() { return ProductSpuStatusEnum.ENABLE.getStatus().equals(code); }
    public boolean isDisabled() { return ProductSpuStatusEnum.DISABLE.getStatus().equals(code); }
    public boolean isRecycle() { return ProductSpuStatusEnum.RECYCLE.getStatus().equals(code); }

    public Integer code() { return code; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSpuStatus that)) return false;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String toString() {
        if (isEnabled()) return "ENABLED";
        if (isDisabled()) return "DISABLED";
        return "RECYCLE";
    }
}
