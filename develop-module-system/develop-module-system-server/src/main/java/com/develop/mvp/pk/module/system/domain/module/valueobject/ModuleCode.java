package com.develop.mvp.pk.module.system.domain.module.valueobject;

import java.util.regex.Pattern;

public record ModuleCode(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[a-z][a-z0-9-]{1,63}$");

    public ModuleCode {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("模块编码必须以小写字母开头，仅允许小写字母、数字和连字符，长度 2-64");
        }
    }

    public static ModuleCode of(String value) {
        return new ModuleCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
