package com.develop.mvp.pk.module.trade.domain.tradeorder.valueobject;

import java.util.Objects;

public record OrderItemProperty(Long propertyId, String propertyName, Long valueId, String valueName) {

    public OrderItemProperty {
        Objects.requireNonNull(propertyId);
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(valueId);
        Objects.requireNonNull(valueName);
    }
}
