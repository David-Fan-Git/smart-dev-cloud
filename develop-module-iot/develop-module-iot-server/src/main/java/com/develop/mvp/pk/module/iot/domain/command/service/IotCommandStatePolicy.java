package com.develop.mvp.pk.module.iot.domain.command.service;

import com.develop.mvp.pk.module.iot.domain.command.model.IotDeviceCommand;

public class IotCommandStatePolicy {

    public boolean canApplyReply(IotDeviceCommand command) {
        return command != null && !command.isFinalState();
    }

}
