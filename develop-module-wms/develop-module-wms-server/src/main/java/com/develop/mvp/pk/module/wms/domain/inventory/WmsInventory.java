package com.develop.mvp.pk.module.wms.domain.inventory;

import com.develop.mvp.pk.module.wms.domain.inventory.event.DomainEvent;
import com.develop.mvp.pk.module.wms.domain.inventory.event.WmsInventoryCreatedEvent;
import com.develop.mvp.pk.module.wms.domain.inventory.event.WmsInventoryStockChangedEvent;
import com.develop.mvp.pk.module.wms.domain.inventory.valueobject.WmsInventoryId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WmsInventory {

    private final WmsInventoryId id;
    private Long skuId;
    private Long warehouseId;
    private BigDecimal quantity;
    private String remark;

    private final List<DomainEvent> events = new ArrayList<>();

    // Full constructor for factory use (package-private)
    WmsInventory(Long id, Long skuId, Long warehouseId, BigDecimal quantity, String remark) {
        this.id = id != null ? WmsInventoryId.of(id) : null;
        this.skuId = Objects.requireNonNull(skuId, "SKU编号不能为空");
        this.warehouseId = Objects.requireNonNull(warehouseId, "仓库编号不能为空");
        this.quantity = normalizeQuantity(quantity);
        this.remark = remark;
    }

    // Minimal constructor for skeleton compatibility
    public WmsInventory(Long id, Long itemId, Long warehouseId) {
        this.id = id != null ? WmsInventoryId.of(id) : null;
        this.skuId = Objects.requireNonNull(itemId, "SKU编号不能为空");
        this.warehouseId = Objects.requireNonNull(warehouseId, "仓库编号不能为空");
        this.quantity = BigDecimal.ZERO;
        this.events.clear();
    }

    // Factory for backward compatibility
    public static WmsInventory of(Long id, Long itemId, Long warehouseId) {
        return new WmsInventory(id, itemId, warehouseId);
    }

    // Business methods

    public void addStock(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("增加库存数量必须大于0");
        }
        BigDecimal oldQuantity = this.quantity;
        this.quantity = this.quantity.add(amount);
        events.add(new WmsInventoryStockChangedEvent(
                this.id != null ? this.id.value() : null, this.skuId, this.warehouseId,
                oldQuantity, this.quantity, amount));
    }

    public void subtractStock(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("减少库存数量必须大于0");
        }
        BigDecimal newQuantity = this.quantity.subtract(amount);
        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("库存不足");
        }
        BigDecimal oldQuantity = this.quantity;
        this.quantity = newQuantity;
        events.add(new WmsInventoryStockChangedEvent(
                this.id != null ? this.id.value() : null, this.skuId, this.warehouseId,
                oldQuantity, this.quantity, amount.negate()));
    }

    public void setQuantity(BigDecimal quantity, String remark) {
        BigDecimal oldQuantity = this.quantity;
        this.quantity = normalizeQuantity(quantity);
        this.remark = remark;
        events.add(new WmsInventoryStockChangedEvent(
                this.id != null ? this.id.value() : null, this.skuId, this.warehouseId,
                oldQuantity, this.quantity, this.quantity.subtract(oldQuantity)));
    }

    private static BigDecimal normalizeQuantity(BigDecimal quantity) {
        BigDecimal normalized = quantity != null ? quantity : BigDecimal.ZERO;
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("库存数量不能小于0");
        }
        return normalized;
    }

    public void updateRemark(String remark) {
        this.remark = remark;
    }

    public void markCreated() {
        events.add(new WmsInventoryCreatedEvent(
                this.id != null ? this.id.value() : null, this.skuId, this.warehouseId, this.quantity));
    }

    // Query methods

    public WmsInventoryId inventoryId() { return id; }
    public Long id() { return id != null ? id.value() : null; }
    public Long skuId() { return skuId; }
    public Long itemId() { return skuId; } // for backward compatibility
    public Long warehouseId() { return warehouseId; }
    public BigDecimal quantity() { return quantity; }
    public Integer stock() { return quantity != null ? quantity.intValue() : 0; } // for backward compatibility
    public String remark() { return remark; }

    public boolean isEmpty() {
        return quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0;
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WmsInventory that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WmsInventory{id=" + id + ", skuId=" + skuId + ", warehouseId=" + warehouseId + '}';
    }
}
