package com.develop.mvp.pk.module.system.infrastructure.module.messaging;

import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleMetricsPort;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleRuntimeState;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;

public class MicrometerModuleMetricsAdapter implements ModuleMetricsPort {

    private final MeterRegistry meterRegistry;

    public MicrometerModuleMetricsAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void recordStateChange(String moduleCode, ModuleRuntimeState state) {
        meterRegistry.counter("system.module.state.changed", "module", moduleCode, "state", state.name()).increment();
    }

    @Override
    public void recordStartDuration(String moduleCode, Duration duration) {
        meterRegistry.timer("system.module.start.duration", "module", moduleCode).record(duration);
    }

    @Override
    public void recordFailure(String moduleCode) {
        meterRegistry.counter("system.module.failure", "module", moduleCode).increment();
    }
}
