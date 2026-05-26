package com.develop.mvp.pk.module.ai.domain.model.valueobject;

import java.util.Objects;

public final class AiModelId {

    private final Long value;

    private AiModelId(Long value) {
        this.value = Objects.requireNonNull(value, "模型ID不能为空");
    }

    public static AiModelId of(Long value) {
        return new AiModelId(value);
    }

    public Long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AiModelId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "AiModelId{" + "value=" + value + '}';
    }
}
