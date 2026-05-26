package com.develop.mvp.pk.module.product.infrastructure.productcategory;

// Skill: AggregateRoot_ProductCategory_Validation_Skill — 仓储实现 ProductCategoryRepositoryImpl
// DDD 角色：ProductCategoryRepository 的 MyBatis 实现

import com.develop.mvp.pk.module.product.dal.dataobject.category.ProductCategoryDO;
import com.develop.mvp.pk.module.product.dal.mysql.category.ProductCategoryMapper;
import com.develop.mvp.pk.module.product.domain.productcategory.ProductCategory;
import com.develop.mvp.pk.module.product.domain.productcategory.ProductCategoryFactory;
import com.develop.mvp.pk.module.product.domain.productcategory.repository.ProductCategoryRepository;
import com.develop.mvp.pk.module.product.domain.productcategory.valueobject.ProductCategoryId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final ProductCategoryMapper productCategoryMapper;

    public ProductCategoryRepositoryImpl(ProductCategoryMapper productCategoryMapper) {
        this.productCategoryMapper = productCategoryMapper;
    }

    @Override
    @Transactional
    public ProductCategory save(ProductCategory category) {
        ProductCategoryDO categoryDO = toDataObject(category);
        if (productCategoryMapper.selectById(category.id().value()) == null) {
            productCategoryMapper.insert(categoryDO);
        } else {
            productCategoryMapper.updateById(categoryDO);
        }
        return category;
    }

    @Override
    @Transactional
    public void delete(ProductCategoryId id) {
        productCategoryMapper.deleteById(id.value());
    }

    @Override
    public ProductCategory findById(ProductCategoryId id) {
        ProductCategoryDO categoryDO = productCategoryMapper.selectById(id.value());
        return categoryDO != null ? toDomain(categoryDO) : null;
    }

    @Override
    public Optional<ProductCategory> findByName(String name) {
        ProductCategoryDO categoryDO = productCategoryMapper.selectOne(ProductCategoryDO::getName, name);
        return Optional.ofNullable(categoryDO).map(this::toDomain);
    }

    @Override
    public List<ProductCategory> findByParentId(Long parentId) {
        return productCategoryMapper.selectList(ProductCategoryDO::getParentId, parentId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ProductCategory> findByStatus(Integer status) {
        return productCategoryMapper.selectListByStatus(status).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ProductCategory> findAll() {
        return productCategoryMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByParentId(Long parentId) {
        return productCategoryMapper.selectCountByParentId(parentId);
    }

    private ProductCategoryDO toDataObject(ProductCategory category) {
        ProductCategoryDO categoryDO = new ProductCategoryDO();
        categoryDO.setId(category.id().value());
        categoryDO.setName(category.name().value());
        categoryDO.setParentId(category.parentId());
        categoryDO.setPicUrl(category.picUrl());
        categoryDO.setSort(category.sort());
        categoryDO.setStatus(category.status());
        return categoryDO;
    }

    private ProductCategory toDomain(ProductCategoryDO categoryDO) {
        return ProductCategoryFactory.reconstitute(
                categoryDO.getId(),
                categoryDO.getName(),
                categoryDO.getParentId(),
                categoryDO.getPicUrl(),
                categoryDO.getSort(),
                categoryDO.getStatus()
        );
    }
}
