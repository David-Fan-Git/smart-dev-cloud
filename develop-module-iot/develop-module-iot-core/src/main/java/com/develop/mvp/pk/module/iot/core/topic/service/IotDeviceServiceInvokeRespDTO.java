package com.develop.mvp.pk.module.iot.core.topic.service;

import com.develop.mvp.pk.module.iot.core.enums.IotDeviceMessageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * IoT 设备服务调用 Response DTO
 * <p>
 * 用于 {@link IotDeviceMessageMethodEnum#SERVICE_INVOKE} 下行消息 ACK 的 data 参数
 *
 * @author David
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceServiceInvokeRespDTO {

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

    /**
     * 服务输出参数
     */
    private Map<String, Object> outputParams;

}
