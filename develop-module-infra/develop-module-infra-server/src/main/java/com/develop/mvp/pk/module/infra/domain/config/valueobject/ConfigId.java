package com.develop.mvp.pk.module.infra.domain.config.valueobject;

// DDD 角色：系统配置标识值对象
import java.util.Objects;

public final class ConfigId {
    private final Long value;

    private ConfigId(Long value) {
        this.value = Objects.requireNonNull(value, "配置ID不能为空");
    }

    public static ConfigId of(Long value) { return new ConfigId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "ConfigId{" + value + '}'; }
}
