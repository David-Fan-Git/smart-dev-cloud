package com.develop.mvp.pk.module.system.application.module.command;

import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleDependency;

import java.util.List;

public record RegisterModuleCommand(
        String code,
        String name,
        String version,
        boolean enabled,
        List<ModuleDependency> dependencies,
        int order,
        String description) {
}
