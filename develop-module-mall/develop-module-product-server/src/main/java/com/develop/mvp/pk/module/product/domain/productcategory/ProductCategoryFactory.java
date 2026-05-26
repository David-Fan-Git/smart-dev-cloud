package com.develop.mvp.pk.module.product.domain.productcategory;

// Skill: AggregateRoot_ProductCategory_Validation_Skill — 工厂 ProductCategoryFactory
// DDD 角色：工厂，负责创建和重建 ProductCategory 聚合

import com.develop.mvp.pk.module.product.domain.productcategory.valueobject.ProductCategoryId;
import com.develop.mvp.pk.module.product.domain.productcategory.valueobject.ProductCategoryName;

public final class ProductCategoryFactory {

    private ProductCategoryFactory() {}

    /** 创建新分类 */
    public static ProductCategory create(Long id, String name, Long parentId,
                                          String picUrl, Integer sort, Integer status) {
        return new ProductCategory(
                ProductCategoryId.of(id),
                ProductCategoryName.of(name),
                parentId,
                picUrl,
                sort,
                status
        );
    }

    /** 从持久化数据重建分类聚合（供仓储实现调用） */
    public static ProductCategory reconstitute(Long id, String name, Long parentId,
                                                String picUrl, Integer sort, Integer status) {
        return new ProductCategory(
                ProductCategoryId.of(id),
                ProductCategoryName.of(name),
                parentId,
                picUrl,
                sort,
                status
        );
    }
}
