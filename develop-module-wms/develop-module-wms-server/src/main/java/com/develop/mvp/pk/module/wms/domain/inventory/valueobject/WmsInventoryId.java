package com.develop.mvp.pk.module.wms.domain.inventory.valueobject;

import java.util.Objects;

public final class WmsInventoryId {

    private final Long value;

    private WmsInventoryId(Long value) {
        this.value = Objects.requireNonNull(value, "库存ID不能为空");
    }

    public static WmsInventoryId of(Long value) {
        return new WmsInventoryId(value);
    }

    public Long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WmsInventoryId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "WmsInventoryId{" + "value=" + value + '}';
    }
}
