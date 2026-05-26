package com.develop.mvp.pk.module.promotion.infrastructure.event;

import com.develop.mvp.pk.module.promotion.domain.seckill.event.SeckillActivityStatusChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SpringDomainEventPublisherTest {

    @Test
    void publish_delegatesToSpringApplicationEventPublisher() {
        ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
        SpringDomainEventPublisher publisher = new SpringDomainEventPublisher(applicationEventPublisher);
        SeckillActivityStatusChangedEvent event = new SeckillActivityStatusChangedEvent(1L, 0, 1);

        publisher.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
    }
}
