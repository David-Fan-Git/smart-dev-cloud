package com.develop.mvp.pk.module.erp.domain.product.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.erp.domain.product.ErpProduct;
import com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductId;
import com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductStatus;

import java.util.List;
import java.util.Optional;

public interface ErpProductRepository {
    ErpProduct save(ErpProduct p);
    void delete(ErpProductId id);
    ErpProduct findById(ErpProductId id);
    Optional<ErpProduct> findByBarCode(String barCode);
    List<ErpProduct> findByStatus(ErpProductStatus status);
    PageResult<ErpProduct> findPage(ErpProductPageQuery query);
    long countByCategoryId(Long categoryId);
    long countByUnitId(Long unitId);
}
