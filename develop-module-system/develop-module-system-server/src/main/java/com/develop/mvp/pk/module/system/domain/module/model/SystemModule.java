package com.develop.mvp.pk.module.system.domain.module.model;

import com.develop.mvp.pk.module.system.domain.module.event.ModuleStateChangedEvent;
import com.develop.mvp.pk.module.system.domain.module.exception.ModuleStateTransitionException;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleDependency;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleRuntimeState;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleVersion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SystemModule {

    private final ModuleCode code;
    private final String name;
    private final ModuleVersion version;
    private final boolean enabled;
    private final List<ModuleDependency> dependencies;
    private final int order;
    private final String description;
    private ModuleRuntimeState state;
    private String failureReason;
    private final List<ModuleStateChangedEvent> events = new ArrayList<>();

    private SystemModule(ModuleCode code, String name, ModuleVersion version, boolean enabled,
                         List<ModuleDependency> dependencies, int order, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("模块名称不能为空");
        }
        this.code = code;
        this.name = name;
        this.version = version;
        this.enabled = enabled;
        this.dependencies = List.copyOf(dependencies == null ? List.of() : dependencies);
        this.order = order;
        this.description = description;
        this.state = enabled ? ModuleRuntimeState.NEW : ModuleRuntimeState.DISABLED;
    }

    public static SystemModule create(ModuleCode code, String name, ModuleVersion version, boolean enabled,
                                      List<ModuleDependency> dependencies, int order, String description) {
        return new SystemModule(code, name, version, enabled, dependencies, order, description);
    }

    public void markResolved() {
        transitTo(ModuleRuntimeState.RESOLVED, null, ModuleRuntimeState.NEW);
    }

    public void markStarting() {
        transitTo(ModuleRuntimeState.STARTING, null, ModuleRuntimeState.RESOLVED, ModuleRuntimeState.STOPPED);
    }

    public void markRunning() {
        transitTo(ModuleRuntimeState.RUNNING, null, ModuleRuntimeState.STARTING);
    }

    public void markStopping() {
        transitTo(ModuleRuntimeState.STOPPING, null, ModuleRuntimeState.RUNNING);
    }

    public void markStopped() {
        transitTo(ModuleRuntimeState.STOPPED, null, ModuleRuntimeState.STOPPING);
    }

    public void markFailed(String reason) {
        ModuleRuntimeState previous = this.state;
        this.state = ModuleRuntimeState.FAILED;
        this.failureReason = reason;
        this.events.add(new ModuleStateChangedEvent(code, previous, this.state, reason, LocalDateTime.now()));
    }

    private void transitTo(ModuleRuntimeState target, String reason, ModuleRuntimeState... allowedSources) {
        for (ModuleRuntimeState allowedSource : allowedSources) {
            if (this.state == allowedSource) {
                ModuleRuntimeState previous = this.state;
                this.state = target;
                this.failureReason = null;
                this.events.add(new ModuleStateChangedEvent(code, previous, target, reason, LocalDateTime.now()));
                return;
            }
        }
        throw new ModuleStateTransitionException(code, state, target);
    }

    public List<ModuleStateChangedEvent> pullEvents() {
        List<ModuleStateChangedEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    public ModuleCode code() {
        return code;
    }

    public String name() {
        return name;
    }

    public ModuleVersion version() {
        return version;
    }

    public boolean enabled() {
        return enabled;
    }

    public List<ModuleDependency> dependencies() {
        return dependencies;
    }

    public int order() {
        return order;
    }

    public String description() {
        return description;
    }

    public ModuleRuntimeState state() {
        return state;
    }

    public String failureReason() {
        return failureReason;
    }
}
