package com.develop.mvp.pk.module.infra.domain.db.valueobject;

// DDD 角色：数据源配置标识值对象
import java.util.Objects;

public final class DataSourceConfigId {
    private final Long value;

    private DataSourceConfigId(Long value) {
        this.value = Objects.requireNonNull(value, "数据源配置ID不能为空");
    }

    public static DataSourceConfigId of(Long value) { return new DataSourceConfigId(value); }

    public Long value() { return value; }

    public boolean isMaster() { return value == 0L; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataSourceConfigId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "DataSourceConfigId{" + value + '}'; }
}
