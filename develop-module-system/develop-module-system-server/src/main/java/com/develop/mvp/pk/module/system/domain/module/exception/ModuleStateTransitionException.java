package com.develop.mvp.pk.module.system.domain.module.exception;

import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleRuntimeState;

public class ModuleStateTransitionException extends RuntimeException {

    private final ModuleCode moduleCode;
    private final ModuleRuntimeState sourceState;
    private final ModuleRuntimeState targetState;

    public ModuleStateTransitionException(ModuleCode moduleCode, ModuleRuntimeState sourceState, ModuleRuntimeState targetState) {
        super("模块 " + moduleCode.value() + " 当前状态 " + sourceState + " 不允许迁移到 " + targetState);
        this.moduleCode = moduleCode;
        this.sourceState = sourceState;
        this.targetState = targetState;
    }

    public ModuleCode moduleCode() {
        return moduleCode;
    }

    public ModuleRuntimeState sourceState() {
        return sourceState;
    }

    public ModuleRuntimeState targetState() {
        return targetState;
    }
}
