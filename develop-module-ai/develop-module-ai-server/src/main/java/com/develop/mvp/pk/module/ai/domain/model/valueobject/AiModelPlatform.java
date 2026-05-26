package com.develop.mvp.pk.module.ai.domain.model.valueobject;

import com.develop.mvp.pk.module.ai.enums.model.AiPlatformEnum;

import java.util.Objects;

public final class AiModelPlatform {

    private final String platform;
    private final String name;

    private AiModelPlatform(String platform, String name) {
        this.platform = Objects.requireNonNull(platform, "模型平台不能为空");
        this.name = name;
    }

    public static AiModelPlatform of(String platform) {
        AiPlatformEnum platformEnum = AiPlatformEnum.validatePlatform(platform);
        return new AiModelPlatform(platformEnum.getPlatform(), platformEnum.getName());
    }

    public String platform() {
        return platform;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AiModelPlatform that)) return false;
        return Objects.equals(platform, that.platform);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform);
    }

    @Override
    public String toString() {
        return "AiModelPlatform{" + "platform='" + platform + '\'' + ", name='" + name + '\'' + '}';
    }
}
