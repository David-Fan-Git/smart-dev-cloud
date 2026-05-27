package com.develop.mvp.pk.module.system.framework.module.config;

import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleMetricsPort;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SystemModuleConfigurationTest {

    @Test
    void createsModuleMetricsPortWithoutMeterRegistry() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SystemModuleConfiguration.class)) {
            assertNotNull(context.getBean(ModuleMetricsPort.class));
        }
    }
}
