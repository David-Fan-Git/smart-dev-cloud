package com.develop.mvp.pk.module.system.domain.module.valueobject;

public record ModuleDependency(ModuleCode moduleCode, String versionRange, boolean optional) {

    public ModuleDependency {
        if (moduleCode == null) {
            throw new IllegalArgumentException("依赖模块编码不能为空");
        }
        if (versionRange == null || versionRange.isBlank()) {
            versionRange = "*";
        }
    }

    public static ModuleDependency required(String moduleCode) {
        return new ModuleDependency(ModuleCode.of(moduleCode), "*", false);
    }

    public static ModuleDependency optional(String moduleCode) {
        return new ModuleDependency(ModuleCode.of(moduleCode), "*", true);
    }
}
