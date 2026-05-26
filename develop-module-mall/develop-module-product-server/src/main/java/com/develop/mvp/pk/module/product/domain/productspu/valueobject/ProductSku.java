package com.develop.mvp.pk.module.product.domain.productspu.valueobject;

// Skill: AggregateRoot_ProductSpu_Validation_Skill — 值对象 ProductSku
// DDD 角色：SKU 值对象，作为 ProductSpu 聚合的一部分
// 验收标准 AC04：final 字段，无 setter

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ProductSku {
    private final Long id;
    private final List<SkuProperty> properties;
    private final Integer price;
    private final Integer marketPrice;
    private final Integer costPrice;
    private final String barCode;
    private final String picUrl;
    private final Integer stock;
    private final Double weight;
    private final Double volume;
    private final Integer salesCount;

    public ProductSku(Long id, List<SkuProperty> properties, Integer price, Integer marketPrice,
                      Integer costPrice, String barCode, String picUrl, Integer stock,
                      Double weight, Double volume, Integer salesCount) {
        this.id = id;
        this.properties = properties != null ? Collections.unmodifiableList(properties) : Collections.emptyList();
        this.price = Objects.requireNonNull(price, "SKU价格不能为空");
        this.marketPrice = marketPrice;
        this.costPrice = costPrice;
        this.barCode = barCode;
        this.picUrl = picUrl;
        this.stock = Objects.requireNonNull(stock, "SKU库存不能为空");
        this.weight = weight;
        this.volume = volume;
        this.salesCount = salesCount != null ? salesCount : 0;
    }

    public Long id() { return id; }
    public List<SkuProperty> properties() { return properties; }
    public Integer price() { return price; }
    public Integer marketPrice() { return marketPrice; }
    public Integer costPrice() { return costPrice; }
    public String barCode() { return barCode; }
    public String picUrl() { return picUrl; }
    public Integer stock() { return stock; }
    public Double weight() { return weight; }
    public Double volume() { return volume; }
    public Integer salesCount() { return salesCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSku that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
