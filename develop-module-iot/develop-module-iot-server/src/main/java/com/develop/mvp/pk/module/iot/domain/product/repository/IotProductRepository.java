package com.develop.mvp.pk.module.iot.domain.product.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.product.query.IotProductPageQuery;
import com.develop.mvp.pk.module.iot.domain.product.model.IotProduct;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductId;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductKey;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IotProductRepository {

    IotProduct save(IotProduct product);

    void delete(IotProductId id);

    IotProduct findById(IotProductId id);

    Optional<IotProduct> findByProductKey(IotProductKey productKey);

    PageResult<IotProduct> findPage(IotProductPageQuery query);

    List<IotProduct> findAll();

    List<IotProduct> findByIds(Collection<Long> ids);

    List<IotProduct> findByDeviceType(Integer deviceType);

    List<IotProduct> findByStatus(Integer status);

    long countByCreateTime(LocalDateTime createTime);

}
