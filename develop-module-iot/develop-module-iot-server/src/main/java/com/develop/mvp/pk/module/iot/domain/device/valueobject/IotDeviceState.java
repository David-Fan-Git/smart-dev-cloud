package com.develop.mvp.pk.module.iot.domain.device.valueobject;

import java.util.Objects;

public final class IotDeviceState {

    public static final IotDeviceState INACTIVE = new IotDeviceState(0, "未激活");
    public static final IotDeviceState ONLINE = new IotDeviceState(1, "在线");
    public static final IotDeviceState OFFLINE = new IotDeviceState(2, "离线");

    private final Integer code;
    private final String name;

    private IotDeviceState(Integer code, String name) {
        this.code = Objects.requireNonNull(code, "设备状态码不能为空");
        this.name = name;
    }

    public static IotDeviceState of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("设备状态码不能为空");
        }
        return switch (code) {
            case 0 -> INACTIVE;
            case 1 -> ONLINE;
            case 2 -> OFFLINE;
            default -> throw new IllegalArgumentException("未知的设备状态码: " + code);
        };
    }

    public Integer code() {
        return code;
    }

    public String name() {
        return name;
    }

    public boolean isInactive() {
        return this == INACTIVE;
    }

    public boolean isOnline() {
        return this == ONLINE;
    }

    public boolean isOffline() {
        return this == OFFLINE;
    }

    public IotDeviceState toOnline() {
        return ONLINE;
    }

    public IotDeviceState toOffline() {
        return OFFLINE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IotDeviceState that)) return false;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "IotDeviceState{" + "code=" + code + ", name=" + name + '}';
    }
}
