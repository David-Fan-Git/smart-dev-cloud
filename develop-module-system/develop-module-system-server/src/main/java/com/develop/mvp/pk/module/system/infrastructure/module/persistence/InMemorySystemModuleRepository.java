package com.develop.mvp.pk.module.system.infrastructure.module.persistence;

import com.develop.mvp.pk.module.system.domain.module.model.SystemModule;
import com.develop.mvp.pk.module.system.domain.module.repository.SystemModuleRepository;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemorySystemModuleRepository implements SystemModuleRepository {

    private final ConcurrentMap<ModuleCode, SystemModule> modules = new ConcurrentHashMap<>();

    @Override
    public void save(SystemModule module) {
        modules.put(module.code(), module);
    }

    @Override
    public Optional<SystemModule> findByCode(ModuleCode code) {
        return Optional.ofNullable(modules.get(code));
    }

    @Override
    public List<SystemModule> findAll() {
        return new ArrayList<>(modules.values()).stream()
                .sorted(Comparator.comparingInt(SystemModule::order).thenComparing(it -> it.code().value()))
                .toList();
    }
}
