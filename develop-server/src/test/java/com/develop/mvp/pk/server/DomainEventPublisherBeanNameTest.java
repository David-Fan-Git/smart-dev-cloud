package com.develop.mvp.pk.server;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DomainEventPublisherBeanNameTest {

    @Test
    void domainEventPublishersUseUniqueSpringBeanNames() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(
                    com.develop.mvp.pk.module.system.infrastructure.user.messaging.SpringDomainEventPublisher.class,
                    com.develop.mvp.pk.module.infra.infrastructure.SpringDomainEventPublisher.class);
            context.refresh();

            assertArrayEquals(
                    new String[]{"systemDomainEventPublisher"},
                    context.getBeanNamesForType(com.develop.mvp.pk.module.system.domain.user.event.DomainEventPublisher.class));
            assertArrayEquals(
                    new String[]{"infraDomainEventPublisher"},
                    context.getBeanNamesForType(com.develop.mvp.pk.module.infra.domain.event.DomainEventPublisher.class));
        }
    }
}
