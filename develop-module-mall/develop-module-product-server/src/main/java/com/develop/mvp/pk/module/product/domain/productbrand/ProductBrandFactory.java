package com.develop.mvp.pk.module.product.domain.productbrand;

// Skill: AggregateRoot_ProductBrand_Validation_Skill — 工厂 ProductBrandFactory
// DDD 角色：工厂，负责创建和重建 ProductBrand 聚合

import com.develop.mvp.pk.module.product.domain.productbrand.valueobject.ProductBrandId;
import com.develop.mvp.pk.module.product.domain.productbrand.valueobject.ProductBrandName;

public final class ProductBrandFactory {

    private ProductBrandFactory() {}

    /** 创建新品牌 */
    public static ProductBrand create(Long id, String name, String picUrl,
                                       Integer sort, String description, Integer status) {
        return new ProductBrand(
                ProductBrandId.of(id),
                ProductBrandName.of(name),
                picUrl,
                sort,
                description,
                status
        );
    }

    /** 从持久化数据重建品牌聚合（供仓储实现调用） */
    public static ProductBrand reconstitute(Long id, String name, String picUrl,
                                             Integer sort, String description, Integer status) {
        return new ProductBrand(
                ProductBrandId.of(id),
                ProductBrandName.of(name),
                picUrl,
                sort,
                description,
                status
        );
    }
}
