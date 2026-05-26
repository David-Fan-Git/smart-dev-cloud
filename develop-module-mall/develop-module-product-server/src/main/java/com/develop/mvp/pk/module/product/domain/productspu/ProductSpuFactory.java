package com.develop.mvp.pk.module.product.domain.productspu;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 工厂 ProductSpuFactory
// DDD 角色：工厂，负责创建和重建 ProductSpu 聚合

import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSku;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSpuId;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSpuStatus;

import java.util.List;

public final class ProductSpuFactory {

    private ProductSpuFactory() {}

    /** 创建新 SPU（默认状态为下架） */
    public static ProductSpu create(Long id, String name, String keyword, String introduction,
                                     String description, Long categoryId, Long brandId, String picUrl,
                                     List<String> sliderPicUrls, Integer sort, Integer status,
                                     Boolean specType, List<ProductSku> skus,
                                     List<Integer> deliveryTypes, Long deliveryTemplateId,
                                     Integer giveIntegral, Boolean subCommissionType,
                                     Integer virtualSalesCount) {
        ProductSpu spu = new ProductSpu(
                ProductSpuId.of(id),
                name, keyword, introduction, description,
                categoryId, brandId, picUrl, sliderPicUrls, sort,
                status != null ? ProductSpuStatus.of(status) : ProductSpuStatus.DISABLED,
                specType, skus, null, null, null, null,
                deliveryTypes, deliveryTemplateId, giveIntegral, subCommissionType,
                0, virtualSalesCount != null ? virtualSalesCount : 0, 0
        );
        // 如果有 SKU，从 SKU 初始化价格和库存
        if (skus != null && !skus.isEmpty()) {
            spu.updateSkus(skus);
        }
        return spu;
    }

    /** 从持久化数据重建 SPU 聚合（供仓储实现调用） */
    public static ProductSpu reconstitute(Long id, String name, String keyword, String introduction,
                                           String description, Long categoryId, Long brandId, String picUrl,
                                           List<String> sliderPicUrls, Integer sort, Integer status,
                                           Boolean specType, List<ProductSku> skus,
                                           Integer price, Integer marketPrice, Integer costPrice, Integer stock,
                                           List<Integer> deliveryTypes, Long deliveryTemplateId,
                                           Integer giveIntegral, Boolean subCommissionType,
                                           Integer salesCount, Integer virtualSalesCount, Integer browseCount) {
        return new ProductSpu(
                ProductSpuId.of(id),
                name, keyword, introduction, description,
                categoryId, brandId, picUrl, sliderPicUrls, sort,
                ProductSpuStatus.of(status),
                specType, skus, price, marketPrice, costPrice, stock,
                deliveryTypes, deliveryTemplateId, giveIntegral, subCommissionType,
                salesCount, virtualSalesCount, browseCount
        );
    }
}
