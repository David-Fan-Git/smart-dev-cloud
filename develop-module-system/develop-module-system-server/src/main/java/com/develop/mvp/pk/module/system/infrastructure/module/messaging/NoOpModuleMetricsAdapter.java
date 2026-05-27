package com.develop.mvp.pk.module.system.infrastructure.module.messaging;

import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleMetricsPort;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleRuntimeState;

import java.time.Duration;

public class NoOpModuleMetricsAdapter implements ModuleMetricsPort {

    @Override
    public void recordStateChange(String moduleCode, ModuleRuntimeState state) {
    }

    @Override
    public void recordStartDuration(String moduleCode, Duration duration) {
    }

    @Override
    public void recordFailure(String moduleCode) {
    }
}
