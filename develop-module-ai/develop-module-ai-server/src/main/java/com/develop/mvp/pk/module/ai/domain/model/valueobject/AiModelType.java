package com.develop.mvp.pk.module.ai.domain.model.valueobject;

import com.develop.mvp.pk.module.ai.enums.model.AiModelTypeEnum;

import java.util.Objects;

public final class AiModelType {

    private final Integer type;
    private final String name;

    private AiModelType(Integer type, String name) {
        this.type = Objects.requireNonNull(type, "模型类型不能为空");
        this.name = name;
    }

    public static AiModelType of(Integer type) {
        if (type == null) {
            throw new IllegalArgumentException("模型类型不能为空");
        }
        for (AiModelTypeEnum typeEnum : AiModelTypeEnum.values()) {
            if (typeEnum.getType().equals(type)) {
                return new AiModelType(typeEnum.getType(), typeEnum.getName());
            }
        }
        throw new IllegalArgumentException("未知的模型类型: " + type);
    }

    public Integer type() {
        return type;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AiModelType that)) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "AiModelType{" + "type=" + type + ", name=" + name + '}';
    }
}
