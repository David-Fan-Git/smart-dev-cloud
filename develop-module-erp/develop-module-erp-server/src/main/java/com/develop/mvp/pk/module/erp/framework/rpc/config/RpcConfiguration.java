package com.develop.mvp.pk.module.erp.framework.rpc.config;

import com.develop.mvp.pk.module.system.api.user.remote.AdminUserRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "erpRpcConfiguration", proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "develop.rpc.remote.system", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableFeignClients(clients = AdminUserRemoteClient.class)
public class RpcConfiguration {
}
