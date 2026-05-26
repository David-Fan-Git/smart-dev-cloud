package com.develop.mvp.pk.module.statistics.domain.productstatistics;

// Skill: AggregateRoot_ProductStatistics_Validation_Skill — 聚合根 ProductStatistics
// DDD 角色：商品统计聚合根，封装商品维度的统计数据
// 验收标准 AC01/AC02：无 MyBatis/Spring 注解

import com.develop.mvp.pk.module.statistics.domain.productstatistics.valueobject.ProductStatisticsId;
import com.develop.mvp.pk.module.statistics.domain.event.DomainEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ProductStatistics {

    private final ProductStatisticsId id;
    private Long spuId;
    private LocalDate date;
    private Integer browseCount;
    private Integer favoriteCount;
    private Integer cartCount;
    private Integer orderCount;
    private Integer orderPayCount;
    private Integer orderPayPrice;
    private final List<DomainEvent> events = new ArrayList<>();

    ProductStatistics(ProductStatisticsId id, Long spuId, LocalDate date,
                      Integer browseCount, Integer favoriteCount, Integer cartCount,
                      Integer orderCount, Integer orderPayCount, Integer orderPayPrice) {
        this.id = id;
        this.spuId = Objects.requireNonNull(spuId);
        this.date = Objects.requireNonNull(date);
        this.browseCount = browseCount != null ? browseCount : 0;
        this.favoriteCount = favoriteCount != null ? favoriteCount : 0;
        this.cartCount = cartCount != null ? cartCount : 0;
        this.orderCount = orderCount != null ? orderCount : 0;
        this.orderPayCount = orderPayCount != null ? orderPayCount : 0;
        this.orderPayPrice = orderPayPrice != null ? orderPayPrice : 0;
    }

    public void incrementBrowse(int count) { this.browseCount += count; }
    public void incrementFavorite(int count) { this.favoriteCount += count; }
    public void incrementCart(int count) { this.cartCount += count; }
    public void incrementOrder(int count) { this.orderCount += count; }
    public void incrementOrderPay(int count, int price) {
        this.orderPayCount += count;
        this.orderPayPrice += price;
    }

    public ProductStatisticsId id() { return id; }
    public Long spuId() { return spuId; }
    public LocalDate date() { return date; }
    public Integer browseCount() { return browseCount; }
    public Integer favoriteCount() { return favoriteCount; }
    public Integer cartCount() { return cartCount; }
    public Integer orderCount() { return orderCount; }
    public Integer orderPayCount() { return orderPayCount; }
    public Integer orderPayPrice() { return orderPayPrice; }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductStatistics that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
