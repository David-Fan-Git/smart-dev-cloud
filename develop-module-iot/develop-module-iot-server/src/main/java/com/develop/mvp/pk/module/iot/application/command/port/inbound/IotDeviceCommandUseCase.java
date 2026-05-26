package com.develop.mvp.pk.module.iot.application.command.port.inbound;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.command.command.AckIotDeviceCommand;
import com.develop.mvp.pk.module.iot.application.command.command.CreateIotDeviceCommand;
import com.develop.mvp.pk.module.iot.application.command.command.MarkIotDeviceCommandTimeout;
import com.develop.mvp.pk.module.iot.application.command.query.IotDeviceCommandQuery;
import com.develop.mvp.pk.module.iot.application.command.result.IotDeviceCommandResult;

public interface IotDeviceCommandUseCase {

    IotDeviceCommandResult createCommand(CreateIotDeviceCommand command);

    void handleAck(AckIotDeviceCommand command);

    void markTimeout(MarkIotDeviceCommandTimeout command);

    IotDeviceCommandResult getCommand(Long commandId);

    PageResult<IotDeviceCommandResult> getCommandPage(IotDeviceCommandQuery query);

}
