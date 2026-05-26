package com.develop.mvp.pk.module.system.domain.module.service;

import com.develop.mvp.pk.module.system.domain.module.exception.ModuleDependencyCycleException;
import com.develop.mvp.pk.module.system.domain.module.exception.ModuleDependencyMissingException;
import com.develop.mvp.pk.module.system.domain.module.model.SystemModule;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleDependency;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModuleDependencyResolverTest {

    @Test
    void resolveSortsDependenciesBeforeDependents() {
        SystemModule user = module("system-user", List.of(ModuleDependency.required("system-permission")));
        SystemModule permission = module("system-permission", List.of());

        List<SystemModule> sorted = new ModuleDependencyResolver().resolve(List.of(user, permission));

        assertEquals(List.of("system-permission", "system-user"), sorted.stream().map(it -> it.code().value()).toList());
    }

    @Test
    void resolveRejectsMissingRequiredDependency() {
        SystemModule user = module("system-user", List.of(ModuleDependency.required("system-permission")));

        ModuleDependencyMissingException ex = assertThrows(ModuleDependencyMissingException.class,
                () -> new ModuleDependencyResolver().resolve(List.of(user)));

        assertEquals("模块 system-user 依赖的模块 system-permission 不存在", ex.getMessage());
    }

    @Test
    void resolveAllowsMissingOptionalDependency() {
        SystemModule user = module("system-user", List.of(ModuleDependency.optional("system-permission")));

        List<SystemModule> sorted = new ModuleDependencyResolver().resolve(List.of(user));

        assertEquals(List.of("system-user"), sorted.stream().map(it -> it.code().value()).toList());
    }

    @Test
    void resolveRejectsCycle() {
        SystemModule user = module("system-user", List.of(ModuleDependency.required("system-permission")));
        SystemModule permission = module("system-permission", List.of(ModuleDependency.required("system-user")));

        ModuleDependencyCycleException ex = assertThrows(ModuleDependencyCycleException.class,
                () -> new ModuleDependencyResolver().resolve(List.of(user, permission)));

        assertTrue(ex.getMessage().contains("模块依赖存在循环"));
    }

    private SystemModule module(String code, List<ModuleDependency> dependencies) {
        return SystemModule.create(ModuleCode.of(code), code, ModuleVersion.of("1.0.0"), true, dependencies, 0, code);
    }
}
