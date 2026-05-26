package com.develop.mvp.pk.module.iot.application.command.service;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.command.command.AckIotDeviceCommand;
import com.develop.mvp.pk.module.iot.application.command.command.CreateIotDeviceCommand;
import com.develop.mvp.pk.module.iot.application.command.command.MarkIotDeviceCommandTimeout;
import com.develop.mvp.pk.module.iot.application.command.port.inbound.IotDeviceCommandUseCase;
import com.develop.mvp.pk.module.iot.application.command.port.outbound.IotDeviceCommandMessagePort;
import com.develop.mvp.pk.module.iot.application.command.query.IotDeviceCommandQuery;
import com.develop.mvp.pk.module.iot.application.command.result.IotDeviceCommandResult;
import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.domain.command.model.IotDeviceCommand;
import com.develop.mvp.pk.module.iot.domain.command.repository.IotDeviceCommandRepository;
import com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IotDeviceCommandApplicationService implements IotDeviceCommandUseCase {

    private final IotDeviceCommandRepository commandRepository;
    private final IotDeviceCommandMessagePort messagePort;

    public IotDeviceCommandApplicationService(IotDeviceCommandRepository commandRepository,
                                              IotDeviceCommandMessagePort messagePort) {
        this.commandRepository = commandRepository;
        this.messagePort = messagePort;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotDeviceCommandResult createCommand(CreateIotDeviceCommand command) {
        IotDeviceCommand deviceCommand = IotDeviceCommand.create(command.deviceId(), command.requestId(),
                command.method(), command.params());
        deviceCommand.markPending();
        IotDeviceMessage message = messagePort.sendToDevice(command.deviceId(), command.method(), command.params(), command.requestId());
        deviceCommand.markSent(message.getId(), message.getServerId());
        IotDeviceCommand savedCommand = commandRepository.save(deviceCommand);
        return toResult(savedCommand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAck(AckIotDeviceCommand command) {
        IotDeviceCommand deviceCommand = commandRepository.findByRequestId(command.deviceId(), command.requestId());
        if (deviceCommand == null || deviceCommand.isFinalState()) {
            return;
        }
        deviceCommand.ack(command.data(), command.code(), command.msg());
        commandRepository.save(deviceCommand);
        messagePort.recordReply(command.deviceId(), command.requestId(), deviceCommand.method(),
                command.data(), command.code(), command.msg());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markTimeout(MarkIotDeviceCommandTimeout command) {
        IotDeviceCommand deviceCommand = commandRepository.findById(IotCommandId.of(command.commandId()));
        if (deviceCommand == null) {
            return;
        }
        deviceCommand.timeout();
        commandRepository.save(deviceCommand);
    }

    @Override
    public IotDeviceCommandResult getCommand(Long commandId) {
        IotDeviceCommand command = commandRepository.findById(IotCommandId.of(commandId));
        return command != null ? toResult(command) : null;
    }

    @Override
    public PageResult<IotDeviceCommandResult> getCommandPage(IotDeviceCommandQuery query) {
        PageResult<IotDeviceCommand> page = commandRepository.findPage(query);
        return new PageResult<>(page.getList().stream().map(this::toResult).toList(), page.getTotal());
    }

    private IotDeviceCommandResult toResult(IotDeviceCommand command) {
        return new IotDeviceCommandResult(command.id(), command.deviceId(), command.requestId().value(), command.method(),
                command.payload().value(), command.status().code(), command.messageId(), command.serverId(),
                command.data(), command.code(), command.msg());
    }

}
