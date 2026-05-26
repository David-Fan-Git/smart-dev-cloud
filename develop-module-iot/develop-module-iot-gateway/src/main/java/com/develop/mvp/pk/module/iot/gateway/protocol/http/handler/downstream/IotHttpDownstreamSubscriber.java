package com.develop.mvp.pk.module.iot.gateway.protocol.http.handler.downstream;

import com.develop.mvp.pk.module.iot.core.messagebus.core.IotMessageBus;
import com.develop.mvp.pk.module.iot.core.mq.message.IotDeviceMessage;
import com.develop.mvp.pk.module.iot.gateway.protocol.AbstractIotProtocolDownstreamSubscriber;
import com.develop.mvp.pk.module.iot.gateway.protocol.IotProtocol;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 HTTP 订阅者：接收下行给设备的消息
 *
 * @author David
 */

@Slf4j
public class IotHttpDownstreamSubscriber extends AbstractIotProtocolDownstreamSubscriber {

    public IotHttpDownstreamSubscriber(IotProtocol protocol, IotMessageBus messageBus) {
        super(protocol, messageBus);
    }

    @Override
    protected void handleMessage(IotDeviceMessage message) {
        log.info("[handleMessage][IoT 网关 HTTP 协议不支持下行消息，忽略消息：{}]", message);
    }

}
