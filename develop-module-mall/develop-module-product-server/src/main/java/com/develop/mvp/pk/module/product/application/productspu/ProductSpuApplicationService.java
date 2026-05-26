package com.develop.mvp.pk.module.product.application.productspu;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 应用服务 ProductSpuApplicationService
// DDD 角色：应用编排服务，不包含业务规则，仅编排领域对象和基础设施

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.product.domain.productspu.ProductSpu;
import com.develop.mvp.pk.module.product.domain.productspu.ProductSpuFactory;
import com.develop.mvp.pk.module.product.domain.productspu.repository.ProductSpuPageQuery;
import com.develop.mvp.pk.module.product.domain.productspu.repository.ProductSpuRepository;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSku;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSpuId;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.SkuProperty;
import com.develop.mvp.pk.module.product.domain.event.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.product.enums.ErrorCodeConstants.*;

@Service
public class ProductSpuApplicationService {

    private final ProductSpuRepository productSpuRepository;
    private final DomainEventPublisher eventPublisher;

    public ProductSpuApplicationService(ProductSpuRepository productSpuRepository,
                                         DomainEventPublisher eventPublisher) {
        this.productSpuRepository = productSpuRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long createSpu(Long id, String name, String keyword, String introduction,
                           String description, Long categoryId, Long brandId, String picUrl,
                           List<String> sliderPicUrls, Integer sort, Integer status,
                           Boolean specType, List<Map<String, Object>> skuParams,
                           List<Integer> deliveryTypes, Long deliveryTemplateId,
                           Integer giveIntegral, Boolean subCommissionType,
                           Integer virtualSalesCount) {
        List<ProductSku> skus = buildSkus(null, skuParams);
        ProductSpu spu = ProductSpuFactory.create(
                id, name, keyword, introduction, description,
                categoryId, brandId, picUrl, sliderPicUrls, sort, status,
                specType, skus, deliveryTypes, deliveryTemplateId,
                giveIntegral, subCommissionType, virtualSalesCount);
        productSpuRepository.save(spu);
        publishEvents(spu);
        return spu.id().value();
    }

    @Transactional
    public void updateSpu(Long id, String name, String keyword, String introduction,
                           String description, Long categoryId, Long brandId, String picUrl,
                           List<String> sliderPicUrls, Integer sort,
                           Boolean specType, List<Map<String, Object>> skuParams,
                           List<Integer> deliveryTypes, Long deliveryTemplateId,
                           Integer giveIntegral, Boolean subCommissionType) {
        ProductSpu spu = findExistingSpu(ProductSpuId.of(id));
        spu.updateProfile(name, keyword, introduction, description, categoryId, brandId,
                picUrl, sliderPicUrls, sort, deliveryTypes, deliveryTemplateId,
                giveIntegral, subCommissionType);
        if (skuParams != null) {
            List<ProductSku> skus = buildSkus(spu.id().value(), skuParams);
            spu.updateSkus(skus);
        }
        spu.setSpecType(specType);
        productSpuRepository.save(spu);
        publishEvents(spu);
    }

    @Transactional
    public void updateSpuStatus(Long id, Integer status) {
        ProductSpu spu = findExistingSpu(ProductSpuId.of(id));
        if (com.develop.mvp.pk.module.product.enums.spu.ProductSpuStatusEnum.ENABLE.getStatus().equals(status)) {
            spu.publish();
        } else if (com.develop.mvp.pk.module.product.enums.spu.ProductSpuStatusEnum.DISABLE.getStatus().equals(status)) {
            spu.unpublish();
        }
        productSpuRepository.save(spu);
        publishEvents(spu);
    }

    @Transactional
    public void recycleSpu(Long id) {
        ProductSpu spu = findExistingSpu(ProductSpuId.of(id));
        spu.recycle();
        productSpuRepository.save(spu);
        publishEvents(spu);
    }

    @Transactional
    public void deleteSpu(Long id) {
        ProductSpu spu = findExistingSpu(ProductSpuId.of(id));
        spu.markDeleted();
        productSpuRepository.delete(spu.id());
        publishEvents(spu);
    }

    @Transactional
    public void updateStock(Long id, int incrCount) {
        productSpuRepository.updateStock(id, incrCount);
    }

    @Transactional
    public void updateBrowseCount(Long id, int incrCount) {
        productSpuRepository.updateBrowseCount(id, incrCount);
    }

    // ── 查询 ──

    public ProductSpu getSpu(Long id) {
        return productSpuRepository.findById(ProductSpuId.of(id));
    }

    public ProductSpu getSpuDetail(Long id) {
        ProductSpu spu = productSpuRepository.findById(ProductSpuId.of(id));
        if (spu == null) throw exception(SPU_NOT_EXISTS);
        return spu;
    }

    public List<ProductSpu> getSpuList(Collection<Long> ids) {
        List<ProductSpuId> spuIds = ids.stream().map(ProductSpuId::of).collect(Collectors.toList());
        return productSpuRepository.findByIds(spuIds);
    }

    public List<ProductSpu> getSpuListByStatus(Integer status) {
        return productSpuRepository.findByStatus(status);
    }

    public PageResult<ProductSpu> getSpuPage(ProductSpuPageQuery query) {
        return productSpuRepository.findPage(query);
    }

    public long getSpuCountByCategoryId(Long categoryId) {
        return productSpuRepository.countByCategoryId(categoryId);
    }

    public Map<Integer, Long> getTabsCount() {
        return productSpuRepository.getTabsCount();
    }

    // ── 私有方法 ──

    private ProductSpu findExistingSpu(ProductSpuId id) {
        ProductSpu spu = productSpuRepository.findById(id);
        if (spu == null) throw exception(SPU_NOT_EXISTS);
        return spu;
    }

    private List<ProductSku> buildSkus(Long spuId, List<Map<String, Object>> skuParams) {
        if (skuParams == null) return Collections.emptyList();
        List<ProductSku> skus = new ArrayList<>();
        for (Map<String, Object> params : skuParams) {
            Long skuId = params.get("id") != null ? ((Number) params.get("id")).longValue() : null;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> propList = (List<Map<String, Object>>) params.get("properties");
            List<SkuProperty> properties = new ArrayList<>();
            if (propList != null) {
                for (Map<String, Object> prop : propList) {
                    properties.add(new SkuProperty(
                            ((Number) prop.get("propertyId")).longValue(),
                            (String) prop.get("propertyName"),
                            ((Number) prop.get("valueId")).longValue(),
                            (String) prop.get("valueName")
                    ));
                }
            }
            skus.add(new ProductSku(
                    skuId, properties,
                    (Integer) params.get("price"),
                    (Integer) params.get("marketPrice"),
                    (Integer) params.get("costPrice"),
                    (String) params.get("barCode"),
                    (String) params.get("picUrl"),
                    (Integer) params.get("stock"),
                    (Double) params.get("weight"),
                    (Double) params.get("volume"),
                    null
            ));
        }
        return skus;
    }

    private void publishEvents(ProductSpu spu) {
        for (var event : spu.pullEvents()) {
            eventPublisher.publish(event);
        }
    }
}
