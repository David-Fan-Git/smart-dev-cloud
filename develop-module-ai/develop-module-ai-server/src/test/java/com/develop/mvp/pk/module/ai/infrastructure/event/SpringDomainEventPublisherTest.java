package com.develop.mvp.pk.module.ai.infrastructure.event;

import com.develop.mvp.pk.module.ai.domain.model.event.AiModelCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SpringDomainEventPublisherTest {

    @Test
    void publish_delegatesToSpringApplicationEventPublisher() {
        ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
        SpringDomainEventPublisher publisher = new SpringDomainEventPublisher(applicationEventPublisher);
        AiModelCreatedEvent event = new AiModelCreatedEvent(1L, "DeepSeek V3", "deepseek-chat", "DeepSeek");

        publisher.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
    }
}
