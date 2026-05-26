package com.develop.mvp.pk.module.crm.domain.customer.valueobject;

import java.util.Objects;

public final class CrmCustomerId {
    private final Long value;

    private CrmCustomerId(Long value) {
        this.value = Objects.requireNonNull(value, "customerId不能为空");
    }

    public static CrmCustomerId of(Long value) { return new CrmCustomerId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrmCustomerId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "CrmCustomerId{" + value + '}'; }
}
