package com.develop.mvp.pk.module.system.application.module.port.inbound;

import com.develop.mvp.pk.module.system.application.module.command.RegisterModuleCommand;
import com.develop.mvp.pk.module.system.application.module.dto.SystemModuleDTO;

import java.util.List;

public interface SystemModuleUseCase {

    void register(RegisterModuleCommand command);

    List<SystemModuleDTO> listModules();

    SystemModuleDTO getModule(String code);

    void startModule(String code);

    void stopModule(String code);
}
