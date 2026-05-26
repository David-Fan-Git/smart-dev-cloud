package com.develop.mvp.pk.module.iot.domain.device.valueobject;

import java.util.Objects;

public final class IotDeviceId {

    private final Long value;

    private IotDeviceId(Long value) {
        this.value = Objects.requireNonNull(value, "设备ID不能为空");
    }

    public static IotDeviceId of(Long value) {
        return new IotDeviceId(value);
    }

    public Long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IotDeviceId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "IotDeviceId{" + "value=" + value + '}';
    }
}
