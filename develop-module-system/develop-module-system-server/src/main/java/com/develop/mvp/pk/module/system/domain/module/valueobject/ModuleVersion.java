package com.develop.mvp.pk.module.system.domain.module.valueobject;

import java.util.regex.Pattern;

public record ModuleVersion(String value) {

    private static final Pattern PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[A-Za-z0-9.-]+)?$");

    public ModuleVersion {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("模块版本必须符合语义化版本格式，例如 1.0.0");
        }
    }

    public static ModuleVersion of(String value) {
        return new ModuleVersion(value);
    }
}
