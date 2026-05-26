package com.develop.mvp.pk.module.mp.framework.rpc.config;

import com.develop.mvp.pk.module.infra.api.file.remote.FileRemoteClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "mpRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = FileRemoteClient.class)
public class RpcConfiguration {
}
