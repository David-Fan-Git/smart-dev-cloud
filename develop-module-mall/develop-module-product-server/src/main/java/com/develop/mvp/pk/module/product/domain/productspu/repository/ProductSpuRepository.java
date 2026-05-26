package com.develop.mvp.pk.module.product.domain.productspu.repository;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 仓储接口 ProductSpuRepository
// DDD 角色：领域层定义的仓储接口，不依赖任何基础设施
// 验收标准 AC05：不 import MyBatis 类

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.product.domain.productspu.ProductSpu;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSpuId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductSpuRepository {
    ProductSpu save(ProductSpu spu);
    void delete(ProductSpuId id);
    ProductSpu findById(ProductSpuId id);
    ProductSpu findByIdIncludeDeleted(ProductSpuId id);
    List<ProductSpu> findByIds(Collection<ProductSpuId> ids);
    List<ProductSpu> findByStatus(Integer status);
    PageResult<ProductSpu> findPage(ProductSpuPageQuery query);
    long countByCategoryId(Long categoryId);
    void updateStock(Long id, int incrCount);
    void updateBrowseCount(Long id, int incrCount);
    Map<Integer, Long> getTabsCount();
}
