package com.develop.mvp.pk.module.infra.domain.config.valueobject;

// DDD 角色：配置键值对象
// 规则：配置键在全局不可重复
import java.util.Objects;

public final class ConfigKey {
    private final String value;

    private ConfigKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("配置键不能为空");
        }
        this.value = value;
    }

    public static ConfigKey of(String value) { return new ConfigKey(value); }

    public String value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigKey that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
