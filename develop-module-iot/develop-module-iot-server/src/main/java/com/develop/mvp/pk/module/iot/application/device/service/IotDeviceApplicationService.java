package com.develop.mvp.pk.module.iot.application.device.service;

import com.develop.mvp.pk.module.iot.application.device.port.inbound.IotDeviceUseCase;
import com.develop.mvp.pk.module.iot.domain.device.event.DomainEventPublisher;
import com.develop.mvp.pk.module.iot.domain.device.repository.IotDeviceRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class IotDeviceApplicationService extends com.develop.mvp.pk.module.iot.application.device.IotDeviceApplicationService implements IotDeviceUseCase {

    public IotDeviceApplicationService(IotDeviceRepository iotDeviceRepository,
                                       DomainEventPublisher eventPublisher) {
        super(iotDeviceRepository, eventPublisher);
    }

}
