package com.develop.mvp.pk.module.infra.domain.config.valueobject;

// DDD 角色：配置类型值对象
// 规则 R01：SYSTEM 类型的配置不可删除
// 规则 R02：CUSTOM 类型为自定义配置
import com.develop.mvp.pk.module.infra.enums.config.ConfigTypeEnum;

import java.util.Objects;

public final class ConfigType {

    public static final ConfigType SYSTEM = new ConfigType(ConfigTypeEnum.SYSTEM.getType());
    public static final ConfigType CUSTOM = new ConfigType(ConfigTypeEnum.CUSTOM.getType());

    private final Integer code;

    private ConfigType(Integer code) {
        this.code = Objects.requireNonNull(code, "配置类型不能为空");
    }

    public static ConfigType of(Integer code) {
        if (ConfigTypeEnum.SYSTEM.getType().equals(code)) return SYSTEM;
        if (ConfigTypeEnum.CUSTOM.getType().equals(code)) return CUSTOM;
        throw new IllegalArgumentException("无效的配置类型: " + code);
    }

    public boolean isSystem() { return SYSTEM.code.equals(code); }
    public boolean isCustom() { return CUSTOM.code.equals(code); }

    public Integer code() { return code; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigType that)) return false;
        return code.equals(that.code);
    }

    @Override
    public int hashCode() { return Objects.hash(code); }

    @Override
    public String toString() { return isSystem() ? "SYSTEM" : "CUSTOM"; }
}
