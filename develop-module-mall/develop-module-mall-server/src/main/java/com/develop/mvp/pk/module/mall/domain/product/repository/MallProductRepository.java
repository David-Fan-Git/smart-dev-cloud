package com.develop.mvp.pk.module.mall.domain.product.repository;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.mall.domain.product.MallProduct;
public interface MallProductRepository {
    MallProduct save(MallProduct p); void delete(Long id);
    MallProduct findById(Long id);
    PageResult<MallProduct> findPage(String name, Long categoryId, Integer status, Integer pageNo, Integer pageSize);
}
