package com.develop.mvp.pk.module.mes.domain.dv.valueobject;

import java.util.Objects;

public final class MesMachineryStatus {

    public static final MesMachineryStatus STOP = new MesMachineryStatus(1, "停机");
    public static final MesMachineryStatus PRODUCING = new MesMachineryStatus(2, "生产中");
    public static final MesMachineryStatus MAINTENANCE = new MesMachineryStatus(3, "维护中");

    private final Integer code;
    private final String name;

    private MesMachineryStatus(Integer code, String name) {
        this.code = Objects.requireNonNull(code, "设备状态码不能为空");
        this.name = name;
    }

    public static MesMachineryStatus of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("设备状态码不能为空");
        }
        return switch (code) {
            case 1 -> STOP;
            case 2 -> PRODUCING;
            case 3 -> MAINTENANCE;
            default -> throw new IllegalArgumentException("未知的设备状态码: " + code);
        };
    }

    public Integer code() {
        return code;
    }

    public String name() {
        return name;
    }

    public boolean isStop() {
        return this == STOP;
    }

    public boolean isProducing() {
        return this == PRODUCING;
    }

    public boolean isMaintenance() {
        return this == MAINTENANCE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MesMachineryStatus that)) return false;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "MesMachineryStatus{" + "code=" + code + ", name=" + name + '}';
    }
}
