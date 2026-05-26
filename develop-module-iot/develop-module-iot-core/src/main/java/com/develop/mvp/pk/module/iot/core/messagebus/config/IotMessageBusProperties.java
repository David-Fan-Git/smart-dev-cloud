package com.develop.mvp.pk.module.iot.core.messagebus.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * IoT 消息总线配置属性
 *
 * @author David
 */
@ConfigurationProperties("develop.iot.message-bus")
@Data
@Validated
public class IotMessageBusProperties {

    /**
     * 消息总线类型
     *
     * 可选值：local、redis、rocketmq、rabbitmq
     */
    @NotNull(message = "IoT 消息总线类型不能为空")
    private String type = "local";

}