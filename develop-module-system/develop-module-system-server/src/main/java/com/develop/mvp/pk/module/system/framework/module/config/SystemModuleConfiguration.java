package com.develop.mvp.pk.module.system.framework.module.config;

import com.develop.mvp.pk.module.system.application.module.command.RegisterModuleCommand;
import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleEventPort;
import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleMetricsPort;
import com.develop.mvp.pk.module.system.application.module.service.SystemModuleApplicationService;
import com.develop.mvp.pk.module.system.domain.module.repository.SystemModuleRepository;
import com.develop.mvp.pk.module.system.domain.module.service.ModuleDependencyResolver;
import com.develop.mvp.pk.module.system.domain.module.valueobject.ModuleDependency;
import com.develop.mvp.pk.module.system.infrastructure.module.messaging.MicrometerModuleMetricsAdapter;
import com.develop.mvp.pk.module.system.infrastructure.module.messaging.NoOpModuleMetricsAdapter;
import com.develop.mvp.pk.module.system.infrastructure.module.messaging.SpringModuleEventPublisher;
import com.develop.mvp.pk.module.system.infrastructure.module.persistence.InMemorySystemModuleRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SystemModuleProperties.class)
@ConditionalOnProperty(prefix = "develop.system.module", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SystemModuleConfiguration {

    @Bean
    public SystemModuleRepository systemModuleRepository() {
        return new InMemorySystemModuleRepository();
    }

    @Bean
    public ModuleDependencyResolver moduleDependencyResolver() {
        return new ModuleDependencyResolver();
    }

    @Bean
    public ModuleEventPort moduleEventPort(ApplicationEventPublisher publisher) {
        return new SpringModuleEventPublisher(publisher);
    }

    @Bean
    public ModuleMetricsPort moduleMetricsPort(ObjectProvider<MeterRegistry> meterRegistry) {
        MeterRegistry registry = meterRegistry.getIfAvailable();
        return registry != null ? new MicrometerModuleMetricsAdapter(registry) : new NoOpModuleMetricsAdapter();
    }

    @Bean
    public SystemModuleApplicationService systemModuleApplicationService(SystemModuleRepository repository,
                                                                         ModuleDependencyResolver resolver,
                                                                         ModuleEventPort eventPort,
                                                                         ModuleMetricsPort metricsPort,
                                                                         SystemModuleProperties properties) {
        SystemModuleApplicationService service = new SystemModuleApplicationService(repository, resolver, eventPort, metricsPort);
        for (SystemModuleProperties.Definition definition : properties.getDefinitions()) {
            service.register(new RegisterModuleCommand(definition.getCode(), definition.getName(), definition.getVersion(),
                    definition.isEnabled(), definition.getDependencies().stream().map(ModuleDependency::required).toList(),
                    definition.getOrder(), definition.getDescription()));
        }
        return service;
    }
}
