package com.develop.mvp.pk.module.system.application.module.service;

import com.develop.mvp.pk.framework.common.exception.ServiceException;
import com.develop.mvp.pk.framework.test.core.ut.BaseMockitoUnitTest;
import com.develop.mvp.pk.module.system.application.module.command.RegisterModuleCommand;
import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleEventPort;
import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleMetricsPort;
import com.develop.mvp.pk.module.system.domain.module.repository.SystemModuleRepository;
import com.develop.mvp.pk.module.system.domain.module.service.ModuleDependencyResolver;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleDependency;
import com.develop.mvp.pk.module.system.infrastructure.module.persistence.InMemorySystemModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SystemModuleApplicationServiceTest extends BaseMockitoUnitTest {

    private SystemModuleRepository repository;
    @Mock
    private ModuleEventPort eventPort;
    @Mock
    private ModuleMetricsPort metricsPort;
    private SystemModuleApplicationService service;

    @BeforeEach
    void setUp() {
        repository = new InMemorySystemModuleRepository();
        service = new SystemModuleApplicationService(repository, new ModuleDependencyResolver(), eventPort, metricsPort);
    }

    @Test
    void registerRejectsDuplicateCode() {
        service.register(command("system-user", true, List.of()));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.register(command("system-user", true, List.of())));

        assertEquals(1_002_030_001, ex.getCode());
    }

    @Test
    void startModuleStartsDependencyFirst() {
        service.register(command("system-permission", true, List.of()));
        service.register(command("system-user", true, List.of(ModuleDependency.required("system-permission"))));

        service.startModule("system-user");

        assertEquals("RUNNING", service.getModule("system-permission").state());
        assertEquals("RUNNING", service.getModule("system-user").state());
        verify(metricsPort, atLeastOnce()).recordStateChange(any(), any());
        verify(eventPort, atLeastOnce()).publish(any());
    }

    @Test
    void startDisabledModuleFails() {
        service.register(command("system-user", false, List.of()));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.startModule("system-user"));

        assertEquals(1_002_030_005, ex.getCode());
    }

    @Test
    void startModuleRejectsMissingRequiredDependency() {
        service.register(command("system-user", true, List.of(ModuleDependency.required("system-permission"))));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.startModule("system-user"));

        assertEquals(1_002_030_002, ex.getCode());
    }

    @Test
    void startModuleDoesNotStartUnrelatedEarlierModule() {
        service.register(command("system-audit", true, List.of()));
        service.register(command("system-permission", true, List.of()));
        service.register(command("system-user", true, List.of(ModuleDependency.required("system-permission"))));

        service.startModule("system-user");

        assertEquals("NEW", service.getModule("system-audit").state());
        assertEquals("RUNNING", service.getModule("system-permission").state());
        assertEquals("RUNNING", service.getModule("system-user").state());
    }

    @Test
    void stopRunningModuleChangesStateToStopped() {
        service.register(command("system-user", true, List.of()));
        service.startModule("system-user");

        service.stopModule("system-user");

        assertEquals("STOPPED", service.getModule("system-user").state());
    }

    private RegisterModuleCommand command(String code, boolean enabled, List<ModuleDependency> dependencies) {
        return new RegisterModuleCommand(code, code, "1.0.0", enabled, dependencies, 0, code);
    }
}
