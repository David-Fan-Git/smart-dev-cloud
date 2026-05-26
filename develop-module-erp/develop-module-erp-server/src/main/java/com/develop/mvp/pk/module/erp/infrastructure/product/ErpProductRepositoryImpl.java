package com.develop.mvp.pk.module.erp.infrastructure.product;

import cn.hutool.core.collection.CollUtil;
import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.erp.dal.dataobject.product.ErpProductDO;
import com.develop.mvp.pk.module.erp.dal.mysql.product.ErpProductMapper;
import com.develop.mvp.pk.module.erp.domain.product.ErpProduct;
import com.develop.mvp.pk.module.erp.domain.product.ErpProductFactory;
import com.develop.mvp.pk.module.erp.domain.product.repository.ErpProductPageQuery;
import com.develop.mvp.pk.module.erp.domain.product.repository.ErpProductRepository;
import com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductId;
import com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductStatus;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ErpProductRepositoryImpl implements ErpProductRepository {

    private final ErpProductMapper erpProductMapper;

    public ErpProductRepositoryImpl(ErpProductMapper erpProductMapper) {
        this.erpProductMapper = erpProductMapper;
    }

    @Override
    public ErpProduct save(ErpProduct p) {
        ErpProductDO productDO = toDataObject(p);
        if (p.id() != null && erpProductMapper.selectById(p.id().value()) != null) {
            erpProductMapper.updateById(productDO);
        } else {
            erpProductMapper.insert(productDO);
        }
        return p;
    }

    @Override
    public void delete(ErpProductId id) {
        erpProductMapper.deleteById(id.value());
    }

    @Override
    public ErpProduct findById(ErpProductId id) {
        ErpProductDO productDO = erpProductMapper.selectById(id.value());
        return productDO != null ? toDomain(productDO) : null;
    }

    @Override
    public Optional<ErpProduct> findByBarCode(String barCode) {
        if (barCode == null) return Optional.empty();
        List<ErpProductDO> list = erpProductMapper.selectList(ErpProductDO::getBarCode, barCode);
        return list.isEmpty() ? Optional.empty() : Optional.of(toDomain(list.get(0)));
    }

    @Override
    public List<ErpProduct> findByStatus(ErpProductStatus status) {
        return erpProductMapper.selectListByStatus(status.code()).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public PageResult<ErpProduct> findPage(ErpProductPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO();
        reqVO.setName(query.name());
        reqVO.setCategoryId(query.categoryId());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<ErpProductDO> doPage = erpProductMapper.selectPage(reqVO);
        List<ErpProduct> products = doPage.getList().stream()
                .map(this::toDomain).collect(Collectors.toList());
        return new PageResult<>(products, doPage.getTotal());
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return erpProductMapper.selectCountByCategoryId(categoryId);
    }

    @Override
    public long countByUnitId(Long unitId) {
        return erpProductMapper.selectCountByUnitId(unitId);
    }

    private ErpProductDO toDataObject(ErpProduct p) {
        ErpProductDO productDO = new ErpProductDO();
        if (p.id() != null) productDO.setId(p.id().value());
        productDO.setName(p.name());
        productDO.setBarCode(p.barCode());
        productDO.setCategoryId(p.categoryId());
        productDO.setUnitId(p.unitId());
        productDO.setStatus(p.status().code());
        productDO.setStandard(p.standard());
        productDO.setRemark(p.remark());
        productDO.setExpiryDay(p.expiryDay());
        productDO.setWeight(p.weight());
        productDO.setPurchasePrice(p.purchasePrice());
        productDO.setSalePrice(p.salePrice());
        productDO.setMinPrice(p.minPrice());
        return productDO;
    }

    private ErpProduct toDomain(ErpProductDO productDO) {
        return ErpProductFactory.reconstitute(
                productDO.getId(), productDO.getName(), productDO.getBarCode(),
                productDO.getCategoryId(), productDO.getUnitId(), productDO.getStatus(),
                productDO.getStandard(), productDO.getRemark(), productDO.getExpiryDay(),
                productDO.getWeight(), productDO.getPurchasePrice(), productDO.getSalePrice(),
                productDO.getMinPrice()
        );
    }
}
