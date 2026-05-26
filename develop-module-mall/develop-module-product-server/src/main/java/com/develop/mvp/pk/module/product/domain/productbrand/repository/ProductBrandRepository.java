package com.develop.mvp.pk.module.product.domain.productbrand.repository;

// Skill: AggregateRoot_ProductBrand_Validation_Skill — 仓储接口 ProductBrandRepository
// DDD 角色：领域层定义的仓储接口，不依赖任何基础设施
// 验收标准 AC05：不 import MyBatis 类

import com.develop.mvp.pk.module.product.domain.productbrand.ProductBrand;
import com.develop.mvp.pk.module.product.domain.productbrand.valueobject.ProductBrandId;

import java.util.List;
import java.util.Optional;

public interface ProductBrandRepository {
    ProductBrand save(ProductBrand brand);
    void delete(ProductBrandId id);
    ProductBrand findById(ProductBrandId id);
    Optional<ProductBrand> findByName(String name);
    List<ProductBrand> findByStatus(Integer status);
    List<ProductBrand> findAll();
    long count();
}
