package com.develop.mvp.pk.module.product.application.productcategory;

// Skill: AggregateRoot_ProductCategory_Validation_Skill — 应用服务 ProductCategoryApplicationService
// DDD 角色：应用编排服务，不包含业务规则，仅编排领域对象和基础设施

import com.develop.mvp.pk.module.product.domain.productcategory.ProductCategory;
import com.develop.mvp.pk.module.product.domain.productcategory.ProductCategoryFactory;
import com.develop.mvp.pk.module.product.domain.productcategory.repository.ProductCategoryRepository;
import com.develop.mvp.pk.module.product.domain.productcategory.valueobject.ProductCategoryId;
import com.develop.mvp.pk.module.product.domain.productcategory.valueobject.ProductCategoryName;
import com.develop.mvp.pk.module.product.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.product.enums.ErrorCodeConstants.*;

@Service
public class ProductCategoryApplicationService {

    private final ProductCategoryRepository productCategoryRepository;
    private final DomainEventPublisher eventPublisher;

    public ProductCategoryApplicationService(ProductCategoryRepository productCategoryRepository,
                                              DomainEventPublisher eventPublisher) {
        this.productCategoryRepository = productCategoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createCategory(Long id, String name, Long parentId, String picUrl, Integer sort, Integer status) {
        // 校验父分类存在
        if (parentId != null && parentId > 0) {
            validateCategoryExists(parentId);
        }
        ProductCategory category = ProductCategoryFactory.create(id, name, parentId, picUrl, sort, status);
        productCategoryRepository.save(category);
        publishEvents(category);
        return category.id().value();
    }

    @Transactional
    public void updateCategory(Long id, String name, Long parentId, String picUrl, Integer sort) {
        ProductCategory category = findExistingCategory(ProductCategoryId.of(id));
        category.updateProfile(ProductCategoryName.of(name), parentId, picUrl, sort);
        productCategoryRepository.save(category);
        publishEvents(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        ProductCategory category = findExistingCategory(ProductCategoryId.of(id));
        // 检查是否有子分类
        long childCount = productCategoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw exception(CATEGORY_EXISTS_CHILDREN);
        }
        productCategoryRepository.delete(category.id());
        publishEvents(category);
    }

    @Transactional
    public void updateCategoryStatus(Long id, Integer status) {
        ProductCategory category = findExistingCategory(ProductCategoryId.of(id));
        if (com.develop.mvp.pk.framework.common.enums.CommonStatusEnum.ENABLE.getStatus().equals(status)) {
            category.enable();
        } else {
            category.disable();
        }
        productCategoryRepository.save(category);
        publishEvents(category);
    }

    // ── 查询 ──

    public ProductCategory getCategory(Long id) {
        return productCategoryRepository.findById(ProductCategoryId.of(id));
    }

    public List<ProductCategory> getCategoryListByParentId(Long parentId) {
        return productCategoryRepository.findByParentId(parentId);
    }

    public List<ProductCategory> getAllCategories() {
        return productCategoryRepository.findAll();
    }

    public List<ProductCategory> getCategoryListByStatus(Integer status) {
        return productCategoryRepository.findByStatus(status);
    }

    // ── 私有方法 ──

    private ProductCategory findExistingCategory(ProductCategoryId id) {
        ProductCategory category = productCategoryRepository.findById(id);
        if (category == null) throw exception(CATEGORY_NOT_EXISTS);
        return category;
    }

    private void validateCategoryExists(Long id) {
        if (productCategoryRepository.findById(ProductCategoryId.of(id)) == null) {
            throw exception(CATEGORY_PARENT_NOT_EXISTS);
        }
    }

    private void publishEvents(ProductCategory category) {
        for (var event : category.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
