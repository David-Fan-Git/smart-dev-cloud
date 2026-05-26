package com.develop.mvp.pk.module.product.infrastructure.productspu;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 仓储实现 ProductSpuRepositoryImpl
// DDD 角色：ProductSpuRepository 的 MyBatis 实现

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.product.dal.dataobject.sku.ProductSkuDO;
import com.develop.mvp.pk.module.product.dal.dataobject.spu.ProductSpuDO;
import com.develop.mvp.pk.module.product.dal.mysql.sku.ProductSkuMapper;
import com.develop.mvp.pk.module.product.dal.mysql.spu.ProductSpuMapper;
import com.develop.mvp.pk.module.product.domain.productspu.ProductSpu;
import com.develop.mvp.pk.module.product.domain.productspu.ProductSpuFactory;
import com.develop.mvp.pk.module.product.domain.productspu.repository.ProductSpuPageQuery;
import com.develop.mvp.pk.module.product.domain.productspu.repository.ProductSpuRepository;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSku;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSpuId;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.SkuProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ProductSpuRepositoryImpl implements ProductSpuRepository {

    private final ProductSpuMapper productSpuMapper;
    private final ProductSkuMapper productSkuMapper;

    public ProductSpuRepositoryImpl(ProductSpuMapper productSpuMapper,
                                     ProductSkuMapper productSkuMapper) {
        this.productSpuMapper = productSpuMapper;
        this.productSkuMapper = productSkuMapper;
    }

    @Override
    @Transactional
    public ProductSpu save(ProductSpu spu) {
        ProductSpuDO spuDO = toDataObject(spu);
        if (productSpuMapper.selectById(spu.id().value()) == null) {
            productSpuMapper.insert(spuDO);
        } else {
            productSpuMapper.updateById(spuDO);
        }
        // 保存 SKU
        for (ProductSku sku : spu.skus()) {
            ProductSkuDO skuDO = toSkuDataObject(sku, spu.id().value());
            if (sku.id() != null && productSkuMapper.selectById(sku.id()) != null) {
                productSkuMapper.updateById(skuDO);
            } else {
                productSkuMapper.insert(skuDO);
            }
        }
        return spu;
    }

    @Override
    @Transactional
    public void delete(ProductSpuId id) {
        productSpuMapper.deleteById(id.value());
        productSkuMapper.deleteBySpuId(id.value());
    }

    @Override
    public ProductSpu findById(ProductSpuId id) {
        ProductSpuDO spuDO = productSpuMapper.selectById(id.value());
        return spuDO != null ? toDomainWithSkus(spuDO) : null;
    }

    @Override
    public ProductSpu findByIdIncludeDeleted(ProductSpuId id) {
        ProductSpuDO spuDO = productSpuMapper.selectByIdIncludeDeleted(id.value());
        return spuDO != null ? toDomainWithSkus(spuDO) : null;
    }

    @Override
    public List<ProductSpu> findByIds(Collection<ProductSpuId> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<Long> rawIds = ids.stream().map(ProductSpuId::value).collect(Collectors.toList());
        return productSpuMapper.selectBatchIds(rawIds).stream()
                .map(this::toDomainWithSkus).collect(Collectors.toList());
    }

    @Override
    public List<ProductSpu> findByStatus(Integer status) {
        return productSpuMapper.selectList(ProductSpuDO::getStatus, status).stream()
                .map(this::toDomainWithSkus).collect(Collectors.toList());
    }

    @Override
    public PageResult<ProductSpu> findPage(ProductSpuPageQuery query) {
        var reqVO = new com.develop.mvp.pk.module.product.controller.admin.spu.vo.ProductSpuPageReqVO();
        reqVO.setName(query.name());
        reqVO.setCategoryId(query.categoryId());
        reqVO.setTabType(query.tabType());
        reqVO.setCreateTime(query.createTime());
        reqVO.setPageNo(query.pageNo());
        reqVO.setPageSize(query.pageSize());

        PageResult<ProductSpuDO> doPage = productSpuMapper.selectPage(reqVO);
        List<ProductSpu> spus = doPage.getList().stream()
                .map(this::toDomainWithSkus).collect(Collectors.toList());
        return new PageResult<>(spus, doPage.getTotal());
    }

    @Override
    public long countByCategoryId(Long categoryId) {
        return productSpuMapper.selectCount(ProductSpuDO::getCategoryId, categoryId);
    }

    @Override
    @Transactional
    public void updateStock(Long id, int incrCount) {
        productSpuMapper.updateStock(id, incrCount);
    }

    @Override
    @Transactional
    public void updateBrowseCount(Long id, int incrCount) {
        productSpuMapper.updateBrowseCount(id, incrCount);
    }

    @Override
    public Map<Integer, Long> getTabsCount() {
        Map<Integer, Long> counts = new LinkedHashMap<>();
        // 销售中
        counts.put(0, productSpuMapper.selectCount(ProductSpuDO::getStatus,
                com.develop.mvp.pk.module.product.enums.spu.ProductSpuStatusEnum.ENABLE.getStatus()));
        // 仓库中
        counts.put(1, productSpuMapper.selectCount(ProductSpuDO::getStatus,
                com.develop.mvp.pk.module.product.enums.spu.ProductSpuStatusEnum.DISABLE.getStatus()));
        // 已售空
        counts.put(2, productSpuMapper.selectCount(ProductSpuDO::getStock, 0));
        // 回收站
        counts.put(4, productSpuMapper.selectCount(ProductSpuDO::getStatus,
                com.develop.mvp.pk.module.product.enums.spu.ProductSpuStatusEnum.RECYCLE.getStatus()));
        return counts;
    }

    private ProductSpu toDomainWithSkus(ProductSpuDO spuDO) {
        // 查询 SKU
        List<ProductSkuDO> skuDOs = productSkuMapper.selectListBySpuId(spuDO.getId());
        List<ProductSku> skus = skuDOs.stream().map(this::toSkuDomain).collect(Collectors.toList());
        return ProductSpuFactory.reconstitute(
                spuDO.getId(), spuDO.getName(), spuDO.getKeyword(),
                spuDO.getIntroduction(), spuDO.getDescription(),
                spuDO.getCategoryId(), spuDO.getBrandId(), spuDO.getPicUrl(),
                spuDO.getSliderPicUrls(), spuDO.getSort(), spuDO.getStatus(),
                spuDO.getSpecType(), skus,
                spuDO.getPrice(), spuDO.getMarketPrice(), spuDO.getCostPrice(), spuDO.getStock(),
                spuDO.getDeliveryTypes(), spuDO.getDeliveryTemplateId(),
                spuDO.getGiveIntegral(), spuDO.getSubCommissionType(),
                spuDO.getSalesCount(), spuDO.getVirtualSalesCount(), spuDO.getBrowseCount()
        );
    }

    private ProductSpuDO toDataObject(ProductSpu spu) {
        ProductSpuDO spuDO = new ProductSpuDO();
        spuDO.setId(spu.id().value());
        spuDO.setName(spu.name());
        spuDO.setKeyword(spu.keyword());
        spuDO.setIntroduction(spu.introduction());
        spuDO.setDescription(spu.description());
        spuDO.setCategoryId(spu.categoryId());
        spuDO.setBrandId(spu.brandId());
        spuDO.setPicUrl(spu.picUrl());
        spuDO.setSliderPicUrls(new ArrayList<>(spu.sliderPicUrls()));
        spuDO.setSort(spu.sort());
        spuDO.setStatus(spu.status().code());
        spuDO.setSpecType(spu.specType());
        spuDO.setPrice(spu.price());
        spuDO.setMarketPrice(spu.marketPrice());
        spuDO.setCostPrice(spu.costPrice());
        spuDO.setStock(spu.stock());
        spuDO.setDeliveryTypes(new ArrayList<>(spu.deliveryTypes()));
        spuDO.setDeliveryTemplateId(spu.deliveryTemplateId());
        spuDO.setGiveIntegral(spu.giveIntegral());
        spuDO.setSubCommissionType(spu.subCommissionType());
        spuDO.setSalesCount(spu.salesCount());
        spuDO.setVirtualSalesCount(spu.virtualSalesCount());
        spuDO.setBrowseCount(spu.browseCount());
        return spuDO;
    }

    private ProductSkuDO toSkuDataObject(ProductSku sku, Long spuId) {
        ProductSkuDO skuDO = new ProductSkuDO();
        skuDO.setId(sku.id());
        skuDO.setSpuId(spuId);
        skuDO.setProperties(sku.properties().stream()
                .map(p -> new ProductSkuDO.Property(p.propertyId(), p.propertyName(), p.valueId(), p.valueName()))
                .collect(Collectors.toList()));
        skuDO.setPrice(sku.price());
        skuDO.setMarketPrice(sku.marketPrice());
        skuDO.setCostPrice(sku.costPrice());
        skuDO.setBarCode(sku.barCode());
        skuDO.setPicUrl(sku.picUrl());
        skuDO.setStock(sku.stock());
        skuDO.setWeight(sku.weight());
        skuDO.setVolume(sku.volume());
        skuDO.setSalesCount(sku.salesCount());
        return skuDO;
    }

    private ProductSku toSkuDomain(ProductSkuDO skuDO) {
        return new ProductSku(
                skuDO.getId(),
                skuDO.getProperties().stream()
                        .map(p -> new SkuProperty(p.getPropertyId(), p.getPropertyName(),
                                p.getValueId(), p.getValueName()))
                        .collect(Collectors.toList()),
                skuDO.getPrice(), skuDO.getMarketPrice(), skuDO.getCostPrice(),
                skuDO.getBarCode(), skuDO.getPicUrl(), skuDO.getStock(),
                skuDO.getWeight(), skuDO.getVolume(), skuDO.getSalesCount()
        );
    }
}
