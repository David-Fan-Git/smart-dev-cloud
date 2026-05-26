package com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class OrderItem {
    private final Long id;
    private final Long userId;
    private final Long cartId;
    private final Long spuId;
    private final String spuName;
    private final Long skuId;
    private final List<OrderItemProperty> properties;
    private final String picUrl;
    private final Integer count;
    private final Boolean commentStatus;
    private final Integer price;
    private final Integer discountPrice;
    private final Integer deliveryPrice;
    private final Integer adjustPrice;
    private final Integer payPrice;
    private final Integer couponPrice;
    private final Integer pointPrice;
    private final Integer usePoint;
    private final Integer givePoint;
    private final Integer vipPrice;
    private final Long afterSaleId;
    private final Integer afterSaleStatus;

    public OrderItem(Long id, Long userId, Long cartId, Long spuId, String spuName, Long skuId,
                     List<OrderItemProperty> properties,
                     String picUrl, Integer count, Boolean commentStatus,
                     Integer price, Integer discountPrice, Integer deliveryPrice, Integer adjustPrice,
                     Integer payPrice, Integer couponPrice, Integer pointPrice, Integer usePoint,
                     Integer givePoint, Integer vipPrice,
                     Long afterSaleId, Integer afterSaleStatus) {
        this.id = id; this.userId = userId; this.cartId = cartId;
        this.spuId = Objects.requireNonNull(spuId); this.spuName = spuName;
        this.skuId = Objects.requireNonNull(skuId);
        this.properties = properties != null ? Collections.unmodifiableList(properties) : Collections.emptyList();
        this.picUrl = picUrl; this.count = Objects.requireNonNull(count);
        this.commentStatus = commentStatus; this.price = Objects.requireNonNull(price);
        this.discountPrice = discountPrice; this.deliveryPrice = deliveryPrice; this.adjustPrice = adjustPrice;
        this.payPrice = Objects.requireNonNull(payPrice); this.couponPrice = couponPrice; this.pointPrice = pointPrice;
        this.usePoint = usePoint; this.givePoint = givePoint; this.vipPrice = vipPrice;
        this.afterSaleId = afterSaleId; this.afterSaleStatus = afterSaleStatus;
    }

    public Long id() { return id; } public Long userId() { return userId; }
    public Long cartId() { return cartId; } public Long spuId() { return spuId; }
    public String spuName() { return spuName; } public Long skuId() { return skuId; }
    public List<OrderItemProperty> properties() { return properties; }
    public String picUrl() { return picUrl; } public Integer count() { return count; }
    public Boolean commentStatus() { return commentStatus; } public Integer price() { return price; }
    public Integer discountPrice() { return discountPrice; } public Integer deliveryPrice() { return deliveryPrice; }
    public Integer adjustPrice() { return adjustPrice; } public Integer payPrice() { return payPrice; }
    public Integer couponPrice() { return couponPrice; } public Integer pointPrice() { return pointPrice; }
    public Integer usePoint() { return usePoint; } public Integer givePoint() { return givePoint; }
    public Integer vipPrice() { return vipPrice; } public Long afterSaleId() { return afterSaleId; }
    public Integer afterSaleStatus() { return afterSaleStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
