package com.develop.mvp.pk.module.iot.domain.command.repository;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.command.query.IotDeviceCommandQuery;
import com.develop.mvp.pk.module.iot.domain.command.model.IotDeviceCommand;
import com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandId;

public interface IotDeviceCommandRepository {

    IotDeviceCommand findById(IotCommandId id);

    IotDeviceCommand findByRequestId(Long deviceId, String requestId);

    PageResult<IotDeviceCommand> findPage(IotDeviceCommandQuery query);

    IotDeviceCommand save(IotDeviceCommand command);

}
