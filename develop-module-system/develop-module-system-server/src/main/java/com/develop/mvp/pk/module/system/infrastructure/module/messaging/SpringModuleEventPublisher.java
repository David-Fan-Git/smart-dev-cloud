package com.develop.mvp.pk.module.system.infrastructure.module.messaging;

import com.develop.mvp.pk.module.system.application.module.port.outbound.ModuleEventPort;
import com.develop.mvp.pk.module.system.domain.module.event.ModuleStateChangedEvent;
import org.springframework.context.ApplicationEventPublisher;

public class SpringModuleEventPublisher implements ModuleEventPort {

    private final ApplicationEventPublisher publisher;

    public SpringModuleEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(ModuleStateChangedEvent event) {
        publisher.publishEvent(new SpringModuleStateChangedEvent(event));
    }
}
