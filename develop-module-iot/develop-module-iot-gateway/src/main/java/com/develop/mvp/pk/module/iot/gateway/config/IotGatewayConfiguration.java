package com.develop.mvp.pk.module.iot.gateway.config;

import com.develop.mvp.pk.module.iot.gateway.protocol.IotProtocolManager;
import com.develop.mvp.pk.module.iot.gateway.serialize.IotMessageSerializerManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IoT 网关配置类
 *
 * @author David
 */
@Configuration
@EnableConfigurationProperties(IotGatewayProperties.class)
public class IotGatewayConfiguration {

    @Bean
    public IotMessageSerializerManager iotMessageSerializerManager() {
        return new IotMessageSerializerManager();
    }

    @Bean
    public IotProtocolManager iotProtocolManager(IotGatewayProperties gatewayProperties) {
        return new IotProtocolManager(gatewayProperties);
    }

}
