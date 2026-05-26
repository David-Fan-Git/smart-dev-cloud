package com.develop.mvp.pk.module.wms.infrastructure.event;

import com.develop.mvp.pk.module.wms.domain.inventory.event.WmsInventoryCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SpringDomainEventPublisherTest {

    @Test
    void publish_delegatesToSpringApplicationEventPublisher() {
        ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
        SpringDomainEventPublisher publisher = new SpringDomainEventPublisher(applicationEventPublisher);
        WmsInventoryCreatedEvent event = new WmsInventoryCreatedEvent(1L, 1L, 100L, new BigDecimal("5.00"));

        publisher.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
    }
}
