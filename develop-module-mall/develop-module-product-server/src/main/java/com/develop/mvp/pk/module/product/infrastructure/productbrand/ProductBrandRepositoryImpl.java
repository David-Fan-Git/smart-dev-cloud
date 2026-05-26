package com.develop.mvp.pk.module.product.infrastructure.productbrand;

// Skill: AggregateRoot_ProductBrand_Validation_Skill — 仓储实现 ProductBrandRepositoryImpl
// DDD 角色：ProductBrandRepository 的 MyBatis 实现

import com.develop.mvp.pk.module.product.dal.dataobject.brand.ProductBrandDO;
import com.develop.mvp.pk.module.product.dal.mysql.brand.ProductBrandMapper;
import com.develop.mvp.pk.module.product.domain.productbrand.ProductBrand;
import com.develop.mvp.pk.module.product.domain.productbrand.ProductBrandFactory;
import com.develop.mvp.pk.module.product.domain.productbrand.repository.ProductBrandRepository;
import com.develop.mvp.pk.module.product.domain.productbrand.valueobject.ProductBrandId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductBrandRepositoryImpl implements ProductBrandRepository {

    private final ProductBrandMapper productBrandMapper;

    public ProductBrandRepositoryImpl(ProductBrandMapper productBrandMapper) {
        this.productBrandMapper = productBrandMapper;
    }

    @Override
    @Transactional
    public ProductBrand save(ProductBrand brand) {
        ProductBrandDO brandDO = toDataObject(brand);
        if (productBrandMapper.selectById(brand.id().value()) == null) {
            productBrandMapper.insert(brandDO);
        } else {
            productBrandMapper.updateById(brandDO);
        }
        return brand;
    }

    @Override
    @Transactional
    public void delete(ProductBrandId id) {
        productBrandMapper.deleteById(id.value());
    }

    @Override
    public ProductBrand findById(ProductBrandId id) {
        ProductBrandDO brandDO = productBrandMapper.selectById(id.value());
        return brandDO != null ? toDomain(brandDO) : null;
    }

    @Override
    public Optional<ProductBrand> findByName(String name) {
        ProductBrandDO brandDO = productBrandMapper.selectByName(name);
        return Optional.ofNullable(brandDO).map(this::toDomain);
    }

    @Override
    public List<ProductBrand> findByStatus(Integer status) {
        return productBrandMapper.selectListByStatus(status).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ProductBrand> findAll() {
        return productBrandMapper.selectList().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return productBrandMapper.selectCount();
    }

    private ProductBrandDO toDataObject(ProductBrand brand) {
        ProductBrandDO brandDO = new ProductBrandDO();
        brandDO.setId(brand.id().value());
        brandDO.setName(brand.name().value());
        brandDO.setPicUrl(brand.picUrl());
        brandDO.setSort(brand.sort());
        brandDO.setDescription(brand.description());
        brandDO.setStatus(brand.status());
        return brandDO;
    }

    private ProductBrand toDomain(ProductBrandDO brandDO) {
        return ProductBrandFactory.reconstitute(
                brandDO.getId(),
                brandDO.getName(),
                brandDO.getPicUrl(),
                brandDO.getSort(),
                brandDO.getDescription(),
                brandDO.getStatus()
        );
    }
}
