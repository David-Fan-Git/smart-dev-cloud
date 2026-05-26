package com.develop.mvp.pk.module.iot.infrastructure.product.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.product.query.IotProductPageQuery;
import com.develop.mvp.pk.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import com.develop.mvp.pk.module.iot.dal.dataobject.product.IotProductDO;
import com.develop.mvp.pk.module.iot.dal.mysql.product.IotProductMapper;
import com.develop.mvp.pk.module.iot.domain.product.model.IotProduct;
import com.develop.mvp.pk.module.iot.domain.product.repository.IotProductRepository;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductId;
import com.develop.mvp.pk.module.iot.domain.product.valueobject.IotProductKey;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class IotProductRepositoryImpl implements IotProductRepository {

    private final IotProductMapper productMapper;

    public IotProductRepositoryImpl(IotProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public IotProduct save(IotProduct product) {
        IotProductDO productDO = toDataObject(product);
        if (product.id() != null && productMapper.selectById(product.id()) != null) {
            productMapper.updateById(productDO);
        } else {
            productMapper.insert(productDO);
        }
        return toDomain(productDO);
    }

    @Override
    public void delete(IotProductId id) {
        productMapper.deleteById(id.value());
    }

    @Override
    public IotProduct findById(IotProductId id) {
        IotProductDO productDO = productMapper.selectById(id.value());
        return productDO != null ? toDomain(productDO) : null;
    }

    @Override
    public Optional<IotProduct> findByProductKey(IotProductKey productKey) {
        if (productKey == null || productKey.value() == null) {
            return Optional.empty();
        }
        IotProductDO productDO = productMapper.selectByProductKey(productKey.value());
        return Optional.ofNullable(productDO != null ? toDomain(productDO) : null);
    }

    @Override
    public PageResult<IotProduct> findPage(IotProductPageQuery query) {
        IotProductPageReqVO reqVO = new IotProductPageReqVO();
        reqVO.setName(query.name());
        reqVO.setProductKey(query.productKey());
        if (query.pageNo() != null) reqVO.setPageNo(query.pageNo());
        if (query.pageSize() != null) reqVO.setPageSize(query.pageSize());
        PageResult<IotProductDO> page = productMapper.selectPage(reqVO);
        return new PageResult<>(page.getList().stream().map(this::toDomain).toList(), page.getTotal());
    }

    @Override
    public List<IotProduct> findAll() {
        return productMapper.selectList().stream().map(this::toDomain).toList();
    }

    @Override
    public List<IotProduct> findByIds(Collection<Long> ids) {
        return productMapper.selectByIds(ids).stream().map(this::toDomain).toList();
    }

    @Override
    public List<IotProduct> findByDeviceType(Integer deviceType) {
        return productMapper.selectList(deviceType).stream().map(this::toDomain).toList();
    }

    @Override
    public List<IotProduct> findByStatus(Integer status) {
        return productMapper.selectListByStatus(status).stream().map(this::toDomain).toList();
    }

    @Override
    public long countByCreateTime(LocalDateTime createTime) {
        return productMapper.selectCountByCreateTime(createTime);
    }

    private IotProductDO toDataObject(IotProduct product) {
        IotProductDO productDO = new IotProductDO();
        productDO.setId(product.id());
        productDO.setName(product.name());
        productDO.setProductKey(product.productKey() != null ? product.productKey().value() : null);
        productDO.setProductSecret(product.productSecret() != null ? product.productSecret().value() : null);
        productDO.setRegisterEnabled(product.registerEnabled());
        productDO.setCategoryId(product.categoryId());
        productDO.setIcon(product.icon());
        productDO.setPicUrl(product.picUrl());
        productDO.setDescription(product.description());
        productDO.setStatus(product.status() != null ? product.status().code() : null);
        productDO.setDeviceType(product.deviceType());
        productDO.setNetType(product.netType());
        productDO.setProtocolType(product.protocolType());
        productDO.setSerializeType(product.serializeType());
        return productDO;
    }

    private IotProduct toDomain(IotProductDO productDO) {
        return IotProduct.reconstitute(productDO.getId(), productDO.getName(), productDO.getProductKey(),
                productDO.getProductSecret(), productDO.getRegisterEnabled(), productDO.getCategoryId(),
                productDO.getIcon(), productDO.getPicUrl(), productDO.getDescription(), productDO.getStatus(),
                productDO.getDeviceType(), productDO.getNetType(), productDO.getProtocolType(), productDO.getSerializeType());
    }

}
