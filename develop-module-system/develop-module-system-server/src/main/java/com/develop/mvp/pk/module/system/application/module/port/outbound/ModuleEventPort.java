package com.develop.mvp.pk.module.system.application.module.port.outbound;

import com.develop.mvp.pk.module.system.domain.module.event.ModuleStateChangedEvent;

public interface ModuleEventPort {

    void publish(ModuleStateChangedEvent event);
}
