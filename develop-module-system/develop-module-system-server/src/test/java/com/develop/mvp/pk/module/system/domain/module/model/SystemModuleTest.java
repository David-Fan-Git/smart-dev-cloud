package com.develop.mvp.pk.module.system.domain.module.model;

import com.develop.mvp.pk.module.system.domain.module.exception.ModuleStateTransitionException;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleCode;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleRuntimeState;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemModuleTest {

    @Test
    void createEnabledModuleStartsAsNew() {
        SystemModule module = SystemModule.create(ModuleCode.of("system-user"), "用户模块", ModuleVersion.of("1.0.0"), true, List.of(), 10, "管理用户");

        assertEquals(ModuleRuntimeState.NEW, module.state());
        assertTrue(module.enabled());
    }

    @Test
    void createDisabledModuleStartsAsDisabled() {
        SystemModule module = SystemModule.create(ModuleCode.of("system-user"), "用户模块", ModuleVersion.of("1.0.0"), false, List.of(), 10, "管理用户");

        assertEquals(ModuleRuntimeState.DISABLED, module.state());
        assertFalse(module.enabled());
    }

    @Test
    void enabledModuleFollowsAllowedLifecycle() {
        SystemModule module = SystemModule.create(ModuleCode.of("system-user"), "用户模块", ModuleVersion.of("1.0.0"), true, List.of(), 10, "管理用户");

        module.markResolved();
        module.markStarting();
        module.markRunning();
        module.markStopping();
        module.markStopped();

        assertEquals(ModuleRuntimeState.STOPPED, module.state());
    }

    @Test
    void disabledModuleCannotStart() {
        SystemModule module = SystemModule.create(ModuleCode.of("system-user"), "用户模块", ModuleVersion.of("1.0.0"), false, List.of(), 10, "管理用户");

        ModuleStateTransitionException ex = assertThrows(ModuleStateTransitionException.class, module::markStarting);
        assertEquals("模块 system-user 当前状态 DISABLED 不允许迁移到 STARTING", ex.getMessage());
    }

    @Test
    void failedModuleKeepsFailureReason() {
        SystemModule module = SystemModule.create(ModuleCode.of("system-user"), "用户模块", ModuleVersion.of("1.0.0"), true, List.of(), 10, "管理用户");

        module.markFailed("依赖缺失");

        assertEquals(ModuleRuntimeState.FAILED, module.state());
        assertEquals("依赖缺失", module.failureReason());
    }
}
