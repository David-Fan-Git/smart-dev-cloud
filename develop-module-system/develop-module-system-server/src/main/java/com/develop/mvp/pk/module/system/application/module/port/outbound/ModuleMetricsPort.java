package com.develop.mvp.pk.module.system.application.module.port.outbound;

import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleRuntimeState;

import java.time.Duration;

public interface ModuleMetricsPort {

    void recordStateChange(String moduleCode, ModuleRuntimeState state);

    void recordStartDuration(String moduleCode, Duration duration);

    void recordFailure(String moduleCode);
}
