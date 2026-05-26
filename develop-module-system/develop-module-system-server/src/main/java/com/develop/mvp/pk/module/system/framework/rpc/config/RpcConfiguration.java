package com.develop.mvp.pk.module.system.framework.rpc.config;

import com.develop.mvp.pk.module.infra.api.config.remote.ConfigRemoteClient;
import com.develop.mvp.pk.module.infra.api.file.remote.FileRemoteClient;
import com.develop.mvp.pk.module.infra.api.websocket.remote.WebSocketSenderRemoteClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Rpc Configuration 配置类。
 */
@Configuration(value = "systemRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {FileRemoteClient.class, WebSocketSenderRemoteClient.class, ConfigRemoteClient.class})
public class RpcConfiguration {
}
