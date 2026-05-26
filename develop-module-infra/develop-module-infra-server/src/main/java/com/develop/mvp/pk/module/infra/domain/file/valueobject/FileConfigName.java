package com.develop.mvp.pk.module.infra.domain.file.valueobject;

// DDD 角色：文件存储配置名称值对象
import java.util.Objects;

public final class FileConfigName {
    private final String value;

    private FileConfigName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("文件配置名称不能为空");
        }
        this.value = value;
    }

    public static FileConfigName of(String value) { return new FileConfigName(value); }

    public String value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileConfigName that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
