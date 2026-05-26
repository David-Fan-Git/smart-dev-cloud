package com.develop.mvp.pk.module.ai.framework.rpc.config;

import com.develop.mvp.pk.module.infra.api.file.remote.FileRemoteClient;
import com.develop.mvp.pk.module.system.api.user.remote.AdminUserRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "aiRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = FileRemoteClient.class)
public class RpcConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "develop.rpc.remote.system", name = "enabled", havingValue = "true", matchIfMissing = true)
    @EnableFeignClients(clients = AdminUserRemoteClient.class)
    public static class SystemRemoteRpcConfiguration {
    }

}
