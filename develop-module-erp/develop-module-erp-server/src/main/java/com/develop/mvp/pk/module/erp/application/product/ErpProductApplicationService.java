package com.develop.mvp.pk.module.erp.application.product;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.erp.domain.product.ErpProduct;
import com.develop.mvp.pk.module.erp.domain.product.ErpProductFactory;
import com.develop.mvp.pk.module.erp.domain.product.event.DomainEvent;
import com.develop.mvp.pk.module.erp.domain.product.event.DomainEventPublisher;
import com.develop.mvp.pk.module.erp.domain.product.repository.ErpProductPageQuery;
import com.develop.mvp.pk.module.erp.domain.product.repository.ErpProductRepository;
import com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class ErpProductApplicationService {

    private final ErpProductRepository erpProductRepository;
    private final DomainEventPublisher eventPublisher;

    public ErpProductApplicationService(ErpProductRepository erpProductRepository,
                                         DomainEventPublisher eventPublisher) {
        this.erpProductRepository = erpProductRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createProduct(String name, String barCode, Long categoryId, Long unitId,
                               Integer status, String standard, String remark,
                               Integer expiryDay, BigDecimal weight,
                               BigDecimal purchasePrice, BigDecimal salePrice,
                               BigDecimal minPrice) {
        ErpProduct product = ErpProductFactory.create(name, barCode, categoryId, unitId,
                status, standard, remark, expiryDay, weight, purchasePrice, salePrice, minPrice);
        erpProductRepository.save(product);
        publishEvents(product);
        return product.id() != null ? product.id().value() : null;
    }

    @Transactional
    public void updateProduct(Long id, String name, String barCode, Long categoryId,
                               Long unitId, String standard, String remark,
                               Integer expiryDay, BigDecimal weight,
                               BigDecimal purchasePrice, BigDecimal salePrice,
                               BigDecimal minPrice) {
        ErpProduct product = findExistingProduct(ErpProductId.of(id));
        product.updateProfile(name, barCode, categoryId, unitId, standard, remark,
                expiryDay, weight, purchasePrice, salePrice, minPrice);
        erpProductRepository.save(product);
        publishEvents(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        ErpProduct product = findExistingProduct(ErpProductId.of(id));
        product.markDeleted();
        erpProductRepository.delete(product.id());
        publishEvents(product);
    }

    public ErpProduct getProduct(Long id) {
        return erpProductRepository.findById(ErpProductId.of(id));
    }

    public List<ErpProduct> getProductListByStatus(Integer status) {
        return erpProductRepository.findByStatus(
                com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductStatus.of(status));
    }

    public PageResult<ErpProduct> getProductPage(String name, String barCode, Long categoryId,
                                                   Integer status, Integer pageNo,
                                                   Integer pageSize) {
        return erpProductRepository.findPage(
                new ErpProductPageQuery(name, barCode, categoryId, status, pageNo, pageSize));
    }

    public long getProductCountByCategoryId(Long categoryId) {
        return erpProductRepository.countByCategoryId(categoryId);
    }

    public long getProductCountByUnitId(Long unitId) {
        return erpProductRepository.countByUnitId(unitId);
    }

    private ErpProduct findExistingProduct(ErpProductId id) {
        ErpProduct product = erpProductRepository.findById(id);
        if (product == null) {
            throw exception(com.develop.mvp.pk.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS);
        }
        return product;
    }

    private void publishEvents(ErpProduct product) {
        for (DomainEvent event : product.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
