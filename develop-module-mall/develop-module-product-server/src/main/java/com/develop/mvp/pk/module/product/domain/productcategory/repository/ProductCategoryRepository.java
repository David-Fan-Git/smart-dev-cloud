package com.develop.mvp.pk.module.product.domain.productcategory.repository;

// Skill: AggregateRoot_ProductCategory_Validation_Skill — 仓储接口 ProductCategoryRepository
// DDD 角色：领域层定义的仓储接口，不依赖任何基础设施
// 验收标准 AC05：不 import MyBatis 类

import com.develop.mvp.pk.module.product.domain.productcategory.ProductCategory;
import com.develop.mvp.pk.module.product.domain.productcategory.valueobject.ProductCategoryId;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository {
    ProductCategory save(ProductCategory category);
    void delete(ProductCategoryId id);
    ProductCategory findById(ProductCategoryId id);
    Optional<ProductCategory> findByName(String name);
    List<ProductCategory> findByParentId(Long parentId);
    List<ProductCategory> findByStatus(Integer status);
    List<ProductCategory> findAll();
    long countByParentId(Long parentId);
}
