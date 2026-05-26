package com.develop.mvp.pk.module.system.domain.module.service;

import com.develop.mvp.pk.module.system.domain.module.exception.ModuleDependencyCycleException;
import com.develop.mvp.pk.module.system.domain.module.exception.ModuleDependencyMissingException;
import com.develop.mvp.pk.module.system.domain.module.model.SystemModule;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleDependency;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleDependencyResolver {

    public List<SystemModule> resolve(List<SystemModule> modules) {
        Map<ModuleCode, SystemModule> moduleMap = modules.stream()
                .collect(Collectors.toMap(SystemModule::code, it -> it, (left, right) -> {
                    throw new IllegalStateException("模块编码重复: " + left.code().value());
                }));
        List<SystemModule> sorted = new ArrayList<>();
        Set<ModuleCode> visiting = new HashSet<>();
        Set<ModuleCode> visited = new HashSet<>();
        for (SystemModule module : modules.stream().sorted(Comparator.comparingInt(SystemModule::order)).toList()) {
            visit(module, moduleMap, visiting, visited, sorted, new ArrayDeque<>());
        }
        return sorted;
    }

    private void visit(SystemModule module, Map<ModuleCode, SystemModule> moduleMap, Set<ModuleCode> visiting,
                       Set<ModuleCode> visited, List<SystemModule> sorted, Deque<ModuleCode> path) {
        if (visited.contains(module.code())) {
            return;
        }
        if (visiting.contains(module.code())) {
            path.addLast(module.code());
            throw new ModuleDependencyCycleException(path.stream().map(ModuleCode::value).collect(Collectors.joining(" -> ")));
        }
        visiting.add(module.code());
        path.addLast(module.code());
        for (ModuleDependency dependency : module.dependencies()) {
            SystemModule dependencyModule = moduleMap.get(dependency.moduleCode());
            if (dependencyModule == null) {
                if (!dependency.optional()) {
                    throw new ModuleDependencyMissingException(module.code(), dependency.moduleCode());
                }
                continue;
            }
            visit(dependencyModule, moduleMap, visiting, visited, sorted, path);
        }
        path.removeLast();
        visiting.remove(module.code());
        visited.add(module.code());
        sorted.add(module);
    }
}
