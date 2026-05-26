package com.develop.mvp.pk.module.ai.domain.model.valueobject;

import java.util.Objects;

public final class AiModelStatus {

    public static final AiModelStatus ENABLED = new AiModelStatus(0, "开启");
    public static final AiModelStatus DISABLED = new AiModelStatus(1, "禁用");

    private final Integer code;
    private final String name;

    private AiModelStatus(Integer code, String name) {
        this.code = Objects.requireNonNull(code, "模型状态码不能为空");
        this.name = name;
    }

    public static AiModelStatus of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("模型状态码不能为空");
        }
        return switch (code) {
            case 0 -> ENABLED;
            case 1 -> DISABLED;
            default -> throw new IllegalArgumentException("未知的模型状态码: " + code);
        };
    }

    public Integer code() {
        return code;
    }

    public String name() {
        return name;
    }

    public boolean isEnabled() {
        return this == ENABLED;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

    public AiModelStatus enable() {
        return ENABLED;
    }

    public AiModelStatus disable() {
        return DISABLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AiModelStatus that)) return false;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "AiModelStatus{" + "code=" + code + ", name=" + name + '}';
    }
}
