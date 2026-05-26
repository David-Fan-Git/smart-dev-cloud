package com.develop.mvp.pk.module.product.domain.productspu;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 聚合根 ProductSpu
// DDD 角色：商品 SPU 聚合根，封装商品完整生命周期和业务规则
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解，不注入 Mapper

import com.develop.mvp.pk.module.product.domain.productspu.event.ProductSpuCreatedEvent;
import com.develop.mvp.pk.module.product.domain.productspu.event.ProductSpuDeletedEvent;
import com.develop.mvp.pk.module.product.domain.productspu.event.ProductSpuStatusChangedEvent;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSku;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSpuId;
import com.develop.mvp.pk.module.product.domain.productspu.valueobject.ProductSpuStatus;
import com.develop.mvp.pk.module.product.domain.event.DomainEvent;

import java.util.*;
import java.util.stream.Collectors;

public final class ProductSpu {

    // ── 聚合根标识 ──
    private final ProductSpuId id;

    // ── 基本信息 ──
    private String name;
    private String keyword;
    private String introduction;
    private String description;
    private Long categoryId;
    private Long brandId;
    private String picUrl;
    private List<String> sliderPicUrls;
    private Integer sort;
    private ProductSpuStatus status;

    // ── SKU 相关 ──
    private Boolean specType;
    private List<ProductSku> skus;
    private Integer price;
    private Integer marketPrice;
    private Integer costPrice;
    private Integer stock;

    // ── 物流相关 ──
    private List<Integer> deliveryTypes;
    private Long deliveryTemplateId;

    // ── 营销相关 ──
    private Integer giveIntegral;
    private Boolean subCommissionType;

    // ── 统计相关 ──
    private Integer salesCount;
    private Integer virtualSalesCount;
    private Integer browseCount;

    // ── 领域事件收集 ──
    private final List<DomainEvent> events = new ArrayList<>();

    ProductSpu(ProductSpuId id, String name, String keyword, String introduction,
               String description, Long categoryId, Long brandId, String picUrl,
               List<String> sliderPicUrls, Integer sort, ProductSpuStatus status,
               Boolean specType, List<ProductSku> skus, Integer price, Integer marketPrice,
               Integer costPrice, Integer stock, List<Integer> deliveryTypes,
               Long deliveryTemplateId, Integer giveIntegral, Boolean subCommissionType,
               Integer salesCount, Integer virtualSalesCount, Integer browseCount) {
        this.id = Objects.requireNonNull(id, "SPU编号不能为空");
        this.name = Objects.requireNonNull(name, "商品名称不能为空");
        this.keyword = keyword;
        this.introduction = introduction;
        this.description = description;
        this.categoryId = Objects.requireNonNull(categoryId, "分类编号不能为空");
        this.brandId = brandId;
        this.picUrl = picUrl;
        this.sliderPicUrls = sliderPicUrls != null ? new ArrayList<>(sliderPicUrls) : new ArrayList<>();
        this.sort = sort != null ? sort : 0;
        this.status = status != null ? status : ProductSpuStatus.DISABLED;
        this.specType = specType != null ? specType : false;
        this.skus = skus != null ? new ArrayList<>(skus) : new ArrayList<>();
        this.price = price;
        this.marketPrice = marketPrice;
        this.costPrice = costPrice;
        this.stock = stock != null ? stock : 0;
        this.deliveryTypes = deliveryTypes != null ? new ArrayList<>(deliveryTypes) : new ArrayList<>();
        this.deliveryTemplateId = deliveryTemplateId;
        this.giveIntegral = giveIntegral;
        this.subCommissionType = subCommissionType;
        this.salesCount = salesCount != null ? salesCount : 0;
        this.virtualSalesCount = virtualSalesCount != null ? virtualSalesCount : 0;
        this.browseCount = browseCount != null ? browseCount : 0;
    }

    // ── 业务方法 ──

    /** 上架商品 */
    public void publish() {
        if (this.status.isRecycle()) {
            throw new IllegalStateException("回收站中的商品不能上架");
        }
        if (this.skus.isEmpty()) {
            throw new IllegalStateException("商品至少需要一个SKU才能上架");
        }
        Integer oldStatus = this.status.code();
        this.status = this.status.enable();
        events.add(new ProductSpuStatusChangedEvent(this.id.value(), oldStatus, this.status.code()));
    }

    /** 下架商品 */
    public void unpublish() {
        Integer oldStatus = this.status.code();
        this.status = this.status.disable();
        events.add(new ProductSpuStatusChangedEvent(this.id.value(), oldStatus, this.status.code()));
    }

    /** 放入回收站 */
    public void recycle() {
        Integer oldStatus = this.status.code();
        this.status = this.status.recycle();
        events.add(new ProductSpuStatusChangedEvent(this.id.value(), oldStatus, this.status.code()));
    }

    /** 标记删除（从回收站彻底删除） */
    public void markDeleted() {
        if (!this.status.isRecycle()) {
            throw new IllegalStateException("只有回收站中的商品才能删除");
        }
        events.add(new ProductSpuDeletedEvent(this.id.value(), this.name));
    }

    /** 更新 SPU 基本信息 */
    public void updateProfile(String name, String keyword, String introduction, String description,
                               Long categoryId, Long brandId, String picUrl, List<String> sliderPicUrls,
                               Integer sort, List<Integer> deliveryTypes, Long deliveryTemplateId,
                               Integer giveIntegral, Boolean subCommissionType) {
        this.name = Objects.requireNonNull(name, "商品名称不能为空");
        this.keyword = keyword;
        this.introduction = introduction;
        this.description = description;
        this.categoryId = Objects.requireNonNull(categoryId, "分类编号不能为空");
        this.brandId = brandId;
        this.picUrl = picUrl;
        this.sliderPicUrls = sliderPicUrls != null ? new ArrayList<>(sliderPicUrls) : this.sliderPicUrls;
        this.sort = sort != null ? sort : this.sort;
        this.deliveryTypes = deliveryTypes != null ? new ArrayList<>(deliveryTypes) : this.deliveryTypes;
        this.deliveryTemplateId = deliveryTemplateId;
        this.giveIntegral = giveIntegral;
        this.subCommissionType = subCommissionType;
    }

    /** 更新 SKU 列表，并重新计算价格和库存 */
    public void updateSkus(List<ProductSku> newSkus) {
        this.skus = new ArrayList<>(newSkus);
        // 重新计算 SPU 级价格和库存
        this.price = newSkus.stream()
                .filter(s -> s.price() != null)
                .mapToInt(ProductSku::price)
                .min().orElse(0);
        this.marketPrice = newSkus.stream()
                .filter(s -> s.marketPrice() != null)
                .mapToInt(ProductSku::marketPrice)
                .min().orElse(0);
        this.costPrice = newSkus.stream()
                .filter(s -> s.costPrice() != null)
                .mapToInt(ProductSku::costPrice)
                .min().orElse(0);
        this.stock = newSkus.stream()
                .filter(s -> s.stock() != null)
                .mapToInt(ProductSku::stock)
                .sum();
    }

    /** 更新库存 */
    public void updateStock(int incrCount) {
        this.stock += incrCount;
        if (this.stock < 0) this.stock = 0;
    }

    /** 增加销量 */
    public void incrementSalesCount(int count) {
        this.salesCount += count;
    }

    /** 增加浏览量 */
    public void incrementBrowseCount(int count) {
        this.browseCount += count;
    }

    /** 设置规格类型 */
    public void setSpecType(Boolean specType) {
        this.specType = specType != null ? specType : false;
    }

    // ── 查询方法 ──

    public ProductSpuId id() { return id; }
    public String name() { return name; }
    public String keyword() { return keyword; }
    public String introduction() { return introduction; }
    public String description() { return description; }
    public Long categoryId() { return categoryId; }
    public Long brandId() { return brandId; }
    public String picUrl() { return picUrl; }
    public List<String> sliderPicUrls() { return Collections.unmodifiableList(sliderPicUrls); }
    public Integer sort() { return sort; }
    public ProductSpuStatus status() { return status; }
    public Boolean specType() { return specType; }
    public List<ProductSku> skus() { return Collections.unmodifiableList(skus); }
    public Integer price() { return price; }
    public Integer marketPrice() { return marketPrice; }
    public Integer costPrice() { return costPrice; }
    public Integer stock() { return stock; }
    public List<Integer> deliveryTypes() { return Collections.unmodifiableList(deliveryTypes); }
    public Long deliveryTemplateId() { return deliveryTemplateId; }
    public Integer giveIntegral() { return giveIntegral; }
    public Boolean subCommissionType() { return subCommissionType; }
    public Integer salesCount() { return salesCount; }
    public Integer virtualSalesCount() { return virtualSalesCount; }
    public Integer browseCount() { return browseCount; }

    public boolean isEnabled() { return status.isEnabled(); }
    public boolean isDisabled() { return status.isDisabled(); }
    public boolean isRecycle() { return status.isRecycle(); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSpu that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "ProductSpu{id=" + id + ", name='" + name + "'}";
    }
}
