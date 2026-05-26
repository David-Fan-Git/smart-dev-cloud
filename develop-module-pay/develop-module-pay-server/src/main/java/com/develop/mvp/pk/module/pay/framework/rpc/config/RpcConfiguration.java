package com.develop.mvp.pk.module.pay.framework.rpc.config;

import com.develop.mvp.pk.module.system.api.social.remote.SocialClientRemoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "payRpcConfiguration", proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "develop.rpc.remote.system", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableFeignClients(clients = {SocialClientRemoteClient.class})
public class RpcConfiguration {
}
