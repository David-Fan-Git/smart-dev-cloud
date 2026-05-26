package com.develop.mvp.pk.module.infra.domain.db.valueobject;

// DDD 角色：数据源连接URL值对象
import java.util.Objects;

public final class DataSourceConfigUrl {
    private final String value;

    private DataSourceConfigUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("数据源连接URL不能为空");
        }
        this.value = value;
    }

    public static DataSourceConfigUrl of(String value) { return new DataSourceConfigUrl(value); }

    public String value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSourceConfigUrl that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
