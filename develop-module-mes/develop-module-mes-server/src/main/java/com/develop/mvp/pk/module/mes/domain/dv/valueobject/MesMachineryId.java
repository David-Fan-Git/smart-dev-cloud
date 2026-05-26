package com.develop.mvp.pk.module.mes.domain.dv.valueobject;

import java.util.Objects;

public final class MesMachineryId {

    private final Long value;

    private MesMachineryId(Long value) {
        this.value = Objects.requireNonNull(value, "设备ID不能为空");
    }

    public static MesMachineryId of(Long value) {
        return new MesMachineryId(value);
    }

    public Long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MesMachineryId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "MesMachineryId{" + "value=" + value + '}';
    }
}
