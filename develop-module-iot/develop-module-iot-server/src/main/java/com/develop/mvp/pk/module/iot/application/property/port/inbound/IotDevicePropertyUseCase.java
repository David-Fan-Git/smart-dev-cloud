package com.develop.mvp.pk.module.iot.application.property.port.inbound;

import com.develop.mvp.pk.module.iot.application.property.command.PostIotDevicePropertyCommand;
import com.develop.mvp.pk.module.iot.application.property.query.IotDeviceLatestPropertyQuery;
import com.develop.mvp.pk.module.iot.application.property.result.IotDeviceLatestPropertyResult;

import java.util.List;

public interface IotDevicePropertyUseCase {

    void postProperty(PostIotDevicePropertyCommand command);

    List<IotDeviceLatestPropertyResult> getLatestProperties(IotDeviceLatestPropertyQuery query);

}
