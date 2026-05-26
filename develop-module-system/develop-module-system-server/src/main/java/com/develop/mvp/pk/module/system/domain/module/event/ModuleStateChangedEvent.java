package com.develop.mvp.pk.module.system.domain.module.event;

import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleRuntimeState;

import java.time.LocalDateTime;

public record ModuleStateChangedEvent(
        ModuleCode moduleCode,
        ModuleRuntimeState previousState,
        ModuleRuntimeState currentState,
        String reason,
        LocalDateTime occurredAt) {
}
