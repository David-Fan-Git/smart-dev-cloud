package com.develop.mvp.pk.module.mp.domain.account.valueobject;

import java.util.Objects;

public final class MpAccountId {
    private final Long value;

    private MpAccountId(Long value) {
        this.value = Objects.requireNonNull(value, "accountId不能为空");
    }

    public static MpAccountId of(Long value) { return new MpAccountId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MpAccountId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "MpAccountId{" + value + '}'; }
}
