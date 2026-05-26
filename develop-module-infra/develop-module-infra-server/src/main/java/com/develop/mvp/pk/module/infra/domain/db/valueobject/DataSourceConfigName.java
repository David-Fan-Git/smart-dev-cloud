package com.develop.mvp.pk.module.infra.domain.db.valueobject;

// DDD 角色：数据源连接名值对象
import java.util.Objects;

public final class DataSourceConfigName {
    private final String value;

    private DataSourceConfigName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("数据源连接名不能为空");
        }
        this.value = value;
    }

    public static DataSourceConfigName of(String value) { return new DataSourceConfigName(value); }

    public String value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSourceConfigName that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
