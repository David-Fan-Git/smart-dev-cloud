package com.develop.mvp.pk.module.product.application.productbrand;

// Skill: AggregateRoot_ProductBrand_Validation_Skill — 应用服务 ProductBrandApplicationService
// DDD 角色：应用编排服务，不包含业务规则，仅编排领域对象和基础设施

import com.develop.mvp.pk.module.product.domain.productbrand.ProductBrand;
import com.develop.mvp.pk.module.product.domain.productbrand.ProductBrandFactory;
import com.develop.mvp.pk.module.product.domain.productbrand.repository.ProductBrandRepository;
import com.develop.mvp.pk.module.product.domain.productbrand.valueobject.ProductBrandId;
import com.develop.mvp.pk.module.product.domain.productbrand.valueobject.ProductBrandName;
import com.develop.mvp.pk.module.product.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.product.enums.ErrorCodeConstants.*;

@Service
public class ProductBrandApplicationService {

    private final ProductBrandRepository productBrandRepository;
    private final DomainEventPublisher eventPublisher;

    public ProductBrandApplicationService(ProductBrandRepository productBrandRepository,
                                           DomainEventPublisher eventPublisher) {
        this.productBrandRepository = productBrandRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createBrand(Long id, String name, String picUrl, Integer sort, String description, Integer status) {
        assertBrandNameUnique(name, null);
        ProductBrand brand = ProductBrandFactory.create(id, name, picUrl, sort, description, status);
        productBrandRepository.save(brand);
        publishEvents(brand);
        return brand.id().value();
    }

    @Transactional
    public void updateBrand(Long id, String name, String picUrl, Integer sort, String description) {
        ProductBrand brand = findExistingBrand(ProductBrandId.of(id));
        assertBrandNameUnique(name, brand.id());
        brand.updateProfile(ProductBrandName.of(name), picUrl, sort, description);
        productBrandRepository.save(brand);
        publishEvents(brand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        ProductBrand brand = findExistingBrand(ProductBrandId.of(id));
        productBrandRepository.delete(brand.id());
        publishEvents(brand);
    }

    @Transactional
    public void updateBrandStatus(Long id, Integer status) {
        ProductBrand brand = findExistingBrand(ProductBrandId.of(id));
        if (com.develop.mvp.pk.framework.common.enums.CommonStatusEnum.ENABLE.getStatus().equals(status)) {
            brand.enable();
        } else {
            brand.disable();
        }
        productBrandRepository.save(brand);
        publishEvents(brand);
    }

    // ── 查询 ──

    public ProductBrand getBrand(Long id) {
        return productBrandRepository.findById(ProductBrandId.of(id));
    }

    public List<ProductBrand> getAllBrands() {
        return productBrandRepository.findAll();
    }

    public List<ProductBrand> getBrandListByStatus(Integer status) {
        return productBrandRepository.findByStatus(status);
    }

    // ── 私有方法 ──

    private ProductBrand findExistingBrand(ProductBrandId id) {
        ProductBrand brand = productBrandRepository.findById(id);
        if (brand == null) throw exception(BRAND_NOT_EXISTS);
        return brand;
    }

    private void assertBrandNameUnique(String name, ProductBrandId excludeId) {
        productBrandRepository.findByName(name)
                .filter(b -> excludeId == null || !b.id().equals(excludeId))
                .ifPresent(b -> { throw exception(BRAND_NAME_EXISTS); });
    }

    private void publishEvents(ProductBrand brand) {
        for (var event : brand.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
