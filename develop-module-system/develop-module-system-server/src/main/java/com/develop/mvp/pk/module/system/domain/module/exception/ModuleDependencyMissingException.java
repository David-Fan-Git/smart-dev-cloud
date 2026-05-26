package com.develop.mvp.pk.module.system.domain.module.exception;

import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;

public class ModuleDependencyMissingException extends RuntimeException {

    private final ModuleCode moduleCode;
    private final ModuleCode dependencyCode;

    public ModuleDependencyMissingException(ModuleCode moduleCode, ModuleCode dependencyCode) {
        super("模块 " + moduleCode.value() + " 依赖的模块 " + dependencyCode.value() + " 不存在");
        this.moduleCode = moduleCode;
        this.dependencyCode = dependencyCode;
    }

    public ModuleCode moduleCode() {
        return moduleCode;
    }

    public ModuleCode dependencyCode() {
        return dependencyCode;
    }
}
