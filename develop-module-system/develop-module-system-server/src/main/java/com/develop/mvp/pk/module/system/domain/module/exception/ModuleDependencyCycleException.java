package com.develop.mvp.pk.module.system.domain.module.exception;

public class ModuleDependencyCycleException extends RuntimeException {

    private final String cyclePath;

    public ModuleDependencyCycleException(String cyclePath) {
        super("模块依赖存在循环: " + cyclePath);
        this.cyclePath = cyclePath;
    }

    public String cyclePath() {
        return cyclePath;
    }
}
