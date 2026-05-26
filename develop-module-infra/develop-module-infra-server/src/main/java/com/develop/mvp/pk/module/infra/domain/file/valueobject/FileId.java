package com.develop.mvp.pk.module.infra.domain.file.valueobject;

// DDD 角色：文件标识值对象
import java.util.Objects;

public final class FileId {
    private final Long value;

    private FileId(Long value) {
        this.value = Objects.requireNonNull(value, "文件ID不能为空");
    }

    public static FileId of(Long value) { return new FileId(value); }

    public Long value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return "FileId{" + value + '}'; }
}
