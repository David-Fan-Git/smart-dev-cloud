package com.develop.mvp.pk.module.iot.infrastructure.command.persistence;

import com.develop.mvp.pk.framework.common.pojo.PageResult;
import com.develop.mvp.pk.module.iot.application.command.query.IotDeviceCommandQuery;
import com.develop.mvp.pk.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import com.develop.mvp.pk.module.iot.domain.command.model.IotDeviceCommand;
import com.develop.mvp.pk.module.iot.domain.command.repository.IotDeviceCommandRepository;
import com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandId;
import com.develop.mvp.pk.module.iot.service.device.message.IotDeviceMessageService;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IotDeviceCommandRepositoryImpl implements IotDeviceCommandRepository {

    private final IotDeviceMessageService deviceMessageService;

    public IotDeviceCommandRepositoryImpl(IotDeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Override
    public IotDeviceCommand findById(IotCommandId id) {
        return null;
    }

    @Override
    public IotDeviceCommand findByRequestId(Long deviceId, String requestId) {
        List<IotDeviceMessageDO> requests = deviceMessageService.getDeviceMessageListByRequestIdsAndReply(
                deviceId, List.of(requestId), false);
        if (requests.isEmpty()) {
            return null;
        }
        IotDeviceMessageDO request = requests.get(0);
        IotDeviceCommand command = IotDeviceCommand.reconstitute(null, deviceId, request.getRequestId(),
                request.getMethod(), request.getParams(), null, request.getId(), request.getServerId(),
                null, null, null);
        command.markSent(request.getId(), request.getServerId());
        List<IotDeviceMessageDO> replies = deviceMessageService.getDeviceMessageListByRequestIdsAndReply(
                deviceId, List.of(requestId), true);
        if (!replies.isEmpty()) {
            IotDeviceMessageDO reply = replies.get(0);
            command.ack(reply.getData(), reply.getCode(), reply.getMsg());
        }
        return command;
    }

    @Override
    public PageResult<IotDeviceCommand> findPage(IotDeviceCommandQuery query) {
        IotDeviceMessagePageReqVO reqVO = new IotDeviceMessagePageReqVO();
        reqVO.setDeviceId(query.deviceId());
        reqVO.setReply(false);
        if (query.pageNo() != null) reqVO.setPageNo(query.pageNo());
        if (query.pageSize() != null) reqVO.setPageSize(query.pageSize());
        PageResult<IotDeviceMessageDO> page = deviceMessageService.getDeviceMessagePage(reqVO);
        return new PageResult<>(page.getList().stream().map(this::toCommand).toList(), page.getTotal());
    }

    @Override
    public IotDeviceCommand save(IotDeviceCommand command) {
        return command;
    }

    private IotDeviceCommand toCommand(IotDeviceMessageDO message) {
        IotDeviceCommand command = IotDeviceCommand.reconstitute(null, message.getDeviceId(), message.getRequestId(),
                message.getMethod(), message.getParams(), null, message.getId(), message.getServerId(),
                null, null, null);
        command.markSent(message.getId(), message.getServerId());
        return command;
    }

}
