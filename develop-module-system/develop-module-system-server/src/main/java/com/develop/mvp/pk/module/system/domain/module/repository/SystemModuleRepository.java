package com.develop.mvp.pk.module.system.domain.module.repository;

import com.develop.mvp.pk.module.system.domain.module.model.SystemModule;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;

import java.util.List;
import java.util.Optional;

public interface SystemModuleRepository {

    void save(SystemModule module);

    Optional<SystemModule> findByCode(ModuleCode code);

    List<SystemModule> findAll();
}
