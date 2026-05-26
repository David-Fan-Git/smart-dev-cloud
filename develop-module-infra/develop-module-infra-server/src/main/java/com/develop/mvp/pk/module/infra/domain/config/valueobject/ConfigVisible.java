package com.develop.mvp.pk.module.infra.domain.config.valueobject;

// DDD 角色：配置可见性值对象
// 规则：不可见的配置（敏感参数）不允许返回给前端
import java.util.Objects;

public final class ConfigVisible {

    public static final ConfigVisible VISIBLE = new ConfigVisible(true);
    public static final ConfigVisible HIDDEN = new ConfigVisible(false);

    private final Boolean value;

    private ConfigVisible(Boolean value) {
        this.value = Objects.requireNonNull(value, "可见性不能为空");
    }

    public static ConfigVisible of(Boolean value) {
        return Boolean.TRUE.equals(value) ? VISIBLE : HIDDEN;
    }

    public boolean isVisible() { return value; }
    public boolean isHidden() { return !value; }

    public Boolean value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigVisible that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return isVisible() ? "VISIBLE" : "HIDDEN"; }
}
