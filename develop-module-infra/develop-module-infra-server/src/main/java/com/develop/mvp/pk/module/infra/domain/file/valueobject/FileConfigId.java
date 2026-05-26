package com.develop.mvp.pk.module.infra.domain.file.valueobject;

// DDD 角色：文件存储配置标识值对象
import java.util.Objects;

public final class FileConfigId {
    private final Long value;

    private FileConfigId(Long value) {
        this.value = Objects.requireNonNull(value, "文件配置ID不能为空");
    }

    public static FileConfigId of(Long value) { return new FileConfigId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileConfigId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "FileConfigId{" + value + '}'; }
}
