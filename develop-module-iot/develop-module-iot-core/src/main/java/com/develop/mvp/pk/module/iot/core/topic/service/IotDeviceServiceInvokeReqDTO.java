package com.develop.mvp.pk.module.iot.core.topic.service;

import com.develop.mvp.pk.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * IoT 设备服务调用 Request DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#SERVICE_INVOKE} 下行消息的 params 参数
 *
 * @author David
 */
@Data
@NoArgsConstructor
public class IotDeviceServiceInvokeReqDTO {

    /**
     * 服务标识符
     */
    private String identifier;

    /**
     * 服务输入参数
     */
    private Map<String, Object> inputParams;

    public IotDeviceServiceInvokeReqDTO(String identifier, Map<String, Object> inputParams) {
        this.identifier = identifier;
        this.inputParams = inputParams;
    }

    public IotDeviceServiceInvokeReqDTO(String identifier, Map<String, Object> inputParams, String commandId,
                                        String traceId, Integer schemaVersion) {
        this(identifier, inputParams);
        this.commandId = commandId;
        this.traceId = traceId;
        this.schemaVersion = schemaVersion;
    }

    /**
     * 命令编号
     */
    private String commandId;

    /**
     * 链路追踪编号
     */
    private String traceId;

    /**
     * 消息结构版本
     */
    private Integer schemaVersion;

}
