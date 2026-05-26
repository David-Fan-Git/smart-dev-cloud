package com.develop.mvp.pk.module.system.application.module.service;

import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.module.system.application.module.command.RegisterModuleCommand;
import com.develop.mvp.pk.module.system.application.module.dto.SystemModuleDTO;
import com.develop.mvp.pk.module.system.application.module.port.inbound.SystemModuleUseCase;
import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleEventPort;
import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleMetricsPort;
import com.develop.mvp.pk.module.system.domain.module.event.ModuleStateChangedEvent;
import com.develop.mvp.pk.module.system.domain.module.exception.ModuleDependencyCycleException;
import com.develop.mvp.pk.module.system.domain.module.exception.ModuleDependencyMissingException;
import com.develop.mvp.pk.module.system.domain.module.exception.ModuleStateTransitionException;
import com.develop.mvp.pk.module.system.domain.module.model.SystemModule;
import com.develop.mvp.pk.module.system.domain.module.repository.SystemModuleRepository;
import com.develop.mvp.pk.module.system.domain.module.service.ModuleDependencyResolver;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleRuntimeState;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleVersion;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.develop.mvp.pk.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.develop.mvp.pk.module.system.enums.ErrorCodeConstants.*;

public class SystemModuleApplicationService implements SystemModuleUseCase {

    private final SystemModuleRepository repository;
    private final ModuleDependencyResolver dependencyResolver;
    private final ModuleEventPort eventPort;
    private final ModuleMetricsPort metricsPort;

    public SystemModuleApplicationService(SystemModuleRepository repository, ModuleDependencyResolver dependencyResolver,
                                          ModuleEventPort eventPort, ModuleMetricsPort metricsPort) {
        this.repository = repository;
        this.dependencyResolver = dependencyResolver;
        this.eventPort = eventPort;
        this.metricsPort = metricsPort;
    }

    @Override
    public void register(RegisterModuleCommand command) {
        ModuleCode code = ModuleCode.of(command.code());
        if (repository.findByCode(code).isPresent()) {
            throw exception(SYSTEM_MODULE_CODE_DUPLICATE, command.code());
        }
        repository.save(SystemModule.create(code, command.name(), ModuleVersion.of(command.version()), command.enabled(),
                command.dependencies(), command.order(), command.description()));
    }

    @Override
    public List<SystemModuleDTO> listModules() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public SystemModuleDTO getModule(String code) {
        return toDTO(getExistingModule(code));
    }

    @Override
    public void startModule(String code) {
        SystemModule target = getExistingModule(code);
        if (!target.enabled()) {
            throw exception(SYSTEM_MODULE_DISABLED, code);
        }
        for (SystemModule module : resolveStartupModules(target)) {
            if (module.state() == ModuleRuntimeState.RUNNING || !module.enabled()) {
                continue;
            }
            startSingleModule(module);
        }
    }

    @Override
    public void stopModule(String code) {
        SystemModule module = getExistingModule(code);
        try {
            module.markStopping();
            module.markStopped();
        } catch (ModuleStateTransitionException ex) {
            throw exception(SYSTEM_MODULE_STATE_INVALID, ex.moduleCode().value(), ex.sourceState(), ex.targetState());
        }
        repository.save(module);
        publishEvents(module);
    }

    private List<SystemModule> resolveStartupModules(SystemModule target) {
        Map<ModuleCode, SystemModule> moduleMap = repository.findAll().stream()
                .collect(Collectors.toMap(SystemModule::code, it -> it, (left, right) -> left, HashMap::new));
        Set<SystemModule> startupModules = new HashSet<>();
        collectRequiredModules(target, moduleMap, startupModules);
        try {
            return dependencyResolver.resolve(List.copyOf(startupModules));
        } catch (ModuleDependencyMissingException ex) {
            throw exception(SYSTEM_MODULE_DEPENDENCY_MISSING, ex.moduleCode().value(), ex.dependencyCode().value());
        } catch (ModuleDependencyCycleException ex) {
            throw exception(SYSTEM_MODULE_DEPENDENCY_CYCLE, ex.cyclePath());
        }
    }

    private void collectRequiredModules(SystemModule module, Map<ModuleCode, SystemModule> moduleMap, Set<SystemModule> startupModules) {
        if (!startupModules.add(module)) {
            return;
        }
        module.dependencies().stream()
                .filter(dependency -> !dependency.optional())
                .map(dependency -> moduleMap.get(dependency.moduleCode()))
                .filter(dependencyModule -> dependencyModule != null)
                .forEach(dependencyModule -> collectRequiredModules(dependencyModule, moduleMap, startupModules));
    }

    private void startSingleModule(SystemModule module) {
        Instant startedAt = Instant.now();
        try {
            if (module.state() == ModuleRuntimeState.NEW) {
                module.markResolved();
            }
            module.markStarting();
            module.markRunning();
            repository.save(module);
            publishEvents(module);
            metricsPort.recordStartDuration(module.code().value(), Duration.between(startedAt, Instant.now()));
        } catch (ModuleStateTransitionException ex) {
            module.markFailed(ex.getMessage());
            repository.save(module);
            publishEvents(module);
            metricsPort.recordFailure(module.code().value());
            throw exception(SYSTEM_MODULE_STATE_INVALID, ex.moduleCode().value(), ex.sourceState(), ex.targetState());
        } catch (ServiceException ex) {
            module.markFailed(ex.getMessage());
            repository.save(module);
            publishEvents(module);
            metricsPort.recordFailure(module.code().value());
            throw ex;
        } catch (RuntimeException ex) {
            module.markFailed(ex.getMessage());
            repository.save(module);
            publishEvents(module);
            metricsPort.recordFailure(module.code().value());
            throw exception(SYSTEM_MODULE_START_FAILED, module.code().value(), ex.getMessage());
        }
    }

    private void publishEvents(SystemModule module) {
        for (ModuleStateChangedEvent event : module.pullEvents()) {
            eventPort.publish(event);
            metricsPort.recordStateChange(event.moduleCode().value(), event.currentState());
        }
    }

    private SystemModule getExistingModule(String code) {
        return repository.findByCode(ModuleCode.of(code))
                .orElseThrow(() -> exception(SYSTEM_MODULE_NOT_EXISTS));
    }

    private SystemModuleDTO toDTO(SystemModule module) {
        return new SystemModuleDTO(module.code().value(), module.name(), module.version().value(), module.enabled(),
                module.dependencies().stream().map(it -> it.moduleCode().value()).toList(), module.order(),
                module.description(), module.state().name(), module.failureReason());
    }
}
