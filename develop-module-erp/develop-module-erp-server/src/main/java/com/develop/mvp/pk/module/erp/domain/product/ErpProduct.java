package com.develop.mvp.pk.module.erp.domain.product;

import com.develop.mvp.pk.module.erp.domain.product.event.DomainEvent;
import com.develop.mvp.pk.module.erp.domain.product.event.ErpProductCreatedEvent;
import com.develop.mvp.pk.module.erp.domain.product.event.ErpProductDeletedEvent;
import com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductId;
import com.develop.mvp.pk.module.erp.domain.product.valueobject.ErpProductStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ErpProduct {

    private final ErpProductId id;
    private String name;
    private String barCode;
    private Long categoryId;
    private Long unitId;
    private ErpProductStatus status;
    private String standard;
    private String remark;
    private Integer expiryDay;
    private BigDecimal weight;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal minPrice;

    private final List<DomainEvent> events = new ArrayList<>();

    ErpProduct(ErpProductId id, String name, String barCode, Long categoryId, Long unitId,
               ErpProductStatus status, String standard, String remark, Integer expiryDay,
               BigDecimal weight, BigDecimal purchasePrice, BigDecimal salePrice,
               BigDecimal minPrice) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "产品名称不能为空");
        this.barCode = barCode;
        this.categoryId = categoryId;
        this.unitId = unitId;
        this.status = status != null ? status : ErpProductStatus.ENABLED;
        this.standard = standard;
        this.remark = remark;
        this.expiryDay = expiryDay;
        this.weight = weight;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.minPrice = minPrice;
    }

    ErpProduct(Long id, String name, String barCode, Long categoryId, Long unitId,
               ErpProductStatus status, String standard, String remark, Integer expiryDay,
               BigDecimal weight, BigDecimal purchasePrice, BigDecimal salePrice,
               BigDecimal minPrice) {
        this.id = id != null ? ErpProductId.of(id) : null;
        this.name = Objects.requireNonNull(name, "产品名称不能为空");
        this.barCode = barCode;
        this.categoryId = categoryId;
        this.unitId = unitId;
        this.status = status != null ? status : ErpProductStatus.ENABLED;
        this.standard = standard;
        this.remark = remark;
        this.expiryDay = expiryDay;
        this.weight = weight;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.minPrice = minPrice;
    }

    public static ErpProduct of(Long id, String name) {
        return new ErpProduct(id != null ? ErpProductId.of(id) : null, name, null, null, null, null, null, null, null, null, null, null, null);
    }

    public void updateProfile(String name, String barCode, Long categoryId, Long unitId,
                               String standard, String remark, Integer expiryDay,
                               BigDecimal weight, BigDecimal purchasePrice,
                               BigDecimal salePrice, BigDecimal minPrice) {
        this.name = Objects.requireNonNull(name, "产品名称不能为空");
        this.barCode = barCode;
        this.categoryId = categoryId;
        this.unitId = unitId;
        this.standard = standard;
        this.remark = remark;
        this.expiryDay = expiryDay;
        this.weight = weight;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.minPrice = minPrice;
    }

    public void enable() {
        this.status = this.status.enable();
    }

    public void disable() {
        this.status = this.status.disable();
    }

    public void markDeleted() {
        events.add(new ErpProductDeletedEvent(this.id.value(), this.name));
    }

    // Query methods

    public ErpProductId id() { return id; }
    public String name() { return name; }
    public String barCode() { return barCode; }
    public Long categoryId() { return categoryId; }
    public Long unitId() { return unitId; }
    public ErpProductStatus status() { return status; }
    public String standard() { return standard; }
    public String remark() { return remark; }
    public Integer expiryDay() { return expiryDay; }
    public BigDecimal weight() { return weight; }
    public BigDecimal purchasePrice() { return purchasePrice; }
    public BigDecimal salePrice() { return salePrice; }
    public BigDecimal minPrice() { return minPrice; }

    public boolean isEnabled() { return status.isEnabled(); }
    public boolean isDisabled() { return status.isDisabled(); }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErpProduct that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "ErpProduct{id=" + id + ", name=" + name + '}';
    }
}
