package com.develop.mvp.pk.module.iot.domain.command.model;

import com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandPayload;
import com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandRequestId;
import com.develop.mvp.pk.module.iot.domain.command.valueobject.IotCommandStatus;

public final class IotDeviceCommand {

    private Long id;
    private Long deviceId;
    private IotCommandRequestId requestId;
    private String method;
    private IotCommandPayload payload;
    private IotCommandStatus status;
    private String messageId;
    private String serverId;
    private Object data;
    private Integer code;
    private String msg;

    private IotDeviceCommand() {
    }

    public static IotDeviceCommand create(Long deviceId, String requestId, String method, Object params) {
        IotDeviceCommand command = new IotDeviceCommand();
        command.deviceId = deviceId;
        command.requestId = IotCommandRequestId.of(requestId);
        command.method = method;
        command.payload = IotCommandPayload.of(params);
        command.status = IotCommandStatus.created();
        return command;
    }

    public static IotDeviceCommand reconstitute(Long id, Long deviceId, String requestId, String method, Object params,
                                                Integer status, String messageId, String serverId, Object data,
                                                Integer code, String msg) {
        IotDeviceCommand command = create(deviceId, requestId, method, params);
        command.id = id;
        command.status = IotCommandStatus.of(status);
        command.messageId = messageId;
        command.serverId = serverId;
        command.data = data;
        command.code = code;
        command.msg = msg;
        return command;
    }

    public void markPending() {
        if (isFinalState()) {
            return;
        }
        status = IotCommandStatus.pending();
    }

    public void markSent(String messageId, String serverId) {
        if (isFinalState()) {
            return;
        }
        this.messageId = messageId;
        this.serverId = serverId;
        status = IotCommandStatus.sent();
    }

    public void ack(Object data, Integer code, String msg) {
        if (IotCommandStatus.acked().equals(status) || isFinalState()) {
            return;
        }
        this.data = data;
        this.code = code;
        this.msg = msg;
        status = IotCommandStatus.acked();
    }

    public void fail(Integer code, String msg) {
        if (isFinalState()) {
            return;
        }
        this.code = code;
        this.msg = msg;
        status = IotCommandStatus.failed();
    }

    public void timeout() {
        if (isFinalState()) {
            return;
        }
        status = IotCommandStatus.timeout();
    }

    public void cancel() {
        if (isFinalState()) {
            return;
        }
        status = IotCommandStatus.canceled();
    }

    public boolean isFinalState() {
        return status != null && status.isFinalState();
    }

    public Long id() {
        return id;
    }

    public Long deviceId() {
        return deviceId;
    }

    public IotCommandRequestId requestId() {
        return requestId;
    }

    public String method() {
        return method;
    }

    public IotCommandPayload payload() {
        return payload;
    }

    public IotCommandStatus status() {
        return status;
    }

    public String messageId() {
        return messageId;
    }

    public String serverId() {
        return serverId;
    }

    public Object data() {
        return data;
    }

    public Integer code() {
        return code;
    }

    public String msg() {
        return msg;
    }

}
